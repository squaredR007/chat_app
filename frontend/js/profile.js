document.addEventListener("DOMContentLoaded", async () => {

    // Apply saved theme
    if (localStorage.getItem("theme") === "dark") {
        document.body.classList.add("dark");
    }

    const username = localStorage.getItem("username");
    const userId = localStorage.getItem("userId");

    // Set username fields from localStorage
    const displayNameEl = document.querySelector(".display-name");
    const usernameEl = document.querySelector(".username");

    if (displayNameEl) displayNameEl.textContent = username || "Unknown";
    if (usernameEl) usernameEl.textContent = username ? `@${username}` : "";

    // Load full user info from backend
    if (userId) {
        try {
            const response = await fetch(`http://localhost:8080/api/user/info?userId=${userId}`);
            const user = await response.json();

            // Fill in bio
            const bioEl = document.querySelector(".info-item:nth-child(1) .info-text");
            if (bioEl) bioEl.textContent = user.biography || "No bio yet.";

            // Fill in phone number
            const phoneEl = document.querySelector(".info-item:nth-child(2) .info-text");
            if (phoneEl) phoneEl.textContent = user.number || "—";

            // Fill in display name if available
            if (displayNameEl && user.displayName) {
                displayNameEl.textContent = user.displayName;
            }

            // Fill in profile image if available
            const avatarEl = document.querySelector(".avatar");
            if (avatarEl && user.profileImage) {
                avatarEl.src = user.profileImage;
            }

        } catch (err) {
            console.error("Failed to load user profile:", err);
        }
    }
});