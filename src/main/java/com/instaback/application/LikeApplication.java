package com.instaback.application;

import com.instaback.dto.LikeableDto;
import com.instaback.dto.request.ReqLike;
import com.instaback.dto.response.PublicatedImageDto;

public interface LikeApplication {

	/**
	 * Save a like object in the database.
	 * 
	 * @param reqLike. object with the data of the Like to be saved.
	 * @return Likeable object. (The like entity dto classes will implement that interface.)
	 * @throws IllegalArgumentException if reqLike is null or if reqLike.type no exists.
	 * @Throws InvalidActionException if item trying to like no exist, or if like record already exist
	 */
	LikeableDto save(ReqLike reqLike);
	
	/**
	 * Delete like record by publicationImage id.
	 * @param publicatedImageId - publicatedImage's id.
	 * @return PublicatedImageDto with the publicatedImage info updated.
	 */
	PublicatedImageDto deleteByPublicatedImageId(Long publicatedImageId);
	
}
