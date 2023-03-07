package com.instaJava.instaJava.service;

import java.io.IOException;
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
import com.instaJava.instaJava.dto.response.ResUser;
import com.instaJava.instaJava.entity.PersonalDetails;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.exception.ImageException;
import com.instaJava.instaJava.mapper.PersonalDetailsMapper;
import com.instaJava.instaJava.mapper.UserMapper;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService,UserService{

	private final UserDao userDao;
	private final PersonalDetailsDao personalDetailsDao;
	private final MessagesUtils messUtils;
	private final UserMapper userMapper;
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
		}catch(IOException e) {
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
	public PersonalDetailsDto getPersonalDetailsByUser() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		PersonalDetails perDet = personalDetailsDao.findByUser(user);
		return personalDetailsMapper.personalDetailsToPersonalDetailsDto(perDet);
	}


	@Override
	@Transactional
	public PersonalDetailsDto savePersonalDetails(PersonalDetailsDto personalDetailsDto) {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		personalDetailsDao.save(
				personalDetailsMapper
				.personalDetailsDtoAndUserToPersonalDetails(personalDetailsDto, user)
				);
		return this.getPersonalDetailsByUser();
	}


	@Override
	@Transactional(readOnly = true)
	public List<ResUser> findByUsernameLike(String username, int limit) {
		List<User> users = userDao.findByUsernameLike(username, limit);
		if(users == null || users.isEmpty()) {
			return null;
		}
		return userMapper.UserToResUser(users);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
