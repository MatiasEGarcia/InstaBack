package com.instaJava.instaJava.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.instaJava.instaJava.dto.request.ReqLogin;
import com.instaJava.instaJava.dto.request.ReqUserRegistration;
import com.instaJava.instaJava.dto.response.ResAuthToken;
import com.instaJava.instaJava.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService service;
	
	@PostMapping("/register")
	public ResponseEntity<ResAuthToken> register(@RequestBody ReqUserRegistration reqUserRegistration){
		return ResponseEntity.ok(service.register(reqUserRegistration));
	}
	
	@PostMapping("/authenticate")
	public ResponseEntity<ResAuthToken> authenticate(@RequestBody ReqLogin reqLogin){
		return ResponseEntity.ok(service.authenticate(reqLogin));
	}
}
