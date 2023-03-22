package com.instaJava.instaJava.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.UserDao;
import com.instaJava.instaJava.dto.request.ReqLogin;
import com.instaJava.instaJava.dto.request.ReqRefreshToken;
import com.instaJava.instaJava.dto.request.ReqUserRegistration;
import com.instaJava.instaJava.dto.response.ResAuthToken;
import com.instaJava.instaJava.entity.RolesEnum;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.exception.AlreadyExistsException;
import com.instaJava.instaJava.exception.InvalidException;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserDao userDao;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final UserDetailsService userDetailsService;
	private final InvTokenService invTokenService;
	private final MessagesUtils messUtils;
 	
	@Transactional
	public ResAuthToken register(ReqUserRegistration reqUserRegistration) {
		if(reqUserRegistration == null) throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument-not-null"));
		if(userDao.existsByUsername(reqUserRegistration.getUsername())) throw new AlreadyExistsException(messUtils.getMessage("exepcion.username-already-exists"));
		User user = User.builder()
				.username(reqUserRegistration.getUsername())
				.password(passwordEncoder.encode(reqUserRegistration.getPassword()))
				.role(RolesEnum.ROLE_USER)
				.build();
		user = userDao.save(user);
		String token = jwtService.generateToken(user);
		String refreshToken = jwtService.generateRefreshToken(user);
		return ResAuthToken.builder().token(token).refreshToken(refreshToken).build();
	}
	
	@Transactional(readOnly = true)
	public ResAuthToken authenticate(ReqLogin reqLogin) {
		if(reqLogin == null) throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument-not-null"));
		User user = userDao.findByUsername(reqLogin.getUsername());
		if(user == null) throw new UsernameNotFoundException(messUtils.getMessage("exepcion.username-not-found"));
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						reqLogin.getUsername(), 
						reqLogin.getPassword())
				);
		String token = jwtService.generateToken(user);
		String refreshToken = jwtService.generateRefreshToken(user);
		return ResAuthToken.builder().token(token).refreshToken(refreshToken).build();
	}

	@Transactional(readOnly = true)
	public ResAuthToken refreshToken(ReqRefreshToken reqRefreshToken) {
		if(reqRefreshToken == null) throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument-not-null"));
		UserDetails userDetails = userDetailsService.loadUserByUsername(jwtService.extractUsername(reqRefreshToken.getRefreshToken()));
		if(jwtService.isTokenValid(reqRefreshToken.getRefreshToken(), userDetails)
				&& !invTokenService.existByToken(reqRefreshToken.getRefreshToken())) {
			 return ResAuthToken.builder()
					.token(jwtService.generateToken(userDetails))
					.refreshToken(reqRefreshToken.getRefreshToken()) //we return the same refreshToken
					.build();
		}else {
			throw new InvalidException(messUtils.getMessage("exepcion.refreshToken-invalid"));
		}
	}
	
	
	
	
	
	
	
}
