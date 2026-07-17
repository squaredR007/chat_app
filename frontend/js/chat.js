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

let isPinned = false;

// ── Config ──
const API_BASE = "http://localhost:8080/api";
const POLL_INTERVAL = 3000;

// ── Read logged-in user from localStorage ──
const currentUsername = localStorage.getItem("username");

if (!currentUsername) {
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

// ── State ──
let allMessages = [];
// Start at current time so poll only fetches NEW messages after page load
// Will be updated properly after loadMessages() completes
let lastPollTimestamp = Date.now();
let pollInterval = null;
let editingMessageId = null;
let deletingMessageId = null;
let otherUsername = null;
// Track message IDs we already have to prevent duplicates
const seenMessageIds = new Set();


//tells the server this user has npw seen this chat
async function markChatAsRead() {
    try {
        await fetch(`${API_BASE}/chat/markRead`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ chatId, username: currentUsername })
        });
    } catch (err) {
        console.error("Failed to mark chat as read:", err);
    }
}

// ── Load chat info ──
async function loadChatInfo() {
    try {
        const response = await fetch(`${API_BASE}/chat/list?username=${encodeURIComponent(currentUsername)}`);
        const chat = Array.isArray(chats) ? chats.find(c => c.chatId === chatId) : null;

        if (!chat) {
            headerName.textContent = "Unknown Chat";
            return;
        }

        isPinned = chat.pinned;
        document.getElementById("pinBtn").textContent = isPinned ? "Unpin" : "Pin";
        document.getElementById("pinBtn").classList.toggle("pinned", isPinned);

        const isSaved = chatId.startsWith("saved_");
        if (isSaved) {
            otherUsername = currentUsername;
            headerName.textContent = "Saved Messages";
            headerAvatar.textContent = "⭐";
            headerAvatar.style.background = "#1a6b3c";
            headerStatus.textContent = "Your personal notes";
        } else {
            otherUsername = chat.user1Username === currentUsername
                ? chat.user2Username
                : chat.user1Username;
            headerName.textContent = otherUsername;
            headerAvatar.textContent = otherUsername ? otherUsername.charAt(0).toUpperCase() : "?";
            headerStatus.textContent = "last seen recently";
        }
    } catch (err) {
        headerName.textContent = "Chat";
        console.error("Failed to load chat info:", err);
    }
}

// ── Load all messages (initial load only) ──
async function loadMessages() {
    try {
        const response = await fetch(`${API_BASE}/chat/messages?chatId=${encodeURIComponent(chatId)}`);
        const messages = await response.json();

        allMessages = Array.isArray(messages) ? messages : [];

        // Track all loaded message IDs so poll doesn't add them again
        allMessages.forEach(m => seenMessageIds.add(m.id));

        // Set timestamp AFTER loading so poll only gets messages newer than now
        lastPollTimestamp = Date.now();

        renderMessages(allMessages);
        scrollToBottom();

        markChatAsRead() ;

        // Start polling only AFTER initial load is complete
        if (!pollInterval) {
            pollInterval = setInterval(pollNewMessages, POLL_INTERVAL);
        }
    } catch (err) {
        messagesArea.innerHTML = `<div class="empty-state">Could not connect to server.<br>Make sure the server is running.</div>`;
        console.error("Failed to load messages:", err);
    }
}

// ── Poll for NEW messages only ──
async function pollNewMessages() {
    try {
        const response = await fetch(`${API_BASE}/chat/poll?chatId=${encodeURIComponent(chatId)}&since=${lastPollTimestamp}`);
        const newMessages = await response.json();

        if (!Array.isArray(newMessages) || newMessages.length === 0) return;

        // Filter out any messages we already have (prevents duplicates)
        const trulyNew = newMessages.filter(m => !seenMessageIds.has(m.id));

        if (trulyNew.length === 0) return;

        // Update timestamp and seen IDs
        lastPollTimestamp = Date.now();
        trulyNew.forEach(m => seenMessageIds.add(m.id));

        allMessages = [...allMessages, ...trulyNew];
        renderMessages(allMessages);
        scrollToBottom();

        markChatAsRead() ;
    } catch (err) {
        console.error("Poll failed:", err);
    }
}

// ── Render all messages ──
function renderMessages(messages) {
    if (!messages || messages.length === 0) {
        messagesArea.innerHTML = `<div class="empty-state">No messages yet. Say hello! 👋</div>`;
        return;
    }

    messagesArea.innerHTML = "";
    messages.forEach(msg => {
        const bubble = createMessageBubble(msg);
        messagesArea.appendChild(bubble);
    });
}


function isImagePath(path) {
    return /\.(png|jpe?g|gif|webp)$/i.test(path || "");
}


function mediaUrl(content) {
    return `${API_BASE}/media/${content}`;
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

    // Action buttons only carry the message id (a safe UUID) instead of
    // embedding raw message content inside an inline onclick string.
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
        <div class="bubble-content">${contentHtml}</div>
        <div class="bubble-meta">
            <span class="bubble-time">${timeStr}</span>
            ${editedTag}
        </div>
        ${actionsHtml}
    `;

    return bubble;
}

// ── Send a text message ──
async function sendMessage() {
    const content = messageInput.value.trim();
    if (!content) return;

    // Clear input immediately to prevent double-sends
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


function fileToBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result); // "data:<type>;base64,...."
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
}


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

// ── Edit message dialog ──
// Content is looked up from allMessages by id instead of being passed through
// an inline onclick string (safer, avoids breaking on special characters).
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
        const response = await fetch(`${API_BASE}/chat/edit`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                chatId: chatId,
                messageId: editingMessageId,
                newContent: newContent
            })
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            alert(errData.error || "Failed to edit message.");
            return;
        }

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
    if (!query) {
        renderMessages(allMessages);
        return;
    }

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

// ── Send on Enter ──
messageInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        sendMessage();
    }
});

// ── Send button ──
sendBtn.addEventListener("click", sendMessage);

// ── Close dialogs when clicking outside ──
document.getElementById("editDialog").addEventListener("click", (e) => {
    if (e.target === document.getElementById("editDialog")) {
        document.getElementById("editDialog").style.display = "none";
        editingMessageId = null;
    }
});

document.getElementById("deleteDialog").addEventListener("click", (e) => {
    if (e.target === document.getElementById("deleteDialog")) {
        document.getElementById("deleteDialog").style.display = "none";
        deletingMessageId = null;
    }
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

document.getElementById("pinBtn").addEventListener("click", async () => {
    if (isPinned) {
        await unpinChat(chatId);
        isPinned = false;
    } else {
        await pinChat(chatId);
        isPinned = true;
    }

    document.getElementById("pinBtn").textContent = isPinned ? "Unpin" : "Pin";
});

//pin chat
async function pinChat(chatId) {
    await fetch(`${API_BASE}/chat/pin`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            chatId: chatId
        })
    });

    alert("Chat pinned");
}

//unpin chat
async function unpinChat(chatId) {
    await fetch(`${API_BASE}/chat/unpin`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            chatId: chatId
        })
    });

    alert("Chat unpinned");
}

// ── Init ──
loadChatInfo();
loadMessages(); // polling starts inside loadMessages() after initial load