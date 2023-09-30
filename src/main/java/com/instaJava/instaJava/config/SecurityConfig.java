package com.instaJava.instaJava.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.instaJava.instaJava.config.filter.JwtAuthenticationFilter;
import com.instaJava.instaJava.config.filter.TimeZoneFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableScheduling
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthFilter;
	private final TimeZoneFilter timeZoneFilter;
	private final AuthenticationProvider authenticationProvider;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf()
			.disable()
			.cors()
			.configurationSource(corsConfigurationSource())
			.and()
			.authorizeHttpRequests()
			.requestMatchers("/api/v1/auth/**")
			.permitAll()
			.anyRequest()
			.authenticated()
			.and()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.authenticationProvider(authenticationProvider)
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(timeZoneFilter, JwtAuthenticationFilter.class);
		return http.build();
	}
	
	/**
	 * Allowing cross-origin requests.
	 * @return WebMvcConfigurer
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		return request -> {
			CorsConfiguration  corsConf =  new CorsConfiguration();
			corsConf.setAllowedOrigins(Arrays.asList("*"));
			corsConf.setAllowedMethods(Arrays.asList("*"));
			corsConf.addExposedHeader("moreInfo"); // With this now the frontend can get the moreInfo header.
			corsConf.setAllowedHeaders(Arrays.asList("*"));
			return corsConf;
		};
	}
}
