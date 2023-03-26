package com.instaJava.instaJava.service;

import java.util.Base64;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dao.PersonalDetailsDao;
import com.instaJava.instaJava.dao.UserDao;
import com.instaJava.instaJava.dto.PersonalDetailsDto;
import com.instaJava.instaJava.entity.PersonalDetails;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.exception.ImageException;
import com.instaJava.instaJava.mapper.PersonalDetailsMapper;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService,UserService{

	private final UserDao userDao;
	private final PersonalDetailsDao personalDetailsDao;
	private final MessagesUtils messUtils;
	private final PersonalDetailsMapper personalDetailsMapper;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userDao.findByUsername(username);
		if(user == null) {
			throw new UsernameNotFoundException(messUtils.getMessage("exepcion.username-not-found"));
		}
		return user;
	}


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


	@Override
	@Transactional(readOnly = true)
	public String getImage(){
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return user.getImage();
	}


	@Override
	@Transactional(readOnly = true)
	public PersonalDetails getPersonalDetailsByUser() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		PersonalDetails perDet = personalDetailsDao.findByUser(user);
		if(perDet == null) throw new IllegalArgumentException(messUtils.getMessage("exepcion.perDet-not-found"));
		return perDet;
	}


	@Override
	@Transactional
	public PersonalDetails savePersonalDetails(PersonalDetailsDto personalDetailsDto) {
		if(personalDetailsDto == null) throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument-not-null"));
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		PersonalDetails perDet = personalDetailsDao.save(
				personalDetailsMapper
				.personalDetailsDtoAndUserToPersonalDetails(personalDetailsDto, user)
				);
		return perDet;
	}


	@Override
	@Transactional(readOnly = true)
	public List<User> findByUsernameLike(String username, int limit) {
		List<User> users = userDao.findByUsernameLike(username, limit);
		if(users.isEmpty()) {
			return null;
		}
		return users;
	}


	@Override
	@Transactional(readOnly= true)
	public boolean existsByUsername(String username) {
		if(username == null) throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument.not.null"));
		return userDao.existsByUsername(username);
	}


	@Override
	@Transactional
	public User changeVisible() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		user.setVisible(user.isVisible() ? false : true);
		return userDao.save(user);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
