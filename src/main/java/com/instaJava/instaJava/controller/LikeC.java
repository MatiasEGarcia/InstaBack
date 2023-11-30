package com.instaJava.instaJava.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.instaJava.instaJava.dto.request.ReqLike;
import com.instaJava.instaJava.dto.response.LikeDto;
import com.instaJava.instaJava.service.LikeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/like")
@RequiredArgsConstructor
@Validated
public class LikeC {

	private final LikeService likeService;

	/**
	 * Save a Like record
	 * 
	 * @param reqLike. Data to save .
	 * @return like saved.
	 */
	@PostMapping(consumes = "application/json", produces = "application/json")
	public ResponseEntity<LikeDto> save(@Valid @RequestBody ReqLike reqLike) {
		return ResponseEntity.ok().body(likeService.save(reqLike));
	}
	
	
}
