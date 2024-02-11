package com.instaJava.instaJava.service;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.entity.PersonalDetails;
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
	 * @return PersonalDetails info.
	 * @throws RecordNotFoundException if there is not a PersonalDetails record associated with authentication user.
	 */
	PersonalDetails getPersonalDetailsByUser();


	/**
	 * Save PersonalDetails record with authenticated user associated.
	 * 
	 * @param name - user's name.
	 * @param lastname - user's lastname.
	 * @param age - user's age.
	 * @param email - user's email.
	 * @return PersonalDetial record saved info.
	 */
	PersonalDetails savePersonalDetails(String name, String lastname, byte age, String email);

	/**
	 * Change the visible state of the User. public or private / true or false
	 * 
	 * @return  User info updated.
	 */
	User changeVisible();
	
	/**
	 * Get a User record by id.
	 * 
	 * @param id. id of the user record wanted.
	 * @return User record.
	 * @throws IllegalArgumentException if @param id is null.
	 * @throws RecordNotFoundException if none user record was found.
	 */
	User findById(Long id);
	
	/**
	 * Get only one User record by many conditions (can't be by password)
	 * 
	 * @param spec - specification that have info to search the users.                     
	 * @return	User info.
	 * @throws IllegalArgumentException if @param reqSearchList is null
	 * @throws RecordNotFoundException if no user was found.
	 */
	User getOneUserManyConditions(Specification<User> spec);

	/**
	 * Get many User records by many conditions(can't be password).
	 * 
	 * @param pageInfoDto,   It has pagination info.
	 * @param spec - specification that have info to search the users.
	 * @return Page<User> with users and pagination info.
	 * @throws IllegalArgumentException if @param reqSearchList or @param
	 *                                  pageInfoDto or pageInfoDto.sortDir or
	 *                                  pageInfoDto.sortField null.
	 * @throws RecordNotFoundException if no user was found.
	 */
	Page<User> getManyUsersManyConditions(PageInfoDto pageInfoDto, Specification<User> spec);

	/**
	 * Ask if exists User record by username.
	 * 
	 * @param username. User's username wanted to find.
	 * @return true if User record exists, else false.
	 * @throws IllegalArgumentException if @param username is null.
	 */
	boolean existsByUsername(String username);
	
	/**
	 * Method to get a list of users by it's username.
	 * @param usernameList - list of username
	 * @throws IllegalArgumentException if usernameList is null
	 * @throws RecordNotFoundException if none user was found.
	 * @return List of users. 
	 */
	List<User> getByUsernameIn(Set<String> usernameList);

}
