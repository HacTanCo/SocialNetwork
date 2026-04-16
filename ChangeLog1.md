# Authentication
## Register
## Login ( có image minh họa workflow)
## Forgot password
Optional<OtpResetPassword> findTopByUserAndOtpCodeAndIsUsedFalseAndExpiredAtAfter(User user, String otpCode,
			Instant now);
same = SELECT *
        FROM otp_reset_passwords
        WHERE user_id = ?
        AND otp_code = ?
        AND is_used = false
        AND expired_at > ?
        LIMIT 1;

