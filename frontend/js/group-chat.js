// ── Config ──
const API_BASE = "http://localhost:8080/api";
const POLL_INTERVAL = 3000;

// ── Read logged-in user from localStorage ──
const currentUsername = localStorage.getItem("username");
const currentUserId = localStorage.getItem("userId");

if (!currentUsername || !currentUserId) {
    window.location.href = "login.html";
}

// ── Apply saved theme ──
if (localStorage.getItem("theme") === "dark") {
    document.body.classList.add("dark");
}

// ── Read chatId from URL ──
const urlParams = new URLSearchParams(window.location.search);
const chatId = urlParams.get("chatId");

if (!chatId) {
    window.location.href = "home.html";
}

// ── DOM References ──
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

// ── State ──
let allMessages = [];
let lastPollTimestamp = 0;
let editingMessageId = null;
let deletingMessageId = null;
let currentGroup = null; // the Group object for this chat

// ── Load group info ──
async function loadGroupInfo() {
    try {
        const response = await fetch(`${API_BASE}/chat/list`);
        const chats = await response.json();

        const chat = chats.find(c => c.chatId === chatId);
        if (!chat || !chat.group) {
            headerName.textContent = "Group";
            return;
        }

        currentGroup = chat.group;

        // Update header
        headerName.textContent = currentGroup.groupName;
        headerAvatar.textContent = currentGroup.groupName.charAt(0).toUpperCase();
        const memberCount = currentGroup.membersUsernames
            ? currentGroup.membersUsernames.length
            : 0;
        headerStatus.textContent = `${memberCount} member${memberCount !== 1 ? "s" : ""}`;

        // Update info panel
        document.getElementById("infoGroupAvatar").textContent =
            currentGroup.groupName.charAt(0).toUpperCase();
        document.getElementById("infoGroupName").textContent = currentGroup.groupName;
        document.getElementById("infoGroupId").textContent = `ID: ${currentGroup.groupId}`;
        document.getElementById("memberCountLabel").textContent =
            `Members · ${memberCount}`;

        // Render member list
        renderMembers(currentGroup);

        // Show add member button only if current user is admin
        if (currentGroup.adminUsername === currentUsername) {
            addMemberSection.style.display = "flex";
        }

    } catch (err) {
        headerName.textContent = "Group";
        console.error("Failed to load group info:", err);
    }
}

// ── Render member list in info panel ──
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
            <div class="member-name">${username}${isMe ? " (you)" : ""}</div>
            ${isAdmin ? `<span class="admin-badge">Admin</span>` : ""}
        `;
        memberList.appendChild(item);
    });
}

// ── Load all messages ──
async function loadMessages() {
    try {
        const response = await fetch(`${API_BASE}/chat/messages?chatId=${chatId}`);
        const messages = await response.json();

        allMessages = messages;
        lastPollTimestamp = Date.now();
        renderMessages(allMessages);
        scrollToBottom();
    } catch (err) {
        messagesArea.innerHTML = `<div class="empty-state">Could not connect to server.<br>Make sure the server is running.</div>`;
        console.error("Failed to load messages:", err);
    }
}

// ── Poll for new messages ──
async function pollNewMessages() {
    try {
        const response = await fetch(`${API_BASE}/chat/poll?chatId=${chatId}&since=${lastPollTimestamp}`);
        const newMessages = await response.json();

        if (newMessages && newMessages.length > 0) {
            allMessages = [...allMessages, ...newMessages];
            lastPollTimestamp = Date.now();
            renderMessages(allMessages);
            scrollToBottom();
        }
    } catch (err) {
        console.error("Poll failed:", err);
    }
}

// ── Render all messages ──
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

// ── Create a single message bubble ──
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
        contentHtml = "📎 Media file";
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

    // Action buttons
    let actionsHtml = "";
    if (isMine && !isDeleted) {
        actionsHtml = `
            <div class="bubble-actions">
                <button class="action-btn" onclick="openEditDialog('${msg.id}', \`${escapeHtml(msg.content)}\`)">Edit</button>
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

// ── Send message ──
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
                content: content
            })
        });

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
            allMessages.push(newMessage);
            renderMessages(allMessages);
            scrollToBottom();
        }
    } catch (err) {
        console.error("Failed to send message:", err);
        alert("Failed to send message. Is the server running?");
    }
}

// ── Info Panel toggle ──
infoToggleBtn.addEventListener("click", () => {
    infoPanel.classList.toggle("open");
});

document.getElementById("headerInfo").addEventListener("click", () => {
    infoPanel.classList.toggle("open");
});

closePanelBtn.addEventListener("click", () => {
    infoPanel.classList.remove("open");
});

// ── Add member ──
addMemberBtn.addEventListener("click", async () => {
    const username = addMemberInput.value.trim();
    if (!username) return;

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
        if (data.status === "member added") {
            addMemberInput.value = "";
            // Refresh group info
            await loadGroupInfo();
            alert(`${username} added to the group!`);
        } else {
            alert("Failed to add member.");
        }
    } catch (err) {
        console.error("Failed to add member:", err);
    }
});

// ── Leave group dialog ──
leaveGroupBtn.addEventListener("click", () => {
    document.getElementById("leaveDialog").style.display = "flex";
});

document.getElementById("leaveCancelBtn").addEventListener("click", () => {
    document.getElementById("leaveDialog").style.display = "none";
});

document.getElementById("leaveConfirmBtn").addEventListener("click", async () => {
    try {
        await fetch(`${API_BASE}/group/removeMember`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                groupId: currentGroup.groupId,
                username: currentUsername
            })
        });
        // Go back to home after leaving
        window.location.href = "home.html";
    } catch (err) {
        console.error("Failed to leave group:", err);
    }
});

// ── Edit message dialog ──
function openEditDialog(messageId, currentContent) {
    editingMessageId = messageId;
    document.getElementById("editInput").value = currentContent;
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

// ── Delete message dialog ──
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

// ── Report message ──
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

// ── Message search ──
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
        if (contentEl && msg.content) {
            const regex = new RegExp(`(${escapeRegex(query)})`, "gi");
            contentEl.innerHTML = escapeHtml(msg.content).replace(
                regex, `<span class="highlight">$1</span>`
            );
        }
        messagesArea.appendChild(bubble);
    });
});

// ── Send on Enter ──
messageInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        sendMessage();
    }
});

sendBtn.addEventListener("click", sendMessage);

// ── File attach placeholder ──
fileInput.addEventListener("change", () => {
    if (fileInput.files.length > 0) {
        alert(`File "${fileInput.files[0].name}" selected. File upload will be implemented in Phase 2.`);
        fileInput.value = "";
    }
});

// ── Close dialogs when clicking outside ──
["editDialog", "deleteDialog", "leaveDialog"].forEach(id => {
    document.getElementById(id).addEventListener("click", (e) => {
        if (e.target === document.getElementById(id)) {
            document.getElementById(id).style.display = "none";
            editingMessageId = null;
            deletingMessageId = null;
        }
    });
});

// ── Helpers ──
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

// ── Init ──
loadGroupInfo();
loadMessages();
setInterval(pollNewMessages, POLL_INTERVAL);
