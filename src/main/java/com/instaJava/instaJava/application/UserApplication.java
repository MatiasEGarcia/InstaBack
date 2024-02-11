package com.instaJava.instaJava.application;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.PersonalDetailsDto;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.dto.response.ResImageString;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.dto.response.UserGeneralInfoDto;
import com.instaJava.instaJava.exception.InvalidActionException;

public interface UserApplication {
	
	/**
	 * Get authenticated user info.
	 * @return authenticated user info.
	 */
	UserDto getByPrincipal();
	
	/**
	 * Update image of the authenticated user.
	 * 
	 * @param file. Image to save.
	 */
	void updateImage(MultipartFile file);
	
	/**
	 * Get the authenticated user image.
	 * 
	 * @return ResImageString object with String base64 of the image.
	 */
	ResImageString getImage();
	
	/**
	 * Save PersonalDetails record with authenticated user associated.
	 * 
	 * @return PersonalDetailsDto with PersonalDetial record saved info.
	 */
	PersonalDetailsDto savePersonalDetails(PersonalDetailsDto personalDetailsDto);
	
	/**
	 * Get personal details by authenticated user.
	 * @return PersonalDetailsDto with PersonalDetails info.
	 */
	PersonalDetailsDto getPersonalDetailsByUser();
	
	/**
	 * Change the visible state of the User. public or private / true or false
	 * 
	 * @return UserDto object with User info updated.
	 */
	UserDto changeVisible();
	
	/**
	 * 
	 * Get a only one User record by only one condition ( can't be by password)
	 * 
	 * @param reqSearch. Object necessary to get a specficiation and do the
	 *                   research.
	 * @return User found info.
	 * @throw {@link InvalidActionException} if the column in the request was 'password'.
	 */
	UserDto getOneUserOneCondition(ReqSearch reqSearch);
	
	
	/**
	 * Get only one User record by many conditions (can't be by password)
	 * 
	 * @param reqSearchList. Object that contain collection of ReqSearch objects and
	 *                       a GlobalOperator to define how combine all the
	 *                       conditions.
	 * @return	User info.
	 * @throw {@link InvalidActionException} if the column in the request was 'password'.
	 */
	UserDto getOneUserManyConditions(ReqSearchList reqSearchList);
	
	/**
	 * Get many User records by one condition(can't be password).
	 * 
	 * @param pageInfoDto, It has pagination info.
	 * @param reqSearch.   condition
	 * @return ResPaginationG with all users info and pagination info.
	 * @throw {@link InvalidActionException} if the column in the request was 'password'.
	 */
	ResPaginationG<UserDto> getManyUsersOneCondition( int pageNo, int pageSize, String sortField,
			Direction sortDir, ReqSearch reqSearch);
	
	/**
	 * Get many User records by many conditions(can't be password).
	 * 
	 * @param pageInfoDto,   It has pagination info.
	 * @param reqSearchList. Collection of conditions
	 * @return Page<User> with users and pagination info.
	 * @throw {@link InvalidActionException} if the column in the request was 'password'.
	 */
	ResPaginationG<UserDto> getManyUsersManyConditions(int pageNo, int pageSize, String sortField,
			Direction sortDir,ReqSearchList reqSearchList);
	
	/**
	 * Get general user info, like number of followers,followed, publications and user basic info.
	 * @param id
	 * @return UserGeneralInfoDto info.
	 */
	UserGeneralInfoDto getUserGeneralInfoById(Long id);
	
}
