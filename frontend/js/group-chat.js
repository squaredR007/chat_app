document.addEventListener("DOMContentLoaded", () => {
    if (window.applyGlobalTheme) {
        window.applyGlobalTheme();
    }

    if (window.applyGlobalBackground) {
        window.applyGlobalBackground();
    }
});

window.addEventListener("backgroundChanged", () => {
    window.applyGlobalTheme?.();
    window.applyGlobalBackground?.();
});

// Config
const API_BASE = "http://localhost:8080/api";
const POLL_INTERVAL = 3000;

// Read logged-in user from localStorage
const currentUsername = localStorage.getItem("username");
const currentUserId = localStorage.getItem("userId");

if (!currentUsername || !currentUserId) {
    window.location.href = "login.html";
}

// Apply saved theme
if (localStorage.getItem("theme") === "dark") {
    document.body.classList.add("dark");
}

// Read chatId from URL
const urlParams = new URLSearchParams(window.location.search);
const chatId = urlParams.get("chatId");

if (!chatId) {
    window.location.href = "home.html";
}

// DOM References
const messagesArea = document.getElementById("messagesArea");
const messageInput = document.getElementById("messageInput");
const sendBtn = document.getElementById("sendBtn");
const headerName = document.getElementById("headerName");
const headerStatus = document.getElementById("headerStatus");
const headerAvatar = document.getElementById("headerAvatar");
const fileInput = document.getElementById("fileInput");
const searchToggleBtn = document.getElementById("searchToggleBtn");
const messageSearchBar = document.getElementById("messageSearchBar");
const messageSearchInput = document.getElementById("messageSearchInput");
const closeSearchBtn = document.getElementById("closeSearchBtn");
const infoToggleBtn = document.getElementById("infoToggleBtn");
const infoPanel = document.getElementById("infoPanel");
const closePanelBtn = document.getElementById("closePanelBtn");
const memberList = document.getElementById("memberList");
const addMemberSection = document.getElementById("addMemberSection");
const addMemberInput = document.getElementById("addMemberInput");
const addMemberBtn = document.getElementById("addMemberBtn");
const leaveGroupBtn = document.getElementById("leaveGroupBtn");

// State
let allMessages = [];
let lastPollTimestamp = Date.now();
let pollInterval = null;
let editingMessageId = null;
let deletingMessageId = null;
let currentGroup = null; // the Group object for this chat
const seenMessageIds = new Set();

// Load group info
async function loadGroupInfo() {
    try {
        const response = await fetch(`${API_BASE}/chat/list?username=${encodeURIComponent(currentUsername)}`);
        const chats = await response.json();
        const chat = Array.isArray(chats) ? chats.find(c => c.chatId === chatId) : null;

        if (!chat || !chat.group) {
            headerName.textContent = "Group";
            return;
        }
        const groupId = chat.group.groupId;
        const groupResponse = await fetch(`${API_BASE}/group/info?groupId=${encodeURIComponent(groupId)}`);
        if (!groupResponse.ok) {
            headerName.textContent = "Group";
            return;
        }

        currentGroup = await groupResponse.json();

        // Update header
        headerName.textContent = currentGroup.groupName;
        headerAvatar.textContent = currentGroup.groupName.charAt(0).toUpperCase();
        headerStatus.textContent = `${currentGroup.memberCount} member${currentGroup.memberCount !== 1 ? "s" : ""}`;

        // Update info panel
        document.getElementById("infoGroupAvatar").textContent =
            currentGroup.groupName.charAt(0).toUpperCase();
        document.getElementById("infoGroupName").textContent = currentGroup.groupName;
        document.getElementById("infoGroupId").textContent = `ID: ${currentGroup.groupId}`;
        document.getElementById("memberCountLabel").textContent =
            `Members · ${currentGroup.memberCount}`;

        renderMembers(currentGroup);

        addMemberSection.style.display =
            currentGroup.adminUsername === currentUsername ? "flex" : "none";

    } catch (err) {
        headerName.textContent = "Group";
        console.error("Failed to load group info:", err);
    }
}

// Render member list in info panel
function renderMembers(group) {
    memberList.innerHTML = "";
    const members = group.membersUsernames || [];

    members.forEach(username => {
        const isAdmin = username === group.adminUsername;
        const isMe = username === currentUsername;

        const item = document.createElement("div");
        item.className = "member-item";
        item.innerHTML = `
            <div class="member-avatar">${username.charAt(0).toUpperCase()}</div>
            <div class="member-name">${escapeHtml(username)}${isMe ? " (you)" : ""}</div>
            ${isAdmin ? `<span class="admin-badge">Admin</span>` : ""}
        `;
        memberList.appendChild(item);
    });
}

// Load all messages
async function loadMessages() {
    try {
        const response = await fetch(`${API_BASE}/chat/messages?chatId=${encodeURIComponent(chatId)}`);
        const messages = await response.json();

        allMessages = Array.isArray(messages) ? messages : [];
        allMessages.forEach(m => seenMessageIds.add(m.id));
        lastPollTimestamp = Date.now();

        renderMessages(allMessages);
        scrollToBottom();

        if (!pollInterval) {
            pollInterval = setInterval(pollNewMessages, POLL_INTERVAL);
        }
    } catch (err) {
        messagesArea.innerHTML = `<div class="empty-state">Could not connect to server.<br>Make sure the server is running.</div>`;
        console.error("Failed to load messages:", err);
    }
}

// Poll for new messages
async function pollNewMessages() {
    try {
        const response = await fetch(`${API_BASE}/chat/poll?chatId=${encodeURIComponent(chatId)}&since=${lastPollTimestamp}`);
        const newMessages = await response.json();

        if (!Array.isArray(newMessages) || newMessages.length === 0) return;

        const trulyNew = newMessages.filter(m => !seenMessageIds.has(m.id));
        if (trulyNew.length === 0) return;

        lastPollTimestamp = Date.now();
        trulyNew.forEach(m => seenMessageIds.add(m.id));

        allMessages = [...allMessages, ...trulyNew];
        renderMessages(allMessages);
        scrollToBottom();
    } catch (err) {
        console.error("Poll failed:", err);
    }
}

// Render all messages
function renderMessages(messages) {
    if (!messages || messages.length === 0) {
        messagesArea.innerHTML = `<div class="empty-state">No messages yet. Say hi to the group! 👋</div>`;
        return;
    }

    messagesArea.innerHTML = "";
    messages.forEach(msg => {
        const bubble = createMessageBubble(msg);
        messagesArea.appendChild(bubble);
    });
}

// NEW: true if the stored media path looks like a common image extension
function isImagePath(path) {
    return /\.(png|jpe?g|gif|webp)$/i.test(path || "");
}

// NEW: builds the URL used to fetch/display an uploaded file (see MediaController)
function mediaUrl(content) {
    return `${API_BASE}/media/${content}`;
}

// Create a single message bubble
function createMessageBubble(msg) {
    const isMine = msg.senderUsername === currentUsername;
    const isDeleted = msg.deleted;

    const bubble = document.createElement("div");
    bubble.className = `message-bubble ${isMine ? "mine" : "theirs"}${isDeleted ? " deleted" : ""}`;
    bubble.dataset.messageId = msg.id;

    const timeStr = formatTimestamp(msg.timestamp);

    let contentHtml;
    if (isDeleted) {
        contentHtml = "🚫 This message was deleted";
    } else if (msg.type === "MEDIA") {
        // FIX/NEW: previously this always showed the static text "📎 Media file".
        // Now it renders the real uploaded image, or a download link otherwise.
        const url = mediaUrl(msg.content);
        contentHtml = isImagePath(msg.content)
            ? `<img src="${url}" alt="shared image" class="message-image" style="max-width:220px;border-radius:12px;display:block;" />`
            : `<a href="${url}" target="_blank" rel="noopener" class="message-file-link">📎 Download file</a>`;
    } else {
        contentHtml = escapeHtml(msg.content || "");
    }

    const editedTag = msg.edited && !isDeleted
        ? `<span class="edited-tag">edited</span>`
        : "";

    // Show sender name above bubble for others' messages
    const senderNameHtml = !isMine
        ? `<div class="sender-name">${escapeHtml(msg.senderUsername)}</div>`
        : "";

    // Action buttons only carry the message id instead of embedding raw
    // message content inside an inline onclick string.
    let actionsHtml = "";
    if (isMine && !isDeleted) {
        actionsHtml = `
            <div class="bubble-actions">
                <button class="action-btn" onclick="openEditDialog('${msg.id}')">Edit</button>
                <button class="action-btn danger" onclick="openDeleteDialog('${msg.id}')">Delete</button>
                <button class="action-btn" onclick="reportMessage('${msg.id}')">Report</button>
            </div>
        `;
    } else if (!isMine && !isDeleted) {
        actionsHtml = `
            <div class="bubble-actions">
                <button class="action-btn" onclick="reportMessage('${msg.id}')">Report</button>
            </div>
        `;
    }

    bubble.innerHTML = `
        ${senderNameHtml}
        <div class="bubble-content">${contentHtml}</div>
        <div class="bubble-meta">
            <span class="bubble-time">${timeStr}</span>
            ${editedTag}
        </div>
        ${actionsHtml}
    `;

    return bubble;
}

// Send a text message
async function sendMessage() {
    const content = messageInput.value.trim();
    if (!content) return;

    messageInput.value = "";

    try {
        const response = await fetch(`${API_BASE}/chat/send`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                chatId: chatId,
                sender: currentUsername,
                content: content,
                type: "TEXT"
            })
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            alert(errData.error || "Failed to send message.");
            messageInput.value = content;
            return;
        }

        const data = await response.json();
        if (data.messageId) {
            const newMessage = {
                id: data.messageId,
                senderUsername: currentUsername,
                content: content,
                type: "TEXT",
                timestamp: localDateTimeArray(),
                edited: false,
                deleted: false
            };
            seenMessageIds.add(data.messageId);
            allMessages.push(newMessage);
            renderMessages(allMessages);
            scrollToBottom();
        }
    } catch (err) {
        console.error("Failed to send message:", err);
        alert("Failed to send message. Is the server running?");
        messageInput.value = content;
    }
}

// NEW: converts a selected File into a base64 data URL for upload
function fileToBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result); // "data:<type>;base64,...."
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
}

// NEW: full upload flow - upload the file to /api/media/upload, then send a
// MEDIA message pointing at the saved path. Replaces the old placeholder that
// just showed an alert saying uploads weren't implemented yet.
fileInput.addEventListener("change", async () => {
    const file = fileInput.files[0];
    if (!file) return;

    try {
        const base64Data = await fileToBase64(file);

        const uploadResponse = await fetch(`${API_BASE}/media/upload`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                fileName: file.name,
                fileData: base64Data
            })
        });

        if (!uploadResponse.ok) {
            const errData = await uploadResponse.json().catch(() => ({}));
            alert(errData.error || "Failed to upload file.");
            return;
        }

        const uploadData = await uploadResponse.json();
        const filePath = uploadData.path;

        const sendResponse = await fetch(`${API_BASE}/chat/send`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                chatId: chatId,
                sender: currentUsername,
                content: filePath,
                type: "MEDIA"
            })
        });

        if (!sendResponse.ok) {
            const errData = await sendResponse.json().catch(() => ({}));
            alert(errData.error || "Failed to send file.");
            return;
        }

        const sendData = await sendResponse.json();
        if (sendData.messageId) {
            const newMessage = {
                id: sendData.messageId,
                senderUsername: currentUsername,
                content: filePath,
                type: "MEDIA",
                timestamp: localDateTimeArray(),
                edited: false,
                deleted: false
            };
            seenMessageIds.add(sendData.messageId);
            allMessages.push(newMessage);
            renderMessages(allMessages);
            scrollToBottom();
        }
    } catch (err) {
        console.error("File upload failed:", err);
        alert("Failed to upload file. Is the server running?");
    } finally {
        fileInput.value = "";
    }
});

// Info Panel toggle
infoToggleBtn.addEventListener("click", () => {
    infoPanel.classList.toggle("open");
});

document.getElementById("headerInfo").addEventListener("click", () => {
    infoPanel.classList.toggle("open");
});

closePanelBtn.addEventListener("click", () => {
    infoPanel.classList.remove("open");
});

// Add member
addMemberBtn.addEventListener("click", async () => {
    const username = addMemberInput.value.trim();
    if (!username || !currentGroup) return;

    try {
        const response = await fetch(`${API_BASE}/group/addMember`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                groupId: currentGroup.groupId,
                username: username
            })
        });
        const data = await response.json();
        if (response.ok && data.status === "member added") {
            addMemberInput.value = "";
            await loadGroupInfo();
            alert(`${username} added to the group!`);
        } else {
            alert(data.error || "Failed to add member.");
        }
    } catch (err) {
        console.error("Failed to add member:", err);
        alert("Could not connect to server.");
    }
});

// Leave group dialog
leaveGroupBtn.addEventListener("click", () => {
    document.getElementById("leaveDialog").style.display = "flex";
});

document.getElementById("leaveCancelBtn").addEventListener("click", () => {
    document.getElementById("leaveDialog").style.display = "none";
});

document.getElementById("leaveConfirmBtn").addEventListener("click", async () => {
    if (!currentGroup) return;
    try {
        const response = await fetch(`${API_BASE}/group/removeMember`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                groupId: currentGroup.groupId,
                username: currentUsername
            })
        });
        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            alert(errData.error || "Failed to leave the group.");
            return;
        }
        window.location.href = "home.html";
    } catch (err) {
        console.error("Failed to leave group:", err);
        alert("Could not connect to server.");
    }
});

// Edit message dialog
function openEditDialog(messageId) {
    const msg = allMessages.find(m => m.id === messageId);
    if (!msg) return;
    if (msg.type === "MEDIA") {
        alert("Media messages can't be edited. You can delete and resend instead.");
        return;
    }
    editingMessageId = messageId;
    document.getElementById("editInput").value = msg.content || "";
    document.getElementById("editDialog").style.display = "flex";
}

document.getElementById("editCancelBtn").addEventListener("click", () => {
    document.getElementById("editDialog").style.display = "none";
    editingMessageId = null;
});

document.getElementById("editConfirmBtn").addEventListener("click", async () => {
    const newContent = document.getElementById("editInput").value.trim();
    if (!newContent || !editingMessageId) return;

    try {
        await fetch(`${API_BASE}/chat/edit`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                chatId: chatId,
                messageId: editingMessageId,
                newContent: newContent
            })
        });

        const msg = allMessages.find(m => m.id === editingMessageId);
        if (msg) {
            msg.content = newContent;
            msg.edited = true;
        }

        renderMessages(allMessages);
        document.getElementById("editDialog").style.display = "none";
        editingMessageId = null;
    } catch (err) {
        console.error("Failed to edit message:", err);
    }
});

// Delete message dialog
function openDeleteDialog(messageId) {
    deletingMessageId = messageId;
    document.getElementById("deleteDialog").style.display = "flex";
}

document.getElementById("deleteCancelBtn").addEventListener("click", () => {
    document.getElementById("deleteDialog").style.display = "none";
    deletingMessageId = null;
});

document.getElementById("deleteConfirmBtn").addEventListener("click", async () => {
    if (!deletingMessageId) return;

    try {
        await fetch(`${API_BASE}/chat/delete`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                chatId: chatId,
                messageId: deletingMessageId,
                requestingUsername: currentUsername
            })
        });

        const msg = allMessages.find(m => m.id === deletingMessageId);
        if (msg) msg.deleted = true;

        renderMessages(allMessages);
        document.getElementById("deleteDialog").style.display = "none";
        deletingMessageId = null;
    } catch (err) {
        console.error("Failed to delete message:", err);
    }
});

// Report message
async function reportMessage(messageId) {
    try {
        await fetch(`${API_BASE}/chat/report`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ chatId, messageId })
        });
        alert("Message reported to admin.");
    } catch (err) {
        console.error("Failed to report message:", err);
    }
}

// Message search
searchToggleBtn.addEventListener("click", () => {
    const isVisible = messageSearchBar.style.display !== "none";
    messageSearchBar.style.display = isVisible ? "none" : "flex";
    if (!isVisible) messageSearchInput.focus();
    else {
        messageSearchInput.value = "";
        renderMessages(allMessages);
    }
});

closeSearchBtn.addEventListener("click", () => {
    messageSearchBar.style.display = "none";
    messageSearchInput.value = "";
    renderMessages(allMessages);
});

messageSearchInput.addEventListener("input", () => {
    const query = messageSearchInput.value.trim().toLowerCase();
    if (!query) { renderMessages(allMessages); return; }

    const filtered = allMessages.filter(m =>
        m.content && m.content.toLowerCase().includes(query)
    );

    messagesArea.innerHTML = "";
    filtered.forEach(msg => {
        const bubble = createMessageBubble(msg);
        const contentEl = bubble.querySelector(".bubble-content");
        if (contentEl && msg.type !== "MEDIA" && msg.content) {
            const regex = new RegExp(`(${escapeRegex(query)})`, "gi");
            contentEl.innerHTML = escapeHtml(msg.content).replace(
                regex, `<span class="highlight">$1</span>`
            );
        }
        messagesArea.appendChild(bubble);
    });
});

// Send on Enter
messageInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        sendMessage();
    }
});

sendBtn.addEventListener("click", sendMessage);

// Close dialogs when clicking outside
["editDialog", "deleteDialog", "leaveDialog"].forEach(id => {
    document.getElementById(id).addEventListener("click", (e) => {
        if (e.target === document.getElementById(id)) {
            document.getElementById(id).style.display = "none";
            editingMessageId = null;
            deletingMessageId = null;
        }
    });
});

// functions
function scrollToBottom() {
    messagesArea.scrollTop = messagesArea.scrollHeight;
}

function formatTimestamp(timestamp) {
    if (!timestamp) return "";
    if (Array.isArray(timestamp)) {
        const [year, month, day, hour, minute] = timestamp;
        const date = new Date(year, month - 1, day, hour, minute);
        return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    }
    return "";
}

function localDateTimeArray() {
    const now = new Date();
    return [now.getFullYear(), now.getMonth() + 1, now.getDate(),
            now.getHours(), now.getMinutes(), now.getSeconds(), 0];
}

function escapeHtml(text) {
    if (!text) return "";
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;");
}

function escapeRegex(text) {
    return text.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

// Init
loadGroupInfo();
loadMessages();