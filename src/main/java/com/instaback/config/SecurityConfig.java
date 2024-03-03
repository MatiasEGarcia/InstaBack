package com.instaback.config;

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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.instaback.config.filter.JwtAuthenticationFilter;
import com.instaback.config.filter.TimeZoneFilter;
import com.instaback.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableScheduling
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthFilter;
	private final TimeZoneFilter timeZoneFilter;
	private final AuthenticationProvider authenticationProvider;
	private final MessagesUtils messUtils;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.cors().configurationSource(corsConfigurationSource());
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		
		//Matchers permitAll
		RequestMatcher webSocketPermitAllMatcher = new AntPathRequestMatcher("/ws/connect/**");
		RequestMatcher authPermitAllMatcher = new AntPathRequestMatcher("/api/v1/auth/**");
		//Matchers config
		http.authorizeHttpRequests()
			.requestMatchers(new OrRequestMatcher(webSocketPermitAllMatcher,authPermitAllMatcher)).permitAll()
			.anyRequest().authenticated(); //now web socket connect won't have to pass through JwtAuthenticationFilter.	
		
		//Authentication config
		http.authenticationProvider(authenticationProvider);
		http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
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
			corsConf.setAllowedOriginPatterns(Arrays.asList("http://127.0.0.1:5173"));//react app origin
			corsConf.setAllowedMethods(Arrays.asList("*"));
			corsConf.setExposedHeaders(Arrays.asList(messUtils.getMessage("key.header-detail-exception"),"X-Unauthorized-Reason"));// With this now the client can get these headers.
			corsConf.setAllowedHeaders(Arrays.asList("*"));
			corsConf.setAllowCredentials(true);
			return corsConf;
		};
	}
}
