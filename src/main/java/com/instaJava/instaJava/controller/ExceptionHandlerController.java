package com.instaJava.instaJava.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.instaJava.instaJava.dto.response.ResErrorMessage;
import com.instaJava.instaJava.exception.AlreadyExistsException;
import com.instaJava.instaJava.exception.InvalidException;
import com.instaJava.instaJava.exception.NotFoundException;
import com.instaJava.instaJava.util.MessagesUtils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class ExceptionHandlerController {//TENGO QUE AGREGAR EL MANEJO DE HttpRequestMethodNotSupportedException(CUANDO SE EQUIVOCAN DE TIPO DE METODO, DELETE, POST,GET,ETC)


	@Autowired
	MessagesUtils messUtils;
	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlerController.class);

	/**
	 * Exception handler for exceptions that we don't know how to hanlde right now.
	 * 
	 * @param e . exception that we need to handle
	 * @return ReponseEntity.
	 */
	@ExceptionHandler(value = { Exception.class })
	public ResponseEntity<ResErrorMessage> exceptionHandler(Exception e) {
		LOGGER.error("There was some Exception: ", e.getMessage());
		ResErrorMessage resMessage = new ResErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.toString(),
				messUtils.getMessage("generic.internal-server-error"), Map.of(messUtils.getMessage("key.detail-exception"),e.getMessage()));
		return ResponseEntity.internalServerError().body(resMessage);
	}

	/**
	 * Exception handler for InvalidExceptions, for example when a refreshToken is
	 * already invalid.
	 * 
	 * @param e. exception that we need to handle.
	 * @return ResponseEntity.
	 */
	@ExceptionHandler(value = { InvalidException.class })
	public ResponseEntity<ResErrorMessage> invalidExceptionHandler(InvalidException e) {
		LOGGER.error("There was some InvalidException", e.getMessage());
		if(e.getStatus().equals(HttpStatus.NO_CONTENT)) {
			return ResponseEntity.noContent().header(messUtils.getMessage("key.header-detail-exception"), e.getMessage()).build();
		}
		return ResponseEntity.status(e.getStatus()).body(ResErrorMessage.builder()
				.error(e.getStatus().toString())
				.message(e.getMessage())
				.build());
	}

	/**
	 * Handler for IllegalArgumentException.
	 * 
	 * @param e. exception that we need to handle.
	 * @return ResponseEntity.
	 */
	@ExceptionHandler(value = { IllegalArgumentException.class })
	public ResponseEntity<ResErrorMessage> illegalArgumentExceptionHandler(IllegalArgumentException e) {
		LOGGER.error("There was some IllegalArgumentException", e.getMessage());
		ResErrorMessage resMessage = ResErrorMessage.builder().error(HttpStatus.INTERNAL_SERVER_ERROR.toString())
				.message(e.getMessage())
				.build();
		return ResponseEntity.internalServerError().body(resMessage);
	}

	/**
	 * This Exception will be throw when The requestBody recive an object but its parts is not fulfilled.
	 * 
	 * @param e. exception that we need to handle.
	 * @return ResponseEntity.
	 */
	@ExceptionHandler(value = { MethodArgumentNotValidException.class })
	public ResponseEntity<ResErrorMessage> handleValidateException(MethodArgumentNotValidException e) {
		LOGGER.error("There was some MethodArgumentNotValidException");
		Map<String, String> errors = new HashMap<>();
		e.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String message = error.getDefaultMessage();
			errors.put(fieldName, message);
		});
		
		ResErrorMessage resMessage = new ResErrorMessage(HttpStatus.BAD_REQUEST.toString()
				,messUtils.getMessage("client.body-not-fulfilled") , errors);
		
		return ResponseEntity.badRequest().body(resMessage);
	}

	/**
	 * This exception occurs for example I try to save a file with a incorrect type, save in
	 * PublicatedImageC.
	 * 
	 * @param e. exception that we need to handle.
	 * @return ResponseEntity.
	 */
	@ExceptionHandler(value = { ConstraintViolationException.class })
	public ResponseEntity<ResErrorMessage> handleConstraintViolationException(ConstraintViolationException e) {
		LOGGER.error("There was some ConstraintViolationException", e.getMessage());
		Map<String, String> errors = new HashMap<>();
		Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();

		constraintViolations.forEach((constraintViolation) -> {
			String fieldName = constraintViolation.getPropertyPath().toString();
			// as a fieldName I get method.objetName from constraingViolation, I only want
			// the objectName (an example is in UserC with uploadImage,method.objetName ->
			// uploadImage.file)
			String result[] = fieldName.split("[.]");
			fieldName = result[1];
			String message = constraintViolation.getMessage();
			errors.put(fieldName, message);
		});
		
		ResErrorMessage resMessage = new ResErrorMessage(HttpStatus.BAD_REQUEST.toString(), messUtils.getMessage("client.type-incorrect"), errors);
		
		return ResponseEntity.badRequest().body(resMessage);
	}

	/**
	 * This exception occurs when some parameter is missing in the request.
	 * 
	 * @param e. exception that we need to handle.
	 * @return ResponseEntity.
	 */
	@ExceptionHandler(value = { MissingServletRequestParameterException.class })
	public ResponseEntity<ResErrorMessage> handleMissingServletRequestParameterException(
			MissingServletRequestParameterException e) {
		LOGGER.error("There was some MissingServletRequestParameterException", e.getMessage());
		
		ResErrorMessage resMessage = new ResErrorMessage(HttpStatus.BAD_REQUEST.toString(), 
				messUtils.getMessage("client.missing-param"), Map.of(e.getParameterName(), e.getMessage()));
		
		return ResponseEntity.badRequest().body(resMessage);

	}

	/**
	 * When a controller has as argument a MultipartFile but the client didn't send
	 * anything, this exception is throw
	 * 
	 * @param e. exception that we need to handle.
	 * @return ResponseEntity.
	 */
	@ExceptionHandler(value = { MissingServletRequestPartException.class })
	public ResponseEntity<ResErrorMessage> handleMissingServletRequestPartException(
			MissingServletRequestPartException e) {
		LOGGER.error("There was some MissingServletRequestPartException", e.getMessage());
		ResErrorMessage resMessage = new ResErrorMessage(HttpStatus.BAD_REQUEST.toString(),
				messUtils.getMessage("vali.part.not.present"),Map.of(e.getRequestPartName(), e.getMessage()));
		return ResponseEntity.badRequest().body(resMessage);

	}

	/**
	 * This exception occurs : when I post an object that needs an enum, but the
	 * string value is not the same any enum values. 
	 * When the client don't pass the object that have the requestBody
	 * @param e. exception that we need to handle.
	 * @return ResponseEntity.
	 */
	@ExceptionHandler(value = { HttpMessageNotReadableException.class })
	public ResponseEntity<ResErrorMessage> hanlderHttpMessageNotReadableException(HttpMessageNotReadableException e) {
		LOGGER.error("There was some HttpMessageNotReadableException", e.getMessage());
		return ResponseEntity.badRequest().body(ResErrorMessage.builder()
				.error(HttpStatus.BAD_REQUEST.toString())
				.message(messUtils.getMessage("client.value-missing-incorrect"))
				.build());
	}

	/**
	 * this exception occurs when the client send an type value that cannot be converted to the correct type value.
	 * @param e. exception that we need to handle.
	 * @return ResponseEntity.
	 */
	@ExceptionHandler(value = { MethodArgumentTypeMismatchException.class })
	public ResponseEntity<ResErrorMessage> hanlderMethodArgumentTypeMismatchException(
			MethodArgumentTypeMismatchException e) {
		LOGGER.error("There was some MethodArgumentTypeMismatchException", e.getMessage());
		ResErrorMessage resMessage = new ResErrorMessage(HttpStatus.BAD_REQUEST.toString(),
				messUtils.getMessage("client.type-incorrect"), Map.of(e.getName(), e.getMessage()));
		return ResponseEntity.badRequest().body(resMessage);
		
	}
	
	/**
	 * When client send a wrong entity property when try to search records this will handle the exception that will be throw. 
	 * @param e PropertyReferenceException
	 * @return ResErrorMessage with all the information required by the client to resolve the incorrect property.
	 */
	@ExceptionHandler(value = {PropertyReferenceException.class})
	public ResponseEntity<ResErrorMessage> handlerPropertyReferenceException(PropertyReferenceException e){
		LOGGER.error("There was some PropertyReferenceException : " , e.getMessage());
		ResErrorMessage resMessage = new ResErrorMessage(HttpStatus.BAD_REQUEST.toString(), 
				messUtils.getMessage("client.property-incorrect"), Map.of(e.getPropertyName(),e.getMessage()));
		return ResponseEntity.badRequest().body(resMessage);
	}
	
	
	@ExceptionHandler(value = {AlreadyExistsException.class})
	public ResponseEntity<ResErrorMessage> handlerAlreadyExistsException(AlreadyExistsException e){
		LOGGER.error("There was some AlreadyExistsException: " , e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
				ResErrorMessage.builder()
				.error(e.getStatus().toString())
				.message(e.getMessage())
				.build());
	}
	
	/**
	 * Exception thrown from loadUserByUsername method from UserDetailsService;
	 * @param e - exception
	 * @return ResponseEntity
	 */
	@ExceptionHandler(value = {UsernameNotFoundException.class})
	public ResponseEntity<ResErrorMessage> handlerUsernameNotFoundException(UsernameNotFoundException e){
		LOGGER.error("There was some UsernameNotFoundException: " , e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
				ResErrorMessage.builder()
				.error(HttpStatus.BAD_REQUEST.toString())
				.message(e.getMessage())
				.build());
	}
	
	@ExceptionHandler(value= {BadCredentialsException.class})
	public ResponseEntity<ResErrorMessage> handlerBadCredentialsException(BadCredentialsException e){
		LOGGER.error("There was some BadCredentialsException: " , e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
				ResErrorMessage.builder()
				.error(HttpStatus.BAD_REQUEST.toString())
				.message(e.getMessage())
				.build());		
	}
	
	@ExceptionHandler(value= {NotFoundException.class})
	public ResponseEntity<ResErrorMessage> handlerNotFoundException(NotFoundException e){
		LOGGER.error("There was some NotFoundException: " , e.getMessage());
		if(e.getStatus().equals(HttpStatus.NO_CONTENT)) {
			return ResponseEntity.noContent().header(messUtils.getMessage("key.header-detail-exception"), e.getMessage()).build();
		}
		return ResponseEntity.status(e.getStatus()).body(ResErrorMessage.builder()
				.error(e.getStatus().toString())
				.message(e.getMessage())
				.build());
	}
	
}












