package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Key;
import java.time.Clock;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.util.MessagesUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

	@Mock private Clock clock;
	@Mock private MessagesUtils messUtils;
	private static final String SECRET_KEY = "7638792F423F4428472B4B6250655368566D597133743677397A244326462948";
	private Key signKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
	private UserDetails userDetails;
	private Long clockMillis = 1679096279455L; //clock millis method always in the test should return this
	private Map<String,Object> extraClaims;
	private String accessToken;
	private String refreshToken;
	@InjectMocks JwtService jwtService;
	
	
	@Test
	void generateTokenWithExtraClaims() {
		extraClaims = new HashMap<>();
		extraClaims.put("someClaim", "someValue");
		userDetails = User.builder()
				.username("x")
				.password("x")
				.role(RolesEnum.ROLE_USER)
				.build();
		accessToken = Jwts.builder()
				.setClaims(extraClaims)
				.setSubject(userDetails.getUsername())
				.setIssuedAt(new Date(clockMillis))
				.setExpiration(new Date(clockMillis + 10 * 60 * 1000))//10 min
				.signWith(signKey, SignatureAlgorithm.HS256)
				.compact();
		when(clock.millis()).thenReturn(clockMillis);
		assertEquals(accessToken, jwtService.generateToken(extraClaims, userDetails));
		verify(clock,times(2)).millis();
	}

	@Test
	void generateTokenWithExtraClaimsArgUserDetailsNullThrow() {
		extraClaims = new HashMap<>();
		extraClaims.put("someClaim", "someValue");
		assertThrows(IllegalArgumentException.class,() -> jwtService.generateToken(extraClaims, userDetails));
		verify(clock,never()).millis();
	}
	
	@Test
	void generateTokenWithExtraClaimsArgExtraClaimsNullThrow() {
		userDetails = User.builder()
				.username("x")
				.password("x")
				.role(RolesEnum.ROLE_USER)
				.build();
		assertThrows(IllegalArgumentException.class,() -> jwtService.generateToken(extraClaims, userDetails));
		verify(clock,never()).millis();
	}
	
	@Test
	void generateRefreshTokenArgumentNull() {
		assertThrows(IllegalArgumentException.class,() -> jwtService.generateRefreshToken(userDetails));
		verify(clock,never()).millis();
	}
	
	@Test
	void generateRefreshToken() {
		userDetails = User.builder()
				.username("x")
				.password("x")
				.role(RolesEnum.ROLE_USER)
				.build();
		refreshToken = Jwts.builder()
				.setSubject(userDetails.getUsername())
				.setExpiration(new Date(clockMillis + 30 * 60 * 1000 ))// 30 min
				.signWith(signKey, SignatureAlgorithm.HS256)
				.compact();
		when(clock.millis()).thenReturn(clockMillis);
		assertEquals(refreshToken,jwtService.generateRefreshToken(userDetails));
		verify(clock).millis();
	}
	
	@Test
	void extractUsernameArgNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> jwtService.extractUsername(accessToken));
	}
	
	@Test
	void extractUsername() {
		extraClaims = new HashMap<>();
		String username = "x";
		accessToken = Jwts.builder()
				.setClaims(extraClaims)
				.setSubject(username)
				.setIssuedAt(new Date(Clock.systemUTC().millis())) //I use this instead of clockMillis, because I need a date that is not expired
				.setExpiration(new Date(Clock.systemUTC().millis() + 10 * 60 * 1000))//10 min
				.signWith(signKey, SignatureAlgorithm.HS256)
				.compact();
		
		assertEquals(username, jwtService.extractUsername(accessToken));
	}
	
	@Test
	void isTokenValidNullTokenThrow() {
		userDetails = User.builder()
				.username("x")
				.password("x")
				.role(RolesEnum.ROLE_USER)
				.build();
		assertThrows(IllegalArgumentException.class,() ->jwtService.isTokenValid(accessToken, userDetails));
	}
	
	@Test
	void isTokenValidNullUserDetailsThrow() {
		String username = "x";
		extraClaims = new HashMap<>();
		accessToken = Jwts.builder()
				.setClaims(extraClaims)
				.setSubject(username)
				.setIssuedAt(new Date(Clock.systemUTC().millis())) //I use this instead of clockMillis, because I need a date that is not expired
				.setExpiration(new Date(Clock.systemUTC().millis() + 10 * 60 * 1000))//10 min
				.signWith(signKey, SignatureAlgorithm.HS256)
				.compact();
		assertThrows(IllegalArgumentException.class,() ->jwtService.isTokenValid(accessToken, userDetails));
	}
	
	@Test
	void isTokenValidReturnTrue() {
		extraClaims = new HashMap<>();
		userDetails = User.builder()
				.username("x")
				.password("x")
				.role(RolesEnum.ROLE_USER)
				.build();
		accessToken = Jwts.builder()
				.setClaims(extraClaims)
				.setSubject(userDetails.getUsername())
				.setIssuedAt(new Date(Clock.systemUTC().millis()))//I use this instead of clockMillis, because I need a date that is not expired
				.setExpiration(new Date(Clock.systemUTC().millis() + 10 * 60 * 1000))//10 min
				.signWith(signKey, SignatureAlgorithm.HS256)
				.compact();
		assertTrue(jwtService.isTokenValid(accessToken, userDetails));
	}
	
	@Test
	void isTokenValidReturnFalse() {
		extraClaims = new HashMap<>();
		userDetails = User.builder()
				.username("x")
				.password("x")
				.role(RolesEnum.ROLE_USER)
				.build();
		accessToken = Jwts.builder()
				.setClaims(extraClaims)
				.setSubject(userDetails.getUsername())
				.setIssuedAt(new Date(Clock.systemUTC().millis())) //I use this instead of clockMillis, because I need a date that is not expired
				.setExpiration(new Date(Clock.systemUTC().millis() + 10 * 60 * 1000))
				.signWith(signKey, SignatureAlgorithm.HS256)
				.compact();
		when(clock.millis()).thenReturn(Clock.systemUTC().millis()+ 10 * 60 * 2000); // return a time before the accessToken expiration 
		assertFalse(jwtService.isTokenValid(accessToken, userDetails));
	}
	
	
	
	
	
	
}
