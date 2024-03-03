package com.instaback.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.instaback.dto.dao.IdValueDto;
import com.instaback.exception.CryptoException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RSAUtils{

	private final PrivateKey privateKey;
	private final PublicKey publicKey;
	private final Environment env;
	private final MessagesUtils messUtils;
	
	@Autowired
	public RSAUtils(Environment env, MessagesUtils messUtils)  {
		this.env = env;
		this.messUtils = messUtils;
		KeyFactory keyFactory;
		
		String publicKeyString = this.env.getProperty("RSA_PUBLIC_KEY_STRING");
		String privateKeyString = this.env.getProperty("RSA_PRIVATE_KEY_STRING");
		
		X509EncodedKeySpec keySpecPublic = new X509EncodedKeySpec(decode(publicKeyString));
		PKCS8EncodedKeySpec keySpecPrivate = new PKCS8EncodedKeySpec(decode(privateKeyString));
		
		try {
			keyFactory = KeyFactory.getInstance("RSA");
			publicKey = keyFactory.generatePublic(keySpecPublic);
			privateKey = keyFactory.generatePrivate(keySpecPrivate);
		} catch (Exception e) {
			throw new CryptoException(messUtils.getMessage("rsa.instanciate-error"), HttpStatus.INTERNAL_SERVER_ERROR, e);
		} 
	}
	
	/**
	 * Encrypt and encode a String.
	 * @param toEncrypt - string to encrypt and encode.
	 * @return encrypted and encoded passed string.
	 * @throws IllegalArgumentException if toEncrypt is null.
	 * @throws NoSuchAlgorithmException 
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public String encrypt(String toEncrypt) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		if(toEncrypt == null) throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		byte[] messageToBytes = toEncrypt.getBytes();
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] encryptedBytes = cipher.doFinal(messageToBytes);
		return encode(encryptedBytes);
	}
	
	/**
	 * Decrypt and decode String.
	 * @param toDecrypt - string to decrypt and decode.
	 * @return decrypted and decoded passed string. 
	 * @throws IllegalArgumentException if toDecrypt is null.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws UnsupportedEncodingException
	 */
	public String decrypt(String toDecrypt) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		if(toDecrypt == null) throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		byte[] encryptedBytes = decode(toDecrypt);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
		return new String(decryptedMessage, "UTF-8");
	}
	
	/**
	 * Will take a list of elements that have a string encrypted and it will decrypt them. 
	 * @param listToDecrypt - list of elements which have a string to decrypt.
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws UnsupportedEncodingException
	 */
	public void decryptAll(List<IdValueDto<String>> listToDecrypt) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException{
		if(listToDecrypt == null) throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		for(IdValueDto<String> element : listToDecrypt) {
			element.setValue(decrypt(element.getValue()));
		}
	}

	private String encode(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}
	
	private byte[] decode(String data) {
		return Base64.getDecoder().decode(data);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
