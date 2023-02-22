package com.instaJava.instaJava.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.instaJava.instaJava.exception.InvalidException;


@ControllerAdvice
public class ExceptionHandlerController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlerController.class);

	@ExceptionHandler(value = { Exception.class })
    public ResponseEntity<Object> ExceptionHandler(Exception e) {
        LOGGER.error("There was some error: ",e.getMessage());
        return new ResponseEntity<Object>(e.getMessage(),HttpStatus.BAD_REQUEST);
    }
	
	@ExceptionHandler(value= {InvalidException.class})
	public ResponseEntity<Object> ExceptionHandler(InvalidException e){
		LOGGER.error("RefreshTokenInvalid",e.getMessage());
		return new ResponseEntity<Object>(e.getMessage(),HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(value= {MethodArgumentNotValidException.class})
	public ResponseEntity<Map<String,String>> handleValidateException(MethodArgumentNotValidException e){
		LOGGER.error("MethodArgumentNotValidException");
		Map<String,String> errors = new HashMap<>();
		e.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError)error).getField();
			String message = error.getDefaultMessage();
			errors.put(fieldName, message);
		});
		return ResponseEntity.badRequest().body(errors);
	}
	
}
