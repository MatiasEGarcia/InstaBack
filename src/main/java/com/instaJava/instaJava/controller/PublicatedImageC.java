package com.instaJava.instaJava.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dto.response.ResMessage;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.dto.response.ResPublicatedImage;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.mapper.PublicatedImageMapper;
import com.instaJava.instaJava.service.PublicatedImageService;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.validator.Image;

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
	
	@GetMapping("/byUser")
	public ResponseEntity<ResPaginationG<ResPublicatedImage>> getByUser(
			@RequestParam(name ="page", defaultValue = "1") String page,
			@RequestParam(name = "pageSize" , defaultValue ="20") String pageSize,
			@RequestParam(name = "sortField", required = false) String sortField,
			@RequestParam(name = "sortDir" , required = false) String sortDir){
		Map<String,String> map = new HashMap<>();
		Page<PublicatedImage> pagePublicatedImage = null; // just for now, when I add else in the if it won't be necessary 
		
		if(sortField == null || sortField.isBlank() || sortDir.isBlank() || sortDir == null) {
			pagePublicatedImage = publicatedImageService
					.findPublicatedImagesByOwner(Integer.parseInt(page), Integer.parseInt(pageSize));
		}else {
			pagePublicatedImage = publicatedImageService
					.findPublicatedImagesByOwnerSorted(Integer.parseInt(page), Integer.parseInt(pageSize), sortField, sortDir);
		}
		map.put("actualPage", page);
		map.put("pageSize", pageSize);
		map.put("sortField", sortField);
		map.put("sortDir", sortDir);
		return ResponseEntity.ok().body(publicImaMapper
				.pageAndMapToResPaginationG(pagePublicatedImage, map));
	}
	
	
}
