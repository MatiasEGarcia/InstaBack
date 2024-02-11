package com.instaJava.instaJava.application;

import com.instaJava.instaJava.dto.request.ReqLike;
import com.instaJava.instaJava.dto.response.LikeDto;

public interface LikeApplication {

	/**
	 * Save a like object in the database.
	 * 
	 * @param reqLike. object with the data of the Like to be saved.
	 * @return LikeDto object with Like record info.
	 * @Throws InvalidActionException if item trying to like no exist, or if like record already exist
	 */
	LikeDto save(ReqLike reqLike);
	
	/**
	 * Delete like record by its publication id and auth user id.
	 * @param publicationId - publication's id.
	 */
	void deleteByPublicationId(Long publicationId);
	
}
