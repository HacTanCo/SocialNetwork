console.log("commment modal")
let currentPostId = null;

// Mở modal và load comment
function openCommentModal(postId) {
    currentPostId = postId;
    const modal = new bootstrap.Modal(document.getElementById('commentModal'));
    loadComments(postId);
    modal.show();
}
// Gửi comment mới
function submitModalComment() {
    const content = document.getElementById('modal-comment-input').value.trim();
    if (!content || !currentPostId) return;

    fetch(`/comment/create`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        },
        body: JSON.stringify({ postId: currentPostId, content })
    }).then(res => res.json())
	.then(data => {
		if (data.success) {
		    document.getElementById('modal-comment-input').value = '';
		    loadComments(currentPostId);

		    const el = document.querySelector(
		        `.comment-count[data-post-id="${currentPostId}"]`
		    );

		    if (el) {
		        el.innerText = data.commentCount; // 👈 dùng data từ server
		    }
		}
	});
}
function loadComments(postId) {
    fetch(`/comment/post/${postId}`)
        .then(res => res.json())
        .then(data => {
            const list = document.getElementById('modal-comment-list');
            list.innerHTML = '';
			console.log(data)
            data.forEach(c => {
                const div = document.createElement('div');
                div.className = 'mb-2';
				let actions = '';

				if (c.owner) {
				    actions = `
				    <div class="dropdown">
				        <button class="btn p-0 border-0 text-muted" data-bs-toggle="dropdown">
				            <i class="bi bi-three-dots"></i>
				        </button>

				        <ul class="dropdown-menu">
				            <li>
				                <button class="dropdown-item"
				                    onclick="startEdit(${c.id})">
				                    Sửa
				                </button>
				            </li>
				            <li>
				                <button class="dropdown-item text-danger"
				                    onclick="deleteComment(${c.id})">
				                    Xóa
				                </button>
				            </li>
				        </ul>
				    </div>
				    `;
				}
                div.innerHTML = `
				<div class="d-flex gap-2">

				    <!-- AVATAR -->
				    <a href="/profile/${c.userId}">
				        <img src="${c.userAvatar}" 
				             class="rounded-circle" 
				             style="width:32px;height:32px;object-fit:cover;">
				    </a>

				    <div class="flex-grow-1">

				        <!-- Bubble -->
				        <div class="bg-light px-3 py-2 rounded-4 d-inline-block">

				            <!-- USERNAME -->
				            <div class="fw-bold" style="font-size:14px;">
				                <a href="/profile/${c.userId}" 
				                   class="text-dark text-decoration-none">
				                    ${c.userName}
				                </a>
				            </div>

				            <div id="comment-content-${c.id}" style="font-size:14px;">
				                ${c.content}
				            </div>

									 <input type="text" 
									        id="comment-input-${c.id}" 
									        class="form-control form-control-sm d-none mt-1"
									        value="${c.content}">

									 <div id="comment-actions-${c.id}" class="d-none mt-1">
									     <button class="btn btn-sm btn-success"
									             onclick="saveEdit(${c.id})">Lưu</button>
									     <button class="btn btn-sm btn-secondary"
									             onclick="cancelEdit(${c.id})">Hủy</button>
									 </div>
							     </div>

							     <!-- Time -->
								 
								 <div class="mt-1 ms-2 d-flex gap-2">
								     <small class="text-muted">${c.timeAgo}</small>

								     <button class="btn btn-sm btn-link p-0 text-primary"
								             onclick="toggleReplyInput(${c.id})">
								         Trả lời
								     </button>
									 ${actions}
								 </div>
								 
								 
								 <!-- Replies -->
								 <div class="mt-2 ms-4">
								 ${c.replies.map(r => {

								     let replyActions = '';

								     if (r.owner) {
								         replyActions = `
								         <div class="dropdown">
								             <button class="btn p-0 border-0 text-muted" data-bs-toggle="dropdown">
								                 <i class="bi bi-three-dots"></i>
								             </button>

								             <ul class="dropdown-menu">
								                 <li>
								                     <button class="dropdown-item"
								                          onclick="startEdit(${r.id})">
								                         Sửa
								                     </button>
								                 </li>
								                 <li>
								                     <button class="dropdown-item text-danger"
								                         onclick="deleteComment(${r.id})">
								                         Xóa
								                     </button>
								                 </li>
								             </ul>
								         </div>
								         `;
								     }

								     return `
									 <div class="d-flex gap-2 align-items-start mt-2">

									     <!-- AVATAR -->
									     <a href="/profile/${r.userId}">
									         <img src="${r.userAvatar}" 
									              class="rounded-circle"
									              style="width:28px;height:28px;object-fit:cover;">
									     </a>

									     <div class="flex-grow-1">

									         <div class="bg-light px-3 py-2 rounded-4 d-inline-block">

									             <!-- USERNAME -->
									             <div class="fw-bold" style="font-size:14px;">
									                 <a href="/profile/${r.userId}" 
									                    class="text-dark text-decoration-none">
									                     ${r.userName}
									                 </a>
									             </div>

									             <div id="comment-content-${r.id}" style="font-size:14px;">
									                 ${r.content}
									             </div>

												 <input type="text" 
												        id="comment-input-${r.id}" 
												        class="form-control form-control-sm d-none mt-1"
												        value="${r.content}">

												 <div id="comment-actions-${r.id}" class="d-none mt-1">
												     <button class="btn btn-sm btn-success"
												             onclick="saveEdit(${r.id})">Lưu</button>
												     <button class="btn btn-sm btn-secondary"
												             onclick="cancelEdit(${r.id})">Hủy</button>
												 </div>
								             </div>

								             <!-- 👇 THÊM Ở ĐÂY -->
								             <div class="mt-1 ms-2 d-flex gap-2 align-items-center">
								                 <small class="text-muted">${r.timeAgo}</small>
								                 ${replyActions}
								             </div>

								         </div>
								     </div>
								     `;
								 }).join('')}
								 </div>
								 <div class="mt-2 ms-4 d-none" id="reply-input-${c.id}">
								 	<div class="d-flex gap-2">
								 		<input type="text" class="form-control form-control-sm" placeholder="Viết trả lời...">

								 			<button class="btn btn-sm btn-primary" onclick="submitReply(${c.id}, this)">
								 				Gửi
								 			</button>
								 	</div>
								 </div>
							 </div>
                    </div>
                `;

                list.appendChild(div);
            });
        });
}

function submitReply(commentId, btn) {
    const input = btn.previousElementSibling;
    const content = input.value.trim();

    if (!content || !currentPostId) return;

    fetch(`/comment/reply`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        },
        body: JSON.stringify({
            postId: currentPostId,
            commentId: commentId,
            content: content
        })
    })
    .then(res => res.json())
    .then(data => {
		if (data.success) {
		    input.value = '';
		    loadComments(currentPostId);

		    const el = document.querySelector(
		        `.comment-count[data-post-id="${currentPostId}"]`
		    );

		    if (el) {
		        el.innerText = data.commentCount;
		    }
		}
    });
}
function toggleReplyInput(commentId) {
    const el = document.getElementById('reply-input-' + commentId);
    el.classList.toggle('d-none');
}
function openEditCommentModal(commentId, content) {
    document.getElementById('editCommentId').value = commentId;
    document.getElementById('editCommentContent').value = content;

    const modal = new bootstrap.Modal(document.getElementById('editCommentModal'));
    modal.show();
}
function submitEditComment() {
    const commentId = document.getElementById('editCommentId').value;
    const content = document.getElementById('editCommentContent').value.trim();

    if (!content) return;

    fetch(`/comment/update`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        },
        body: JSON.stringify({ commentId, content })
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            bootstrap.Modal.getInstance(document.getElementById('editCommentModal')).hide();
            loadComments(currentPostId);
        }
    });
}
function deleteComment(commentId) {
    if (!confirm("Bạn chắc chắn muốn xóa?")) return;

    fetch(`/comment/delete`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        },
        body: JSON.stringify({ commentId })
    })
    .then(res => res.json())
    .then(data => {
		if (data.success) {
		    loadComments(currentPostId);

		    const el = document.querySelector(
		        `.comment-count[data-post-id="${currentPostId}"]`
		    );

		    if (el) {
		        el.innerText = data.commentCount;
		    }
		}
    });
}
function startEdit(id) {
    document.getElementById(`comment-content-${id}`).classList.add('d-none');
    document.getElementById(`comment-input-${id}`).classList.remove('d-none');
    document.getElementById(`comment-actions-${id}`).classList.remove('d-none');
}
function cancelEdit(id) {
    document.getElementById(`comment-content-${id}`).classList.remove('d-none');
    document.getElementById(`comment-input-${id}`).classList.add('d-none');
    document.getElementById(`comment-actions-${id}`).classList.add('d-none');
}
function saveEdit(id) {
    const input = document.getElementById(`comment-input-${id}`);
    const content = input.value.trim();

    if (!content) return;

    fetch(`/comment/update`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        },
        body: JSON.stringify({
            commentId: id,
            content: content
        })
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            // update UI ngay lập tức
            document.getElementById(`comment-content-${id}`).innerText = content;

            cancelEdit(id);
        }
    });
}
/*
// Load comment + reply từ server (AJAX)
function loadComments(postId) {
    fetch(`/comment/post/${postId}`) // endpoint trả về JSON: comment + reply
        .then(res => res.json())
        .then(data => {
            const list = document.getElementById('modal-comment-list');
            list.innerHTML = '';

            data.forEach(c => {
                const commentDiv = document.createElement('div');
                commentDiv.className = 'comment mb-2';
                let actions = '';
                if (c.isOwner) { // backend phải trả về field isOwner = true nếu comment của user
                    actions = `
					<div class="dropdown">
				       <button class="btn p-0 border-0 text-muted" type="button" data-bs-toggle="dropdown">
							<i class="bi bi-three-dots fs-5"></i>
						</button>

						<ul class="dropdown-menu dropdown-menu-end" style="border: 1px solid #dbdbdb; border-radius: 8px;">

							<li>
								<button class="btn btn-sm btn-link text-warning" onclick="openEditCommentModal(${c.commentId}, '${c.content.replaceAll("'", "\\'")}')">Sửa</button>
							</li>

							<li>
								<button class="btn btn-sm btn-link text-danger" onclick="deleteComment(${c.commentId})">Xóa</button>
							</li>

						</ul>
						</div>
											
				    `;
                }
				

				commentDiv.innerHTML = `
				    <div class="d-flex gap-2 align-items-start">
				        <img src="${c.userAvatar}" 
				             class="rounded-circle" 
				             style="width:32px; height:32px; object-fit:cover;">
				        
				        <div class="flex-grow-1">
				            <!-- Comment chính -->
				            <div class="bg-light px-3 py-2 rounded-4 d-inline-block">
				                <div class="fw-bold">${c.userName}</div>
				                <div>${c.content}</div>
				            </div>

				            <!-- Thời gian + hành động -->
				            <div class="d-flex gap-3 mt-1 ms-2">
				                <small class="text-muted">${c.timeAgo}</small>
				                <button class="btn btn-sm btn-link text-primary p-0" 
				                        onclick="toggleReplyInput(${c.commentId})">
				                    Trả lời
				                </button>
				                ${actions} <!-- nút edit/delete nếu có -->
				            </div>

				            <!-- Danh sách replies -->
				            <div class="mt-3" id="replies-${c.commentId}">
							${c.replies.map(r => {

							    let replyActions = '';

							    if (r.isOwner) {
							        replyActions = `
							        <div class="dropdown">
							            <button class="btn p-0 border-0 text-muted" data-bs-toggle="dropdown">
							                <i class="bi bi-three-dots"></i>
							            </button>

							            <ul class="dropdown-menu dropdown-menu-end">
							                <li>
							                    <button class="dropdown-item"
							                        onclick="openEditCommentModal(${r.commentId}, '${r.content.replaceAll("'", "\\'")}')">
							                        Sửa
							                    </button>
							                </li>
							                <li>
							                    <button class="dropdown-item text-danger"
							                        onclick="deleteComment(${r.commentId})">
							                        Xóa
							                    </button>
							                </li>
							            </ul>
							        </div>
							        `;
							    }

							    return `
							    <div class="d-flex gap-2 align-items-start mt-3">
							        <img src="${r.userAvatar}"
							             class="rounded-circle"
							             style="width:28px; height:28px; object-fit:cover;">
							        
							        <div class="flex-grow-1">

							            <div class="bg-light px-3 py-2 rounded-4 d-inline-block">
							                <div class="fw-bold" style="font-size: 0.95rem;">
							                    ${r.userName}
							                </div>
							                <div style="font-size: 0.95rem;">
							                    ${r.content}
							                </div>
							            </div>
							            
							            <div class="d-flex gap-3 mt-1 ms-2 align-items-center">
							                <small class="text-muted">${r.timeAgo}</small>
							                ${replyActions}
							            </div>

							        </div>
							    </div>
							    `;
							}).join('')}
				            </div>

				            <!-- Input trả lời -->
				            <div class="mt-3 d-none" id="reply-input-${c.commentId}">
				                <div class="d-flex gap-2">
				                    <img src="${c.userAvatar || '/default-avatar.jpg'}" 
				                         class="rounded-circle" 
				                         style="width:28px; height:28px; object-fit:cover; margin-top:4px;">
				                    
				                    <div class="flex-grow-1">
				                        <input type="text" 
				                               class="form-control form-control-sm" 
				                               placeholder="Viết trả lời...">
				                        <button class="btn btn-sm btn-primary mt-2" 
				                                onclick="submitReply(${c.commentId}, this)">
				                            Gửi
				                        </button>
				                    </div>
				                </div>
				            </div>
				        </div>
				    </div>
				`;
                list.appendChild(commentDiv);
            });
        });
}

// Mở/ẩn input reply
function toggleReplyInput(commentId) {
    const inputDiv = document.getElementById('reply-input-' + commentId);
    inputDiv.classList.toggle('d-none');
}



// Gửi reply cho comment
function submitReply(commentId, btn) {
    const input = btn.previousElementSibling;
    const content = input.value.trim();
    if (!content || !currentPostId) return;

    fetch(`/comment/reply`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        },
        body: JSON.stringify({ postId: currentPostId, commentId, content })
    }).then(res => res.json())
        .then(data => {
            if (data.success) {
                input.value = '';
                loadComments(currentPostId);
            }
        });
}
// Mở modal sửa comment
function openEditCommentModal(commentId, content) {
    document.getElementById('editCommentId').value = commentId;
    document.getElementById('editCommentContent').value = content;

    const modal = new bootstrap.Modal(document.getElementById('editCommentModal'));
    modal.show();
}

// Submit update comment
function submitEditComment() {
    const commentId = document.getElementById('editCommentId').value;
    const newContent = document.getElementById('editCommentContent').value.trim();
    if (!newContent) return;

    fetch(`/comment/update`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        },
        body: JSON.stringify({ commentId, content: newContent })
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                const modalEl = document.getElementById('editCommentModal');
                bootstrap.Modal.getInstance(modalEl).hide();
                loadComments(currentPostId); // reload comment list
            } else {
                alert(data.message || 'Cập nhật thất bại');
            }
        });
}
// Xóa comment
function deleteComment(commentId) {
    if (!confirm("Bạn chắc chắn muốn xóa bình luận này?")) return;

    fetch(`/comment/delete`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        },
        body: JSON.stringify({ commentId })
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                loadComments(currentPostId); // reload comment list
            } else {
                alert(data.message || 'Xóa thất bại');
            }
        });
}*/