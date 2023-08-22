package com.instaJava.instaJava.service;


import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dto.request.ReqLogin;
import com.instaJava.instaJava.dto.request.ReqRefreshToken;
import com.instaJava.instaJava.dto.request.ReqUserRegistration;
import com.instaJava.instaJava.dto.response.ResAuthToken;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.exception.AlreadyExistsException;
import com.instaJava.instaJava.exception.TokenException;
import com.instaJava.instaJava.util.MessagesUtils;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final UserDetailsService userDetailsService;
	private final InvTokenService invTokenService;
	private final MessagesUtils messUtils;
 	
	/**
	 * Save a user and return tokens.
	 * 
	 * @param reqUserRegistration. object that contain data to the registration of the user.
	 * @throws IllegalArgumentException if @param reqUserRegistration is null
	 * @throws AlreadyExistsException if the reqUserRegistration.username is already used by another user.
	 * @return ResAuthToken object which contains tokens for later requests.
	 */
	@Transactional
	public ResAuthToken register(ReqUserRegistration reqUserRegistration) {
		if(reqUserRegistration == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		if(userService.existsByUsername(reqUserRegistration.getUsername())) throw new AlreadyExistsException(messUtils.getMessage("excepcion.username-already-exists"));
		User user = User.builder()
				.username(reqUserRegistration.getUsername())
				.password(passwordEncoder.encode(reqUserRegistration.getPassword()))
				.role(RolesEnum.ROLE_USER)
				.build();
		user = userService.save(user);
		String token = jwtService.generateToken(user);
		String refreshToken = jwtService.generateRefreshToken(user);
		return ResAuthToken.builder().token(token).refreshToken(refreshToken).build();
	}

	/**
	 * With this the user already register can authenticate and get tokens to do requests
	 * 
	 * @param reqLogin. object that contain data for the login of the user
	 * @return ResAuthToken object which contains tokens for later requests.
	 * @throws IllegalArgumentException if @param reqLogin is null.
	 * @throws UsernameNotFoundException if user no exists.
	 */
	@Transactional(readOnly = true)
	public ResAuthToken authenticate(ReqLogin reqLogin) {
		if(reqLogin == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		Optional<User> user = userService.getByUsername(reqLogin.getUsername());
		if(user.isEmpty()) throw new UsernameNotFoundException(messUtils.getMessage("excepcion.username-not-found"));
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						reqLogin.getUsername(), 
						reqLogin.getPassword())
				);
		String token = jwtService.generateToken(user.get());
		String refreshToken = jwtService.generateRefreshToken(user.get());
		return ResAuthToken.builder().token(token).refreshToken(refreshToken).build();
	}

	/**
	 * 
	 * Get Valid tokens for requests if the user give valid refresh token.
	 * 
	 * @param reqRefreshToken. contains old tokens expired or invalidated
	 * @return ResAuthToken object with valid tokens.
	 * @throws IllegalArgumentException if @param reqRefreshToken is null.
	 * @throws TokenException if the the refresh token is expired or invalidated because was logout.
	 */
	@Transactional(readOnly = true)
	public ResAuthToken refreshToken(ReqRefreshToken reqRefreshToken) {
		if(reqRefreshToken == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		UserDetails userDetails;
		try {
			userDetails = userDetailsService.loadUserByUsername(jwtService.extractUsername(reqRefreshToken.getRefreshToken()));
		} catch (ExpiredJwtException e) {
			throw new TokenException(messUtils.getMessage("exception.refreshToken-invalid"));
		}
		if(jwtService.isTokenValid(reqRefreshToken.getRefreshToken(), userDetails)
				&& !invTokenService.existByToken(reqRefreshToken.getRefreshToken())) {
			 return ResAuthToken.builder()
					.token(jwtService.generateToken(userDetails))
					.refreshToken(reqRefreshToken.getRefreshToken()) //we return the same refreshToken
					.build();
		}else {
			throw new TokenException(messUtils.getMessage("exception.refreshToken-invalid"));
		}
	}
	
	
	
	
	
	
	
}
