console.log("AI Chat loaded");

// ================= LOAD HISTORY =================
function loadAIHistory() {
    fetch("/ai/history")
        .then(res => res.json())
        .then(data => {
            const box = document.getElementById("aiChatMessages");
            box.innerHTML = "";

            data.forEach(m => {
                addMessage(
                    m.role === "USER" ? "Bạn" : "AI",
                    m.content
                );
            });

            scrollToBottom();
        });
}
// ================= SEND =================
function sendAiMessage() {
    const input = document.getElementById("aiChatInput");
    const message = input.value.trim();

    if (!message) return;

    addMessage("Bạn", message);
    input.value = "";

    const loadingId = addMessage("AI", "Đang trả lời...");

    // 🔥 lấy CSRF
    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;

	fetch("/ai/chat", {
	    method: "POST",
	    headers: {
	        "Content-Type": "application/x-www-form-urlencoded",
	        [header]: token
	    },
	    body: "message=" + encodeURIComponent(message)
	})
	.then(res => res.json())
	.then(data => {
	    updateMessage(loadingId, data.reply);
	})
	.catch(() => {
	    updateMessage(loadingId, "Lỗi rồi 😢");
	});
}

// ================= ENTER =================
function handleAiEnter(e) {
    if (e.key === "Enter") {
        sendAiMessage();
    }
}

// ================= RENDER =================
function addMessage(sender, text) {
    const box = document.getElementById("aiChatMessages");
    const div = document.createElement("div");
    const id = crypto.randomUUID();

    div.id = "msg-" + id;

    div.className = sender === "Bạn"
        ? "align-self-end bg-primary text-white p-2 rounded"
        : "align-self-start bg-light p-2 rounded";

    div.style.maxWidth = "70%";
    div.style.wordBreak = "break-word";

    div.textContent = text; // ✅ tránh XSS

    box.appendChild(div);

    scrollToBottom();

    return id;
}

// update loading → reply
function updateMessage(id, newText) {
    const div = document.getElementById("msg-" + id);
    if (div) div.textContent = newText;
}

// ================= SCROLL =================
function scrollToBottom() {
    const box = document.querySelector("#aiChatModal .modal-body");
    box.scrollTop = box.scrollHeight;
}

// ================= LOAD KHI MỞ MODAL =================
document.getElementById("aiChatModal")
    .addEventListener("show.bs.modal", function () {
        loadAIHistory();
    });