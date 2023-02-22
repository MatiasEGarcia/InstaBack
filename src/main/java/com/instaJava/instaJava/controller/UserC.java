package com.instaJava.instaJava.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.request.PersonalDetailsDto;
import com.instaJava.instaJava.dto.request.ReqLogout;
import com.instaJava.instaJava.dto.response.ResMessage;
import com.instaJava.instaJava.service.InvTokenService;
import com.instaJava.instaJava.service.UserService;
import com.instaJava.instaJava.validator.Image;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserC {
	
	private final InvTokenService invTokenService;
	private final UserService userService;

	@PostMapping("/image")
	public ResponseEntity<byte[]> uploadImage( @RequestParam("img") @NotNull @Image  MultipartFile file){//I HAVE TO FIND the way to validate this
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
	public ResponseEntity<ResMessage> logout(@Valid @RequestBody ReqLogout reqLogout){
		List<String> invTokens = new ArrayList<>();
		invTokens.add(reqLogout.getToken());
		invTokens.add(reqLogout.getRefreshToken());
		invTokenService.invalidateTokens(invTokens);
		return ResponseEntity.ok().body(new ResMessage("User logout successfully!"));
	}
	
	@PostMapping("/personalDetails")
	public ResponseEntity<PersonalDetailsDto> savePersonalDetails(@Valid @RequestBody PersonalDetailsDto personalDetailsDto){
		return ResponseEntity.ok().body(userService.savePersonalDetails(personalDetailsDto));
	}
	
	@GetMapping("/personalDetails")
	public ResponseEntity<PersonalDetailsDto> getPersonalDetails(){
		return ResponseEntity.ok().body(userService.getPersonalDetailsByUser());
	}
	
	
	
	
}
