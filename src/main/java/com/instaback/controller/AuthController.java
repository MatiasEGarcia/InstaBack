package com.instaback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.instaback.dto.request.ReqLogin;
import com.instaback.dto.request.ReqRefreshToken;
import com.instaback.dto.request.ReqUserRegistration;
import com.instaback.dto.response.ResAuthToken;
import com.instaback.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	
	/**
	 * User registration.
	 * 
	 * @param reqUserRegistration. Object with necessary data to registration
	 * @return tokens for requestss
	 */
	@PostMapping(value="/register", consumes = "application/json", produces = "application/json")
	public ResponseEntity<ResAuthToken> register(@Valid @RequestBody ReqUserRegistration reqUserRegistration){
		return ResponseEntity.ok(authService.register(reqUserRegistration));
	}
	
	/**
	 * User authentication.
	 * 
	 * @param reqLogin. Object with necessary data to authentication.
	 * @return tokens for requests.
	 */
	@PostMapping(value="/authenticate", consumes = "application/json", produces = "application/json")
	public ResponseEntity<ResAuthToken> authenticate(@Valid @RequestBody ReqLogin reqLogin){
		return ResponseEntity.ok(authService.authenticate(reqLogin));
	}
	
	/**
	 * When token expired but refresh token not, send both and this return new tokens.
	 * 
	 * @param reqRefreshToken. Object with 2 tokens, simple token and refresh token.
	 * @return valid tokens for requests.
	 */
	@PostMapping(value="/refreshToken", consumes = "application/json", produces = "application/json")
	public ResponseEntity<ResAuthToken> refreshToken(@Valid @RequestBody ReqRefreshToken reqRefreshToken){
		return ResponseEntity.ok(authService.refreshToken(reqRefreshToken));
	}
}
