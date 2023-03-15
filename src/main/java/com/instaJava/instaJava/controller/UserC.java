package com.instaJava.instaJava.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.PersonalDetailsDto;
import com.instaJava.instaJava.dto.request.ReqLogout;
import com.instaJava.instaJava.dto.response.ResImageString;
import com.instaJava.instaJava.dto.response.ResMessage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.mapper.PersonalDetailsMapper;
import com.instaJava.instaJava.mapper.UserMapper;
import com.instaJava.instaJava.service.InvTokenService;
import com.instaJava.instaJava.service.UserService;
import com.instaJava.instaJava.util.MessagesUtils;
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
	private final MessagesUtils messUtils;
	private final PersonalDetailsMapper personalDetailsMapper;
	private final UserMapper userMapper;

	@PostMapping("/image")
	public ResponseEntity<ResImageString> uploadImage(@RequestParam("img") @NotNull @Image MultipartFile file) {
		userService.updateImage(file);
		return ResponseEntity.status(HttpStatus.OK).body(ResImageString.builder()
				.image64(userService.getImage())
				.build());
	}

	@GetMapping("/image")
	public ResponseEntity<String> downloadImage() {
		return ResponseEntity.status(HttpStatus.OK).body(userService.getImage());
	}

	@GetMapping("/logout")
	public ResponseEntity<ResMessage> logout(@Valid @RequestBody ReqLogout reqLogout) {
		List<String> invTokens = new ArrayList<>();
		invTokens.add(reqLogout.getToken());
		invTokens.add(reqLogout.getRefreshToken());
		invTokenService.invalidateTokens(invTokens);
		return ResponseEntity.ok().body(new ResMessage("User logout successfully!"));
	}

	@PostMapping("/personalDetails")
	public ResponseEntity<PersonalDetailsDto> savePersonalDetails(
			@Valid @RequestBody PersonalDetailsDto personalDetailsDto) {
		personalDetailsDto = personalDetailsMapper
				.personalDetailsToPersonalDetailsDto(userService.savePersonalDetails(personalDetailsDto));
		return ResponseEntity.ok().body(personalDetailsDto);
	}

	@GetMapping("/personalDetails")
	public ResponseEntity<PersonalDetailsDto> getPersonalDetails() {
		PersonalDetailsDto personalDetailsDto = personalDetailsMapper
				.personalDetailsToPersonalDetailsDto(userService.getPersonalDetailsByUser());
		return ResponseEntity.ok().body(personalDetailsDto);
	}

	@GetMapping("/like")
	public ResponseEntity<?> getUserForUsernameLike(@RequestParam(name = "username") String username,
			@RequestParam(name = "limit", defaultValue = "100") String limit) {
		List<User> users = userService.findByUsernameLike(username, Integer.parseInt(limit));
		if (users == null) {
			return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.there-no-users")).build();
		}
		return ResponseEntity.ok().body(userMapper.UserToResUser(users));
	}

}
