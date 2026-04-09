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
			
			// 🔥 nếu là event seen (không có content)
			   if (!message.content && message.senderId) {
			       updateSeenStatus(message.senderId);
			       return;
			   }
			   
            const friendId = message.senderId == currentUserId
                ? message.receiverId
                : message.senderId;

				if (!openChats[friendId]) {
				    // 🔥 chỉ show notification thôi
				    showNewMessageBadge(friendId);
				}

            // 🔥 render mini chat
            renderMiniMessage(message, friendId);
			
        });

    });
}
function updateSeenStatus(friendId) {
    const box = document.getElementById("chat-body-" + friendId);
    if (!box) return;

    // 🔥 gọi lại logic set status
    updateLastMessageStatus(box, "Đã xem");
}
function updateLastMessageStatus(box, status) {

    // 🔥 xóa tất cả status cũ
    const allStatus = box.querySelectorAll(".msg-status");
    allStatus.forEach(e => e.remove());

    // 🔥 tìm tất cả tin của mình
    const myMessages = box.querySelectorAll(".msg-row.me");
    if (myMessages.length === 0) return;

    const lastMsg = myMessages[myMessages.length - 1];

    const bubble = lastMsg.querySelector(".chat-bubble");

    // 🔥 add status mới
    const statusEl = document.createElement("small");
    statusEl.className = "msg-status";
    statusEl.style.cssText = "font-size:9px; opacity:0.6; display:block; text-align:right;";
    statusEl.innerText = status;

    bubble.appendChild(statusEl);
}
connectWS();
/*function handleOpenChat(btn) {
    const id = btn.getAttribute("data-id");
    const name = btn.getAttribute("data-name");

    openChat(id, name);
}*/
// mở chat
/*function openChat(friendId, friendName) {
    document.getElementById('chatFriendId').value = friendId;
    document.getElementById('chatFriendName').innerText = friendName;

    loadChat(friendId);
}*/
// bind sau khi load
/*document.addEventListener("DOMContentLoaded", function() {
    const input = document.getElementById("chatInput");

    input.addEventListener("keydown", function(e) {
        if (e.key === "Enter") {
            e.preventDefault();
            sendMessage();
        }
    });
});*/
// load lịch sử
/*function loadChat(friendId) {
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
*/
// gửi tin nhắn
/*function sendMessage() {
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
}*/

// render tin nhắn
/*function renderMessage(m) {
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
}*/

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
					onfocus="handleFocusMini(${friendId})"
					onkeydown="handleEnterMini(event, ${friendId})">
	        <button onclick="sendMiniMessage(${friendId})">➤</button>
	    </div>
	`;

    container.appendChild(box);
    openChats[friendId] = true;

    loadMiniChat(friendId);
	
}
function handleFocusMini(friendId) {

    const box = document.getElementById("chat-body-" + friendId);
    if (!box) return;

    // 🔥 tìm tin NHẬN (không phải của mình)
    const messages = box.querySelectorAll(".msg-row:not(.me)");

    if (messages.length === 0) return;

    // 🔥 kiểm tra có tin chưa read không (dựa vào status UI hoặc data)
    let hasUnread = false;

    messages.forEach(msg => {
        if (!msg.classList.contains("read")) {
            hasUnread = true;
        }
    });

    if (!hasUnread) return; // ❌ không gửi seen nếu không có tin mới

    // ✅ mới gửi seen
    stompClient.send("/app/chat.seen", {}, JSON.stringify({
        senderId: friendId,
        receiverId: currentUserId
    }));

    // 🔥 mark UI là đã đọc
    messages.forEach(msg => msg.classList.add("read"));
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
	
	let status = "";
	    if (isMe) {
	        if (m.read) {
	            status = "Đã xem";
	        } else if (m.delivered) {
	            status = "Đã nhận";
	        } else {
	            status = "Đã gửi";
	        }
	    }
		
    const row = document.createElement("div");
    row.className = "msg-row " + (isMe ? "me" : "");

    row.innerHTML = `
        <div class="msg-wrap">
            <img src="${m.senderAvatar || '/default-avatar.png'}"
                 onerror="this.src='/default-avatar.png'">

				 <div class="chat-bubble ${isMe ? 'me' : 'other'}">
				     <div>${m.content}</div>
				     <small style="font-size:10px; opacity:0.7;">
				        ${formatTimeSmart(m.createdAt)}
				     </small>
				 </div>
				
        </div>	
    `;

    box.appendChild(row);
    box.scrollTop = box.scrollHeight;
	// 🔥 chỉ giữ status ở tin cuối của mình
	if (isMe) {
	    const finalStatus = m.read ? "Đã xem"
	        : m.delivered ? "Đã nhận"
	        : "Đã gửi";

	    updateLastMessageStatus(box, finalStatus);
	}
}
function formatTimeSmart(time) {
    const date = new Date(time);
    const now = new Date();

    const isToday =
        date.getDate() === now.getDate() &&
        date.getMonth() === now.getMonth() &&
        date.getFullYear() === now.getFullYear();

    const yesterday = new Date();
    yesterday.setDate(now.getDate() - 1);

    const isYesterday =
        date.getDate() === yesterday.getDate() &&
        date.getMonth() === yesterday.getMonth() &&
        date.getFullYear() === yesterday.getFullYear();

    const timeStr = date.toLocaleTimeString("vi-VN", {
        hour: "2-digit",
        minute: "2-digit"
    });

    if (isToday) {
        return timeStr; // 14:30
    }

    if (isYesterday) {
        return "Hôm qua " + timeStr;
    }

    return date.toLocaleDateString("vi-VN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric"
    }) + " " + timeStr;
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