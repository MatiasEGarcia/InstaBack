package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.fail;

import java.security.Key;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

	private static final String SECRET_KEY = "7638792F423F4428472B4B6250655368566D597133743677397A244326462948";
	private Key signKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
 	private Map<String,Object> extraClaims;
	private UserDetails userDetails = User.builder()
			.username("Mati")
			.password("random")
			.build();
	private String accessToken = Jwts.builder().compact();
	private String refreshToken;
	@InjectMocks JwtService jwtService;
	
	
	void generateToken() {
		
		
		
		
	}

}
