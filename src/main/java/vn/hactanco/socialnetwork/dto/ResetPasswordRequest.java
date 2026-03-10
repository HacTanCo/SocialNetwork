package vn.hactanco.socialnetwork.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {
	@NotBlank(message = "Mật khẩu mới không được để trống")
	private String newPassword;

	@NotBlank(message = "Xác nhận mật khẩu không được để trống")
	private String confirmPassword;
}
