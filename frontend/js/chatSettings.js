document.addEventListener("DOMContentLoaded", () => {


    const checkbox = document.getElementById("checkbox-dark-mode");


    const savedTheme = localStorage.getItem("theme");



    if(savedTheme === "dark"){


        document.body.classList.add("dark");


        if(checkbox){

            checkbox.checked = true;

        }

    }




    if(checkbox){


        checkbox.addEventListener("change", function(){


            const isDark = this.checked;



            document.body.classList.toggle(
                "dark",
                isDark
            );



            localStorage.setItem(
                "theme",
                isDark ? "dark" : "light"
            );



            saveTheme(isDark);



            const savedBackground = localStorage.getItem("background");



            if(savedBackground){

                applyBackground(savedBackground);

            }



        });



    }





    const options = document.querySelectorAll(".background-option");



    options.forEach(option => {

        const background = option.dataset.background;

        console.log("background name:", background);

        const imagePath = `../image/${background}-light.png`;

        console.log("image path:", imagePath);

        option.style.backgroundImage = `url("${imagePath}")`;

        option.addEventListener("click", () => {

            localStorage.setItem("background", background);

            applyBackground(background);

            saveBackground(background);

        });

    });





    const savedBg = localStorage.getItem("background");



    if(savedBg){

        applyBackground(savedBg);

    }



});







function applyBackground(backgroundName){



    const theme = localStorage.getItem("theme");



    const mode = theme === "dark"
        ? "dark"
        : "light";




    document.body.style.backgroundImage =
        `url("../image/${backgroundName}-${mode}.png")`;



    document.body.style.backgroundSize =
        "cover";



    document.body.style.backgroundPosition =
        "center";



}








async function saveTheme(isDark){



    const userId = localStorage.getItem("userId");



    if(!userId) return;




    try{



        await fetch(
            "http://localhost:8080/api/settings/changeDarkMode",
            {

                method:"POST",

                headers:{
                    "Content-Type":"application/json"
                },


                body:JSON.stringify({

                    userId,

                    darkmode:isDark

                })


            }
        );



    }

    catch(err){

        console.error(err);

    }


}








async function saveBackground(background){



    const userId = localStorage.getItem("userId");



    if(!userId) return;




    try{



        await fetch(
            "http://localhost:8080/api/settings/changeBackground",
            {

                method:"POST",

                headers:{
                    "Content-Type":"application/json"
                },


                body:JSON.stringify({

                    userId,

                    background

                })

            }
        );

    }
    catch(err){
        console.error(err);
    }

}