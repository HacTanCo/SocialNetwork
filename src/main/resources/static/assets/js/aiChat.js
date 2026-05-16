console.log("AI Chat loaded");

// ================= MARKDOWN RENDERER =================
function renderMarkdown(text) {
    if (!text) return "";

    // Escape HTML để tránh XSS trước
    let html = text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;");

    // 1. Code blocks ```...```
    html = html.replace(/```([\s\S]*?)```/g, (_, code) => {
        const blockId = 'cb-' + Math.random().toString(36).slice(2, 9);
        return `<div class="ai-code-block">
  <div class="ai-code-header">
    <span class="ai-code-lang"><i class="bi bi-code-slash"></i> Code</span>
    <button class="ai-copy-btn" onclick="copyCode('${blockId}')" title="Sao chép">
      <i class="bi bi-copy" id="icon-${blockId}"></i>
      <span id="label-${blockId}">Copy</span>
    </button>
  </div>
  <pre><code id="${blockId}">${code.trim()}</code></pre>
</div>`;
    });

    // 2. Headers (# Title)
    html = html.replace(/^###### (.*$)/gim, '<h6>$1</h6>');
    html = html.replace(/^##### (.*$)/gim, '<h5>$1</h5>');
    html = html.replace(/^#### (.*$)/gim, '<h4>$1</h4>');
    html = html.replace(/^### (.*$)/gim, '<h3>$1</h3>');
    html = html.replace(/^## (.*$)/gim, '<h2>$1</h2>');
    html = html.replace(/^# (.*$)/gim, '<h1>$1</h1>');

    // 3. Horizontal Rules (---)
    html = html.replace(/^---$/gm, '<hr>');

    // 4. Blockquotes (> text)
    // Cần xử lý nhiều dòng blockquote liên tiếp
    html = html.replace(/^\> (.*$)/gim, '<blockquote>$1</blockquote>');

    // 5. Links ([text](url))
    html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>');

    // 6. Inline code `...`
    html = html.replace(/`([^`]+)`/g, '<code>$1</code>');

    // 7. Bold **...**
    html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');

    // 8. Italic *...*
    html = html.replace(/\*(.+?)\*/g, '<em>$1</em>');

    // 9. Lists (Unordered & Ordered)
    // Tạm thời xử lý đơn giản: gom các dòng <li> vào <ul> hoặc <ol>
    html = html.replace(/^[ \t]*[-*] (.+)/gm, '<li>$1</li>');
    html = html.replace(/(<li>.*<\/li>)/s, (block) => `<ul>${block}</ul>`);
    
    // Lưu ý: Regex trên có thể gom cả list ordered và unordered vào 1 khối nếu chúng sát nhau.
    // Cần tinh chỉnh nếu muốn tách biệt hoàn toàn.

    // 10. Paragraphs: double newline → <p>
    // Chỉ wrap những phần không phải là thẻ block
    const blockTags = ['h1','h2','h3','h4','h5','h6','ul','ol','blockquote','hr','div','pre'];
    
    html = html.split(/\n{2,}/).map(p => {
        const trimmed = p.trim();
        if (!trimmed) return "";
        
        const startsWithBlock = blockTags.some(tag => trimmed.startsWith(`<${tag}`));
        if (startsWithBlock) return trimmed;
        
        return `<p>${trimmed.replace(/\n/g, '<br>')}</p>`;
    }).join('');

    return html;
}

// ================= COPY CODE =================
function copyCode(blockId) {
    const codeEl = document.getElementById(blockId);
    if (!codeEl) return;

    const text = codeEl.innerText;
    navigator.clipboard.writeText(text).then(() => {
        const icon = document.getElementById('icon-' + blockId);
        const label = document.getElementById('label-' + blockId);

        // Feedback
        icon.className = 'bi bi-check-lg';
        label.textContent = 'Đã copy!';

        setTimeout(() => {
            icon.className = 'bi bi-copy';
            label.textContent = 'Copy';
        }, 2000);
    }).catch(() => {
        // fallback cho trình duyệt cũ
        const range = document.createRange();
        range.selectNodeContents(codeEl);
        window.getSelection().removeAllRanges();
        window.getSelection().addRange(range);
        document.execCommand('copy');
        window.getSelection().removeAllRanges();
    });
}

// ================= LOAD HISTORY =================
function loadAIHistory() {
    fetch("/ai/history")
        .then(res => res.json())
        .then(data => {
            const box = document.getElementById("aiChatMessages");
            box.innerHTML = "";

            if (data.length === 0) {
                box.innerHTML = `
                    <div class="ai-welcome">
                        <span class="ai-welcome-icon">🤖</span>
                        Xin chào! Tôi có thể giúp gì cho bạn hôm nay?
                    </div>`;
            } else {
                data.forEach(m => {
                    addMessage(
                        m.role === "USER" ? "Bạn" : "AI",
                        m.content,
                        true // fromHistory – render markdown ngay
                    );
                });
            }

            scrollToBottom();
        });
}

// ================= SEND =================
function sendAiMessage() {
    const input = document.getElementById("aiChatInput");
    const message = input.value.trim();

    if (!message) return;

    // Xoá placeholder welcome nếu còn
    const welcome = document.querySelector(".ai-welcome");
    if (welcome) welcome.remove();

    addMessage("Bạn", message, true);
    input.value = "";

    const loadingId = addLoadingMessage();

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
            updateMessage(loadingId, "Có lỗi xảy ra 😢 Vui lòng thử lại.");
        });
}

// ================= ENTER =================
function handleAiEnter(e) {
    if (e.key === "Enter") {
        sendAiMessage();
    }
}

// ================= RENDER MESSAGE =================
function addMessage(sender, text, renderMd = false) {
    const box = document.getElementById("aiChatMessages");
    const id = crypto.randomUUID();
    const isUser = sender === "Bạn";

    const row = document.createElement("div");
    row.className = "ai-message-row" + (isUser ? " user-row" : "");

    const avatarIcon = isUser
        ? `<div class="ai-avatar user-avatar-icon"><i class="bi bi-person-fill"></i></div>`
        : `<div class="ai-avatar bot-avatar"><i class="bi bi-robot"></i></div>`;

    const bubbleClass = isUser ? "ai-bubble ai-user" : "ai-bubble ai-assistant";

    const contentHtml = renderMd
        ? `<div class="ai-content">${renderMarkdown(text)}</div>`
        : `<div class="ai-content">${text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")}</div>`;

    row.innerHTML = `
        ${!isUser ? avatarIcon : ""}
        <div class="${bubbleClass}" id="msg-${id}">
            ${contentHtml}
        </div>
        ${isUser ? avatarIcon : ""}
    `;

    box.appendChild(row);
    scrollToBottom();

    return id;
}

// Loading animation
function addLoadingMessage() {
    const box = document.getElementById("aiChatMessages");
    const id = crypto.randomUUID();

    const row = document.createElement("div");
    row.className = "ai-message-row";
    row.innerHTML = `
        <div class="ai-avatar bot-avatar"><i class="bi bi-robot"></i></div>
        <div class="ai-bubble ai-assistant" id="msg-${id}">
            <div class="ai-loading-dots">
                <span></span><span></span><span></span>
            </div>
        </div>
    `;

    box.appendChild(row);
    scrollToBottom();
    return id;
}

// update loading → reply
function updateMessage(id, newText) {
    const div = document.getElementById("msg-" + id);
    if (div) {
        div.innerHTML = `<div class="ai-content">${renderMarkdown(newText)}</div>`;
    }
}

// ================= SCROLL =================
function scrollToBottom() {
    const box = document.querySelector("#aiChatModal .modal-body");
    if (box) box.scrollTop = box.scrollHeight;
}

// ================= LOAD KHI MỞ MODAL =================
const aiChatModalEl = document.getElementById("aiChatModal");
if (aiChatModalEl) {
    aiChatModalEl.addEventListener("shown.bs.modal", function () {
        loadAIHistory();

        // delay nhẹ để chắc chắn DOM render xong
        setTimeout(() => {
            scrollToBottom();
        }, 50);
    });
}