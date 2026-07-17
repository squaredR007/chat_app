document.addEventListener("DOMContentLoaded", () => {
    if (window.applyGlobalTheme) window.applyGlobalTheme();
    if (window.applyGlobalBackground) window.applyGlobalBackground();
});

// Config
const API_BASE = "http://localhost:8080/api";

// Read logged-in user
const currentUsername = localStorage.getItem("username");
if (!currentUsername) {
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

// DOM references
const chatNameLabel = document.getElementById("chatNameLabel");
const historyList = document.getElementById("historyList");
const backBtn = document.getElementById("backBtn");

backBtn.addEventListener("click", (e) => {
    e.preventDefault();
    if (window.history.length > 1) {
        window.history.back();
    } else {
        window.location.href = "home.html";
    }
});

// Figure out the chat's display name (reuses the same /api/chat/list the
// other pages use, so it stays consistent with what the user sees elsewhere)
async function loadChatName() {
    try {
        const response = await fetch(`${API_BASE}/chat/list?username=${encodeURIComponent(currentUsername)}`);
        const chats = await response.json();
        const chat = Array.isArray(chats) ? chats.find(c => c.chatId === chatId) : null;

        if (!chat) {
            chatNameLabel.textContent = "";
            return;
        }

        if (chat.chatId === `saved_${currentUsername}`) {
            chatNameLabel.textContent = "Saved Messages";
        } else if (chat.group) {
            chatNameLabel.textContent = chat.group.groupName || "Group";
        } else {
            chatNameLabel.textContent = chat.user1Username === currentUsername
                ? chat.user2Username
                : chat.user1Username;
        }
    } catch (err) {
        chatNameLabel.textContent = "";
        console.error("Failed to load chat name:", err);
    }
}

// Load edited/deleted messages for this chat
async function loadHistory() {
    try {
        const response = await fetch(`${API_BASE}/chat/history?chatId=${encodeURIComponent(chatId)}`);
        if (!response.ok) {
            throw new Error(`Server returned ${response.status}`);
        }
        const messages = await response.json();
        renderHistory(Array.isArray(messages) ? messages : []);
    } catch (err) {
        historyList.innerHTML = `<div class="empty-state">Could not connect to server.<br>Make sure the server is running.</div>`;
        console.error("Failed to load history:", err);
    }
}

function renderHistory(messages) {
    if (messages.length === 0) {
        historyList.innerHTML = `<div class="empty-state">No edited or deleted messages in this chat.</div>`;
        return;
    }

    historyList.innerHTML = "";

    messages.forEach(msg => {
        const entry = document.createElement("div");
        entry.className = `history-entry${msg.deleted ? " deleted" : ""}`;

        const tag = msg.deleted
            ? `<span class="history-tag deleted">Deleted</span>`
            : `<span class="history-tag edited">Edited</span>`;

        let contentHtml;
        if (msg.deleted) {
            contentHtml = `
                <div class="history-content-row current">
                    <span class="label">Last known content</span>
                    <span class="value">${msg.type === "MEDIA" ? "📎 Media" : escapeHtml(msg.content || "")}</span>
                </div>
            `;
        } else {
            contentHtml = `
                ${msg.previousContent ? `
                    <div class="history-content-row previous">
                        <span class="label">Before</span>
                        <span class="value">${escapeHtml(msg.previousContent)}</span>
                    </div>
                ` : ""}
                <div class="history-content-row current">
                    <span class="label">Current</span>
                    <span class="value">${escapeHtml(msg.content || "")}</span>
                </div>
            `;
        }

        entry.innerHTML = `
            <div class="history-entry-meta">
                <span class="history-sender">${escapeHtml(msg.senderUsername || "")}</span>
                <span class="history-time">${formatTimestamp(msg.timestamp)}</span>
            </div>
            ${tag}
            ${contentHtml}
        `;

        historyList.appendChild(entry);
    });
}

function formatTimestamp(timestamp) {
    if (!timestamp || !Array.isArray(timestamp)) return "";
    const [year, month, day, hour, minute] = timestamp;
    const date = new Date(year, month - 1, day, hour, minute);
    return date.toLocaleString([], {
        month: "short", day: "numeric",
        hour: "2-digit", minute: "2-digit"
    });
}

function escapeHtml(text) {
    if (!text) return "";
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;");
}

// Init
loadChatName();
loadHistory();
