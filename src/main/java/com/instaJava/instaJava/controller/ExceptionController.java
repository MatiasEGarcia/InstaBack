package com.instaJava.instaJava.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.jsonwebtoken.ExpiredJwtException;

@ControllerAdvice
public class ExceptionController {

	@ExceptionHandler(value= {ExpiredJwtException.class})
	public ResponseEntity<String> expiredJwtExceptionHandler(Exception e){
		return new ResponseEntity<>(e.getMessage(),HttpStatus.FORBIDDEN);
	}
}
