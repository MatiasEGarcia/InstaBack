package com.instaJava.instaJava.config.filter;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instaJava.instaJava.dto.response.ResMessage;
import com.instaJava.instaJava.service.InvTokenService;
import com.instaJava.instaJava.service.JwtService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;
	private final InvTokenService invTokenService;

	/**
	 * 
	 * Check if in the request is there a header "Authorization", if there is get the jwt inside,
	 * else continue to next filter.
	 * Check if token exist in database as token invalidated in a logout or not. 
	 * if is invalidated respond Forbidden.else get pertinent info from jwt.
	 * If token is expired respond Forbidden.
	 * 
	 * @param request. request object sended by the client.
	 * @param response . response object.
	 * @param filterChain. chain of filters.
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		final String authHeader = request.getHeader("Authorization");
		final String jwt;
		final String username;
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response); // will return forbidden unless the path is /auth/**
			return;
		}
		jwt = authHeader.substring(7);
		//Now I have to check if this token is invalidated from a logout user
		if(invTokenService.existByToken(jwt)) {
			response.setStatus(FORBIDDEN.value());
			new ObjectMapper().writeValue(response.getOutputStream(), new ResMessage("Token invalidated"));
		}
		
		try {
			username = jwtService.extractUsername(jwt);
			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				// we need to autenticate
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);
				if (jwtService.isTokenValid(jwt, userDetails)) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
							null, userDetails.getAuthorities());
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}

			}
			filterChain.doFilter(request, response);
		} catch (ExpiredJwtException e) {
			response.setHeader("error", e.getMessage());
			response.setStatus(FORBIDDEN.value());
			Map<String, String> error = new HashMap<>();
			error.put("error_exception_message", e.getMessage());
			error.put("error_messages", "Token invalidated");
			response.setContentType(APPLICATION_JSON_VALUE);
			new ObjectMapper().writeValue(response.getOutputStream(), error);
		}
	}

}
