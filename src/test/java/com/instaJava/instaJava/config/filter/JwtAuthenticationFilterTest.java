package com.instaJava.instaJava.config.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.service.InvTokenService;
import com.instaJava.instaJava.service.JwtService;
import com.instaJava.instaJava.util.MessagesUtils;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

	
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;
    @Mock private Authentication auth;
    @Mock private SecurityContext securityContext;
    @Mock private MessagesUtils messUtils;
    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailservice;
    @Mock private InvTokenService invTokenService;	
    @InjectMocks private JwtAuthenticationFilter filter;
    
    @BeforeEach
	void setUp() {
		request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
	}

    /**
     * These test are to know if a selected paths that not need authentication can be call for anyone.
     */
    @Test
    void isRegisterPathFreeAuthentication() throws Exception {
    	request.setMethod("POST");
    	request.setRequestURI("/api/v1/auth/register");
    	
    	filter.doFilterInternal(request, response, filterChain);
    	
    	assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
    @Test
    void isAuthenticatePathFreeAuthentication() throws Exception {
    	request.setMethod("POST");
    	request.setRequestURI("/api/v1/auth/authenticate");
    	
    	filter.doFilterInternal(request, response, filterChain);
    	
    	assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
    @Test
    void isRefreshTokenPathFreeAuthentication() throws Exception {
    	request.setMethod("GET");
    	request.setRequestURI("/api/v1/auth/refreshToken");
    	
    	filter.doFilterInternal(request, response, filterChain);
    	
    	assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
    
    
    @Test
    void pathNotAuthFreeAndWithoutAuthTokenUnauthorized() throws Exception {
    	request.setMethod("GET");
    	request.setRequestURI("/api/v1/users/image");//for this endpoint the client needs to authenticate
    	
    	filter.doFilterInternal(request, response, filterChain);
    	
    	assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }
    
    @Test
    void invalidAuthTokenForbidden() throws Exception {
    	request.setMethod("GET");
    	request.setRequestURI("/api/v1/users/image");
    	request.addHeader("Authorization", "Bearer randomToken");
    	when(invTokenService.existByToken(any(String.class))).thenReturn(true);//means that the token is invalid, for example, belongs to a session that already was logout
    	
    	filter.doFilterInternal(request, response, filterChain);
    	
    	assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    	verify(invTokenService).existByToken(any(String.class));
    }
    
    @Test
    void expiredAuthTokenForbidden() throws Exception{
    	UserDetails userDetails = User.builder().build();
    	String username = "x";
    	String token = "randomToken";
    	request.setMethod("GET");
    	request.setRequestURI("/api/v1/users/image");
    	request.addHeader("Authorization", "Bearer "+ token);
    	when(invTokenService.existByToken(any(String.class))).thenReturn(false);//the token wasn't used it to logout yet
    	when(jwtService.extractUsername(any(String.class))).thenReturn(username);
    	when(userDetailservice.loadUserByUsername(username)).thenReturn(userDetails);
    	when(jwtService.isTokenValid(token, userDetails)).thenReturn(false); //token was expired
    
    	
    	filter.doFilterInternal(request, response, filterChain);
    	
    	assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    	verify(invTokenService).existByToken(any(String.class));
    	verify(jwtService).extractUsername(any(String.class));
    	verify(userDetailservice).loadUserByUsername(username);
    	verify(jwtService).isTokenValid(token, userDetails);
    }
    
}
