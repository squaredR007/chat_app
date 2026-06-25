document.addEventListener("DOMContentLoaded", () => {

    if (localStorage.getItem("theme") === "dark") {
        document.body.classList.add("dark");
    }

});

const form = document.querySelector(".main-form");

form?.addEventListener("submit", async (e) => {

    e.preventDefault();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const repeatPassword = document.getElementById("repeatPassword").value;
    const number = document.getElementById("number").value;

    if (password !== repeatPassword) {

        alert("Passwords do not match!");

        return;

    }

    try {

        const response = await fetch("http://localhost:7600/api/auth/signup", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                username,
                password,
                number
            })
        });

        const data = await response.json();

        if (data.success) {

            alert("Account created");

            window.location.href = "../pages/home.html";

        } else {

            alert("Signup failed");

        }

    } catch (err) {

        alert("Server is unavailable");

    }

});