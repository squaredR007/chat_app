document.addEventListener("DOMContentLoaded", () => {

    if (localStorage.getItem("theme") === "dark") {
        document.body.classList.add("dark");
    }

    document.querySelectorAll(".value-input").forEach(input => {
        input.disabled = true;
    });

    loadUserInfo();
});

let editing = false;

async function toggleEdit() {
    const button = document.getElementById("main-btn");
    editing = !editing;

    document.querySelectorAll(".value-input").forEach(input => {input.disabled = !editing;});

    if (editing) {
        button.textContent = "Save";
        document.getElementById("input-password").focus();
        return;
    }

    button.textContent = "edit";
        await changeUsername();
        await changeNumber();
        await changePassword();
        document.getElementById("input-password").value = "";
}

async function changeUsername() {

    const userId = localStorage.getItem("userId");

    if (!userId) {
        window.location.href = "../pages/login.html";
        return;
    }

    const username = document.getElementById("input-username").value;

    try {

        const response = await fetch("http://localhost:8080/api/settings/changeUsername", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                userId,
                username
            })
        });

        const data = await response.json();

        alert(data.success ? "Username updated" : "Failed to update username");
        if(data.success){
            document.getElementById("text-username").innerText = username;
        }

    } catch (err) {

        alert("Server error");

    }

}

async function changeNumber() {

    const userId = localStorage.getItem("userId");
    const number = document.getElementById("input-number").value;

    try {

        const response = await fetch("http://localhost:8080/api/settings/changeNumber", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                userId,
                number
            })
        });

        const data = await response.json();

        alert(data.success ? "Number updated" : "Failed to update number");
        if(data.success){
            document.getElementById("text-number").innerText = number;
        }

    } catch (err) {

        alert("Server error");

    }

}

async function deleteAccount() {

    const userId = localStorage.getItem("userId");

    try {

        const response = await fetch("http://localhost:8080/api/settings/deleteAccount", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                userId
            })
        });

        const data = await response.json();

        if (data.success) {

            localStorage.clear();

            alert("Account deleted");

            window.location.href = "../pages/login.html";

        } else {

            alert("Failed to delete account");

        }

    } catch (err) {

        alert("Server error");

    }

}

async function loadUserInfo() {

    const userId = localStorage.getItem("userId");

    if (!userId) {
        window.location.href = "../pages/login.html";
        return;
    }

    try {

        const response = await fetch(`http://localhost:8080/api/user/info?userId=${userId}`);

        const data = await response.json();

        document.getElementById("input-username").value = data.username;
        document.getElementById("input-number").value = data.number;

        document.getElementById("text-username").innerText = data.username;
        document.getElementById("text-number").innerText = data.number;
        document.getElementById("text-password").innerText = "••••••••";
        document.getElementById("input-password").placeholder = "Enter new password";

    } catch (err) {

        alert("Cannot load user info");

    }

}

async function changePassword() {

    const userId = localStorage.getItem("userId");
    const password = document.getElementById("input-password").value;

    if (!password) {
        return;
    }

    try {

        const response = await fetch("http://localhost:8080/api/settings/changePassword", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                userId,
                password
            })
        });

        const data = await response.json();

        alert(data.success ? "Password updated" : "Failed to update password");

    } catch (err) {

        alert("Server error");

    }

}