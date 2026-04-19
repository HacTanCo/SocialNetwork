function openFriendsModal() {

    const userId = document.body.getAttribute("data-profile-id");

    fetch(`/api/friends/${userId}`)
        .then(res => res.json())
        .then(data => {

            const container = document.getElementById("friendsList");
            container.innerHTML = "";

            data.forEach(u => {
                console.log(u);

                let actionBtn = "";

                // 🔥 chính mình → không hiện nút
                if (u.userId == currentUserId) {
					actionBtn = `
						<button class="btn btn-sm btn-warning" disabled>
							ME
						</button>
				    `;
                }
                // 🔥 đã là bạn
                else if (u.isFriend) {
                    actionBtn = `
                        <button class="btn btn-sm btn-secondary">
                            Bạn bè
                        </button>
                    `;
                }
                // 🔥 đã gửi lời mời
                else if (u.pending) {
                    actionBtn = `
                        <button class="btn btn-sm btn-outline-primary"
                                onclick="toggleFollow(this, ${u.userId})">
                            Hủy lời mời
                        </button>
                    `;
                }
                // 🔥 chưa kết bạn
                else {
                    actionBtn = `
                        <button class="btn btn-sm btn-primary"
                                onclick="toggleFollow(this, ${u.userId})">
                            Kết bạn
                        </button>
                    `;
                }

                container.innerHTML += `
                    <div class="d-flex align-items-center justify-content-between border rounded p-2">

                        <div class="d-flex align-items-center gap-2">
                            <img src="${u.avatar || '/assets/images/default-avatar.jpg'}"
                                 class="rounded-circle"
                                 width="40" height="40"
                                 style="cursor:pointer"
                                 onclick="goToProfile(${u.userId})">

                            <div style="cursor:pointer"
                                 onclick="goToProfile(${u.userId})">
                                 ${u.name}
                            </div>
                        </div>

                        <div class="d-flex gap-2">

                            <button class="btn btn-sm btn-outline-primary"
                                    onclick="goToProfile(${u.userId})">
                                Xem
                            </button>

                            ${actionBtn}

                        </div>

                    </div>
                `;
            });

            const modal = new bootstrap.Modal(document.getElementById("friendsModal"));
            modal.show();

        });
}
function goToProfile(userId) {
    window.location.href = `/profile/${userId}`;
}


document.addEventListener('hidden.bs.modal', function (event) {
    document.activeElement.blur();
});