package com.instaJava.instaJava.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.response.ResMessage;
import com.instaJava.instaJava.dto.response.ResPublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.mapper.PublicatedImageMapper;
import com.instaJava.instaJava.service.PublicatedImageService;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.validator.Image;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/publicatedImages")
@RequiredArgsConstructor
@Validated
public class PublicatedImageC {
	
	private final PublicatedImageMapper publicImaMapper;
	private final PublicatedImageService publicatedImageService;
	private final MessagesUtils messUtils;

	@PostMapping("/save")
	public ResponseEntity<ResPublicatedImage> save(@RequestParam("img")  @Image  MultipartFile file,
			@RequestParam("description") String description){
		ResPublicatedImage resPublicatedImage = publicImaMapper
				.publicatedImageAndUserToResPublicatedImage(
						publicatedImageService.save(description,file) 
						,(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
						);
		
		return ResponseEntity.ok().body(resPublicatedImage);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<ResMessage> deleteById(@PathVariable("id") Long id){
		publicatedImageService.deleteById(id);
		return ResponseEntity.ok()
				.body(ResMessage.builder().message(messUtils.getMessage("mess.publi-image-deleted")).build());
	}
}
