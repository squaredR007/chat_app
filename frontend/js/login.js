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

    try {

        const response = await fetch("http://localhost:8080/api/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                username,
                password
            })
        });

        const data = await response.json();

        if (data.success) {

            localStorage.setItem("username", username);
            localStorage.setItem("userId", data.userId);

            alert("Login successful");

            window.location.href = "../home/home.html";

        } else {

            alert("Wrong username or password");

        }

    } catch (err) {

        alert("Server is unavailable");

    }

});