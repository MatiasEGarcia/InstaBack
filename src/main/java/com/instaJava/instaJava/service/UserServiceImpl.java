package com.instaJava.instaJava.service;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dao.PersonalDetailsDao;
import com.instaJava.instaJava.dao.UserDao;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.PersonalDetailsDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.entity.PersonalDetails;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.exception.ImageException;
import com.instaJava.instaJava.mapper.PersonalDetailsMapper;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService,UserService{

	private final UserDao userDao;
	private final PersonalDetailsDao personalDetailsDao;
	private final MessagesUtils messUtils;
	private final PersonalDetailsMapper personalDetailsMapper;
	private final SpecificationService<User> specService;
	private final PageableUtils pagUtils;

	/**
	 * Save User.
	 * 
	 * @param user. User object to be saved
	 * @return User object that was saved in database.
	 * @throws IllegalArgumentException if @param user is null.
	 */
	@Override
	@Transactional
	public User save(User user) {
		if(user == null) throw new IllegalArgumentException("exception.argument-not-null");
		return userDao.save(user);
	}
	
	/**
	 * 
	 * Get user by User.username.
	 * 
	 * @param username. value of the User record to search.
	 * @return UserDetails object.
	 * @throws UsernameNotFoundException if user with @param username no exists.
	 */
	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userDao.findByUsername(username);
		if(user == null) {
			throw new UsernameNotFoundException(messUtils.getMessage("excepcion.username-not-found"));
		}
		return user;
	}


	/**
	 * Update image of the authenticated user.
	 * 
	 * @param file. Image to save.
	 * @throws ImageException if there was an error getting bytes of the @param image.
	 */
	@Override
	@Transactional
	public void updateImage(MultipartFile file){
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		try {
			user.setImage(Base64.getEncoder().encodeToString(file.getBytes()));
		}catch(Exception e) {
			throw new ImageException(e);
		}
		userDao.save(user);
	}


	/**
	 * Get the authenticated user image.
	 * 
	 * @return String base64 of the image.
	 */
	@Override
	@Transactional(readOnly = true)
	public String getImage(){
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return user.getImage();
	}

	//I have to test with postman this
	/**
	 * Get personal details by authenticated user.
	 *
	 * @return PersonalDetails optional. empty if there is no PersonalDetails record associated with authenticated user.
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<PersonalDetails> getPersonalDetailsByUser() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		PersonalDetails perDet = user.getPersonalDetails();
		if(perDet == null) return Optional.empty();
		return Optional.of(perDet);
	}

	/**
	 * Save PersonalDetails record with authenticated user associated.
	 * @return PersonalDetails record saved.
	 * @throws IllegalArgumentException if personalDetailsDto is null.
	 */

	@Override
	@Transactional
	public PersonalDetails savePersonalDetails(PersonalDetailsDto personalDetailsDto) {
		if(personalDetailsDto == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		PersonalDetails perDet = personalDetailsDao.save(
				personalDetailsMapper
				.personalDetailsDtoAndUserToPersonalDetails(personalDetailsDto, user)
				);
		return perDet;
	}

	/**
	 * Change the visible state of the User. public or private / true or false
	 * 
	 * @return User already updated.
	 */
	@Override
	@Transactional
	public User changeVisible() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		user.setVisible(user.isVisible() ? false : true);
		return userDao.save(user);
	}

	/**
	 * Get a User record by id.
	 * 
	 * @param id. id of the user record wanted.
	 * @return Optional user.
	 * @throws IllegalArgumentException if @param id is null.
	 */
	@Override
	@Transactional(readOnly= true)
	public Optional<User> getById(Long id) {
		if(id == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		return userDao.findById(id);
	}
	
	/**
	 * Get a User record by username.
	 * 
	 * @param username. username of the user record wanted.
	 * @return Optional user.
	 * @throws IllegalArgumentException if @param username is null.
	 */
	@Override
	@Transactional(readOnly= true)
	public Optional<User> getByUsername(String username) {
		if(username == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		User user = userDao.findByUsername(username);
		if(user == null) return Optional.empty();
		return Optional.of(user);
	}
	
	/**
	 * 
	 * Get a only one User record by only one condition ( can't be by password)
	 * 
	 * @param reqSearch. Object necessary to get a specficiation and do the research.
	 * @return Optional user
	 * @throws IllegalArgumentException if @param reqSearch is null.
	 */
	@Override
	@Transactional(readOnly= true)
	public Optional<User> getOneUserOneCondition(ReqSearch reqSearch) {
		if(reqSearch == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		this.passNotAvailableForSearch(reqSearch);
		return userDao.findOne(specService.getSpecification(reqSearch));
	}

	/**
	 * Get only one User record by many conditions (can't be by password)
	 *  
	 * @param reqSearchList. Object that contain collection of ReqSearch objects and a GlobalOperator to define how combine all the conditions.
	 * @return Optional user
	 * @throws IllegalArgumentException if @param reqSearchList is null
	 */
	@Override
	@Transactional(readOnly= true)
	public Optional<User> getOneUserManyConditions(ReqSearchList reqSearchList) {
		if(reqSearchList == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		this.passNotAvailableForSearch(reqSearchList.getReqSearchs());
		return userDao.findOne(specService.getSpecification(reqSearchList.getReqSearchs(),
				reqSearchList.getGlobalOperator()));
	}
	
	/**
	 * Get many User records by one condition(can't be password).
	 * 
	 * @param pageInfoDto, It has pagination info.
	 * @param reqSearch. condition
	 * @return page collection with User records.
	 * @throws IllegalArgumentException if @param reqSearch or @param pageInfoDto or pageInfoDto.sortDir or pageInfoDto.sortField null.
	 */
	@Override
	@Transactional(readOnly= true)
	public Page<User> getManyUsersOneCondition(PageInfoDto pageInfoDto,
			ReqSearch reqSearch) {
		if(reqSearch == null || pageInfoDto == null ||
				pageInfoDto.getSortDir() == null || pageInfoDto.getSortField() == null )throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null-empty"));
		this.passNotAvailableForSearch(reqSearch);
		return userDao.findAll(specService.getSpecification(reqSearch), pagUtils.getPageable(pageInfoDto));
	}

	/**
	 * Get many User records by many conditions(can't be password).
	 * 
	 * @param pageInfoDto, It has pagination info.
	 * @param reqSearchList. Collection of conditions
	 * @return page collection with User records.
	 * @throws IllegalArgumentException if @param reqSearchList or @param pageInfoDto or pageInfoDto.sortDir or pageInfoDto.sortField null.
	 */
	@Override
	@Transactional(readOnly= true)
	public Page<User> getManyUsersManyConditions(PageInfoDto pageInfoDto,
			ReqSearchList reqSearchList) {
		if(reqSearchList == null || pageInfoDto == null ||
				pageInfoDto.getSortDir() == null || pageInfoDto.getSortField() == null )throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null-empty"));
		this.passNotAvailableForSearch(reqSearchList.getReqSearchs());
		return userDao.findAll(specService.getSpecification(reqSearchList.getReqSearchs(), reqSearchList.getGlobalOperator()),pagUtils.getPageable(pageInfoDto));
	}

	/**
	 * Ask if exists User record by username.
	 * 
	 * @param username. User's username wanted to find.
	 * @return true if User record exists, else false.
	 * @throws IllegalArgumentException if @param username is null.
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean existsByUsername(String username) {
		if(username == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		return userDao.existsByUsername(username);
	}
	
	/**
	 * Ask if exists User record by one condition(can't be password).
	 * 
	 * @param reqSearch. conditions details
	 * @return true if User record exists, else false.
	 * @throws IllegalArgumentException if @param reqSearch is null.
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean existsOneCondition(ReqSearch reqSearch) {
		if(reqSearch == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		this.passNotAvailableForSearch(reqSearch);
		return userDao.exists(specService.getSpecification(reqSearch));
	}

	/**
	 * Ask if exists User record by many conditions(can't be password).
	 * 
	 * @param reqSearchList. Collection of conditions details
	 * @return true if User record exists, else false.
	 * @throws IllegalArgumentException if @param reqSearchList is null.
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean existsManyConditions(ReqSearchList reqSearchList) {
		if(reqSearchList == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		this.passNotAvailableForSearch(reqSearchList.getReqSearchs());
		return userDao.exists(specService.getSpecification(reqSearchList.getReqSearchs(), reqSearchList.getGlobalOperator()));
	}

	/**
	 * Method to test conditions and see if there is one for password.
	 * 
	 * @param reqSearch. condition to be tested.
	 * @throws IllegalArgumentException if one of the conditions is for password.
	 */
	private void passNotAvailableForSearch(ReqSearch reqSearch) {
		if(reqSearch.getColumn() == null) {
			return;
		}else if(reqSearch.getColumn().equalsIgnoreCase("password")) throw new IllegalArgumentException(messUtils.getMessage("exception.password-not-searchable"));
	}
	
	/**
	 * Method to test conditions and see if there is one for password.
	 * 
	 * @param searchs. Collections of conditions to be tested.
	 * @throws IllegalArgumentException if one of the conditions is for password.
	 */
	private void passNotAvailableForSearch(List<ReqSearch> searchs) {
		boolean isTherePassowrdColumn = searchs.stream()
				.filter(s -> s.getColumn() != null)
				.anyMatch(s -> s.getColumn().equalsIgnoreCase("password"));
		if(isTherePassowrdColumn) throw new IllegalArgumentException(messUtils.getMessage("exception.password-not-searchable"));
	}
}
