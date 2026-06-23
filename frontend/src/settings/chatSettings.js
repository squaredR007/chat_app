const checkbox =document.getElementById("checkbox-dark-mode");

checkbox.addEventListener("change",function(){
        if(this.checked){
        document.body.classList.add("dark");
        }else{
            document.body.classList.remove("dark");
        }
    }
);

async function saveTheme(){

    const userId =localStorage.getItem("userId");
    const darkmode =document.getElementById("checkbox-dark-mode").checked;

    const response =await fetch("http://localhost:8080/api/settings/changeDarkMode",
            {
                method:"POST",
                body:JSON.stringify({userId,darkmode})
            }
        );

    console.log(await response.json());
}

document.body.classList.toggle("dark", darkmode);