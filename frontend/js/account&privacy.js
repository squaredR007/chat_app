document.addEventListener("DOMContentLoaded", () => {

    if (localStorage.getItem("theme") === "dark") {
        document.body.classList.add("dark");
    }

    document.querySelectorAll(".value-input").forEach(input => {
        input.disabled = true;
    });

});

let editing = false;

function toggleEdit() {

    editing = !editing;

    document.querySelectorAll(".value-input").forEach(input => {
        input.disabled = !editing;
    });

}

async function changeUsername() {

    const userId = localStorage.getItem("userId");

    if (!userId) {
        window.location.href = "../login/login.html";
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

            window.location.href = "../login/login.html";

        } else {

            alert("Failed to delete account");

        }

    } catch (err) {

        alert("Server error");

    }

}