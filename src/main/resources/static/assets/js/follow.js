console.log("follow ");
async function toggleFollow(btn, userId) {
    try {
        btn.disabled = true;

        // 👉 lấy CSRF
        const token = document.querySelector('meta[name="_csrf"]').content;
        const header = document.querySelector('meta[name="_csrf_header"]').content;

        const res = await fetch(`/friend/toggle`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                [header]: token   // 🔥 QUAN TRỌNG
            },
            body: `friend_id=${userId}`
        });

        if (!res.ok) return;

        const result = await res.text();

		if (result === "followed") {
		    btn.innerText = "Hủy lời mời";
		} else if (result === "unfollowed") {
		    btn.innerText = "Kết bạn";
		}

    } catch (e) {
        console.error(e);
    } finally {
        btn.disabled = false;
    }
}
