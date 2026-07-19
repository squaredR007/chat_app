// Config
const API_BASE = "http://localhost:8080/api";

// Read logged-in user from localStorage
const currentUsername = localStorage.getItem("username");

if (!currentUsername) {
    window.location.href = "../pages/login.html";
}

// Apply saved theme
if (localStorage.getItem("theme") === "dark") {
    document.body.classList.add("dark");
}

// DOM References
const contactList = document.getElementById("contactList");
const contactSearchInput = document.getElementById("contactSearchInput");
const privateFeedback = document.getElementById("privateFeedback");
const groupFeedback = document.getElementById("groupFeedback");
const memberChips = document.getElementById("memberChips");
const groupMemberInput = document.getElementById("groupMemberInput");

// State
let groupMembers = [currentUsername]; // admin is always a member
let allContacts = [];

// Tab switching
function switchTab(tab) {
    // Hide all content
    document.querySelectorAll(".tab-content").forEach(el => el.classList.add("hidden"));
    document.querySelectorAll(".tab").forEach(el => el.classList.remove("active"));

    // Show selected
    document.getElementById(`content${capitalize(tab)}`).classList.remove("hidden");
    document.getElementById(`tab${capitalize(tab)}`).classList.add("active");

    // Load contacts when that tab is opened
    if (tab === "contacts") loadContacts();
}

function capitalize(str) {
    return str.charAt(0).toUpperCase() + str.slice(1);
}

// ── Private chat ──

document.getElementById("startPrivateChatBtn").addEventListener("click", async () => {
    const targetUsername = document.getElementById("privateChatUsername").value.trim();

    if (!targetUsername) {
        showFeedback(privateFeedback, "Please enter a username.", "error");
        return;
    }

    if (targetUsername === currentUsername) {
        showFeedback(privateFeedback, "You can't chat with yourself! (Use Saved Messages instead 💌)", "error");
        return;
    }

    // Generate a consistent chatId from both usernames
    const sorted = [currentUsername, targetUsername].sort();
    const chatId = `private_${sorted[0]}_${sorted[1]}`;

    try {
        const response = await fetch(`${API_BASE}/chat/create`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                chatId: chatId,
                user1: currentUsername,
                user2: targetUsername
            })
        });

        const data = await response.json();

        if (response.ok) {
            showFeedback(privateFeedback, "Chat ready! Redirecting... 💬", "success");
            setTimeout(() => {
                window.location.href = `chat.html?chatId=${encodeURIComponent(chatId)}`;
            }, 800);
        } else {
            showFeedback(privateFeedback, data.error || "Could not start the chat. Check the username and try again.", "error");
        }
    } catch (err) {
       
        showFeedback(privateFeedback, "Could not connect to server.", "error");
        console.error("Chat create error:", err);
    }
});

// ── creating group ──

// Add member to the pending group member list
document.getElementById("addMemberToListBtn").addEventListener("click", () => {
    addMemberToList();
});

groupMemberInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") addMemberToList();
});

function addMemberToList() {
    const username = groupMemberInput.value.trim();
    if (!username) return;

    if (groupMembers.includes(username)) {
        groupMemberInput.value = "";
        return;
    }

    groupMembers.push(username);
    groupMemberInput.value = "";
    renderMemberChips();
}

// ── Block user ──
document.getElementById("blockUserBtn").addEventListener("click", async () => {

    const username = document.getElementById("privateChatUsername").value.trim();

    if (!username) {
        showFeedback(privateFeedback, "Enter a username first.", "error");
        return;
    }

    if (username === currentUsername) {
        showFeedback(privateFeedback, "You can't block yourself.", "error");
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/auth/blockUser`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                ownerUsername: currentUsername,
                blockedUsername: username
            })
        });

        const data = await response.json();

        if (data.success) {
            showFeedback(privateFeedback, "User blocked successfully.", "success");
        } else {
            showFeedback(privateFeedback, "Couldn't block this user.", "error");
        }

    } catch (err) {
        console.error(err);
        showFeedback(privateFeedback, "Server error.", "error");
    }
});

function renderMemberChips() {
    memberChips.innerHTML = "";
    groupMembers.forEach(username => {
        const isAdmin = username === currentUsername;
        const chip = document.createElement("div");
        chip.className = "chip";
        chip.innerHTML = `
            <div class="chip-avatar">${username.charAt(0).toUpperCase()}</div>
            <span>${escapeHtml(username)}${isAdmin ? " 👑" : ""}</span>
            ${!isAdmin ? `<button class="chip-remove" data-username="${escapeHtml(username)}">✕</button>` : ""}
        `;
        memberChips.appendChild(chip);
    });

    memberChips.querySelectorAll(".chip-remove").forEach(btn => {
        btn.addEventListener("click", () => removeMember(btn.dataset.username));
    });
}

function removeMember(username) {
    groupMembers = groupMembers.filter(m => m !== username);
    renderMemberChips();
}

// Create the group

document.getElementById("createGroupBtn").addEventListener("click", async () => {
    const groupName = document.getElementById("groupName").value.trim();

    if (!groupName) {
        showFeedback(groupFeedback, "Please enter a group name.", "error");
        return;
    }

    if (groupMembers.length < 2) {
        showFeedback(groupFeedback, "Add at least one other member to create a group.", "error");
        return;
    }

    // Generate unique ids
    const groupId = `group_${Date.now()}`;
    const chatId = `gchat_${Date.now()}`;

    try {
        // Step 1: Create the group
        const groupResponse = await fetch(`${API_BASE}/group/create`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                groupId: groupId,
                chatId: chatId,
                groupName: groupName,
                adminUsername: currentUsername
            })
        });

        const groupData = await groupResponse.json();

        if (!groupResponse.ok || groupData.status !== "group created") {
            showFeedback(groupFeedback, groupData.error || "Failed to create group.", "error");
            return;
        }

        // Step 2: Add each member (skip admin, already added by GroupService)
        const membersToAdd = groupMembers.filter(m => m !== currentUsername);
        let allMembersAdded = true;

        for (const member of membersToAdd) {
            const addResponse = await fetch(`${API_BASE}/group/addMember`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    groupId: groupId,
                    username: member
                })
            });
            if (!addResponse.ok) {
                allMembersAdded = false;
            }
        }

        showFeedback(
            groupFeedback,
            allMembersAdded
                ? `Group "${groupName}" created! Redirecting... 🎀`
                : `Group created, but some members could not be added.`,
            allMembersAdded ? "success" : "error"
        );
        setTimeout(() => {
            window.location.href = `group-chat.html?chatId=${encodeURIComponent(chatId)}`;
        }, 900);

    } catch (err) {
        showFeedback(groupFeedback, "Could not connect to server.", "error");
        console.error("Group create error:", err);
    }
});

// ── contacts tab ──

async function loadContacts() {
    contactList.innerHTML = `<div class="empty-state">Loading...</div>`;

    try {
        // Fetch this user's chats to find existing private chats
        // (contacts = users we already have private chats with)
        const response = await fetch(`${API_BASE}/chat/list?username=${encodeURIComponent(currentUsername)}`);
        const chats = await response.json();

        // Filter private chats involving current user
        const privateChats = chats.filter(c =>
            c.user1Username && c.user2Username &&
            (c.user1Username === currentUsername || c.user2Username === currentUsername) &&
            !c.chatId.startsWith("saved_")
        );

        allContacts = privateChats.map(c => ({
            username: c.user1Username === currentUsername ? c.user2Username : c.user1Username,
            chatId: c.chatId
        }));

        renderContacts(allContacts);

    } catch (err) {
        contactList.innerHTML = `<div class="empty-state">Could not load contacts.<br>Make sure the server is running.</div>`;
        console.error("Failed to load contacts:", err);
    }
}

function renderContacts(contacts) {
    if (contacts.length === 0) {
        contactList.innerHTML = `<div class="empty-state">No contacts yet.<br>Start a new private chat to add someone! 💌</div>`;
        return;
    }

    contactList.innerHTML = "";
    contacts.forEach(contact => {
        const item = document.createElement("a");
        item.href = `chat.html?chatId=${encodeURIComponent(contact.chatId)}`;
        item.className = "contact-item";
        item.innerHTML = `
            <div class="contact-avatar">${contact.username.charAt(0).toUpperCase()}</div>
            <div>
                <div class="contact-name">${escapeHtml(contact.username)}</div>
                <div class="contact-username">@${escapeHtml(contact.username)}</div>
            </div>
        `;
        contactList.appendChild(item);
    });
}

// Contact search filter
contactSearchInput.addEventListener("input", () => {
    const query = contactSearchInput.value.trim().toLowerCase();
    if (!query) {
        renderContacts(allContacts);
        return;
    }
    const filtered = allContacts.filter(c => c.username.toLowerCase().includes(query));
    renderContacts(filtered);
});

// Feedback helper
function showFeedback(el, message, type) {
    el.textContent = message;
    el.className = `feedback ${type}`;
    // Auto-clear after 4 seconds
    setTimeout(() => {
        if (el.textContent === message) {
            el.textContent = "";
            el.className = "feedback";
        }
    }, 4000);
}

// innerHTML unescaped.
function escapeHtml(text) {
    if (!text) return "";
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;");
}

// Init: render current user chip
renderMemberChips();