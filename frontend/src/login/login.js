const form = document.querySelector(".main-form");
form.addEventListener("submit", function(e){

    e.preventDefault();

    const username= document.getElementById("username").value;
    const password= document.getElementById("password").value;

    console.log(username);
    console.log(password);

});