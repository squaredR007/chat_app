document.addEventListener("DOMContentLoaded", async () => {
    await loadProfileSettings();

    const imageInput = document.getElementById("input-profile-image");
    imageInput.addEventListener("change", handleImageSelected);
});

let editing = false;
let pendingProfileImage = null;

async function loadProfileSettings() {
    const username = localStorage.getItem("username");
    if (!username) {
        alert("Please login again.");
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/api/user/info?username=${username}`);
        const user = await response.json();

        document.getElementById("text-displayname").textContent = user.displayName || user.username;
        document.getElementById("text-biography").textContent = user.biography || "";
        document.getElementById("input-displayname").value = user.displayName || user.username;
        document.getElementById("input-biography").value = user.biography || "";

        const preview = document.getElementById("preview-profile-image");
        preview.src = user.profileImage || "";
        preview.style.visibility = user.profileImage ? "visible" : "hidden";

    } catch (err) {
        console.error("Failed loading profile:", err);
    }
}

function handleImageSelected(event) {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
        pendingProfileImage = reader.result;
        const preview = document.getElementById("preview-profile-image");
        preview.src = pendingProfileImage;
        preview.style.visibility = "visible";
        document.getElementById("text-profile-image").textContent = file.name;
    };
    reader.readAsDataURL(file);
}

function setEditingUI(isEditing) {
    const displayNameText = document.getElementById("text-displayname");
    const biographyText = document.getElementById("text-biography");
    const displayNameInput = document.getElementById("input-displayname");
    const biographyInput = document.getElementById("input-biography");
    const imageInput = document.getElementById("input-profile-image");

    displayNameText.style.display = isEditing ? "none" : "inline";
    biographyText.style.display = isEditing ? "none" : "inline";
    displayNameInput.style.display = isEditing ? "inline" : "none";
    biographyInput.style.display = isEditing ? "inline" : "none";
    imageInput.style.display = isEditing ? "inline" : "none";
}

async function toggleEdit() {
    const button = document.getElementById("main-btn");

    if (!editing) {
        setEditingUI(true);
        button.textContent = "Save";
        editing = true;
        return;
    }

    const nameResult = await changeDisplayName();
    const bioResult = await changeBiography();
    const imageResult = await changeProfileImage();

    if (nameResult && bioResult && imageResult) {
        pendingProfileImage = null;
        document.getElementById("text-profile-image").textContent = "";
        await loadProfileSettings();
        alert("Profile updated successfully");
    } else {
        alert("Failed to update profile");
    }

    setEditingUI(false);
    button.textContent = "edit";
    editing = false;
}

async function changeDisplayName() {
    const username = localStorage.getItem("username");
    const newName = document.getElementById("input-displayname").value;

    try {
        const response = await fetch("http://localhost:8080/api/settings/changeDisplayName", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({username, newName})
        });

        const data = await response.json();
        return data.success;

    } catch (err) {
        console.error(err);
        return false;
    }
}

async function changeBiography() {
    const username = localStorage.getItem("username");
    const biography = document.getElementById("input-biography").value;

    try {
        const response = await fetch("http://localhost:8080/api/settings/changeBiography", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({username, biography})
        });

        const data = await response.json();
        return data.success;

    } catch (err) {
        console.error(err);
        return false;
    }
}

async function changeProfileImage() {

    if (!pendingProfileImage) return true;

    const username = localStorage.getItem("username");

    try {
        const response = await fetch("http://localhost:8080/api/settings/changeProfileImage", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({username, profileImage: pendingProfileImage})
        });

        const data = await response.json();
        return data.success;

    } catch (err) {
        console.error(err);
        return false;
    }
}