package com.instaJava.instaJava.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.PersonalDetailsDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.entity.PersonalDetails;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.exception.ImageException;

public interface UserService {

	/**
	 * Save User.
	 * 
	 * @param user. User object to be saved
	 * @return User object that was saved in database.
	 * @throws IllegalArgumentException if @param user is null.
	 */
	User save(User user);

	/**
	 * Update image of the authenticated user.
	 * 
	 * @param file. Image to save.
	 * @throws ImageException if there was an error getting bytes of the @param
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
	 *
	 * @return PersonalDetails optional. empty if there is no PersonalDetails record
	 *         associated with authenticated user.
	 */
	Optional<PersonalDetails> getPersonalDetailsByUser();


	/**
	 * Save PersonalDetails record with authenticated user associated.
	 * 
	 * @return PersonalDetails record saved.
	 * @throws IllegalArgumentException if personalDetailsDto is null.
	 */
	PersonalDetails savePersonalDetails(PersonalDetailsDto personalDetailsDto);

	/**
	 * Change the visible state of the User. public or private / true or false
	 * 
	 * @return User already updated.
	 */
	User changeVisible();

	/**
	 * Method to get authenticated user info.
	 */
	User getByPrincipal();

	/**
	 * Get a User record by id.
	 * 
	 * @param id. id of the user record wanted.
	 * @return Optional user.
	 * @throws IllegalArgumentException if @param id is null.
	 */
	Optional<User> getById(Long id);

	/**
	 * Method to get a list of users by it's username.
	 * @param usernameList - list of username
	 * @throws IllegalArgumentException if usernameList is null
	 * @throws RecordNotFoundException if none user was found.
	 * @return List of users. can be empty.
	 */
	List<User> getByUsernameIn(List<String> usernameList);

	/**
	 * Get a User record by username.
	 * 
	 * @param username. username of the user record wanted.
	 * @return Optional user.
	 * @throws IllegalArgumentException if @param username is null.
	 */
	Optional<User> getByUsername(String username);

	/**
	 * 
	 * Get a only one User record by only one condition ( can't be by password)
	 * 
	 * @param reqSearch. Object necessary to get a specficiation and do the
	 *                   research.
	 * @return Optional user
	 * @throws IllegalArgumentException if @param reqSearch is null.
	 */
	Optional<User> getOneUserOneCondition(ReqSearch reqSearch);

	/**
	 * Get only one User record by many conditions (can't be by password)
	 * 
	 * @param reqSearchList. Object that contain collection of ReqSearch objects and
	 *                       a GlobalOperator to define how combine all the
	 *                       conditions.
	 * @return Optional user
	 * @throws IllegalArgumentException if @param reqSearchList is null
	 */
	Optional<User> getOneUserManyConditions(ReqSearchList reqSearchList);

	/**
	 * Get many User records by one condition(can't be password).
	 * 
	 * @param pageInfoDto, It has pagination info.
	 * @param reqSearch.   condition
	 * @return page collection with User records.
	 * @throws IllegalArgumentException if @param reqSearch or @param pageInfoDto or
	 *                                  pageInfoDto.sortDir or pageInfoDto.sortField
	 *                                  null.
	 */
	Page<User> getManyUsersOneCondition(PageInfoDto pageInfoDto, ReqSearch reqSearch);

	/**
	 * Get many User records by many conditions(can't be password).
	 * 
	 * @param pageInfoDto,   It has pagination info.
	 * @param reqSearchList. Collection of conditions
	 * @return page collection with User records.
	 * @throws IllegalArgumentException if @param reqSearchList or @param
	 *                                  pageInfoDto or pageInfoDto.sortDir or
	 *                                  pageInfoDto.sortField null.
	 */
	Page<User> getManyUsersManyConditions(PageInfoDto pageInfoDto, ReqSearchList reqSearchList);

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
}
