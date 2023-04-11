package com.instaJava.instaJava.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.instaJava.instaJava.dto.response.ResLike;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;
import com.instaJava.instaJava.mapper.LikeMapper;
import com.instaJava.instaJava.service.LikeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/like")
@RequiredArgsConstructor
@Validated
public class LikeC {

	private final LikeService likeService;
	private final LikeMapper likeMapper;
	
	
	@PostMapping
	public ResponseEntity<ResLike> save(
			@RequestParam(name = "type")TypeItemLikedEnum type,
			@RequestParam(name = "itemId") Long itemId,
			@RequestParam(name = "decision") boolean decision){
		return ResponseEntity.ok().body(likeMapper.likeToResLike(likeService.save(type, itemId, decision)));
	}
	
}
