(function () {

    function applyGlobalTheme() {
        if (localStorage.getItem("theme") === "dark") {
            document.body.classList.add("dark");
        } else {
            document.body.classList.remove("dark");
        }
    }

    function applyGlobalBackground() {
        const backgroundName = localStorage.getItem("background");

        if (!backgroundName) return;

        const mode = localStorage.getItem("theme") === "dark" ? "dark" : "light";

        document.body.style.backgroundImage = `url("../image/${backgroundName}-${mode}.png")`;
        document.body.style.backgroundSize = "cover";
        document.body.style.backgroundPosition = "center";
        document.body.style.backgroundRepeat = "no-repeat";
        document.body.style.backgroundAttachment = "fixed";
    }

    document.addEventListener("DOMContentLoaded", () => {
        applyGlobalTheme();
        applyGlobalBackground();
    });


    window.applyGlobalTheme = applyGlobalTheme;
    window.applyGlobalBackground = applyGlobalBackground;
})();
