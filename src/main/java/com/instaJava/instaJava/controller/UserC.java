package com.instaJava.instaJava.controller;

import java.util.List;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.application.UserApplication;
import com.instaJava.instaJava.dto.PersonalDetailsDto;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.WebSocketAuthInfoDto;
import com.instaJava.instaJava.dto.request.ReqLogout;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.dto.response.ResImageString;
import com.instaJava.instaJava.dto.response.ResMessage;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.dto.response.UserGeneralInfoDto;
import com.instaJava.instaJava.service.InvTokenService;
import com.instaJava.instaJava.service.WebSocketService;
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
	private final WebSocketService webSocketService;
	private final MessagesUtils messUtils;
	private final UserApplication userApplication;

	/**
	 * Get basic user info from the authenticated user.
	 * 
	 * @return ResponseEntity with basic user info.
	 */
	@GetMapping(value = "/userBasicInfo", produces = "application/json")
	public ResponseEntity<UserDto> getAuthBasicUserInfo() {
		return ResponseEntity.ok(userApplication.getByPrincipal());
	}

	/**
	 * Save an image.
	 * 
	 * @param file. image to save.
	 * @return ResponseEntity with the image saved as base64.
	 */
	@PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/json")
	public ResponseEntity<ResImageString> uploadImage(@RequestPart("img") @NotNull @Image MultipartFile file) {
		userApplication.updateImage(file);
		return ResponseEntity.status(HttpStatus.OK).body(userApplication.getImage());
	}

	/**
	 * Get the User.image from the authenticated user that is saved in database.
	 * 
	 * @return ResponseEntity with the image as base64.
	 */
	@GetMapping(value = "/image", produces = "application/json")
	public ResponseEntity<ResImageString> downloadImage() {
		return ResponseEntity.status(HttpStatus.OK).body(userApplication.getImage());
	}

	/**
	 * Why post? Because I'm creating invTokens and saving them in the db.
	 * 
	 * @param reqLogout. Contain tokens to invalidate.
	 * @return a message to indicate that the logout was successfully.
	 */
	@PostMapping(value = "/logout", consumes = "application/json", produces = "application/json") 
	public ResponseEntity<ResMessage> logout(@Valid @RequestBody ReqLogout reqLogout) {
		invTokenService.invalidateTokens(List.of(reqLogout.getToken(), reqLogout.getRefreshToken()));
		return ResponseEntity.ok().body(new ResMessage(messUtils.getMessage("user.logout-ok")));
	}

	/**
	 * Save personal details by associating them with the authenticated user.
	 * 
	 * @param personalDetailsDto . Object with the info to create a PersonalDetail
	 *                           object and save it in database
	 * @return personalDetails saved
	 */
	@PostMapping(value = "/personalDetails", consumes = "application/json", produces = "application/json")
	public ResponseEntity<PersonalDetailsDto> savePersonalDetails(
			@Valid @RequestBody PersonalDetailsDto personalDetailsDto) {
		return ResponseEntity.ok().body(userApplication.savePersonalDetails(personalDetailsDto));
	}

	/**
	 * Update User.visible attribute.
	 * 
	 * @return user information updated.
	 */
	@PutMapping(value = "/visible", produces = "application/json")
	public ResponseEntity<UserDto> setVisible() {
		return ResponseEntity.ok().body(userApplication.changeVisible());
	}

	/**
	 * Get authenticated user personal details.
	 * 
	 * @return personal details, else a message that nothing was found.
	 */
	@GetMapping(value = "/personalDetails", produces = "application/json")
	public ResponseEntity<PersonalDetailsDto> getPersonalDetails() {
		return ResponseEntity.ok().body(userApplication.getPersonalDetailsByUser());
	}

	/**
	 * Is POST, but the client will use it as a GET to get one user by one
	 * condition.
	 * 
	 * @param reqSearch. object with conditions to user search.
	 * @return user that was found,else a message that there wasn't any that meet
	 *         the conditions.
	 */
	@PostMapping(value = "/searchOne/oneCondition", consumes = "application/json", produces = "application/json")
	public ResponseEntity<UserDto> searchUserWithOneCondition(@Valid @RequestBody ReqSearch reqSearch) {
		return ResponseEntity.ok().body(userApplication.getOneUserOneCondition(reqSearch));
	}

	/**
	 * is POST, but the client will use it as a GET to get one user by many
	 * conditions
	 * 
	 * @param reqSearchList. object with a collection of conditions to user search.
	 * @return user that was found,else a message that there wasn't any that meet
	 *         the conditions.
	 */
	@PostMapping(value = "searchOne/manyConditions", consumes = "application/json", produces = "application/json")
	public ResponseEntity<UserDto> searchUserWithManyConditions(@Valid @RequestBody ReqSearchList reqSearchList) {
		return ResponseEntity.ok().body(userApplication.getOneUserManyConditions(reqSearchList));
	}

	/**
	 * Is POST, but the client will use it as a GET to get many users by one
	 * condition
	 * 
	 * @param reqSearch. object with conditions to user search.
	 * @param pageNo.    For pagination, number of the page.
	 * @param pageSize.  For pagination, size of the elements in the same page.
	 * @param sortField. For pagination, sorted by..
	 * @param sortDir.   In what direction is sorted, asc or desc.
	 * @return paginated users that were found,else a message that there wasn't any
	 *         that meet the conditions.
	 */
	@PostMapping(value = "/searchAll/oneCondition", consumes = "application/json", produces = "application/json")
	public ResponseEntity<ResPaginationG<UserDto>> searchUsersWithOneCondition(@Valid @RequestBody ReqSearch reqSearch,
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "id") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "ASC") Direction sortDir) {
		return ResponseEntity.ok().body(userApplication.getManyUsersOneCondition(Integer.parseInt(pageNo),
				Integer.parseInt(pageSize), sortField, sortDir, reqSearch));
	}

	/**
	 * Is POST, but the client will use it as a GET to get many users by many
	 * conditions
	 * 
	 * @param reqSearchList. object with a collection of conditions to user search.
	 * @param pageNo.        For pagination, number of the page.
	 * @param pageSize.      For pagination, size of the elements in the same page.
	 * @param sortField.     For pagination, sorted by..
	 * @param sortDir.       In what direction is sorted, asc or desc.
	 * @return paginated users that were found,else a message that there wasn't any
	 *         that meet the conditions.
	 */
	@PostMapping(value = "/searchAll/manyConditions", consumes = "application/json", produces = "application/json")
	public ResponseEntity<ResPaginationG<UserDto>> searchUsersWithManyConditions(
			@Valid @RequestBody ReqSearchList reqSearchList,
			@RequestParam(name = "page", defaultValue = "0") String pageNo,
			@RequestParam(name = "pageSize", defaultValue = "20") String pageSize,
			@RequestParam(name = "sortField", defaultValue = "id") String sortField,
			@RequestParam(name = "sortDir", defaultValue = "ASC") Direction sortDir) {
		return ResponseEntity.ok().body(userApplication.getManyUsersManyConditions(Integer.parseInt(pageNo),
				Integer.parseInt(pageSize), sortField, sortDir, reqSearchList));
	}

	@GetMapping(value = "/generalInfoById/{id}")
	public ResponseEntity<UserGeneralInfoDto> getUserGeneralInfoById(@PathVariable("id") Long id) {
		return ResponseEntity.ok().body(userApplication.getUserGeneralInfoById(id));
	}

	/*
	 * FALTA TESTEAR Handler to get webSocket uuidToken.
	 */
	@GetMapping(value = "/webSocketToken", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<WebSocketAuthInfoDto> getWebSocketToken() {
		return ResponseEntity.ok().body(webSocketService.getWebSocketToken());
	}

}
