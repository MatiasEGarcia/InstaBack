package com.instaJava.instaJava.converter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.instaJava.instaJava.exception.CryptoException;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.RSAUtils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

@Converter
@Component
@RequiredArgsConstructor
public class MessageBodyCryptoConverter implements AttributeConverter<String, String>{

	private final RSAUtils rsaUtils;
	private final MessagesUtils messUtils;
	
	/**
	 * Encrypt msgBody.
	 * @return encrypted msgBody.
	 * @throws CryptoException if there was some error trying to encrypt the message.
	 */
	@Override
	public String convertToDatabaseColumn(String msgBody) {
		if(msgBody == null) return null;
		try {
			return rsaUtils.encrypt(msgBody);
		} catch (Exception e) {
			throw new CryptoException(messUtils.getMessage("message.encrypt-fail") , HttpStatus.BAD_REQUEST, e);
		}
	}

	/**
	 * Decrypt the msgBody.
	 * @return decrypted msgBody
	 * @throws CryptoException if there was some error trying to encrypt the message
	 */
	@Override
	public String convertToEntityAttribute(String msgBody) {
		if(msgBody == null) return null;
		try {
			return rsaUtils.decrypt(msgBody);
		}catch(Exception e) {
			throw new CryptoException(messUtils.getMessage("message.decrypt-fail") , HttpStatus.BAD_REQUEST, e);
		}
	}

	
}
