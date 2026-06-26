// at the top of settings.js
const username = localStorage.getItem("username");
const userId = localStorage.getItem("userId");
const displayName = localStorage.getItem("displayName");

// then set the fields
document.querySelector(".display-name").textContent = displayName || username || "Unknown";
document.addEventListener("DOMContentLoaded", () => {

    if (localStorage.getItem("theme") === "dark") {
        document.body.classList.add("dark");
    }

});

function logout() {
    // clear ALL user data from localStorage
    localStorage.removeItem("username");
    localStorage.removeItem("userId");
    localStorage.removeItem("displayName");

    // redirect to login
    window.location.href = "login.html";
}