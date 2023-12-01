package com.instaJava.instaJava.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
import com.instaJava.instaJava.exception.InvalidTokenException;
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
	 * @param reqUserRegistration. object that contain data to the registration of
	 *                             the user.
	 * @throws IllegalArgumentException if @param reqUserRegistration is null
	 * @throws AlreadyExistsException   if the reqUserRegistration.username is
	 *                                  already used by another user.
	 * @return ResAuthToken object which contains tokens for later requests.
	 */
	@Transactional
	public ResAuthToken register(ReqUserRegistration reqUserRegistration) {
		if (reqUserRegistration == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		User user;
		String token;
		String refreshToken;
		if (userService.existsByUsername(reqUserRegistration.getUsername()))
			throw new AlreadyExistsException(messUtils.getMessage("user.username-already-exists"),HttpStatus.BAD_REQUEST);
		user = User.builder().username(reqUserRegistration.getUsername())
				.password(passwordEncoder.encode(reqUserRegistration.getPassword())).role(RolesEnum.ROLE_USER).build();
		user = userService.save(user);
		token = jwtService.generateToken(user);
		refreshToken = jwtService.generateRefreshToken(user);
		return ResAuthToken.builder().token(token).refreshToken(refreshToken).build();
	}

	/**
	 * With this the user already register can authenticate and get tokens to do
	 * requests
	 * 
	 * @param reqLogin. object that contain data for the login of the user
	 * @return ResAuthToken object which contains tokens for later requests.
	 * @throws IllegalArgumentException  if @param reqLogin is null.
	 * @throws BadCredentialsException if userName o password are incorrect.
	 */
	@Transactional(readOnly = true)
	public ResAuthToken authenticate(ReqLogin reqLogin) {
		if (reqLogin == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		String token;
		String refreshToken;
		//check user credentials, if there is something wrong then throw BadCredentialsException.
		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(reqLogin.getUsername(), reqLogin.getPassword()));
		UserDetails user = userDetailsService.loadUserByUsername(reqLogin.getUsername());
		token = jwtService.generateToken(user);
		refreshToken = jwtService.generateRefreshToken(user);
		return ResAuthToken.builder().token(token).refreshToken(refreshToken).build();
	}

	/**
	 * 
	 * Get Valid tokens for requests if the user give valid refresh token.
	 * 
	 * @param reqRefreshToken.-  contains old tokens expired or invalidated
	 * @return ResAuthToken  - object with valid tokens.
	 * @throws IllegalArgumentException - if @param reqRefreshToken is null.
	 * @throws InvalidTokenException           - if the the refresh token is expired or
	 *                                  invalidated because was logout.
	 */
	@Transactional(readOnly = true)
	public ResAuthToken refreshToken(ReqRefreshToken reqRefreshToken) {
		if (reqRefreshToken == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		UserDetails userDetails;
		try {
			userDetails = userDetailsService
					.loadUserByUsername(jwtService.extractUsername(reqRefreshToken.getRefreshToken()));
		} catch (ExpiredJwtException e) {
			throw new InvalidTokenException(messUtils.getMessage("client-refreshToken-invalid"),
					HttpStatus.BAD_REQUEST,e);
		}
		if (jwtService.isTokenValid(reqRefreshToken.getRefreshToken(), userDetails)
				&& !invTokenService.existByToken(reqRefreshToken.getRefreshToken())) {
			return ResAuthToken.builder().token(jwtService.generateToken(userDetails))
					.refreshToken(reqRefreshToken.getRefreshToken()) // we return the same refreshToken
					.build();
		} else {
			throw new InvalidTokenException(messUtils.getMessage("client-refreshToken-invalid"),
					HttpStatus.BAD_REQUEST);
		}
	}

}
