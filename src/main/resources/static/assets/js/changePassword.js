async function changePassword() {
	const token = document.querySelector('meta[name="_csrf"]').content;
	const header = document.querySelector('meta[name="_csrf_header"]').content;
    const oldPassword = document.getElementById("oldPassword").value.trim();
    const newPassword = document.getElementById("newPassword").value.trim();
    const confirmPassword = document.getElementById("confirmPassword").value.trim();
    const error = document.getElementById("changePassError");

    error.innerText = "";

    if (!oldPassword || !newPassword || !confirmPassword) {
        error.innerText = "Vui lòng nhập đầy đủ thông tin";
        return;
    }

    if (newPassword.length < 6) {
        error.innerText = "Mật khẩu mới phải từ 6 ký tự";
        return;
    }

    if (newPassword !== confirmPassword) {
        error.innerText = "Xác nhận mật khẩu không khớp";
        return;
    }

    const res = await fetch("/change-password", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
			[header]: token
        },
        body: new URLSearchParams({
            oldPassword,
            newPassword
        })
    });

    const msg = await res.text();

    if (!res.ok) {
        error.innerText = msg;
        return;
    }
	// reset input
	document.getElementById("oldPassword").value = "";
	document.getElementById("newPassword").value = "";
	document.getElementById("confirmPassword").value = "";
	alert("Đổi mật khẩu thành công!");
}
