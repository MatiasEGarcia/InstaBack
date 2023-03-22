package com.instaJava.instaJava.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.instaJava.instaJava.dto.response.RespValidError;
import com.instaJava.instaJava.exception.InvalidException;
import com.instaJava.instaJava.util.MessagesUtils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;


@ControllerAdvice
public class ExceptionHandlerController {
	
	@Autowired MessagesUtils messUtils;
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
		LOGGER.error("------------MethodArgumentNotValidException");
		Map<String,String> errors = new HashMap<>();
		e.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError)error).getField();
			String message = error.getDefaultMessage();
			errors.put(fieldName, message);
		});
		return ResponseEntity.badRequest().body(errors);
	}
	
	@ExceptionHandler(value = { ConstraintViolationException.class })
	public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException e) {
		LOGGER.error("ConstraintViolationException :", e.getMessage());
		Map<String, String> errors = new HashMap<>();
		Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
		
		constraintViolations.forEach((constraintViolation) -> {
			String fieldName = constraintViolation.getPropertyPath().toString();
			//as a fieldName I get method.objetName from constraingViolation, I only want the objectName (an example is in UserC with uploadImage,method.objetName -> uploadImage.file)
			String result[] = fieldName.split("[.]");
			fieldName = result[1];
			String message = constraintViolation.getMessage();
			errors.put(fieldName, message);
		});
		return ResponseEntity.badRequest().body(errors);
	}

	@ExceptionHandler(value = {MissingServletRequestParameterException.class})
	public ResponseEntity<RespValidError> handleMissingServletRequestParameterException(MissingServletRequestParameterException e){
		return ResponseEntity.badRequest().body(RespValidError.builder()
				.field(e.getParameterName())
				.errorMessage(e.getMessage())
				.build());
	}
	
	//when a controller has as argument a MultipartFile but the client didn't send anything, this exception is throw
	@ExceptionHandler(value = {MissingServletRequestPartException.class})
	public ResponseEntity<RespValidError> handleMissingServletRequestPartException(MissingServletRequestPartException e){
		return ResponseEntity.badRequest().body(RespValidError.builder()
				.field(e.getRequestPartName())
				.errorMessage(messUtils.getMessage("vali.part.not.present"))
				.build());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
