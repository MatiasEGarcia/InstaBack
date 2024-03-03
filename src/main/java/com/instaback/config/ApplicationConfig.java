package com.instaback.config;

import java.time.Clock;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.instaback.service.UserServiceImpl;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableCaching
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


	@Bean//needs EnableCaching class annotation
	public CacheManager cacheManager() {
		//implementation of spring cache
		CaffeineCache webSocketCache = new CaffeineCache("webSocketCache",
				Caffeine.newBuilder().expireAfterWrite(30,TimeUnit.SECONDS).build());
		
		//interface provided by the Spring Framework that serves as a central point for managing caches in an application
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Collections.singletonList(webSocketCache));
		return cacheManager;
	}
}
