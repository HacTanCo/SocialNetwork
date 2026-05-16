console.log("Notification script loaded");

function loadNotifications() {
    fetch("/notifications")
        .then(res => res.json())
        .then(data => {
            const container = document.getElementById("notificationList");
            container.innerHTML = "";

            data.forEach(n => {
                console.log("Loading notification:", n);
                const item = document.createElement("div");
                const avatar = n.senderAvatar || "/images/default-avatar.png";

                item.className = `d-flex align-items-center p-3 border-bottom notification-item ${!n.read ? 'unread' : ''}`;
                item.style.cursor = "pointer";

                item.innerHTML = `
                    <div class="position-relative me-3">
                        <img src="${avatar}"
                             style="width:48px; height:48px; border-radius:50%; object-fit:cover;">
                    </div>

                    <div style="flex:1;">
                        <div class="${!n.read ? 'fw-bold' : ''}" style="font-size:15px; line-height:1.45;">
                            ${n.content}
                        </div>
                        <div style="font-size:13px; color:#8e8e8e; margin-top:2px;">
                            ${new Date(n.createdAt).toLocaleString('vi-VN')}
                        </div>
                    </div>

                    <!-- Chấm đỏ ở góc phải ngoài cùng -->
                    ${!n.read ? `
                        <div class="unread-dot"></div>
                    ` : ''}
                `;

                item.onclick = () => handleNotificationClick(n);
                container.appendChild(item);
            });
        })
        .catch(err => console.error("Lỗi load thông báo:", err));
}
function handleNotificationClick(n) {
    // Đánh dấu đã đọc
    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;

    fetch(`/notifications/read/${n.id}`, {
        method: "POST",
        headers: { [header]: token }
    });
    if (n.type === "FRIEND_REQUEST" || n.type === "FRIEND_ACCEPT") {
        window.location.href = "/friend";
        return;
    }
    if (n.postId) {
        if (n.type === "LIKE") {
            goToPost(n.postId);
        }

        if (n.type === "COMMENT" || n.type === "REPLY") {
            // Có thể mở comment modal sau khi scroll
            setTimeout(() => openCommentModal(n.postId), 800);
        }

    }

    // Đóng modal thông báo
    const modal = bootstrap.Modal.getInstance(document.getElementById("notificationModal"));
    modal.hide();
    // Cập nhật lại số badge sau khi đọc
    setTimeout(() => {
        loadUnreadCount();
    }, 300);
}
function goToPost(postId) {
    let post = document.querySelector(`[data-post-id="${postId}"]`);

    if (post) {
        post.scrollIntoView({ behavior: "smooth", block: "center" });
        highlightPost(post);
    } else {
        // Post chưa load → chuyển trang và truyền param
        window.location.href = `/?postId=${postId}`;
    }
}

function highlightPost(post) {
    post.style.transition = "box-shadow 0.3s";
    post.style.boxShadow = `
	    0 0 0 4px rgba(245, 96, 64, 0.25), 
	    0 0 0 8px rgba(245, 96, 64, 0.15),
	    0 0 0 12px rgba(245, 96, 64, 0.08)
	`;
    setTimeout(() => {
        post.style.boxShadow = "none";
    }, 2000);
}
// mở modal thì load
document.getElementById("notificationModal")
    .addEventListener("show.bs.modal", function() {
        loadNotifications();
        loadUnreadCount();
    });

function loadUnreadCount() {
    fetch("/notifications/unread-count")
        .then(res => res.json())
        .then(count => {
            console.log("Unread notification count:", count);
            const badge = document.getElementById("notification-unread-badge");

            if (count > 0) {
                badge.style.display = "inline-block";
                badge.innerText = count;
            } else {
                badge.style.display = "none";
                badge.innerText = "0";
            }
        });
}
// Hàm lấy tổng số tin nhắn chưa đọc
function loadUnreadMessageCount() {
    fetch("/messages/unread-total")
        .then(res => res.json())
        .then(count => {

            const badge = document.getElementById("message-unread-badge");

            if (count > 0) {
                badge.style.display = "flex";
                badge.textContent = count > 99 ? "99+" : count;
            } else {
                badge.style.display = "none";
            }
        })
        .catch(err => console.error("Lỗi load unread message count:", err));
}

// --- WebSocket Realtime Notification ---
let notificationStompClient = null;

function connectNotificationWS() {
    // currentUserId được set ở cuối body trong thẻ script inline
    if (typeof window.currentUserId === 'undefined' || !window.currentUserId) {
        console.warn("currentUserId not found, skipping notification WS connection.");
        return;
    }
    
    const socket = new SockJS('/ws');
    notificationStompClient = Stomp.over(socket);
    
    notificationStompClient.debug = null; // Tắt log ping pong liên tục

    notificationStompClient.connect({}, function(frame) {
        console.log('Connected to Notification WS: ' + frame);
        notificationStompClient.subscribe('/topic/notifications/' + window.currentUserId, function(message) {
            const notification = JSON.parse(message.body);
            handleNewNotificationRealtime(notification);
        });
    }, function(error) {
        console.error("Notification WS Error: ", error);
        setTimeout(connectNotificationWS, 5000);
    });
}

function handleNewNotificationRealtime(n) {
    console.log("New realtime notification received:", n);
    
    // Tăng số đếm badge
    const badge = document.getElementById("notification-unread-badge");
    if(badge) {
        let text = badge.innerText ? badge.innerText.trim() : "";
        let count = parseInt(text || "0");
        count++;
        badge.style.display = "inline-block";
        badge.innerText = count;
    }

    // Nếu modal đang mở, thêm ngay lên đầu danh sách
    const modalEl = document.getElementById('notificationModal');
    if (modalEl && modalEl.classList.contains('show')) {
        const container = document.getElementById("notificationList");
        if(container) {
            const item = document.createElement("div");
            const avatar = n.senderAvatar || "/images/default-avatar.png";

            item.className = `d-flex align-items-center p-3 border-bottom notification-item unread`;
            item.style.cursor = "pointer";

            item.innerHTML = `
                <div class="position-relative me-3">
                    <img src="${avatar}" style="width:48px; height:48px; border-radius:50%; object-fit:cover;">
                </div>
                <div style="flex:1;">
                    <div class="fw-bold" style="font-size:15px; line-height:1.45;">
                        ${n.content}
                    </div>
                    <div style="font-size:13px; color:#8e8e8e; margin-top:2px;">
                        ${new Date(n.createdAt).toLocaleString('vi-VN')}
                    </div>
                </div>
                <div class="unread-dot"></div>
            `;

            item.onclick = () => handleNotificationClick(n);
            container.insertBefore(item, container.firstChild);
        }
    }
}

window.addEventListener("load", connectNotificationWS);