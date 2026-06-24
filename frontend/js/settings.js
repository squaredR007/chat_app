document.addEventListener("DOMContentLoaded", () => {

    if (localStorage.getItem("theme") === "dark") {
        document.body.classList.add("dark");
    }

});

function logout() {

    localStorage.clear();

    window.location.href = "../login/login.html";

}