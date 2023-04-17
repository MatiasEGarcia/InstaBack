package com.instaJava.instaJava.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.PersonalDetailsDto;
import com.instaJava.instaJava.dto.request.ReqLogout;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.dto.response.ResImageString;
import com.instaJava.instaJava.dto.response.ResMessage;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.dto.response.ResUser;
import com.instaJava.instaJava.entity.PersonalDetails;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.mapper.PersonalDetailsMapper;
import com.instaJava.instaJava.mapper.UserMapper;
import com.instaJava.instaJava.service.InvTokenService;
import com.instaJava.instaJava.service.UserService;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.validator.Image;
import com.instaJava.instaJava.validator.IsEnum;

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
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResImageString.builder().image64(userService.getImage()).build());
	}

	@GetMapping("/image")
	public ResponseEntity<ResImageString> downloadImage() {
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResImageString.builder().image64(userService.getImage()).build());
	}

	// Why post? Because I'm creating invTokens and saving them in the db
	@PostMapping("/logout")
	public ResponseEntity<ResMessage> logout(@Valid @RequestBody ReqLogout reqLogout) {
		List<String> invTokens = new ArrayList<>();
		invTokens.add(reqLogout.getToken());
		invTokens.add(reqLogout.getRefreshToken());
		invTokenService.invalidateTokens(invTokens);
		return ResponseEntity.ok().body(new ResMessage(messUtils.getMessage("mess.successfully-logout")));
	}

	@PostMapping("/personalDetails")
	public ResponseEntity<PersonalDetailsDto> savePersonalDetails(
			@Valid @RequestBody PersonalDetailsDto personalDetailsDto) {
		personalDetailsDto = personalDetailsMapper
				.personalDetailsToPersonalDetailsDto(userService.savePersonalDetails(personalDetailsDto));
		return ResponseEntity.ok().body(personalDetailsDto);
	}

	@PutMapping("/visible")
	public ResponseEntity<ResUser> setVisible() {
		return ResponseEntity.ok().body(userMapper.UserToResUser(userService.changeVisible()));
	}

	
	@GetMapping("/personalDetails")
	public ResponseEntity<PersonalDetailsDto> getPersonalDetails() {
		Optional<PersonalDetails> optPersDetails = userService.getPersonalDetailsByUser();
		if (optPersDetails.isEmpty()) {
			return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.perDet-not-found")).build();
		}
		PersonalDetailsDto personalDetailsDto = personalDetailsMapper
				.personalDetailsToPersonalDetailsDto(optPersDetails.get());
		return ResponseEntity.ok().body(personalDetailsDto);
	}

	
	// is POST, but the client will use it as a GET to get one user by one condition
	@PostMapping("/searchOne/oneCondition")
	public ResponseEntity<ResUser> searchUserWithOneCondition(@Valid @RequestBody ReqSearch reqSearch) {
		Optional<User> optUser = userService.getOneUserOneCondition(reqSearch);
		if (optUser.isEmpty())return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.there-no-users")).build();
		return ResponseEntity.ok().body(userMapper.UserToResUser(optUser.get()));
	}

	// is POST, but the client will use it as a GET to get one user by many conditions
	@PostMapping("searchOne/manyConditions")
	public ResponseEntity<ResUser> searchUserWithManyConditions(@Valid @RequestBody ReqSearchList reqSearchList) {
		Optional<User> optUser = userService.getOneUserManyConditions(reqSearchList);
		if (optUser.isEmpty())
			return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.there-no-users")).build();
		return ResponseEntity.ok().body(userMapper.UserToResUser(optUser.get()));
	}

	// is POST, but the client will use it as a GET to get users by one condition
	@PostMapping("/searchAll/oneCondition")
	public ResponseEntity<ResPaginationG<ResUser>> searchUsersWithOneCondition(@Valid @RequestBody ReqSearch reqSearch,
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "userId") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "asc") @IsEnum(enumSource = Direction.class) String sortDir) {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(Integer.parseInt(pageNo))
				.pageSize(Integer.parseInt(pageSize)).sortField(sortField).sortDir(sortDir).build();
		Page<User> pageUsers = userService.getManyUsersOneCondition(pageInfoDto, reqSearch);
		if (pageUsers.getContent().isEmpty()) {
			return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.there-no-users")).build();
		}
		return ResponseEntity.ok().body(userMapper.pageAndPageInfoDtoToResPaginationG(pageUsers, pageInfoDto));
	}

	// is POST, but the client will use it as a GET to get users by one condition
	@PostMapping("/searchAll/manyConditions")
	public ResponseEntity<ResPaginationG<ResUser>> searchUsersWithManyConditions(@Valid @RequestBody ReqSearchList reqSearchList,
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "userId") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "asc") @IsEnum(enumSource = Direction.class) String sortDir) {
		PageInfoDto pageInfoDto = PageInfoDto.builder().pageNo(Integer.parseInt(pageNo))
				.pageSize(Integer.parseInt(pageSize)).sortField(sortField).sortDir(sortDir).build();
		Page<User> pageUsers = userService.getManyUsersManyConditions(pageInfoDto, reqSearchList);
		if (pageUsers.getContent().isEmpty()) {
			return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.there-no-users")).build();
		}
		return ResponseEntity.ok().body(userMapper.pageAndPageInfoDtoToResPaginationG(pageUsers, pageInfoDto));
	}

}
