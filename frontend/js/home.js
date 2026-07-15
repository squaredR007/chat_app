// ── Force fresh session check before anything else ──
// Runs immediately before any other code to prevent stale data
(function() {
    const username = localStorage.getItem("username");
    const userId = localStorage.getItem("userId");
    if (!username || !userId) {
        window.location.href = "../pages/login.html";
    }
})();

// ── Config ──
const API_BASE = "http://localhost:8080/api";

// ── Read logged-in user from localStorage (set by login page) ──
const currentUsername = localStorage.getItem("username");
const currentUserId = localStorage.getItem("userId");

// ── DOM References ──
const chatList = document.getElementById("chatList");
const searchInput = document.getElementById("searchInput");
const currentUserAvatar = document.getElementById("currentUserAvatar");

// Show current user's initial in the sidebar avatar
currentUserAvatar.textContent = currentUsername ? currentUsername.charAt(0).toUpperCase() : "?";

// ── Theme Toggle ──
const themeToggle = document.getElementById("themeToggle");

const savedTheme = localStorage.getItem("theme");
if (savedTheme === "dark") {
    document.body.classList.add("dark");
    themeToggle.textContent = "☀️";
}

themeToggle.addEventListener("click", () => {
    const isDark = document.body.classList.toggle("dark");
    themeToggle.textContent = isDark ? "☀️" : "🌙";
    localStorage.setItem("theme", isDark ? "dark" : "light");

    if (currentUserId) {
        fetch(`${API_BASE}/settings/changeDarkMode`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                userId: currentUserId,
                darkmode: isDark
            })
        }).catch(err => console.error("Failed to save theme:", err));
    }
});

// ── State ──
let allChats = [];

// ── Fetch all chats from backend ──
async function loadChats() {
    try {
        const response = await fetch(`${API_BASE}/chat/list`);

        if (!response.ok) {
            throw new Error(`Server returned ${response.status}`);
        }

        const data = await response.json();
        allChats = Array.isArray(data) ? data : [];

        const myChats = allChats.filter(chat => isMyChat(chat));

        myChats.sort((a, b) => {
            if (a.pinned && !b.pinned) return -1;
            if (!a.pinned && b.pinned) return 1;
            return 0;
        });

        renderChats(myChats);
    } catch (error) {
        chatList.innerHTML = `<div class="empty-state">Could not connect to server.<br>Make sure the server is running.</div>`;
        console.error("Failed to load chats:", error);
    }
}

// ── Check if a chat belongs to the current user ──
function isMyChat(chat) {
    if (!chat || !chat.chatId) return false;

    // Always show saved messages for current user
    if (chat.chatId === `saved_${currentUsername}`) {
        return true;
    }

    // Private chat
    if (chat.user1Username && chat.user2Username) {
        return chat.user1Username === currentUsername ||
               chat.user2Username === currentUsername;
    }

    // Group chat
    if (chat.group && chat.group.membersUsernames) {
        return chat.group.membersUsernames.includes(currentUsername);
    }

    return false;
}

// ── Render the chat list ──
function renderChats(chats) {
    chatList.innerHTML = "";

    const archiveRow = document.createElement("a");
    archiveRow.href = "#";
    archiveRow.className = "archive-row";
    archiveRow.innerHTML = `
        <span class="archive-icon">🗂️</span>
        <span>Archived Chats</span>
    `;
    chatList.appendChild(archiveRow);

    if (chats.length === 0) {
        const empty = document.createElement("div");
        empty.className = "empty-state";
        empty.innerHTML = "No conversations yet.<br>Start a new one!";
        chatList.appendChild(empty);
        return;
    }

    chats.forEach(chat => {
        const item = createChatItem(chat);
        chatList.appendChild(item);
    });
}

// ── Create a single chat list item ──
function createChatItem(chat) {
    const isSaved = chat.chatId === `saved_${currentUsername}`;
    const isGroup = chat.group != null;

    let displayName;
    if (isSaved) {
        displayName = "Saved Messages";
    } else if (isGroup) {
        displayName = chat.group.groupName || "Unknown Group";
    } else {
        displayName = chat.user1Username === currentUsername
            ? chat.user2Username
            : chat.user1Username;
    }

    const avatarLetter = displayName ? displayName.charAt(0).toUpperCase() : "?";

    let avatarClass = "chat-avatar";
    if (isSaved) avatarClass += " saved";
    else if (isGroup) avatarClass += " group";

    const pinIcon = chat.pinned ? `<span class="pin-icon">📌</span>` : "";

    const item = document.createElement("a");

    if (isGroup) {
        item.href = `group-chat.html?chatId=${chat.chatId}`;
    } else {
        item.href = `chat.html?chatId=${chat.chatId}`;
    }

    item.className = `chat-item${chat.pinned ? " pinned" : ""}`;
    item.innerHTML = `
        <div class="${avatarClass}">${isSaved ? "⭐" : avatarLetter}</div>
        <div class="chat-info">
            <div class="chat-name">${displayName}</div>
            <div class="chat-preview">${getLastMessagePreview(chat)}</div>
        </div>
        <div class="chat-meta">
            <span class="chat-time">${getLastMessageTime(chat)}</span>
            ${pinIcon}
        </div>
    `;

    return item;
}

// ── Get last message preview text ──
function getLastMessagePreview(chat) {
    const messages = chat.messages;
    if (!messages || messages.length === 0) return "No messages yet";
    const last = messages[messages.length - 1];
    if (last.deleted) return "🚫 Message deleted";
    if (last.type === "MEDIA") return "📎 Media";
    return last.content ? last.content.substring(0, 40) : "";
}

// ── Get last message time ──
function getLastMessageTime(chat) {
    const messages = chat.messages;
    if (!messages || messages.length === 0) return "";
    const last = messages[messages.length - 1];
    if (!last.timestamp) return "";
    if (Array.isArray(last.timestamp)) {
        const [year, month, day, hour, minute] = last.timestamp;
        const date = new Date(year, month - 1, day, hour, minute);
        return formatTime(date);
    }
    return "";
}

// ── Format time ──
function formatTime(date) {
    const now = new Date();
    const isToday = date.toDateString() === now.toDateString();
    if (isToday) {
        return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    }
    return date.toLocaleDateString([], { month: "short", day: "numeric" });
}

// ── Search filtering ──
searchInput.addEventListener("input", () => {
    const query = searchInput.value.trim().toLowerCase();
    if (!query) {
        renderChats(allChats.filter(chat => isMyChat(chat)));
        return;
    }
    const filtered = allChats.filter(chat => {
        if (!isMyChat(chat)) return false;
        return getDisplayName(chat).toLowerCase().includes(query);
    });
    renderChats(filtered);
});

// ── Helper to get display name ──
function getDisplayName(chat) {
    if (chat.chatId && chat.chatId.startsWith("saved_")) return "Saved Messages";
    if (chat.group) return chat.group.groupName || "";
    return chat.user1Username === currentUsername ? chat.user2Username : chat.user1Username;
}

// ── Poll for new chats every 3 seconds ──
setInterval(loadChats, 3000);

// ── Initial load ──
loadChats();
