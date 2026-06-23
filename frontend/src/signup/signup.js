const form = document.querySelector(".main-form");
form.addEventListener("submit", async (e) => {

    e.preventDefault();

    const username= document.getElementById("username").value;
    const password= document.getElementById("password").value;
    const repeatPassword= document.getElementById("repeatPassword").value;
    const number= document.getElementById("number").value;

    if(password !== repeatPassword){
        alert("passwords do not match!");
        return;
    }

    const responce= await fetch ({"http://localhost:8080/api/auth/signup",
    method: "POST",
    body: JSON.stringify({username, password, number})
    }
    );

    if (await responce.json().success)
        alert ("account created")
    else alert ("signup failed")

});