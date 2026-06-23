document.querySelectorAll(".value-input").forEach(input => {
    input.disabled = true;
});

let editing = false;
function toggleEdit(){
    const inputs= document.querySelectorAll(".value-input");
    editing= !editing;

    inputs.forEach(input => {
        input.disabled = !editing;
    });
}

async function changeUsername(){
    if (!userId){
        window.location.href="../login/login.html";
        return;
    }

    const userId =localStorage.getItem("userId");
    const username =document.getElementById("input-username").value;
    const response =await fetch("http://localhost:8080/api/settings/changeUsername",
            {
                method:"POST",
                body:JSON.stringify({userId, username})
            }
        );

    alert(await response.json().success);
}

async function changeNumber(){

    const userId =localStorage.getItem("userId");
    const number =document.getElementById("input-number").value;
    const response =await fetch("http://localhost:8080/api/settings/changeNumber",
            {
                method:"POST",
                body:JSON.stringify({userId,number})
            }
        );

    alert(await response.json().success);
}

async function deleteAccount(){

    const userId =localStorage.getItem("userId");
    const response =await fetch("http://localhost:8080/api/settings/deleteAccount",
            {
                method:"POST",
                body:JSON.stringify({userId})
            }
        );

        if(await response.json().success){
        alert("Account deleted");

    }
}