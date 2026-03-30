console.log("preview image")
/*function previewImage(input) {
    const preview = document.getElementById("imagePreview");

    if (input.files && input.files[0]) {
        const reader = new FileReader();

        reader.onload = function (e) {
            preview.src = e.target.result;
            preview.style.display = "block";
        };

        reader.readAsDataURL(input.files[0]);
    } else {
        preview.src = "";
        preview.style.display = "none";
    }
}*/
let selectedFiles = [];

function previewMedia(input){

    selectedFiles = Array.from(input.files);

    renderPreview();
}

function renderPreview(){

    const preview = document.getElementById("mediaPreview");
    const carouselInner = document.getElementById("carouselInner");
    const counter = document.getElementById("mediaCounter");

    carouselInner.innerHTML = "";

    if(selectedFiles.length === 0){
        preview.style.display = "none";
        return;
    }

    preview.style.display = "block";

    selectedFiles.forEach((file, index)=>{

        const url = URL.createObjectURL(file);

        const item = document.createElement("div");
        item.classList.add("carousel-item");

        if(index === 0) item.classList.add("active");

        const wrapper = document.createElement("div");
        wrapper.className = "media-wrapper";

        // tạo media
        if(file.type.startsWith("image")){
            const img = document.createElement("img");
            img.src = url;
            img.className = "img-fluid";
            wrapper.appendChild(img);
        }

        if(file.type.startsWith("video")){
            const video = document.createElement("video");
            video.src = url;
            video.controls = true;
            video.className = "img-fluid";
            wrapper.appendChild(video);
        }

        // nút xoá
        const removeBtn = document.createElement("button");
        removeBtn.innerHTML = "✖";
        removeBtn.className = "remove-media";

        removeBtn.onclick = function(){
            selectedFiles.splice(index,1);
            updateInputFiles();
            renderPreview();
        }

        wrapper.appendChild(removeBtn);

        item.appendChild(wrapper);
        carouselInner.appendChild(item);
    })

    // cập nhật counter ban đầu
    counter.innerText = `1 / ${selectedFiles.length}`;
}

function updateInputFiles(){

    const input = document.querySelector('input[name="files"]');

    const dataTransfer = new DataTransfer();

    selectedFiles.forEach(file=>{
        dataTransfer.items.add(file);
    });

    input.files = dataTransfer.files;
}


/* cập nhật counter khi slide */
document.addEventListener("DOMContentLoaded", function(){

    const carousel = document.getElementById("mediaPreview");

    carousel.addEventListener("slid.bs.carousel", function (event) {

        const counter = document.getElementById("mediaCounter");
        const total = selectedFiles.length;

        counter.innerText = `${event.to + 1} / ${total}`;

    });

});

document.getElementById("avatarInput").addEventListener("change", function (e) {
    const file = e.target.files[0];

    if (file) {
        const preview = document.getElementById("avatarPreview");
        preview.src = URL.createObjectURL(file);
    }
});