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

function confirmDelete(){

    const answer= confirm("Are you sure you want to delete your account?");
    if(answer){
        alert("Account deleted");
    }

}