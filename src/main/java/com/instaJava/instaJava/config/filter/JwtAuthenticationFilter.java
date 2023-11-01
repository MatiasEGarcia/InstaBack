package com.instaJava.instaJava.config.filter;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instaJava.instaJava.dto.response.ResErrorMessage;
import com.instaJava.instaJava.service.InvTokenService;
import com.instaJava.instaJava.service.JwtService;
import com.instaJava.instaJava.util.MessagesUtils;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * @author matia
 *
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final MessagesUtils messUtils;
	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;
	private final InvTokenService invTokenService;
    private final List<String> excludedEndpoints = Arrays.asList("/ws/connect/**", "/api/v1/auth/**");

	/**
	 * 
	 * Check if in the request is there a header "Authorization", if there is get
	 * the jwt inside, else continue to next filter. Check if token exist in
	 * database as token invalidated in a logout or not. if is invalidated respond
	 * Forbidden.else get pertinent info from jwt. If token is expired respond
	 * Forbidden.
	 * 
	 * @param request.     request object sended by the client.
	 * @param response     . response object.
	 * @param filterChain. chain of filters.
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		final String authHeader;
		final String jwt;
		final String username;
		
		authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			jwt = authHeader.substring(7);
			// Now I have to check if this token is invalidated from a logout user
			if (invTokenService.existByToken(jwt)) {
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				response.setContentType(APPLICATION_JSON_VALUE);
				new ObjectMapper().writeValue(response.getOutputStream(), ResErrorMessage.builder()
						.error(HttpStatus.UNAUTHORIZED.toString()).message(messUtils.getMessage("mess.auth-token-invalid")).build());
			}

			try {
				username = jwtService.extractUsername(jwt);
				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {//my app is stateless so there shouldn't be any auth before the request
					// we need to autenticate
					UserDetails userDetails = userDetailsService.loadUserByUsername(username);
					//if token is not valid because is expired, then return a forbidden status
					if (jwtService.isTokenValid(jwt, userDetails)) {
						UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
								userDetails, null, userDetails.getAuthorities());
						authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(authToken);
					}else {
						response.setStatus(HttpStatus.UNAUTHORIZED.value());
						response.setContentType(APPLICATION_JSON_VALUE);
						new ObjectMapper().writeValue(response.getOutputStream(), ResErrorMessage.builder()
								.error(HttpStatus.UNAUTHORIZED.toString()).message(messUtils.getMessage("mess.auth-token-expired"))
								.build());
					}

				}
				filterChain.doFilter(request, response);
			} catch (ExpiredJwtException e) {
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				response.setContentType(APPLICATION_JSON_VALUE);
				new ObjectMapper().writeValue(response.getOutputStream(), ResErrorMessage.builder()
						.error(HttpStatus.UNAUTHORIZED.toString()).message(messUtils.getMessage("mess.auth-token-expired"))
						.details(Map.of("exception_message", e.getMessage()))
						.build());
			}
			
		} else {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.setContentType(APPLICATION_JSON_VALUE);
			new ObjectMapper().writeValue(response.getOutputStream(), ResErrorMessage.builder()
					.error(HttpStatus.UNAUTHORIZED.toString()).message(messUtils.getMessage("mess.client-not-authenticated")).build());
		}
	}

    /**
    *To check if the request path is one of the paths that not need authentication.
    */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String requestUrl = request.getRequestURI();
		return excludedEndpoints.stream().anyMatch(url -> new AntPathMatcher().match(url, requestUrl));
	}
}
