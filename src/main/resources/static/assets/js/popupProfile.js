console.log("popup profile loaded");

const popup = document.getElementById("profilePopup");

let hoverTimeout;
let hideTimeout;

// 🔥 cache user để tránh gọi API nhiều lần
const userCache = {};

// ===== render popup =====
function renderPopup(user, target) {
    popup.innerHTML = `
        <div class="d-flex gap-2 align-items-center mb-2">
            <img src="${user.avatar}" 
                 class="rounded-circle"
                 style="width:50px;height:50px;object-fit:cover;">
            <div>
                <div class="fw-bold">${user.name}</div>
                <small class="text-muted">${user.postCount} bài viết</small>
            </div>
        </div>

        <div class="small text-muted">
            ${user.bio && user.bio.trim() !== "" ? user.bio : "Chưa có bio"}
        </div>

        <a href="/profile/${user.id}" 
           class="btn btn-sm btn-primary w-100 mt-2">
            Xem trang cá nhân
        </a>
    `;

    popup.classList.remove("d-none");

    const rect = target.getBoundingClientRect();

    popup.style.top = (window.scrollY + rect.bottom + 8) + "px";
    popup.style.left = (window.scrollX + rect.left) + "px";
}

// ===== hover vào avatar =====
document.addEventListener("mouseover", function (e) {

    const target = e.target.closest(".user-avatar");
    if (!target) return;

    const userId = target.dataset.userId;

    clearTimeout(hideTimeout);
    clearTimeout(hoverTimeout);

    // delay nhẹ (giống Facebook)
    hoverTimeout = setTimeout(() => {

        // 🔥 nếu đã cache thì dùng luôn
        if (userCache[userId]) {
            renderPopup(userCache[userId], target);
            return;
        }

        // gọi API
        fetch(`/api/user/${userId}`)
            .then(res => res.json())
            .then(user => {
                userCache[userId] = user;
                renderPopup(user, target);
            });

    }, 200);

});

// ===== rời avatar =====
document.addEventListener("mouseout", function (e) {

    const target = e.target.closest(".user-avatar");
    if (!target) return;

    clearTimeout(hoverTimeout);

    hideTimeout = setTimeout(() => {
        popup.classList.add("d-none");
    }, 200);

});

// ===== hover vào popup (không bị tắt) =====
popup.addEventListener("mouseenter", () => {
    clearTimeout(hideTimeout);
});

// ===== rời popup =====
popup.addEventListener("mouseleave", () => {
    popup.classList.add("d-none");
});