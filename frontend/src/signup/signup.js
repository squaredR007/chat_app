const form = document.querySelector(".main-form");
form.addEventListener("submit", function(e){

    e.preventDefault();

    const password= document.getElementById("password").value;
    const repeatPassword= document.getElementById("repeatPassword").value;

    if(password !== repeatPassword){

        alert("the password is wrong");

        return;
    }

    alert("Welcome");

});