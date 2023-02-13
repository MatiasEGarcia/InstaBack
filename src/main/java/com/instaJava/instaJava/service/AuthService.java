package com.instaJava.instaJava.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.UserDao;
import com.instaJava.instaJava.dto.request.ReqLogin;
import com.instaJava.instaJava.dto.request.ReqUserRegistration;
import com.instaJava.instaJava.dto.response.ResAuthToken;
import com.instaJava.instaJava.entity.RolesEnum;
import com.instaJava.instaJava.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserDao userDao;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	
	@Transactional
	public ResAuthToken register(ReqUserRegistration reqUserRegistration) {
		User user = User.builder()
				.username(reqUserRegistration.getUsername())
				.password(passwordEncoder.encode(reqUserRegistration.getPassword()))
				.role(RolesEnum.ROLE_USER)
				.build();
		user = userDao.save(user);
		String token = jwtService.generateToken(user);
		return ResAuthToken.builder().token(token).build();
	}
	
	@Transactional
	public ResAuthToken authenticate(ReqLogin reqLogin) {
		User user = userDao.findByUsername(reqLogin.getUsername());
		if(user == null) {
			throw new UsernameNotFoundException("This username : "+ reqLogin.getUsername() +" not found");
		}
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						reqLogin.getUsername(), 
						reqLogin.getPassword())
				);
		String token = jwtService.generateToken(user);
		
		return ResAuthToken.builder().token(token).build();
	}
}
