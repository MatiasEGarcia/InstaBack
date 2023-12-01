package com.instaJava.instaJava.service;

import java.security.Key;
import java.time.Clock;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.instaJava.instaJava.util.MessagesUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

	private final Clock clock;
	private final MessagesUtils messUtils;
	private static final String SECRET_KEY = "7638792F423F4428472B4B6250655368566D597133743677397A244326462948";

	/**
	 * Extract subject from a token give.
	 * 
	 * @param token. token from where extract subject claim
	 * @return username.
	 */
	public String extractUsername(String token) {
		return extractClaim(token,Claims::getSubject);
	}

	/**
	 * 
	 * @param <T> . type of data that return the claim extraction.
	 * @param token . string token from where get claims.
	 * @param claimsResolver. function to get claim.
	 * @return claim.
	 */
	public <T> T extractClaim(String token, Function<Claims,T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}
	
	/**
	 * Method to generate access token without extra claims.
	 * 
	 * @param userDetails. user from who extract info to generate the token.
	 * @return accessToken.
	 */
	public String generateToken(UserDetails userDetails) {
		return generateToken(new HashMap<>(),userDetails);
	}
	
	/**
	 * Method to generate access token with extra claims.
	 * 
	 * @param extraClaims. others claims.
	 * @param userDetails. user from who extract info to generate the token.
	 * @return accessToken.
	 * @throws IllegalArgumentException if @param extraClaims or @param userDetails are null.
	 */
	public String generateToken(Map<String,Object> extraClaims,UserDetails userDetails) {
		if(userDetails == null || extraClaims == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		String accessToken = Jwts.builder()
				.setClaims(extraClaims)
				.setSubject(userDetails.getUsername())
				.setIssuedAt(new Date(clock.millis()))
				.setExpiration(new Date(clock.millis() + 10 * 60 * 1000 ))// 10 min //I have to test this.
				.signWith(getSignKey(), SignatureAlgorithm.HS256)
				.compact();
		return accessToken;
	}
	
	/**
	 * Method to generate refresh tokens
	 * 
	 * @param userDetails. object from where get the data to create token
	 * @return refresh token.
	 * @throws IllegalArgumentException if @param userDetails is null.
	 */
	public String generateRefreshToken(UserDetails userDetails) {
		if(userDetails == null)  throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		String refreshToken = Jwts.builder()
				.setSubject(userDetails.getUsername())
				.setExpiration(new Date(clock.millis() + 30 * 60 * 1000 ))// 30 min
				.signWith(getSignKey(), SignatureAlgorithm.HS256)
				.compact();
		return refreshToken;
	}
	
	
	/**
	 * Verify if token is still valid, and not expired before
	 * 
	 * @param token. token to verify if is valid.
	 * @param userDetails. needed to see if the token belongs to this user
	 * @return true if is valid, else false.
	 */
	public boolean isTokenValid(String token,UserDetails userDetails) {
		if(userDetails == null) throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		final String username = this.extractUsername(token);
		return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
	}
	
	/**
	 * @see {@link #isTokenValid(String, UserDetails) isTokenValid} method.
	 */
	private boolean isTokenExpired(String token) {
		//if expiration is before current time means that is expired and return true.
		//If expiration is after current time means that is not expired and return false.
		return extractExpiration(token).before(new Date(clock.millis()));
	}
	
	/**
	 * Extract expiration date from token.
	 * 
	 * @param token . Token from where extract date.
	 * @return date of expiration.
	 */
	private Date extractExpiration(String token) {
		return extractClaim(token,Claims::getExpiration);
	}
	

	/**
	 * 
	 * @see {@link #extractClaim(String, Function<Claims,T>) extractClaim} method
	 */ 
	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSignKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
	

	private Key getSignKey() {
		byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
		return Keys.hmacShaKeyFor(keyBytes);
	}
	
	
}
