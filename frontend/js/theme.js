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

        if (!backgroundName) {
            document.body.style.backgroundImage = "";
            return;
        }

        const mode =
            localStorage.getItem("theme") === "dark"
                ? "dark"
                : "light";

        const imagePath = `../image/${backgroundName}-${mode}.png`;

        document.body.style.backgroundImage = `url("${imagePath}")`;
        document.body.style.backgroundSize = "cover";
        document.body.style.backgroundPosition = "center";
        document.body.style.backgroundRepeat = "no-repeat";
        document.body.style.backgroundAttachment = "fixed";
    }

    window.applyGlobalTheme = applyGlobalTheme;
    window.applyGlobalBackground = applyGlobalBackground;

    function refreshTheme() {
        applyGlobalTheme();
        applyGlobalBackground();
    }

    document.addEventListener("DOMContentLoaded", refreshTheme);

    window.addEventListener("pageshow", refreshTheme);

})();