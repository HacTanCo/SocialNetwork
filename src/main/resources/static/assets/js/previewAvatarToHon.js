console.log("Preview Avatar To Hon Loaded");

// xem ảnh đại diện to hơn
document.getElementById("avatarInput").addEventListener("change", function (e) {
    const file = e.target.files[0];

    if (file) {
        const preview = document.getElementById("avatarPreview");
        preview.src = URL.createObjectURL(file);
    }
});