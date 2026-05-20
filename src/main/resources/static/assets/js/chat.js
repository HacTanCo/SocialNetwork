console.log("Chat");
let chatStompClient = null;
let totalUnread = 0;

let localStream = null;
let peerConnection = null;
let pendingCandidates = [];
let currentCallRemoteId = null;

// ===== Recording state =====
let mediaRecorder = null;
let recordedChunks = [];
let isRecording = false;
let recordingTimerInterval = null;
let recordingSeconds = 0;
let recordingBlob = null;
const MAX_RECORDING_SECONDS = 5 * 60; // 5 phút

const config = {
    iceServers: [
        { urls: "stun:stun.l.google.com:19302" }
    ]
};
// 25/4 
function connectWS() {
    const socket = new SockJS('/ws');
    chatStompClient = Stomp.over(socket);

    chatStompClient.connect({}, function () {
        chatStompClient.subscribe('/topic/call/' + currentUserId, async function (msg) {
            const data = JSON.parse(msg.body);

            console.log("Received call msg:", data.type, "from:", data.from, "to:", data.to, "me:", currentUserId);
            if (data.type === "offer") {

                // CHỈ xử lý nếu mình là người nhận
                if (data.to != currentUserId) return;

                handleIncomingCall(data);
            }

            if (data.type === "answer") {
                if (data.to != currentUserId) return;

                if (!peerConnection) return;

                await peerConnection.setRemoteDescription(JSON.parse(data.data));

                // Thêm các ICE candidate đã chờ sẵn
                pendingCandidates.forEach(c => peerConnection.addIceCandidate(c));
                pendingCandidates = [];
            }

            if (data.type === "ice") {
                if (data.to != currentUserId) return;

                const candidate = JSON.parse(data.data);

                if (peerConnection && peerConnection.remoteDescription) {
                    await peerConnection.addIceCandidate(candidate);
                } else {
                    // Chưa có remote description → lưu tạm
                    pendingCandidates.push(candidate);
                }
            }

            if (data.type === "reject") {

                // CHỈ xử lý nếu mình là người nhận reject
                if (data.to != currentUserId) return;

                alert("Cuộc gọi bị từ chối");
                endCall();
            }
            if (data.type === "cancel") {
                if (data.to != currentUserId) return;
                // Đóng modal incoming call nếu đang hiện
                const modal = document.getElementById("incomingCallModal");
                if (modal) modal.remove();
            }
            if (data.type === "hangup") {
                if (data.to != currentUserId) return;
                // Bên kia đã kết thúc cuộc gọi → đóng ở phía mình
                endCall(false); // false = không gửi lại hangup (tránh vòng lặp)
            }
            if (data.type === "recording_started") {
                if (data.to != currentUserId) return;
                // Bên kia bắt đầu ghi → vô hiệu hoá nút ghi của mình + hiển thị banner
                const btn = document.getElementById('btnRecord');
                if (btn) {
                    btn.disabled = true;
                    btn.style.opacity = '0.4';
                }
                const banner = document.getElementById('recordingBanner');
                if (banner) 
                    banner.style.display = 'block';
            }
            if (data.type === "recording_stopped") {
                if (data.to != currentUserId) return;
                // Bên kia dừng ghi → mở lại nút + ẩn banner
                const btn = document.getElementById('btnRecord');
                if (btn) { 
                    btn.disabled = false; 
                    btn.style.opacity = '1'; 
                }
                const banner = document.getElementById('recordingBanner');
                if (banner) 
                    banner.style.display = 'none';
            }
        });

        chatStompClient.subscribe('/topic/chat/' + currentUserId, function (msg) {

            const message = JSON.parse(msg.body);
            //  HANDLE DELETE
            if (message.id && !message.content && !message.mediaUrl && !message.createdAt) {

                const existing = document.querySelector(`[data-id='${message.id}']`);
                if (existing) {
                    existing.remove(); //  xóa luôn khỏi UI
                }

                return;
            }
            // =HANDLE UPDATE
            //  nếu message đã tồn tại → update UI, không render mới
            if (message.id && message.content && message.createdAt) {

                const existing = document.querySelector(`[data-id='${message.id}']`);

                if (existing) {
                    if (!existing.isConnected) return;

                    const bubble = existing.querySelector(".chat-bubble");
                    if (!bubble) return; //  FIX 1: chặn null// Chưa có remote description → lưu tạm

                    const contentDiv = bubble.querySelector(".msg-content");
                    if (!contentDiv) return; //  FIX 2: chặn null

                    contentDiv.innerText = message.content;

                    const timeEl = bubble.querySelector("small");
                    if (timeEl) {
                        timeEl.innerText = formatTimeSmart(message.createdAt) + " (đã sửa)";
                    }

                    return;
                }
            }

            if (!message.content && !message.mediaUrl && message.senderId) {

                //  CHỈ xử lý nếu mình là người nhận seen
                if (message.receiverId == currentUserId) {
                    updateSeenStatus(message.senderId);
                    //clearBadge(message.senderId);
                }

                return;
            }

            const friendId = message.senderId == currentUserId
                ? message.receiverId
                : message.senderId;

            if (!openChats[friendId]) {
                //  chỉ show notification thôi
                showNewMessageBadge(friendId);

                totalUnread++;
                updateTotalBadge(totalUnread);
            }

            //  render mini chat
            renderMiniMessage(message, friendId);

        });

    });
}
// 25/4
function showNewMessageBadge(friendId) {
    const badge = document.getElementById("badge-" + friendId);
    if (!badge) return;

    badge.style.display = "inline-block";

    let count = parseInt(badge.innerText) || 0;
    badge.innerText = count + 1;
}
// 25/4
function updateSeenStatus(friendId) {
    const box = document.getElementById("chat-body-" + friendId);
    if (!box) return;

    //  gọi lại logic set status
    updateLastMessageStatus(box, "Đã xem");
}
// 25/4
function updateLastMessageStatus(box, status) {

    //  xóa tất cả status cũ
    const allStatus = box.querySelectorAll(".msg-status");
    allStatus.forEach(e => e.remove());

    //  tìm tất cả tin của mình
    const myMessages = box.querySelectorAll(".msg-row.me");
    if (myMessages.length === 0) return;

    const lastMsg = myMessages[myMessages.length - 1];

    const bubble = lastMsg.querySelector(".chat-bubble");

    //  add status mới
    const statusEl = document.createElement("small");
    statusEl.className = "msg-status";
    statusEl.style.cssText = "font-size:9px; opacity:0.6; display:block; text-align:right;";
    statusEl.innerText = status;

    bubble.appendChild(statusEl);
}


// v 25/4
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
// 25/4
function clearBadge(friendId) {
    const badge = document.getElementById("badge-" + friendId);
    if (!badge) return;

    badge.innerText = "0";
    badge.style.display = "none";
}
// 25/4
function openChatBox(btn) {
    const friendId = btn.getAttribute("data-id");
    const friendName = btn.getAttribute("data-name");
    const friendAvatar = btn.getAttribute("data-avatar");
    const badge = document.getElementById("badge-" + friendId);
    if (badge) {
        const count = parseInt(badge.innerText) || 0;
        totalUnread -= count;
    }

    clearBadge(friendId);
    updateTotalBadge(totalUnread);
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
			<div class="d-flex align-items-center gap-2">

			    

			    <!-- 🎥 Video call -->
			    <i class="bi bi-camera-video-fill"
			       style="cursor:pointer"
			       onclick="startVideoCall(${friendId})"></i>

			    <!-- ❌ Close -->
			    <span onclick="closeChat(${friendId})" style="cursor:pointer">✖</span>
			</div>
	    </div>

	    <div class="chat-body" id="chat-body-${friendId}"></div>

	    <div class="chat-input">
			<input type="text" 
					placeholder="Nhắn..." 
					onfocus="handleFocusMini(${friendId})"
					onkeydown="handleEnterMini(event, ${friendId})">
			<label class="file-btn">
				<i class="bi bi-file-image"></i>
				<input type="file" 
			           class="file-input" 
			           accept="image/*,video/*"
					   multiple
			           onchange="sendFile(${friendId}, this)">
				
			</label>
	        <button onclick="sendMiniMessage(${friendId})">➤</button>
	    </div>
	`;

    container.appendChild(box);
    openChats[friendId] = true;

    loadMiniChat(friendId);

    //  focus input sau khi render
    setTimeout(() => {
        const input = box.querySelector("input");
        if (input) input.focus();
    }, 100);


}

// 18/5 
async function startVideoCall(friendId) {
    console.log("currentUserName =", currentUserName);
    currentCallRemoteId = friendId; // Lưu ID người nhận
    pendingCandidates = []; // Reset danh sách ICE candidate chờ
    // Bước 1: Lấy camera + mic của người gọi
    localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });

    showCallUI(localStream);
    // Bước 2: Tạo PeerConnection (WebRTC)
    peerConnection = new RTCPeerConnection(config);
    // Thêm tất cả track (video + audio) vào connection
    localStream.getTracks().forEach(track =>
        peerConnection.addTrack(track, localStream)
    );
    // Khi nhận được stream từ bên kia → hiển thị vào remoteVideo
    peerConnection.ontrack = e => {
        document.getElementById("remoteVideo").srcObject = e.streams[0];
    };
    // Khi có ICE candidate → gửi qua WebSocket cho bên kia
    peerConnection.onicecandidate = e => {
        if (e.candidate) {
            chatStompClient.send("/app/call", {}, JSON.stringify({
                type: "ice",
                from: currentUserId,
                to: friendId,
                data: JSON.stringify(e.candidate)
            }));
        }
    };
    // Bước 3: Tạo Offer (SDP)
    const offer = await peerConnection.createOffer();
    await peerConnection.setLocalDescription(offer);
    // Bước 4: Gửi Offer qua WebSocket đến người nhận
    chatStompClient.send("/app/call", {}, JSON.stringify({
        type: "offer",
        from: currentUserId,
        fromName: currentUserName,
        to: friendId,
        data: JSON.stringify(offer)
    }));
    window.callTimeout = setTimeout(() => {
        if (peerConnection && !peerConnection.remoteDescription) {
            // Gửi cancel cho bên nhận
            chatStompClient.send("/app/call", {}, JSON.stringify({
                type: "cancel",
                from: currentUserId,
                to: friendId 
            }));
            alert("Không có phản hồi từ người nhận");
            endCall();
        }
    }, 5000);
}
// 18/5 
async function handleIncomingCall(data) {
    console.log("Incoming call from:", data.from);

    // Hiện modal thay vì confirm()
    showIncomingCallModal(data);
}
// 18/5 
function showIncomingCallModal(data) {
    // Tạo modal động
    const existing = document.getElementById("incomingCallModal");
    if (existing) existing.remove();

    const modal = document.createElement("div");
    modal.id = "incomingCallModal";
    modal.style.cssText = `
        position: fixed;
        top: 30px;
        left: 50%;
        transform: translateX(-50%);
        background: white;
        border: 1px solid #ddd;
        border-radius: 12px;
        padding: 20px 28px;
        z-index: 999999;
        box-shadow: 0 8px 30px rgba(0,0,0,0.15);
        text-align: center;
        min-width: 280px;
    `;

    modal.innerHTML = `
        <div style="font-size:15px; font-weight:600; margin-bottom:6px;">
            <i class="bi bi-camera-video"></i> Cuộc gọi video đến
        </div>
        <div style="font-size:13px; color:#666; margin-bottom:16px;">
            Người dùng #${data.fromName || 'Ai đó'} đang gọi cho bạn
        </div>
        <div style="display:flex; gap:12px; justify-content:center;">
            <button id="btnAcceptCall" style="
                background:#28a745; color:white;
                border:none; border-radius:8px;
                padding:8px 24px; font-size:14px; cursor:pointer;">
                <i class="bi bi-telephone-inbound-fill"></i>
            </button>
            <button id="btnRejectCall" style="
                background:#dc3545; color:white;
                border:none; border-radius:8px;
                padding:8px 24px; font-size:14px; cursor:pointer;">
                 <i class="bi bi-telephone-x-fill"></i>
            </button>
        </div>
    `;

    document.body.appendChild(modal);

    // Xử lý nút Nhận
    document.getElementById("btnAcceptCall").onclick = async () => {
        modal.remove();
        await acceptCall(data);
    };

    // Xử lý nút Từ chối
    document.getElementById("btnRejectCall").onclick = () => {
        modal.remove();
        chatStompClient.send("/app/call", {}, JSON.stringify({
            type: "reject",
            from: currentUserId,
            to: data.from
        }));
    };
}

// Tách logic accept ra riêng
// 18/5 
async function acceptCall(data) {
    pendingCandidates = [];
    currentCallRemoteId = data.from; // Lưu ID người gọi để gửi hangup sau này

    localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
    showCallUI(localStream);

    peerConnection = new RTCPeerConnection(config);

    localStream.getTracks().forEach(track =>
        peerConnection.addTrack(track, localStream)
    );

    peerConnection.ontrack = e => {
        document.getElementById("remoteVideo").srcObject = e.streams[0];
    };

    peerConnection.onicecandidate = e => {
        if (e.candidate) {
            chatStompClient.send("/app/call", {}, JSON.stringify({
                type: "ice",
                from: currentUserId,
                to: data.from,
                data: JSON.stringify(e.candidate)
            }));
        }
    };
    // Bước quan trọng: set Offer từ người gọi vào remoteDescription
    await peerConnection.setRemoteDescription(JSON.parse(data.data));
    // Thêm các ICE candidate đã nhận trước đó (nếu có)
    pendingCandidates.forEach(c => peerConnection.addIceCandidate(c));
    pendingCandidates = [];
    // Tạo Answer
    const answer = await peerConnection.createAnswer();
    await peerConnection.setLocalDescription(answer);

    chatStompClient.send("/app/call", {}, JSON.stringify({
        type: "answer",
        from: currentUserId,
        to: data.from,
        data: JSON.stringify(answer)
    }));
}
// 18/5 
function showCallUI(stream) {
    document.getElementById("callModal").style.display = "block";
    document.getElementById("localVideo").srcObject = stream;
}
// 18/5 
function endCall(sendSignal = true) {
    // Nếu đang ghi → dừng ghi trước (sẽ tự upload sau)
    if (isRecording) {
        stopRecording(true); // true = called from endCall
    }

    // Gửi tín hiệu hangup cho bên kia nếu còn kết nối
    if (sendSignal && currentCallRemoteId && chatStompClient && chatStompClient.connected) {
        chatStompClient.send("/app/call", {}, JSON.stringify({
            type: "hangup",
            from: currentUserId,
            to: currentCallRemoteId
        }));
    }

    if (peerConnection) {
        peerConnection.close();
        peerConnection = null;
    }

    if (localStream) {
        localStream.getTracks().forEach(t => t.stop());
        localStream = null;
    }

    // Reset recording UI
    const btnRecord = document.getElementById('btnRecord');
    if (btnRecord) { btnRecord.disabled = false; btnRecord.style.opacity = '1'; }
    const iconRecord = document.getElementById('iconRecord');
    if (iconRecord) iconRecord.className = 'bi bi-record-circle';
    const timerEl = document.getElementById('recordTimer');
    if (timerEl) timerEl.style.display = 'none';
    const banner = document.getElementById('recordingBanner');
    if (banner) banner.style.display = 'none';

    currentCallRemoteId = null;
    document.getElementById("callModal").style.display = "none";
}

// ======================== RECORDING ========================
// 18/5 
function toggleRecording() {
    if (isRecording) {
        stopRecording(false);
    } else {
        startRecording();
    }
}
// 18/5 
async function startRecording() {
    if (!localStream) {
        alert("Chưa có luồng video để ghi.");
        return;
    }

    recordedChunks = [];
    recordingSeconds = 0;
    recordingBlob = null;

    // -- Mix 2 luồng video lên canvas --
    const canvas = document.getElementById('recordCanvas');
    const W = 1280, H = 480;
    canvas.width = W;
    canvas.height = H;
    const ctx = canvas.getContext('2d');

    const localVideo = document.getElementById('localVideo');
    const remoteVideo = document.getElementById('remoteVideo');

    function drawFrame() {
        if (!isRecording) return;
        ctx.clearRect(0, 0, W, H);
        // Remote (bên trái, full half)
        if (remoteVideo.readyState >= 2) {
            ctx.drawImage(remoteVideo, 0, 0, W / 2, H);
        } else {
            ctx.fillStyle = '#222';
            ctx.fillRect(0, 0, W / 2, H);
        }
        // Local (bên phải)
        if (localVideo.readyState >= 2) {
            ctx.drawImage(localVideo, W / 2, 0, W / 2, H);
        } else {
            ctx.fillStyle = '#333';
            ctx.fillRect(W / 2, 0, W / 2, H);
        }
        requestAnimationFrame(drawFrame);
    }

    // -- Mix 2 audio track --
    const audioCtx = new AudioContext();
    const dest = audioCtx.createMediaStreamDestination();

    [localStream, document.getElementById('remoteVideo').srcObject]
        .filter(Boolean)
        .forEach(stream => {
            const audioTracks = stream.getAudioTracks();
            if (audioTracks.length > 0) {
                const source = audioCtx.createMediaStreamSource(new MediaStream(audioTracks));
                source.connect(dest);
            }
        });

    // -- Tạo combined stream (canvas video + mixed audio) --
    const canvasStream = canvas.captureStream(30); // 30fps
    const combinedStream = new MediaStream([
        ...canvasStream.getVideoTracks(),
        ...dest.stream.getAudioTracks()
    ]);

    // -- Khởi động MediaRecorder --
    const mimeType = MediaRecorder.isTypeSupported('video/webm;codecs=vp9,opus')
        ? 'video/webm;codecs=vp9,opus'
        : 'video/webm';

    mediaRecorder = new MediaRecorder(combinedStream, { mimeType });

    mediaRecorder.ondataavailable = e => {
        if (e.data && e.data.size > 0) recordedChunks.push(e.data);
    };

    mediaRecorder.onstop = () => {
        recordingBlob = new Blob(recordedChunks, { type: mimeType });
        audioCtx.close();
        uploadRecording(recordingBlob);
    };

    mediaRecorder.start(1000); // lấy chunk mỗi 1 giây
    isRecording = true;

    drawFrame(); // bắt đầu vẽ canvas

    // -- UI feedback --
    const iconRecord = document.getElementById('iconRecord');
    if (iconRecord) iconRecord.className = 'bi bi-stop-circle-fill';
    document.getElementById('btnRecord').style.background = 'rgba(255,77,77,0.7)';

    const timerEl = document.getElementById('recordTimer');
    if (timerEl) timerEl.style.display = 'inline';

    recordingTimerInterval = setInterval(() => {
        recordingSeconds++;
        const m = String(Math.floor(recordingSeconds / 60)).padStart(2, '0');
        const s = String(recordingSeconds % 60).padStart(2, '0');
        if (timerEl) timerEl.textContent = `${m}:${s}`;

        // Tự dừng sau MAX_RECORDING_SECONDS
        if (recordingSeconds >= MAX_RECORDING_SECONDS) {
            stopRecording(false);
        }
    }, 1000);

    // -- Báo cho bên kia biết đang bị ghi --
    if (currentCallRemoteId && chatStompClient && chatStompClient.connected) {
        chatStompClient.send("/app/call", {}, JSON.stringify({
            type: "recording_started",
            from: currentUserId,
            to: currentCallRemoteId
        }));
    }
}
// 18/5 
function stopRecording(calledFromEndCall = false) {
    if (!isRecording || !mediaRecorder) return;

    clearInterval(recordingTimerInterval);
    isRecording = false;

    try { mediaRecorder.stop(); } catch (e) { /* ignore */ }

    // -- UI feedback --
    const iconRecord = document.getElementById('iconRecord');
    if (iconRecord) iconRecord.className = 'bi bi-record-circle';
    const btnRecord = document.getElementById('btnRecord');
    if (btnRecord) btnRecord.style.background = 'rgba(255,255,255,0.15)';
    const timerEl = document.getElementById('recordTimer');
    if (timerEl) timerEl.style.display = 'none';

    // -- Báo cho bên kia biết dừng ghi --
    if (!calledFromEndCall && currentCallRemoteId && chatStompClient && chatStompClient.connected) {
        chatStompClient.send("/app/call", {}, JSON.stringify({
            type: "recording_stopped",
            from: currentUserId,
            to: currentCallRemoteId
        }));
    }
}
// 18/5 
async function uploadRecording(blob) {
    if (!blob || blob.size === 0 || !currentCallRemoteId) return;

    const duration = recordingSeconds;
    const receiverId = currentCallRemoteId; // snapshot trước khi reset

    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;

    const formData = new FormData();
    formData.append('file', blob, 'recording.webm');
    formData.append('receiverId', receiverId);
    formData.append('duration', duration);

    try {
        const res = await fetch('/call/recording/upload', {
            method: 'POST',
            headers: { [header]: token },
            body: formData
        });
        const data = await res.json();
        if (data.url) {
            showRecordingResult(blob, data.url);
        }
    } catch (err) {
        console.error('Upload recording failed:', err);
    }
}

function showRecordingResult(blob, serverUrl) {
    const modal = document.getElementById('recordingResultModal');
    const player = document.getElementById('recordingPlayer');
    const btnDownload = document.getElementById('btnDownloadRecording');

    if (!modal || !player) return;

    // Dùng local blob URL để phát ngay lập tức
    const blobUrl = URL.createObjectURL(blob);
    player.src = blobUrl;
    if (btnDownload) {
        btnDownload.href = serverUrl; // Link tải từ server
    }

    modal.style.display = 'flex';
}

function closeRecordingModal() {
    const modal = document.getElementById('recordingResultModal');
    const player = document.getElementById('recordingPlayer');
    if (player) {
        player.pause();
        if (player.src && player.src.startsWith('blob:')) {
            URL.revokeObjectURL(player.src);
        }
        player.src = '';
    }
    if (modal) modal.style.display = 'none';
    recordingBlob = null;
    recordedChunks = [];
    recordingSeconds = 0;
}

function showRemoteVideo(stream) {
    let video = document.createElement("video");

    video.srcObject = stream;
    video.autoplay = true;

    video.style = `
        position: fixed;
        bottom: 20px;
        left: 20px;
        width: 200px;
        border-radius: 10px;
        z-index: 9999;
    `;

    document.body.appendChild(video);
}
function showVideoPreview(stream) {
    let video = document.createElement("video");

    video.srcObject = stream;
    video.autoplay = true;
    video.muted = true;

    video.style = `
        position: fixed;
        bottom: 20px;
        right: 20px;
        width: 200px;
        border-radius: 10px;
        z-index: 9999;
    `;

    document.body.appendChild(video);
}
// 24/4
function sendFile(friendId, input) {
    const files = input.files;
    if (!files || files.length === 0) return;

    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;

    Array.from(files).forEach(file => {
        if (file.size > 10 * 1024 * 1024) {
            showError("File quá lớn (tối đa 10MB)");
            return; // ❗ không gọi fetch nữa
        }
        const formData = new FormData();
        formData.append("file", file);

        fetch("/chat/upload", {
            method: "POST",
            body: formData,
            headers: {
                [header]: token
            }
        })
            .then(async res => {
                const data = await res.json(); // ✅ luôn parse JSON

                if (!res.ok || data.error) {
                    throw new Error(data.error || "Upload thất bại");

                }

                return data;
            })
            .then(data => {

                const url = data.url; // ✅ lấy đúng field

                let type = "IMAGE";
                if (file.type.startsWith("video")) {
                    type = "VIDEO";
                }

                chatStompClient.send("/app/chat/send", {}, JSON.stringify({
                    senderId: currentUserId,
                    receiverId: friendId,
                    content: "",
                    mediaUrl: url,
                    type: type
                }));

            })
            .catch(err => {
                showError(err.message);
            });

    });

    input.value = "";
}
// 25/4
function showError(msg) {
    const div = document.createElement("div");
    div.innerText = msg;

    div.style = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #dc3545;
        color: white;
        padding: 10px 15px;
        border-radius: 8px;
        z-index: 99999;
        font-size: 13px;
        box-shadow: 0 4px 10px rgba(0,0,0,0.2);
    `;

    document.body.appendChild(div);

    setTimeout(() => div.remove(), 3000);
}
// 25/4
function openImagePreview(src) {
    const modal = document.getElementById("imagePreviewModal");
    const img = document.getElementById("previewImg");

    img.src = src;
    modal.style.display = "flex";
}
// 25/4
function closeImagePreview() {
    document.getElementById("imagePreviewModal").style.display = "none";
}
// v 25/4
function handleFocusMini(friendId) {

    const box = document.getElementById("chat-body-" + friendId);
    if (!box) return;

    //  tìm tin NHẬN (không phải của mình)
    const messages = box.querySelectorAll(".msg-row:not(.me)");

    if (messages.length === 0) return;

    //  kiểm tra có tin chưa read không (dựa vào status UI hoặc data)
    let hasUnread = false;

    messages.forEach(msg => {
        if (!msg.classList.contains("read")) {
            hasUnread = true;
        }
    });

    if (!hasUnread) return; // không gửi seen nếu không có tin mới

    // mới gửi seen
    chatStompClient.send("/app/chat/seen", {}, JSON.stringify({
        senderId: friendId,
        receiverId: currentUserId
    }));

    //  mark UI là đã đọc
    messages.forEach(msg => msg.classList.add("read"));
}
// 25/4
function sendMiniMessage(friendId) {
    const input = document.querySelector(`#chat-box-${friendId} input`);
    const content = input.value.trim();
    if (!content) return;

    chatStompClient.send("/app/chat/send", {}, JSON.stringify({
        content: content,
        senderId: currentUserId,
        receiverId: friendId
    }));

    input.value = '';
}
// 25/4
function handleEnterMini(e, friendId) {
    if (e.key === "Enter") {
        e.preventDefault();
        sendMiniMessage(friendId);
    }
}
// 25/4
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
        }

    }
    let mediaHtml = "";

    if (m.type === "IMAGE" && m.mediaUrl) {
        mediaHtml = `
		    <img src="${m.mediaUrl}" 
		         class="chat-media"
		         onclick="openImagePreview('${m.mediaUrl}')"
		         style="cursor:pointer;">
		`;
    }
    else if (m.type === "VIDEO" && m.mediaUrl) {
        mediaHtml = `
	        <video controls class="chat-media">
	            <source src="${m.mediaUrl}">
	        </video>
	    `;
    }
    else if (m.type === "RECORDING" && m.mediaUrl) {
        const durSec = m.duration || 0;
        const mm = String(Math.floor(durSec / 60)).padStart(2, '0');
        const ss = String(durSec % 60).padStart(2, '0');
        const durLabel = durSec > 0 ? `${mm}:${ss}` : '';
        mediaHtml = `
            <div class="recording-msg-card">
                <div class="recording-msg-header">
                    <span class="recording-msg-icon">🎬</span>
                    <div class="recording-msg-info">
                        <span class="recording-msg-title">Bản ghi cuộc gọi</span>
                        ${durLabel ? `<span class="recording-msg-dur">${durLabel}</span>` : ''}
                    </div>
                </div>
                <video controls class="recording-msg-video">
                    <source src="${m.mediaUrl}" type="video/webm">
                </video>
                <a href="${m.mediaUrl}" download class="recording-msg-download">
                    <i class="bi bi-download"></i> Tải xuống
                </a>
            </div>
        `;
    }
    const row = document.createElement("div");
    row.className = "msg-row " + (isMe ? "me" : "");

    // THÊM: gắn id để sau này update đúng message
    row.setAttribute("data-id", m.id);
    // THÊM: nút sửa (chỉ hiện với message của mình + TEXT)
    let actions = ""; // QUAN TRỌNG

    if (isMe) {
        actions = `
	    <div class="dropdown msg-actions">
	        <button class="btn p-0 border-0 text-muted" data-bs-toggle="dropdown">
	            <i class="bi bi-three-dots"></i>
	        </button>

	        <ul class="dropdown-menu dropdown-menu-end">
	            ${m.type === "TEXT" ? `
	            <li>
	                <button class="dropdown-item"
	                    onclick="editMessage(${m.id})">
	                    Sửa
	                </button>
	            </li>
	            ` : ""}

	            <li>
	                <button class="dropdown-item text-danger"
	                    onclick="deleteMessage(${m.id})">
	                    Xóa
	                </button>
	            </li>
	        </ul>
	    </div>
	    `;
    }

    row.innerHTML = `
	    <div class="msg-wrap">
	        <img src="${m.senderAvatar || '/default-avatar.png'}"
	             onerror="this.src='/default-avatar.png'">

	        

	        <div class="chat-bubble ${isMe ? 'me' : 'other'}">
				
				${m.content ? `<div class="msg-content">${m.content}</div>` : ""}
	            ${mediaHtml}

	            <small style="font-size:10px; opacity:0.7;">
	                ${formatTimeSmart(m.createdAt)}
	            </small>
	        </div>
			${actions} 
	    </div>
	`;

    box.appendChild(row);
    box.scrollTop = box.scrollHeight;
    //  chỉ giữ status ở tin cuối của mình
    if (isMe) {
        const finalStatus = m.read ? "Đã xem"
            : m.delivered ? "Đã nhận"
                : "Đã gửi";

        updateLastMessageStatus(box, finalStatus);
    }
}
// 25/4
function deleteMessage(id) {

    if (!confirm("Bạn có chắc muốn xóa tin nhắn này?")) return;

    chatStompClient.send("/app/chat/delete", {}, JSON.stringify({
        id: id
    }));
}
// 25/4
function editMessage(id) {
    // data-id nằm ở row.setAttribute("data-id", m.id);
    const row = document.querySelector(`[data-id='${id}']`);
    if (!row) return;

    const bubble = row.querySelector(".chat-bubble");
    if (!bubble) return;

    const contentDiv = bubble.querySelector(".msg-content");
    if (!contentDiv) return;

    const oldText = contentDiv.innerText;

    const input = document.createElement("input");
    input.type = "text";
    input.value = oldText;
    input.className = "form-control form-control-sm";

    contentDiv.replaceWith(input);
    input.focus();

    let isSaving = false;
    let isHandled = false; //  KEY: chặn xử lý nhiều lần

    function safeReplace(newText) {
        if (isHandled) return; //  chống double run
        isHandled = true;

        if (!input.parentNode) return;

        const div = document.createElement("div");
        div.className = "msg-content";
        div.innerText = newText;

        input.replaceWith(div);
    }

    input.addEventListener("keydown", function (e) {

        // ENTER = save
        if (e.key === "Enter") {
            e.preventDefault();

            const newContent = input.value.trim();
            if (!newContent) return;

            isSaving = true;

            safeReplace(newContent); //  UI revert ngay

            chatStompClient.send("/app/chat/update", {}, JSON.stringify({
                id: id,
                content: newContent
            }));
        }

        // ESC = cancel
        if (e.key === "Escape") {
            safeReplace(oldText); //  chỉ gọi 1 lần duy nhất
        }
    });

    input.addEventListener("blur", function () {
        if (!isSaving) {
            safeReplace(oldText); //  blur cũng dùng chung logic
        }
    });
}
// 25/4
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
// 25/4
function loadMiniChat(friendId) {
    fetch('/chat/' + friendId)
        .then(res => res.json())
        .then(data => {

            const box = document.getElementById("chat-body-" + friendId);
            box.innerHTML = '';

            data.forEach(m => renderMiniMessage(m, friendId));
            //  FIX: scroll sau khi render xong toàn bộ
            setTimeout(() => {
                box.scrollTop = box.scrollHeight;
            }, 50);
        });
}
// 25/4
function closeChat(friendId) {
    delete openChats[friendId];

    const box = document.getElementById("chat-box-" + friendId);
    if (box) box.remove();
}
// 25/4 hiển thị các badge chưa đọc ở Friend
function initUnreadMessage() {
    fetch(`/chat/unread-count?userId=${currentUserId}`)
        .then(res => res.json())
        .then(data => {

            totalUnread = 0;

            Object.entries(data).forEach(([friendId, count]) => {
                totalUnread += count;

                const badge = document.getElementById("badge-" + friendId);
                if (badge && count > 0) {
                    badge.innerText = count;
                    badge.style.display = "inline-block";
                }
            });

            updateTotalBadge(totalUnread);
        });
}
// 25/4 cái này là ở sidebar left
function updateTotalBadge(total) {
    const badge = document.getElementById("message-unread-badge");
    if (!badge) return;

    if (total > 0) {
        badge.innerText = total;
        badge.style.display = "inline-block";
    } else {
        badge.style.display = "none";
    }
}