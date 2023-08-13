package com.instaJava.instaJava.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.instaJava.instaJava.dto.request.ReqLike;
import com.instaJava.instaJava.dto.request.ReqLikeList;
import com.instaJava.instaJava.dto.response.ResLike;
import com.instaJava.instaJava.dto.response.ResListG;
import com.instaJava.instaJava.entity.Like;
import com.instaJava.instaJava.mapper.LikeMapper;
import com.instaJava.instaJava.service.LikeService;
import com.instaJava.instaJava.util.MessagesUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/like")
@RequiredArgsConstructor
@Validated
public class LikeC {

	private final LikeService likeService;
	private final LikeMapper likeMapper;
	private final MessagesUtils messUtils;

	/**
	 * Save a Like record
	 * 
	 * @param reqLike. Data to save .
	 * @return like saved.
	 */
	@PostMapping(consumes = "application/json", produces = "application/json")
	public ResponseEntity<ResLike> save(@Valid @RequestBody ReqLike reqLike) {
		Optional<Like> optLike = likeService.save(reqLike);
		if (optLike.isEmpty())
			return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.not-successfully-saved"))
					.build();
		return ResponseEntity.ok().body(likeMapper.likeToResLike(optLike.get()));
	}

	/**
	 * Save a collection of Likes
	 * 
	 * @param reqLikeList. Object with a collection of Likes data to be saved
	 * @return Like collection saved.
	 */
	
	@PostMapping(value="/all", consumes = "application/json", produces = "application/json")
	public ResponseEntity<ResListG<ResLike>> saveAll(@Valid @RequestBody ReqLikeList reqLikeList) {
		List<Like> likeListSaved = likeService.saveAll(reqLikeList.getReqLikeList());
		if (likeListSaved.isEmpty())
			return ResponseEntity.noContent().header("moreInfo", messUtils.getMessage("mess.not-successfully-saved"))
					.build();
		return ResponseEntity.ok().body(likeMapper.likeListToResListG(1, likeListSaved)); // if you don't understand the
																							// 1 , go to the likeMapper
																							// class
	}
	
	
}
