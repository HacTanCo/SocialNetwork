console.log("sua, xoa post")
function editPost(btn) {

    const post = btn.closest(".post");

    const postId = post.dataset.postId;

    const content = post.querySelector(".post-content").innerText;

    document.getElementById("editPostId").value = postId;

    document.getElementById("editPostContent").value = content;

    const modal = new bootstrap.Modal(
        document.getElementById("editPostModal")
    );

    modal.show();
}

function submitEditPost() {

    const postId = document.getElementById("editPostId").value;

    const content = document.getElementById("editPostContent").value;

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch("/post/update/" + postId, {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            [csrfHeader]: csrfToken
        },
        body: "content=" + encodeURIComponent(content)
    })
    .then(res => {
        if (res.ok) {
            location.reload();
        }
    });
}
function deletePost(btn) {

    const post = btn.closest(".post");
    const postId = post.dataset.postId;

    if (!confirm("Bạn chắc chắn muốn xóa bài viết?")) return;

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    fetch(`/post/delete/${postId}`, {
        method: "POST",
        headers: {
            [csrfHeader]: csrfToken
        }
    })
    .then(() => {

        // xóa post khỏi UI ngay
        post.style.transition = "opacity 0.3s";
        post.style.opacity = "0";

        setTimeout(() => {
            post.remove();
        }, 300);

    })
    .catch(() => {
        alert("Xóa bài viết thất bại");
    });

}

