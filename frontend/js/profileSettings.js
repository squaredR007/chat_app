document.addEventListener("DOMContentLoaded",async () => {

    if (localStorage.getItem("theme") === "dark") {
        document.body.classList.add("dark");
    }
await loadProfileSettings();
});

let editing = false;

async function loadProfileSettings() {
    const userId = localStorage.getItem("userId");
    if (!userId) {
        alert("Please login again.");
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/api/user/info?userId=${userId}`);
        const user = await response.json();

        document.getElementById("text-displayname").textContent =user.displayName || user.username;
        document.getElementById("text-biography").textContent =user.biography || "";
        document.getElementById("input-displayname").value =user.displayName || user.username;
        document.getElementById("input-biography").value =user.biography || "";

    } catch (err) {
        console.error("Failed loading profile:", err);
    }
}

async function toggleEdit() {
    const button = document.getElementById("main-btn");
    const displayNameInput = document.getElementById("input-displayname");
    const biographyInput = document.getElementById("input-biography");

    if (!editing) {
        displayNameInput.style.display = "inline";
        biographyInput.style.display = "inline";
        button.textContent = "Save";
        editing = true;

    } else {
        const nameResult = await changeDisplayName();
        const bioResult = await changeBiography();

        if (nameResult && bioResult) {
        await loadProfileSettings();
            alert("Profile updated successfully");
        } else {
            alert("Failed to update profile");
        }
            button.textContent = "Edit";
            editing = false;
    }
}

async function changeDisplayName() {
    const userId = localStorage.getItem("userId");
    const newName = document.getElementById("input-displayname").value;

    try {
        const response = await fetch("http://localhost:8080/api/settings/changeDisplayName", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({userId, newName})
        });

        const data = await response.json();
        return data.success;

    } catch (err) {
        console.error(err);
        return false;
    }
}

async function changeBiography() {
    const userId = localStorage.getItem("userId");
    const biography = document.getElementById("input-biography").value;

    try {
        const response = await fetch("http://localhost:8080/api/settings/changeBiography", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({userId,biography})
        });

        const data = await response.json();
        return data.success;

    } catch (err) {
        console.error(err);
        return false;
    }
}