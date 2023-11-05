package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.instaJava.instaJava.dto.request.ReqLogin;
import com.instaJava.instaJava.dto.request.ReqRefreshToken;
import com.instaJava.instaJava.dto.request.ReqUserRegistration;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.exception.AlreadyExistsException;
import com.instaJava.instaJava.exception.TokenException;
import com.instaJava.instaJava.util.MessagesUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock private UserService userService;
	@Mock private PasswordEncoder passwordEncoder;
	@Mock private JwtService jwtService;
	@Mock private AuthenticationManager authenticationManager;
	@Mock private UserDetailsService userDetailsService;
	@Mock private InvTokenService invTokenService;
	@Mock private MessagesUtils messUtils;
	@InjectMocks private AuthService authService;
	
	@Test
	void registerArgNullThrow() {
		assertThrows(IllegalArgumentException.class
				,() -> authService.register(null));
	}

	@Test
	void registerUsernameAlreadyExistThrow() {
		ReqUserRegistration reqUserR = ReqUserRegistration.builder()
				.username("random")
				.password("random")
				.build();
		when(userService.existsByUsername(reqUserR.getUsername())).thenReturn(true);
		assertThrows(AlreadyExistsException.class,() -> authService.register(reqUserR));
	}
	
	@Test
	void register() {
		String token = "token";
		String refreshToken = "refreshToken";
		ReqUserRegistration reqUserR = ReqUserRegistration.builder()
				.username("random")
				.password("random")
				.build();
		User user = User.builder()
				.username(reqUserR.getUsername())
				.password("randomEnconde")
				.role(RolesEnum.ROLE_USER)
				.build();
		when(userService.existsByUsername(reqUserR.getUsername())).thenReturn(false);
		when(passwordEncoder.encode(reqUserR.getPassword())).thenReturn("randomEnconde");
		when(userService.save(user)).thenReturn(user);
		when(jwtService.generateToken(user)).thenReturn(token);
		when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);
		assertNotNull(authService.register(reqUserR));
		verify(userService).save(user);
		verify(jwtService).generateToken(user);
		verify(jwtService).generateRefreshToken(user);
	}

	
	@Test
	void authenticateArgNullThrow() {
		assertThrows(IllegalArgumentException.class
				,() -> authService.authenticate(null));
	}
	
	@Test
	void authenticate() {
		String token = "token";
		String refreshToken = "refreshToken";
		ReqLogin reqLogin = ReqLogin.builder()
				.username("random")
				.password("random")
				.build();
		User user = User.builder()
				.username(reqLogin.getUsername())
				.password("randomEnconde")
				.role(RolesEnum.ROLE_USER)
				.build();
		when(jwtService.generateToken(user)).thenReturn(token);
		when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);
		when(userService.getByUsername(reqLogin.getUsername())).thenReturn(Optional.of(user));
		
		assertNotNull(authService.authenticate(reqLogin));
		
		verify(authenticationManager).authenticate(
				new UsernamePasswordAuthenticationToken(reqLogin.getUsername(),reqLogin.getPassword()));
		verify(jwtService).generateToken(user);
		verify(jwtService).generateRefreshToken(user);
	}
	
	@Test
	void authenticateUserNoExistThrow() {
		ReqLogin reqLogin = ReqLogin.builder()
				.username("random")
				.password("random")
				.build();
		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(reqLogin.getUsername(),reqLogin.getPassword());
		when(authenticationManager.authenticate(authToken)).thenThrow(BadCredentialsException.class);
		assertThrows(BadCredentialsException.class,() -> authService.authenticate(reqLogin));
		verify(authenticationManager).authenticate(authToken);
		verify(userService,never()).getByUsername(reqLogin.getUsername());
		verify(jwtService,never()).generateToken(null);
		verify(jwtService,never()).generateRefreshToken(null);
	}
	
	@Test
	void refreshTokenArgNullThrow() {
		assertThrows(IllegalArgumentException.class
				,() -> authService.refreshToken(null));
	}
	
	@Test
	void refreshTokenInvalidThrow() {
		ReqRefreshToken reqRefreshToken = ReqRefreshToken.builder()
				.token("token")
				.refreshToken("refreshToken")
				.build();
		User user = User.builder().build();
		
		when(jwtService.extractUsername(reqRefreshToken.getRefreshToken())).thenReturn("Mati");
		when(userDetailsService.loadUserByUsername("Mati")).thenReturn(user);
		when(jwtService.isTokenValid(reqRefreshToken.getRefreshToken(), user)).thenReturn(false);
		
		assertThrows(TokenException.class
				,() -> authService.refreshToken(reqRefreshToken));
		
		verify(jwtService).extractUsername(reqRefreshToken.getRefreshToken());
		verify(userDetailsService).loadUserByUsername("Mati");
		verify(jwtService).isTokenValid(reqRefreshToken.getRefreshToken(), user);
		
	}
	
	@Test
	void refreshTokenExistByTokenThrow() {
		ReqRefreshToken reqRefreshToken = ReqRefreshToken.builder()
				.token("token")
				.refreshToken("refreshToken")
				.build();
		User user = User.builder().build();
		
		when(jwtService.extractUsername(reqRefreshToken.getRefreshToken())).thenReturn("Mati");
		when(userDetailsService.loadUserByUsername("Mati")).thenReturn(user);
		when(jwtService.isTokenValid(reqRefreshToken.getRefreshToken(), user)).thenReturn(true);
		when(invTokenService.existByToken(reqRefreshToken.getRefreshToken())).thenReturn(true);
		
		assertThrows(TokenException.class
				,() -> authService.refreshToken(reqRefreshToken));
		
		verify(jwtService).extractUsername(reqRefreshToken.getRefreshToken());
		verify(userDetailsService).loadUserByUsername("Mati");
		verify(jwtService).isTokenValid(reqRefreshToken.getRefreshToken(), user);
		
	}
	
	@Test
	void refreshToken() {
		ReqRefreshToken reqRefreshToken = ReqRefreshToken.builder()
				.token("token")
				.refreshToken("refreshToken")
				.build();
		User user = User.builder().build();
		
		when(jwtService.extractUsername(reqRefreshToken.getRefreshToken())).thenReturn("Mati");
		when(userDetailsService.loadUserByUsername("Mati")).thenReturn(user);
		when(jwtService.isTokenValid(reqRefreshToken.getRefreshToken(), user)).thenReturn(true);
		when(invTokenService.existByToken(reqRefreshToken.getRefreshToken())).thenReturn(false);
		
		assertNotNull(authService.refreshToken(reqRefreshToken));
		
		verify(jwtService).extractUsername(reqRefreshToken.getRefreshToken());
		verify(userDetailsService).loadUserByUsername("Mati");
		verify(jwtService).isTokenValid(reqRefreshToken.getRefreshToken(), user);
		
	}
	
	
	
	
	
	
	
	
}
