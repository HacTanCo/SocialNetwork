package vn.hactanco.socialnetwork.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyOtpRequest {

	@NotBlank(message = "Otp không được để trống")
	// @Size(max = 6, message = "Otp tối đa 6 ký tự")
	@Pattern(regexp = "\\d{6}", message = "Otp phải gồm đúng 6 chữ số")
	private String otp;
}
