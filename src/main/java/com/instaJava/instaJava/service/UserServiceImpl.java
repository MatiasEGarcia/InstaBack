package com.instaJava.instaJava.service;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dao.UserDao;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.exception.ImageException;
import com.instaJava.instaJava.util.ImageUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService,UserService{

	private final UserDao userDao;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userDao.findByUsername(username);
		if(user == null) {
			throw new UsernameNotFoundException("Username not found");
		}
		return user;
	}


	@Override
	@Transactional
	public void updateImage(MultipartFile file){
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		try {
			user.setImage(ImageUtils.compressImage(file.getBytes()));
		}catch(IOException e) {
			throw new ImageException(e);
		}
		userDao.save(user);
	}


	@Override
	@Transactional(readOnly = true)
	public byte[] getImage(){
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return ImageUtils.decompressImage(user.getImage());
	}
	
	

}
