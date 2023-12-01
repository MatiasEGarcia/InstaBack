package com.instaJava.instaJava.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.PersonalDetailsDto;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.dto.response.UserGeneralInfoDto;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.exception.InvalidImageException;
import com.instaJava.instaJava.exception.RecordNotFoundException;

public interface UserService {

	/**
	 * Save User.
	 * 
	 * @param user. User object to be saved
	 * @return UserDto object with User which was saved in database info.
	 * @throws IllegalArgumentException if @param user is null.
	 */
	User save(User user);

	/**
	 * Update image of the authenticated user.
	 * 
	 * @param file. Image to save.
	 * @throws InvalidImageException if there was an error getting bytes of the @param
	 *                        image.
	 */
	void updateImage(MultipartFile file);

	/**
	 * Get the authenticated user image.
	 * 
	 * @return String base64 of the image.
	 */
	String getImage();

	/**
	 * Get personal details by authenticated user.
	 * @return PersonalDetailsDto with PersonalDetails info.
	 * @throws RecordNotFoundException if there is not a PersonalDetails record associated with authentication user.
	 */
	PersonalDetailsDto getPersonalDetailsByUser();


	/**
	 * Save PersonalDetails record with authenticated user associated.
	 * 
	 * @return PersonalDetailsDto with PersonalDetial record saved info.
	 * @throws IllegalArgumentException if personalDetailsDto is null.
	 */
	PersonalDetailsDto savePersonalDetails(PersonalDetailsDto personalDetailsDto);

	/**
	 * Change the visible state of the User. public or private / true or false
	 * 
	 * @return UserDto object with User info updated.
	 */
	UserDto changeVisible();

	/**
	 * Method to get authenticated user info.
	 */
	UserDto getByPrincipal();

	/**
	 * Get a User record by id.
	 * 
	 * @param id. id of the user record wanted.
	 * @return UserDto object with User record info.
	 * @throws IllegalArgumentException if @param id is null.
	 * @throws RecordNotFoundException if none user record was found.
	 */
	UserDto getById(Long id);

	/**
	 * Method to get a list of users by it's username.
	 * @param usernameList - list of username
	 * @throws IllegalArgumentException if usernameList is null
	 * @throws RecordNotFoundException if none user was found.
	 * @return List of users. 
	 */
	List<User> getByUsernameIn(List<String> usernameList);

	/**
	 * 
	 * Get a only one User record by only one condition ( can't be by password)
	 * 
	 * @param reqSearch. Object necessary to get a specficiation and do the
	 *                   research.
	 * @return UserDto object with User found info.
	 * @throws IllegalArgumentException if @param reqSearch is null.
	 * @throws RecordNotFoundException if no user was found.
	 */
	UserDto getOneUserOneCondition(ReqSearch reqSearch);

	/**
	 * Get only one User record by many conditions (can't be by password)
	 * 
	 * @param reqSearchList. Object that contain collection of ReqSearch objects and
	 *                       a GlobalOperator to define how combine all the
	 *                       conditions.
	 * @return UserDto object with User found info.
	 * @throws IllegalArgumentException if @param reqSearchList is null
	 * @throws RecordNotFoundException if no user was found.
	 */
	UserDto getOneUserManyConditions(ReqSearchList reqSearchList);

	/**
	 * Get many User records by one condition(can't be password).
	 * 
	 * @param pageInfoDto, It has pagination info.
	 * @param reqSearch.   condition
	 * @return ResPaginationG with all users info and pagination info.
	 * @throws IllegalArgumentException if @param reqSearch or @param pageInfoDto or
	 *                                  pageInfoDto.sortDir or pageInfoDto.sortField
	 *                                  null.
	 * @throws RecordNotFoundException if no user was found.
	 */
	ResPaginationG<UserDto> getManyUsersOneCondition(PageInfoDto pageInfoDto, ReqSearch reqSearch);

	/**
	 * Get many User records by many conditions(can't be password).
	 * 
	 * @param pageInfoDto,   It has pagination info.
	 * @param reqSearchList. Collection of conditions
	 * @return ResPaginationG with all users info and pagination info.
	 * @throws IllegalArgumentException if @param reqSearchList or @param
	 *                                  pageInfoDto or pageInfoDto.sortDir or
	 *                                  pageInfoDto.sortField null.
	 * @throws RecordNotFoundException if no user was found.
	 */
	ResPaginationG<UserDto> getManyUsersManyConditions(PageInfoDto pageInfoDto, ReqSearchList reqSearchList);

	/**
	 * Ask if exists User record by username.
	 * 
	 * @param username. User's username wanted to find.
	 * @return true if User record exists, else false.
	 * @throws IllegalArgumentException if @param username is null.
	 */
	boolean existsByUsername(String username);

	/**
	 * Ask if exists User record by one condition(can't be password).
	 * 
	 * @param reqSearch. conditions details
	 * @return true if User record exists, else false.
	 * @throws IllegalArgumentException if @param reqSearch is null.
	 */
	boolean existsOneCondition(ReqSearch reqSearch);

	/**
	 * Ask if exists User record by many conditions(can't be password).
	 * 
	 * @param reqSearchList. Collection of conditions details
	 * @return true if User record exists, else false.
	 * @throws IllegalArgumentException if @param reqSearchList is null.
	 */
	boolean existsManyConditions(ReqSearchList reqSearchList);
	
	/**
	 * Getting all general user info, like username, user's image, user's publication number , followers, followed, etc
	 * @param userId
	 * @return
	 */
	UserGeneralInfoDto getGeneralUserInfoByUserId(Long userId);
	
}
