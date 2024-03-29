package com.instaback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.instaback.application.LikeApplication;
import com.instaback.dto.LikeableDto;
import com.instaback.dto.request.ReqLike;
import com.instaback.dto.response.PublicatedImageDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/like")
@RequiredArgsConstructor
@Validated
public class LikeC {

	private final LikeApplication likeApplication;

	/**
	 * Save a Like record
	 * 
	 * @param reqLike. Data to save .
	 * @return LikeableDto object.
	 */
	@PostMapping(consumes = "application/json", produces = "application/json")
	public ResponseEntity<LikeableDto> save(@Valid @RequestBody ReqLike reqLike) {
		return ResponseEntity.ok().body(likeApplication.save(reqLike));
	}
	
	//FALTA TESTS
	/**
	 * To delete the auth user's like from a specific publication.
	 * @param publicationId - publication that have the like.
	 * @return {@link PublicatedImageDto} with all the info updated. 
	 */
	@DeleteMapping(value="/byPublicatedImageId/{publicatedImageId}")
	public ResponseEntity<PublicatedImageDto> byPublicatedImageId(@PathVariable(value="publicatedImageId") Long publicatedImageId){
		return ResponseEntity.ok().body(likeApplication.deleteByPublicatedImageId(publicatedImageId));
	}
}
