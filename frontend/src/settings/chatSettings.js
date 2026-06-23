const checkbox =document.getElementById("checkbox-dark-mode");

checkbox.addEventListener("change",function(){
        if(this.checked){
        document.body.classList.add("dark");
        }else{
            document.body.classList.remove("dark");
        }
    }
);