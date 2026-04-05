console.log("Chat");
let stompClient = null;

// connect websocket
/*function connectWS() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {

        // subscribe nhận tin nhắn
        stompClient.subscribe('/topic/chat/' + currentUserId, function (msg) {
            const message = JSON.parse(msg.body);
            renderMessage(message);
            // Không gọi scrollBottom() ở đây nữa
        });

    });
}*/
function connectWS() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function() {

        stompClient.subscribe('/topic/chat/' + currentUserId, function(msg) {

            const message = JSON.parse(msg.body);

            const friendId = message.senderId == currentUserId
                ? message.receiverId
                : message.senderId;

            // 🔥 auto mở chat box nếu chưa mở
            if (!openChats[friendId]) {
                openChatBox({
                    getAttribute: (attr) => {
                        if (attr === "data-id") return friendId;
                        if (attr === "data-name") return message.senderName || "User";
                    }
                });
            }

            // 🔥 render mini chat
            renderMiniMessage(message, friendId);
        });

    });
}

connectWS();
function handleOpenChat(btn) {
    const id = btn.getAttribute("data-id");
    const name = btn.getAttribute("data-name");

    openChat(id, name);
}
// mở chat
function openChat(friendId, friendName) {
    document.getElementById('chatFriendId').value = friendId;
    document.getElementById('chatFriendName').innerText = friendName;

    loadChat(friendId);
}
// bind sau khi load
document.addEventListener("DOMContentLoaded", function() {
    const input = document.getElementById("chatInput");

    input.addEventListener("keydown", function(e) {
        if (e.key === "Enter") {
            e.preventDefault();
            sendMessage();
        }
    });
});
// load lịch sử
function loadChat(friendId) {
    fetch('/chat/' + friendId)
        .then(res => res.json())
        .then(data => {
            const box = document.getElementById('chatMessages');
            box.innerHTML = '';

            data.forEach(renderMessage);

            // Scroll xuống cuối khi load lịch sử
            setTimeout(() => scrollToBottom(true), 50);
        })
        .catch(err => console.error("Load chat error:", err));
}

// gửi tin nhắn
function sendMessage() {
    const input = document.getElementById('chatInput');
    const content = input.value.trim();
    if (!content) return;

    const friendId = document.getElementById('chatFriendId').value;

    stompClient.send("/app/chat.send", {}, JSON.stringify({
        content: content,
        senderId: currentUserId,
        receiverId: friendId
    }));

    input.value = '';

    // Không cần setTimeout ở đây nữa vì renderMessage sẽ xử lý
}

// render tin nhắn
function renderMessage(m) {
    const box = document.getElementById('chatMessages');
    if (!box) return;

    const isMe = m.senderId == currentUserId;

    const div = document.createElement('div');
    div.className = "d-flex " + (isMe ? "justify-content-end" : "justify-content-start");

    div.innerHTML = `
        <div class="d-flex ${isMe ? 'flex-row-reverse' : ''} align-items-end gap-2">
            <img src="${m.senderAvatar || '/default-avatar.png'}" 
                 class="rounded-circle" width="35" height="35" 
                 onerror="this.src='/default-avatar.png'">
            <div class="${isMe ? 'bg-primary text-white' : 'bg-white border'} px-3 py-2 rounded-3">
                ${m.content}
            </div>
        </div>
    `;

    box.appendChild(div);

    // Luôn scroll xuống khi là tin nhắn của mình
    // Khi nhận tin thì chỉ scroll nếu đang ở gần cuối
    scrollToBottom(isMe);
}

function scrollToBottom(force = false) {
    const box = document.getElementById('chatMessages');
    if (!box) return;

    // Force = true → luôn scroll (dùng khi gửi tin nhắn của mình)
    // Force = false → chỉ scroll nếu đang ở gần cuối (giống Facebook)
    const isNearBottom = box.scrollHeight - box.scrollTop - box.clientHeight < 150;

    if (force || isNearBottom) {
        // Dùng cả 2 cách để tăng độ tin cậy
        requestAnimationFrame(() => {
            box.scrollTop = box.scrollHeight;

            // Đôi khi vẫn cần thêm 1 tick nữa
            setTimeout(() => {
                box.scrollTop = box.scrollHeight;
            }, 10);
        });
    }
}


// chatbox 
const openChats = {};

function openChatBox(btn) {
    const friendId = btn.getAttribute("data-id");
    const friendName = btn.getAttribute("data-name");
    const friendAvatar = btn.getAttribute("data-avatar");

    if (openChats[friendId]) return;

    const container = document.getElementById("chatContainer");

    const box = document.createElement("div");
    box.className = "chat-box";
    box.id = "chat-box-" + friendId;

    box.innerHTML = `
	    <div class="chat-header d-flex justify-content-between align-items-center">

		<a href="/profile/${friendId}" 
		       class="d-flex align-items-center gap-2 text-decoration-none text-dark">

		        <img src="${friendAvatar || '/default-avatar.png'}"
		             class="rounded-circle"
		             width="30" height="30">

		        <span style="font-weight:500;">${friendName}</span>
		    </a>

		    <span onclick="closeChat(${friendId})" style="cursor:pointer">✖</span>
	    </div>

	    <div class="chat-body" id="chat-body-${friendId}"></div>

	    <div class="chat-input">
	        <input type="text" 
	               placeholder="Nhắn..." 
	               onkeydown="handleEnterMini(event, ${friendId})">
	        <button onclick="sendMiniMessage(${friendId})">➤</button>
	    </div>
	`;

    container.appendChild(box);
    openChats[friendId] = true;

    loadMiniChat(friendId);
}
function sendMiniMessage(friendId) {
    const input = document.querySelector(`#chat-box-${friendId} input`);
    const content = input.value.trim();
    if (!content) return;

    stompClient.send("/app/chat.send", {}, JSON.stringify({
        content: content,
        senderId: currentUserId,
        receiverId: friendId
    }));

    input.value = '';
}
function handleEnterMini(e, friendId) {
    if (e.key === "Enter") {
        e.preventDefault();
        sendMiniMessage(friendId);
    }
}

function renderMiniMessage(m, friendId) {
    const box = document.getElementById("chat-body-" + friendId);
    if (!box) return;

    const isMe = m.senderId == currentUserId;

    const row = document.createElement("div");
    row.className = "msg-row " + (isMe ? "me" : "");

    row.innerHTML = `
        <div class="msg-wrap">
            <img src="${m.senderAvatar || '/default-avatar.png'}"
                 onerror="this.src='/default-avatar.png'">

            <div class="chat-bubble ${isMe ? 'me' : 'other'}">
                ${m.content}
            </div>
        </div>
    `;

    box.appendChild(row);
    box.scrollTop = box.scrollHeight;
}

function loadMiniChat(friendId) {
    fetch('/chat/' + friendId)
        .then(res => res.json())
        .then(data => {
            const box = document.getElementById("chat-body-" + friendId);
            box.innerHTML = '';

            data.forEach(m => renderMiniMessage(m, friendId));
        });
}
function closeChat(friendId) {
    delete openChats[friendId];

    const box = document.getElementById("chat-box-" + friendId);
    if (box) box.remove();
}