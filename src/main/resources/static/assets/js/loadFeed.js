let page = 0;
const PAGE_SIZE = 10;

function loadMore() {

    page++;

    // Xác định URL tùy theo trang hiện tại (home hoặc profile)
    const profileId = document.body.getAttribute("data-profile-id");
    const url = profileId
        ? `/profile/${profileId}?page=${page}`
        : `/home?page=${page}`;

    const btn = document.getElementById("loadMoreBtn");

    fetch(url)
        .then(res => res.text())
        .then(html => {

            const parser = new DOMParser();
            const doc = parser.parseFromString(html, "text/html");

            const newPosts = doc.querySelectorAll(".post");

            const feed = document.querySelector("#feed");

            newPosts.forEach(post => feed.appendChild(post));

            // Ẩn nút nếu không còn bài viết hoặc ít hơn PAGE_SIZE
            if (newPosts.length < PAGE_SIZE) {
                if (btn) btn.style.display = "none";
            }
        });
}

// Ẩn nút Load More nếu số bài viết ban đầu đã ít hơn PAGE_SIZE
document.addEventListener("DOMContentLoaded", function () {
    const initialPosts = document.querySelectorAll("#feed .post").length;
    const btn = document.getElementById("loadMoreBtn");
    if (btn && initialPosts < PAGE_SIZE) {
        btn.style.display = "none";
    }
});