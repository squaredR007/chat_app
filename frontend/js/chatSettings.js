document.addEventListener("DOMContentLoaded", () => {

    const checkbox = document.getElementById("checkbox-dark-mode");
    const savedTheme = localStorage.getItem("theme");

    if (checkbox) {
    checkbox.checked = savedTheme === "dark";

    checkbox.addEventListener("change", function () {
    const isDark = this.checked;

    localStorage.setItem("theme", isDark ? "dark" : "light");

    window.applyGlobalTheme();
    window.applyGlobalBackground();
    saveTheme(isDark);
    });
}

const options = document.querySelectorAll(".background-option");

    options.forEach(option => {
        const background = option.dataset.background;
        const imagePath = `../image/${background}-light.png`;
        option.style.backgroundImage = `url("${imagePath}")`;

        option.addEventListener("click", () => {
            localStorage.setItem("background", background);
            window.applyGlobalBackground();
            saveBackground(background);
        });
    });
});

async function saveTheme(isDark){

    const userId = localStorage.getItem("userId");
    if(!userId) return;

    try{
        await fetch("http://localhost:8080/api/settings/changeDarkMode",
            {
                method:"POST",
                headers:{"Content-Type":"application/json"},
                body:JSON.stringify({userId, darkmode:isDark})
            }
        );
    }
    catch(err){
        console.error(err);
    }
}

async function saveBackground(background) {
    const userId = localStorage.getItem("userId");
    if (!userId) return;

    try {
        await fetch("http://localhost:8080/api/settings/changeBackground", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ userId, background })
        });
    } catch (err) {
        console.error(err);
    }
}