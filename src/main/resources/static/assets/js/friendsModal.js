function openFriendsModal() {

    const userId = document.body.getAttribute("data-profile-id");

    fetch(`/api/friends/${userId}`)
        .then(res => res.json())
        .then(data => {

            const container = document.getElementById("friendsList");
            container.innerHTML = "";

            data.forEach(u => {
				console.log(u)
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

                            ${u.isFriend ? `
                                <button class="btn btn-sm btn-primary"
                                        onclick="openChatFromModal(${u.userId}, '${u.name}')">
                                    Nhắn tin
                                </button>
                            ` : ''}

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

function openChatFromModal(userId, name) {

    // đóng modal friends
    const friendsModal = bootstrap.Modal.getInstance(document.getElementById("friendsModal"));
    if (friendsModal) friendsModal.hide();

    // mở chat
    openChat(userId, name); // bạn đã có sẵn function này rồi
}
document.addEventListener('hidden.bs.modal', function (event) {
    document.activeElement.blur();
});