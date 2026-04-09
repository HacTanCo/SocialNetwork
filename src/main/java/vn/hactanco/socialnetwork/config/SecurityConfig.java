package vn.hactanco.socialnetwork.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;

import vn.hactanco.socialnetwork.service.UserService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	UserDetailsService userDetailsService(UserService userService) {
		return new CustomUserDetailsService(userService);
	}

	@Bean
	DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider dao = new DaoAuthenticationProvider(userDetailsService);
		dao.setPasswordEncoder(passwordEncoder);
		return dao;
	}

	@Bean
	SpringSessionRememberMeServices rememberMeServices() {
		SpringSessionRememberMeServices rememberMeServices = new SpringSessionRememberMeServices();
		rememberMeServices.setValiditySeconds(60 * 60 * 24 * 7);
		// rememberMeServices.setAlwaysRemember(true);//ignore checkbox
		return rememberMeServices;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// @formatter:off
		String[] WHITELIST = { 
				"/", "/assets/**", "/css/**", 
				"/js/**", "/images/**", "/login", 
				"/register", "/forgot-password",
				"/verify-otp", "/reset-password",
				"/chat/upload"
				};
		http.authorizeHttpRequests((requests) -> requests
				.requestMatchers(WHITELIST).permitAll()
//				.requestMatchers("/user/**").hasRole("ADMIN")
				.anyRequest().authenticated());
		http.formLogin(form -> form
				.loginPage("/login")
				.defaultSuccessUrl("/redirect")
				.failureUrl("/login?error")
				.permitAll());
		// http.exceptionHandling(e -> e.accessDeniedPage("/access-deny"));
		 http.sessionManagement(s ->s
				 .invalidSessionUrl("/login?expired")
				 .maximumSessions(1)
				 .maxSessionsPreventsLogin(false)
				 .expiredUrl("/login?expire"));
		 http.rememberMe(r -> r.rememberMeServices(rememberMeServices()));
//		 http.csrf(c -> c.disable());
		return http.build();
	}
}
