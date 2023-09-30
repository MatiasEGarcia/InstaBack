package com.instaJava.instaJava.config.filter;

import java.io.IOException;
import java.time.ZoneId;
import java.util.TimeZone;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instaJava.instaJava.dto.response.ResErrorMessage;
import com.instaJava.instaJava.util.MessagesUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TimeZoneFilter extends OncePerRequestFilter {

	private final MessagesUtils messUtils;

	/**
	 * Check if there is a TimeZone header in the request to set a default TimeZone
	 * in the request.
	 * 
	 * @param request     - request object sended by the client.
	 * @param response    - response object.
	 * @param filterChain - chain of filters.
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String zoneIdHeader = request.getHeader("Timezone");
		if (zoneIdHeader == null) {// if client doesn't gave this header
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
			filterChain.doFilter(request, response); // request continues
		} else {
			if (ZoneId.getAvailableZoneIds().contains(zoneIdHeader)) {
				TimeZone.setDefault(TimeZone.getTimeZone(zoneIdHeader));
				filterChain.doFilter(request, response);
			} else {
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				new ObjectMapper().writeValue(response.getOutputStream(),
						ResErrorMessage.builder().error(HttpStatus.BAD_REQUEST.toString())
								.message(messUtils.getMessage("mess.wrong-zone-id-in-header")).details(ZoneId.SHORT_IDS)
								.build());
			}
		}

	}

}
