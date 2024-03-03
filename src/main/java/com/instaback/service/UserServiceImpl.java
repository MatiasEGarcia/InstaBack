package com.instaback.service;

import java.util.Base64;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.instaback.dao.PersonalDetailsDao;
import com.instaback.dao.UserDao;
import com.instaback.dto.PageInfoDto;
import com.instaback.entity.PersonalDetails;
import com.instaback.entity.User;
import com.instaback.exception.InvalidImageException;
import com.instaback.exception.RecordNotFoundException;
import com.instaback.util.MessagesUtils;
import com.instaback.util.PageableUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService, UserService {

	private final UserDao userDao;
	private final PersonalDetailsDao personalDetailsDao;
	private final MessagesUtils messUtils;
	private final PageableUtils pagUtils;

	
	@Override
	@Transactional
	public User save(User user) {
		if (user == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		return userDao.save(user);
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userDao.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException(messUtils.getMessage("user.username-not-found"));
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
			throw new InvalidImageException(messUtils.getMessage("generic.image-base-64"), HttpStatus.BAD_REQUEST, e);
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
	public PersonalDetails getPersonalDetailsByUser() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		PersonalDetails perDet = user.getPersonalDetails();
		if (perDet == null)
			throw new RecordNotFoundException(messUtils.getMessage("perDet.not-found"), HttpStatus.NOT_FOUND);
		return perDet;
	}

	@Override
	@Transactional
	public PersonalDetails savePersonalDetails(String name, String lastname, byte age, String email) {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		PersonalDetails pDetails = new PersonalDetails(null, name, lastname, age, email, user);
		pDetails = personalDetailsDao.save(pDetails);
		return pDetails;
	}

	@Override
	@Transactional
	public User changeVisible() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		user.setVisible(user.isVisible() ? false : true);
		return userDao.save(user);
	}

	@Override
	@Transactional(readOnly = true)
	public User findById(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		return userDao.findById(id).orElseThrow(
				() -> new RecordNotFoundException(messUtils.getMessage("user.not-found"), HttpStatus.NOT_FOUND));
	}

	@Override
	@Transactional(readOnly = true)
	public User getOneUserManyConditions(Specification<User> spec) {
		if (spec == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		User user = userDao.findOne(spec).orElseThrow(() -> new RecordNotFoundException(messUtils.getMessage("user.not-found"),
						HttpStatus.NOT_FOUND));
		return user;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<User> getManyUsersManyConditions(PageInfoDto pageInfoDto, Specification<User> spec) {
		if (spec == null || pageInfoDto == null || pageInfoDto.getSortDir() == null
				|| pageInfoDto.getSortField() == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		Page<User> page = userDao.findAll(spec, pagUtils.getPageable(pageInfoDto));
		if (!page.hasContent()) {
			throw new RecordNotFoundException(messUtils.getMessage("user.group-not-found"), HttpStatus.NO_CONTENT);
		}
		return page;
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
	public List<User> getByUsernameIn(Set<String> usernameList) {
		if (usernameList == null || usernameList.isEmpty())
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		List<User> userList = userDao.findByUsernameIn(usernameList);
		if (userList.isEmpty()) {
			throw new RecordNotFoundException(messUtils.getMessage("user.group-not-found"), HttpStatus.NO_CONTENT);
		}
		return userList;
	}
}
