const form = document.querySelector(".main-form");
form.addEventListener("submit", async (e) => {

    e.preventDefault();

    const username= document.getElementById("username").value;
    const password= document.getElementById("password").value;
    try {
    const respons= await fetch("http://localhost:8080/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password })
    });

    if (await respons.json().success)
        alret ("login successful");
    else alret ("wrong username or password");
    } catch(eror){
    alret("server  is unavailable")
    }

});