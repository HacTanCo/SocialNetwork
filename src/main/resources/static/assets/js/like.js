console.log("like")
const token = document.querySelector('meta[name="_csrf"]').content;
const header = document.querySelector('meta[name="_csrf_header"]').content;
const likePost = async (icon, postId) => {
    try {
        const res = await fetch(`/post/like/${postId}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
				[header]: token
            }
        });

        if (!res.ok) {
            throw new Error("Something went wrong");
        }

        const data = await res.json();

        // 🎯 toggle icon
        if (data.liked) {
            icon.classList.add("bi-heart-fill", "text-danger");
            icon.classList.remove("bi-heart");
        } else {
            icon.classList.remove("bi-heart-fill", "text-danger");
            icon.classList.add("bi-heart");
        }

        // 🎯 update số like
        document.getElementById(`like-count-${postId}`).innerText = data.likeCount;

    } catch (e) {
        console.error(e);
    }
};