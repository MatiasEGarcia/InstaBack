package com.instaJava.instaJava.service;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.entity.PersonalDetails;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.exception.ImageException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.mapper.PersonalDetailsMapper;
import com.instaJava.instaJava.mapper.UserMapper;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService, UserService {

	private final UserDao userDao;
	private final PersonalDetailsDao personalDetailsDao;
	private final MessagesUtils messUtils;
	private final PersonalDetailsMapper personalDetailsMapper;
	private final UserMapper userMapper;
	private final SpecificationService<User> specService;
	private final PageableUtils pagUtils;

	
	@Override
	@Transactional
	public User save(User user) {
		if (user == null)
			throw new IllegalArgumentException("exception.argument-not-null");
		return userDao.save(user);
	}

	
	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userDao.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException(messUtils.getMessage("excepcion.username-not-found"));
		}
		return user;
	}

	
	@Override
	@Transactional
	public void updateImage(MultipartFile file) {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		try {
			user.setImage(Base64.getEncoder().encodeToString(file.getBytes()));
		} catch (Exception e) {
			throw new ImageException(e);
		}
		userDao.save(user);
	}

	
	@Override
	@Transactional(readOnly = true)
	public String getImage() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return user.getImage();
	}

	
	@Override
	@Transactional(readOnly = true)
	public Optional<PersonalDetails> getPersonalDetailsByUser() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		PersonalDetails perDet = user.getPersonalDetails();
		if (perDet == null)
			return Optional.empty();
		return Optional.of(perDet);
	}


	@Override
	@Transactional
	public PersonalDetails savePersonalDetails(PersonalDetailsDto personalDetailsDto) {
		if (personalDetailsDto == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		PersonalDetails perDet = personalDetailsDao
				.save(personalDetailsMapper.personalDetailsDtoAndUserToPersonalDetails(personalDetailsDto, user));
		return perDet;
	}

	
	@Override
	@Transactional
	public User changeVisible() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		user.setVisible(user.isVisible() ? false : true);
		return userDao.save(user);
	}

	
	@Override
	public User getByPrincipal() {
		return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	
	@Override
	@Transactional(readOnly = true)
	public UserDto getById(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		Optional<User> optUser = userDao.findById(id);
		if(optUser.isEmpty()) {//tengo que testear este if
			throw new RecordNotFoundException(messUtils.getMessage("excepcion.record-by-id-not-found"),
					"userId" , List.of(id.toString()),HttpStatus.NOT_FOUND);
		}
		return userMapper.userToUserDto(optUser.get());
	}

	
	@Override
	@Transactional(readOnly = true)
	public Optional<User> getByUsername(String username) {
		if (username == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		User user = userDao.findByUsername(username);
		if (user == null)
			return Optional.empty();
		return Optional.of(user);
	}

	
	@Override
	@Transactional(readOnly = true)
	public Optional<User> getOneUserOneCondition(ReqSearch reqSearch) {
		if (reqSearch == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		this.passNotAvailableForSearch(reqSearch);
		return userDao.findOne(specService.getSpecification(reqSearch));
	}

	
	@Override
	@Transactional(readOnly = true)
	public Optional<User> getOneUserManyConditions(ReqSearchList reqSearchList) {
		if (reqSearchList == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		this.passNotAvailableForSearch(reqSearchList.getReqSearchs());
		return userDao.findOne(
				specService.getSpecification(reqSearchList.getReqSearchs(), reqSearchList.getGlobalOperator()));
	}

	
	@Override
	@Transactional(readOnly = true)
	public Page<User> getManyUsersOneCondition(PageInfoDto pageInfoDto, ReqSearch reqSearch) {
		if (reqSearch == null || pageInfoDto == null || pageInfoDto.getSortDir() == null
				|| pageInfoDto.getSortField() == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null-empty"));
		this.passNotAvailableForSearch(reqSearch);
		return userDao.findAll(specService.getSpecification(reqSearch), pagUtils.getPageable(pageInfoDto));
	}

	
	@Override
	@Transactional(readOnly = true)
	public Page<User> getManyUsersManyConditions(PageInfoDto pageInfoDto, ReqSearchList reqSearchList) {
		if (reqSearchList == null || pageInfoDto == null || pageInfoDto.getSortDir() == null
				|| pageInfoDto.getSortField() == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null-empty"));
		this.passNotAvailableForSearch(reqSearchList.getReqSearchs());
		return userDao.findAll(
				specService.getSpecification(reqSearchList.getReqSearchs(), reqSearchList.getGlobalOperator()),
				pagUtils.getPageable(pageInfoDto));
	}

	
	@Override
	@Transactional(readOnly = true)
	public boolean existsByUsername(String username) {
		if (username == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		return userDao.existsByUsername(username);
	}

	
	@Override
	@Transactional(readOnly = true)
	public boolean existsOneCondition(ReqSearch reqSearch) {
		if (reqSearch == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		this.passNotAvailableForSearch(reqSearch);
		return userDao.exists(specService.getSpecification(reqSearch));
	}

	
	@Override
	@Transactional(readOnly = true)
	public boolean existsManyConditions(ReqSearchList reqSearchList) {
		if (reqSearchList == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		this.passNotAvailableForSearch(reqSearchList.getReqSearchs());
		return userDao
				.exists(specService.getSpecification(reqSearchList.getReqSearchs(), reqSearchList.getGlobalOperator()));
	}

	@Override
	@Transactional(readOnly = true)
	public List<User> getByUsernameIn(List<String> usernameList) {
		if (usernameList == null || usernameList.isEmpty())
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null-empty"));
		List<User> userList = userDao.findByUsernameIn(usernameList);
		if(userList.isEmpty()) {
			throw new RecordNotFoundException(messUtils.getMessage("mess.there-no-users"), "username", usernameList, HttpStatus.NO_CONTENT);
		}
		return userList;
	}

	/**
	 * Method to test conditions and see if there is one for password.
	 * 
	 * @param reqSearch. condition to be tested.
	 * @throws IllegalArgumentException if one of the conditions is for password.
	 */
	private void passNotAvailableForSearch(ReqSearch reqSearch) {
		if (reqSearch.getColumn() == null) {
			return;
		} else if (reqSearch.getColumn().equalsIgnoreCase("password"))
			throw new IllegalArgumentException(messUtils.getMessage("exception.password-not-searchable"));
	}

	/**
	 * Method to test conditions and see if there is one for password.
	 * 
	 * @param searchs. Collections of conditions to be tested.
	 * @throws IllegalArgumentException if one of the conditions is for password.
	 */
	private void passNotAvailableForSearch(List<ReqSearch> searchs) {
		boolean isTherePassowrdColumn = searchs.stream().filter(s -> s.getColumn() != null)
				.anyMatch(s -> s.getColumn().equalsIgnoreCase("password"));
		if (isTherePassowrdColumn)
			throw new IllegalArgumentException(messUtils.getMessage("exception.password-not-searchable"));
	}

}
