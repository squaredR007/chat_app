document.addEventListener("DOMContentLoaded", () => {

    if (localStorage.getItem("theme") === "dark") {
        document.body.classList.add("dark");
    }

    const displayNameEl = document.querySelector(".display-name");
    const usernameEl = document.querySelector(".username");

    const username = localStorage.getItem("username");

    if (displayNameEl) {
        displayNameEl.textContent = username || "Unknown";
    }

    if (usernameEl) {
        usernameEl.textContent = username ? `@${username}` : "";
    }

});