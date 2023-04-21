package com.instaJava.instaJava.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.instaJava.instaJava.dto.response.ResLike;
import com.instaJava.instaJava.entity.Like;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;
import com.instaJava.instaJava.mapper.LikeMapper;
import com.instaJava.instaJava.service.LikeService;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/like")
@RequiredArgsConstructor
@Validated
public class LikeC {

	private final LikeService likeService;
	private final LikeMapper likeMapper;
	private final MessagesUtils messUtils;
	
	/*
	 * return  status noContent with a header with info if something happened and the record didn't save
	 * return status ok with the record saved if the record was saved successfully
	 * */
	@PostMapping
	public ResponseEntity<ResLike> save(
			@RequestParam(name = "type")TypeItemLikedEnum type,
			@RequestParam(name = "itemId") Long itemId,
			@RequestParam(name = "decision") boolean decision){
		Optional<Like> optLike = likeService.save(type, itemId, decision);
		if(optLike.isEmpty()) return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.like-didnt-saved")).build();
		return ResponseEntity.ok().body(likeMapper.likeToResLike(optLike.get()));
	}
	
}
