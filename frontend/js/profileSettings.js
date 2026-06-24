document.addEventListener("DOMContentLoaded", () => {

    if (localStorage.getItem("theme") === "dark") {
        document.body.classList.add("dark");
    }

});

const imageInput = document.getElementById("input-profile-image");

imageInput?.addEventListener("change", function () {

    const file = this.files[0];

    console.log(file);

});

async function changeDisplayName() {

    const userId = localStorage.getItem("userId");

    const newName = document.getElementById("input-displayname").value;

    try {

        await fetch("http://localhost:8080/api/settings/changeDisplayName", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                userId,
                newName
            })
        });

    } catch (err) {

        console.error(err);

    }

}

async function changeBiography() {

    const userId = localStorage.getItem("userId");

    const biography = document.getElementById("input-biography").value;

    try {

        await fetch("http://localhost:8080/api/settings/changeBiography", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                userId,
                biography
            })
        });

    } catch (err) {

        console.error(err);

    }

}