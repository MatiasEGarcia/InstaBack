package com.instaJava.instaJava.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.instaJava.instaJava.service.UserServiceImpl;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

	private final UserServiceImpl userServiceImpl;
	
	/**
	 * Set userDetailsService and password encoder for DaoAuthenticationProvider. 
	 * 
	 * @return DaoAuthenticationProvider.
	 */
	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userServiceImpl);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}
	
	/**
	 * Bean to get BCryptPasswordEncoder and encode passwords.
	 * 
	 * @return BCryptPasswordEncoder.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	/**
	 * To get AuthentitcationManager
	 * 
	 * @param config . AuthenticationConfiguration
	 * @return AuthenticationManager.
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
		return config.getAuthenticationManager();
	}
	
	/**
	 * Bean to get UTC Time clock.
	 * 
	 * @return UTC clock.
	 */
	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}
}
