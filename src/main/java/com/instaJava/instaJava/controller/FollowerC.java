package com.instaJava.instaJava.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.instaJava.instaJava.dto.response.ResFollowStatus;
import com.instaJava.instaJava.entity.Follower;
import com.instaJava.instaJava.mapper.FollowerMapper;
import com.instaJava.instaJava.service.FollowerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/follower")
@RequiredArgsConstructor
public class FollowerC {
	
	private final FollowerService follService;
	private final FollowerMapper follMapper;

	@PostMapping
	public ResponseEntity<ResFollowStatus> save(@RequestParam(name = "followed") String followed){
		Follower fol = follService.save(Long.parseLong(followed)); 
		return ResponseEntity.ok().body(follMapper.FollowerToResFollowStatus(fol));
	}
	
}
