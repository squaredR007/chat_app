//Config
const API_BASE = "http://localhost:7600/api";
const POLL_INTERVAL = 3000; // poll every 3 seconds

//Read logged-in user from localStorage
const currentUsername = localStorage.getItem("username");
const currentUserId = localStorage.getItem("userId");

// If not logged in, redirect to login
if (!currentUsername || !currentUserId) {
    window.location.href = "login.html";
}

// Apply saved theme
if (localStorage.getItem("theme") === "dark") {
    document.body.classList.add("dark");
}

// Read chatId from URL query params
const urlParams = new URLSearchParams(window.location.search);
const chatId = urlParams.get("chatId");

// If no chatId in URL, go back to home
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

// State
let allMessages = [];         // full message list for this chat
let lastPollTimestamp = 0;    // last time we polled 
let editingMessageId = null;  // tracks which message is being edited
let deletingMessageId = null; // tracks which message is being deleted
let otherUsername = null;     // the other user in this private chat

// Load chat info (to get the other user's name)
async function loadChatInfo() {
    try {
        const response = await fetch(`${API_BASE}/chat/list`);
        const chats = await response.json();

        // Find this specific chat by chatId
        const chat = chats.find(c => c.chatId === chatId);
        if (!chat) {
            headerName.textContent = "Unknown Chat";
            return;
        }

        // Determine the other user's username
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
            headerAvatar.textContent = otherUsername.charAt(0).toUpperCase();
            loadOtherUserStatus();
        }
    } catch (err) {
        headerName.textContent = "Chat";
        console.error("Failed to load chat info:", err);
    }
}

// Load the other user's last seen status
async function loadOtherUserStatus() {
    headerStatus.textContent = "last seen recently";
}

// Load all messages for this chat
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

//Poll for new messages since last poll
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
        // silently fail on poll errors
        console.error("Poll failed:", err);
    }
}

//Render all messages
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

//Create a single message bubble
function createMessageBubble(msg) {
    const isMine = msg.senderUsername === currentUsername;
    const isDeleted = msg.deleted;

    const bubble = document.createElement("div");
    bubble.className = `message-bubble ${isMine ? "mine" : "theirs"}${isDeleted ? " deleted" : ""}`;
    bubble.dataset.messageId = msg.id;

    // Format timestamp
    const timeStr = formatTimestamp(msg.timestamp);

    // Message content
    let contentHtml;
    if (isDeleted) {
        contentHtml = "🚫 This message was deleted";
    } else if (msg.type === "MEDIA") {
        contentHtml = "📎 Media file";
    } else {
        contentHtml = escapeHtml(msg.content || "");
    }

    // Edited tag
    const editedTag = msg.edited && !isDeleted
        ? `<span class="edited-tag">edited</span>`
        : "";

    // Action buttons (only for own non-deleted messages)
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
        // Can report others' messages but not edit/delete them
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

// Send a message
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
            // Add the new message locally so it appears immediately
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

// Edit message dialog
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

        // Update locally
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

        // Mark locally as deleted
        const msg = allMessages.find(m => m.id === deletingMessageId);
        if (msg) msg.deleted = true;

        renderMessages(allMessages);
        document.getElementById("deleteDialog").style.display = "none";
        deletingMessageId = null;
    } catch (err) {
        console.error("Failed to delete message:", err);
    }
});

//Report message
async function reportMessage(messageId) {
    try {
        await fetch(`${API_BASE}/chat/report`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                chatId: chatId,
                messageId: messageId
            })
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
        renderMessages(allMessages); // clear highlights
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

    // Highlight matching messages
    const filtered = allMessages.filter(m =>
        m.content && m.content.toLowerCase().includes(query)
    );

    messagesArea.innerHTML = "";
    filtered.forEach(msg => {
        const bubble = createMessageBubble(msg);
        // Highlight the matching text
        const contentEl = bubble.querySelector(".bubble-content");
        if (contentEl && msg.content) {
            const regex = new RegExp(`(${escapeRegex(query)})`, "gi");
            contentEl.innerHTML = escapeHtml(msg.content).replace(
                regex,
                `<span class="highlight">$1</span>`
            );
        }
        messagesArea.appendChild(bubble);
    });
});

//Send on Enter key
messageInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        sendMessage();
    }
});

//Send button click
sendBtn.addEventListener("click", sendMessage);

//File attach (placeholder)
fileInput.addEventListener("change", () => {
    if (fileInput.files.length > 0) {
        alert(`File "${fileInput.files[0].name}" selected. File upload will be fully implemented in Phase 2.`);
        fileInput.value = "";
    }
});

//Close dialogs when clicking outside
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

//Scroll to the latest message
function scrollToBottom() {
    messagesArea.scrollTop = messagesArea.scrollHeight;
}

// Format Java LocalDateTime array to readable string
function formatTimestamp(timestamp) {
    if (!timestamp) return "";
    if (Array.isArray(timestamp)) {
        const [year, month, day, hour, minute] = timestamp;
        const date = new Date(year, month - 1, day, hour, minute);
        return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    }
    return "";
}

// Returns current time as a LocalDateTime-style array
function localDateTimeArray() {
    const now = new Date();
    return [now.getFullYear(), now.getMonth() + 1, now.getDate(),
            now.getHours(), now.getMinutes(), now.getSeconds(), 0];
}

//Escape HTML to prevent XSS
function escapeHtml(text) {
    if (!text) return "";
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;");
}

//Escape special regex characters
function escapeRegex(text) {
    return text.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

// Init
loadChatInfo();
loadMessages();

// Start polling for new messages
setInterval(pollNewMessages, POLL_INTERVAL);
