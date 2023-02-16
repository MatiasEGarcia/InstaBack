package com.instaJava.instaJava.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.request.ReqLogout;
import com.instaJava.instaJava.dto.response.ResMessage;
import com.instaJava.instaJava.service.InvTokenService;
import com.instaJava.instaJava.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserC {
	
	private final InvTokenService invTokenService;
	private final UserService userService;

	@PostMapping("/image")
	public ResponseEntity<byte[]> uploadImage(@RequestParam("img") MultipartFile file){
		userService.updateImage(file);
		return ResponseEntity.status(HttpStatus.OK)
				.contentType(MediaType.valueOf("image/png"))
				.body(userService.getImage());
	}
	
	@GetMapping("/image")
	public ResponseEntity<byte[]> downloadImage(){
		return ResponseEntity.status(HttpStatus.OK)
				.contentType(MediaType.valueOf("image/png"))
				.body(userService.getImage());
	}
	
	@GetMapping("/logout")
	public ResponseEntity<ResMessage> logout(@RequestBody ReqLogout reqLogout){
		invTokenService.invalidateToken(reqLogout.getToken());
		return ResponseEntity.ok().body(new ResMessage("User logout successfully!"));
	}
}
