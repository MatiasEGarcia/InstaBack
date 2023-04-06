package com.instaJava.instaJava.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.dto.response.ResFollowStatus;
import com.instaJava.instaJava.dto.response.ResFollower;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Follower;
import com.instaJava.instaJava.mapper.FollowerMapper;
import com.instaJava.instaJava.service.FollowerService;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.validator.IsEnum;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/follower")
@RequiredArgsConstructor
public class FollowerC {
	
	private final FollowerService follService;
	private final FollowerMapper follMapper;
	private final MessagesUtils messUtils;

	@PostMapping("/save")
	public ResponseEntity<ResFollowStatus> save(@RequestParam(name = "followed") String followed){
		Follower fol = follService.save(Long.parseLong(followed)); 
		return ResponseEntity.ok().body(follMapper.FollowerToResFollowStatus(fol));
	}
	
	@PostMapping("/AllBy")
	public ResponseEntity<ResPaginationG<ResFollower>> getFollower(
			@RequestBody ReqSearch reqSearch,
			@RequestParam(name ="page", defaultValue = "1") String page,
			@RequestParam(name = "pageSize" , defaultValue ="20") String pageSize,
			@RequestParam(name = "sortField", defaultValue="FollowerId") String sortField,
			@RequestParam(name = "sortDir" , defaultValue = "asc")@IsEnum(enumSource = Direction.class) String sortDir){
		Map<String,String> map;
		HttpHeaders headers;
		Page<Follower> followersPage = follService.search(Integer.parseInt(page),
				Integer.parseInt(pageSize), sortField, sortDir, reqSearch);
		
		if(!followersPage.isEmpty()) {
			map = new HashMap<>();
			map.put("actualPage", page);
			map.put("pageSize", pageSize);
			map.put("sortField", sortField);
			map.put("sortDir", sortDir);
			return ResponseEntity.ok().body(follMapper
					.pageAndMapToResPaginationG(followersPage, map));
		}
		headers = new HttpHeaders();
		headers.add("Info-header", messUtils.getMessage("mess.not-followers"));		
		return new ResponseEntity<> (headers, HttpStatus.NO_CONTENT);
	}
	
	
}
