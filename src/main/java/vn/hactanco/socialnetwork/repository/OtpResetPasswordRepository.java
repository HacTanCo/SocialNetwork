package vn.hactanco.socialnetwork.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hactanco.socialnetwork.model.OtpResetPassword;
import vn.hactanco.socialnetwork.model.User;

@Repository
public interface OtpResetPasswordRepository extends JpaRepository<OtpResetPassword, Long> {

	Optional<OtpResetPassword> findTopByUserAndOtpCodeAndIsUsedFalseAndExpiredAtAfter(User user, String otpCode,
			Instant now);

}