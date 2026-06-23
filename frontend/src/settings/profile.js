const imageInput= document.getElementById("input-profile-image");

imageInput.addEventListener("change",function(){
        const file =this.files[0];
        console.log(file);
    }
);