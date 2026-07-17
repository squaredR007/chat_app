// Force fresh session check before anything else
// Runs immediately before any other code to prevent stale data
(function () {
    const username = localStorage.getItem("username");

    if (!username) {
        window.location.href = "../pages/login.html";
    }
})();

// Config
const API_BASE = "http://localhost:8080/api";

// Read logged-in user from localStorage
const currentUsername = localStorage.getItem("username");

// Apply saved theme & background
document.addEventListener("DOMContentLoaded", () => {
    window.applyGlobalTheme?.();
    window.applyGlobalBackground?.();
});

// DOM References
const chatList = document.getElementById("chatList");
const searchInput = document.getElementById("searchInput");
const currentUserAvatar = document.getElementById("currentUserAvatar");
const themeToggle = document.getElementById("themeToggle");

// Show current user's initial
currentUserAvatar.textContent = currentUsername
    ? currentUsername.charAt(0).toUpperCase()
    : "?";

// Initialize theme icon
themeToggle.textContent =
    localStorage.getItem("theme") === "dark" ? "☀️" : "🌙";

// Theme Toggle
themeToggle.addEventListener("click", () => {
    const isDark = localStorage.getItem("theme") !== "dark";

    localStorage.setItem("theme", isDark ? "dark" : "light");

    // Apply theme/background everywhere
    window.applyGlobalTheme?.();
    window.applyGlobalBackground?.();

    // Update icon
    themeToggle.textContent =
        document.body.classList.contains("dark") ? "☀️" : "🌙";

    // Save to backend
    if (currentUsername) {
        fetch(`${API_BASE}/settings/changeDarkMode`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                username: currentUsername,
                darkmode: isDark
            })
        }).catch(err => console.error("Failed to save theme:", err));
    }
});

// Listen for changes from other pages/tabs
window.addEventListener("storage", (e) => {
    if (e.key === "theme" || e.key === "background") {
        window.applyGlobalTheme?.();
        window.applyGlobalBackground?.();

        themeToggle.textContent =
            document.body.classList.contains("dark") ? "☀️" : "🌙";
    }
});

// State
let allChats = [];
let showingArchived = false;

// Fetch chats
async function loadChats() {
    try {
        const response = await fetch(
            `${API_BASE}/chat/list?username=${encodeURIComponent(currentUsername)}`
        );

        if (!response.ok) {
            throw new Error(`Server returned ${response.status}`);
        }

        const data = await response.json();
        allChats = Array.isArray(data) ? data : [];

        renderCurrentView();
    } catch (error) {
        chatList.innerHTML = `
            <div class="empty-state">
                Could not connect to server.<br>
                Make sure the server is running.
            </div>
        `;
        console.error("Failed to load chats:", error);
    }
}

// Picks the right list (recent vs. archived)
function renderCurrentView() {
    let visibleChats = allChats.filter(chat =>
        showingArchived ? chat.archived : !chat.archived
    );

    visibleChats.sort((a, b) => {
        if (a.pinned && !b.pinned) return -1;
        if (!a.pinned && b.pinned) return 1;
        return getLastMessageMillis(b) - getLastMessageMillis(a);
    });

    renderChats(visibleChats);
}

function getLastMessageMillis(chat) {
    const messages = chat.messages;

    if (!messages || messages.length === 0) return 0;

    const last = messages[messages.length - 1];

    if (!last.timestamp || !Array.isArray(last.timestamp)) return 0;

    const [year, month, day, hour, minute, second] = last.timestamp;

    return new Date(
        year,
        month - 1,
        day,
        hour,
        minute,
        second || 0
    ).getTime();
}

// Render chats
function renderChats(chats) {
    chatList.innerHTML = "";

    if (showingArchived) {
        const backRow = document.createElement("a");
        backRow.href = "#";
        backRow.className = "archive-row";
        backRow.innerHTML =
            `<span class="archive-icon">←</span><span>Back to chats</span>`;

        backRow.addEventListener("click", e => {
            e.preventDefault();
            showingArchived = false;
            renderCurrentView();
        });

        chatList.appendChild(backRow);
    } else {
        const archiveRow = document.createElement("a");
        archiveRow.href = "#";
        archiveRow.className = "archive-row";
        archiveRow.innerHTML =
            `<span class="archive-icon">🗂️</span><span>Archived Chats</span>`;

        archiveRow.addEventListener("click", e => {
            e.preventDefault();
            showingArchived = true;
            renderCurrentView();
        });

        chatList.appendChild(archiveRow);
    }

    if (chats.length === 0) {
        const empty = document.createElement("div");
        empty.className = "empty-state";
        empty.innerHTML = showingArchived
            ? "No archived chats."
            : "No conversations yet.<br>Start a new one!";

        chatList.appendChild(empty);
        return;
    }

    chats.forEach(chat => chatList.appendChild(createChatItem(chat)));
}

// Create chat item
function createChatItem(chat) {
    const isSaved = chat.chatId === `saved_${currentUsername}`;
    const isGroup = chat.group != null;

    let displayName;

    if (isSaved) {
        displayName = "Saved Messages";
    } else if (isGroup) {
        displayName = chat.group.groupName || "Unknown Group";
    } else {
        displayName =
            chat.user1Username === currentUsername
                ? chat.user2Username
                : chat.user1Username;
    }

    const avatarLetter = displayName
        ? displayName.charAt(0).toUpperCase()
        : "?";

    let avatarClass = "chat-avatar";

    if (isSaved) avatarClass += " saved";
    else if (isGroup) avatarClass += " group";

    const pinIcon = chat.pinned
        ? `<span class="pin-icon">📌</span>`
        : "";

    const item = document.createElement("a");

    item.href = isGroup
        ? `group-chat.html?chatId=${encodeURIComponent(chat.chatId)}`
        : `chat.html?chatId=${encodeURIComponent(chat.chatId)}`;

    item.className = `chat-item${chat.pinned ? " pinned" : ""}`;

    item.innerHTML = `
        <div class="${avatarClass}">
            ${isSaved ? "⭐" : avatarLetter}
        </div>

        <div class="chat-info">
            <div class="chat-name">${escapeHtml(displayName || "")}</div>
            <div class="chat-preview">${getLastMessagePreview(chat)}</div>
        </div>

        <div class="chat-meta">
            <span class="chat-time">${getLastMessageTime(chat)}</span>
            ${pinIcon}
        </div>
    `;

    return item;
}

function getLastMessagePreview(chat) {
    const messages = chat.messages;

    if (!messages || messages.length === 0)
        return "No messages yet";

    const last = messages[messages.length - 1];

    if (last.deleted) return "🚫 Message deleted";
    if (last.type === "MEDIA") return "📎 Media";

    return last.content
        ? escapeHtml(last.content.substring(0, 40))
        : "";
}

function getLastMessageTime(chat) {
    const messages = chat.messages;

    if (!messages || messages.length === 0) return "";

    const last = messages[messages.length - 1];

    if (!last.timestamp || !Array.isArray(last.timestamp))
        return "";

    const [year, month, day, hour, minute] = last.timestamp;

    return formatTime(
        new Date(year, month - 1, day, hour, minute)
    );
}

function formatTime(date) {
    const now = new Date();

    if (date.toDateString() === now.toDateString()) {
        return date.toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit"
        });
    }

    return date.toLocaleDateString([], {
        month: "short",
        day: "numeric"
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

// Search
searchInput.addEventListener("input", () => {
    const query = searchInput.value.trim().toLowerCase();

    const base = allChats.filter(chat =>
        showingArchived ? chat.archived : !chat.archived
    );

    if (!query) {
        renderChats(base);
        return;
    }

    renderChats(
        base.filter(chat =>
            getDisplayName(chat)
                .toLowerCase()
                .includes(query)
        )
    );
});

function getDisplayName(chat) {
    if (chat.chatId?.startsWith("saved_"))
        return "Saved Messages";

    if (chat.group)
        return chat.group.groupName || "";

    return chat.user1Username === currentUsername
        ? chat.user2Username
        : chat.user1Username;
}

// Poll
setInterval(loadChats, 3000);

// Initial load
loadChats();