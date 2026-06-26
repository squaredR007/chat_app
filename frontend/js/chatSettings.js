document.addEventListener("DOMContentLoaded", () => {

    const checkbox = document.getElementById("checkbox-dark-mode");

    const savedTheme = localStorage.getItem("theme");

    if (savedTheme === "dark") {

        document.body.classList.add("dark");

        if (checkbox) {
            checkbox.checked = true;
        }

    }

    if (checkbox) {

        checkbox.addEventListener("change", function () {

            const isDark = this.checked;

            document.body.classList.toggle("dark", isDark);

            localStorage.setItem(
                "theme",
                isDark ? "dark" : "light"
            );

            saveTheme(isDark);

        });

    }

});

async function saveTheme(isDark) {

    const userId = localStorage.getItem("userId");

    if (!userId) return;

    try {

        const response = await fetch("http://localhost:8080/api/settings/changeDarkMode", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                userId,
                darkmode: isDark
            })
        });

        console.log(await response.json());

    } catch (err) {

        console.error(err);

    }

}