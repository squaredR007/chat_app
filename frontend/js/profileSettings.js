const imageInput= document.getElementById("input-profile-image");

imageInput.addEventListener("change",function(){
        const file =this.files[0];
        console.log(file);
    }
);

async function changeDisplayName(){

    const userId =localStorage.getItem("userId");
    const newName =document.getElementById("input-displayname").value;

    await fetch("http://localhost:8080/api/settings/changeDisplayName",
        {
            method:"POST",
            body:JSON.stringify({userId,newName})
        }
    );
}

async function changeBiography(){

    const userId =localStorage.getItem("userId");

    const biography =document.getElementById("input-biography").value;

    await fetch("http://localhost:8080/api/settings/changeBiography",
        {
            method:"POST",
            body:JSON.stringify({userId,biography})
        }
    );
}