package com.instaJava.instaJava.service;

import com.instaJava.instaJava.dto.request.ReqLike;
import com.instaJava.instaJava.dto.response.LikeDto;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;
import com.instaJava.instaJava.exception.InvalidException;
import com.instaJava.instaJava.exception.RecordNotFoundException;

public interface LikeService {
	
	/**
	 * Check if Like record exist and Delete a Like record by likeId.
	 * @param likeId. id of the Like record.
	 * @throws IllegalArgumentException if @param likeId is null.
	 * @throws RecordNotFoundException if record to delete was not found.
	 * @throws InvalidException if auth user is not he owner of the like wanted to delete.
	 */
	void deleteById(Long likeId);
	
	/**
	 * 
	 * To check if a Like record exist by type, item and owner id.
	 * 
	 * @param type. type of the item that was liked, ej : COMMENT.
	 * @param itemId. id of the item
	 * @param ownerLikeId. id of the user owner of the Like record.
	 * @return true if the Like record already exists, else false.
	 * @throws IllegalArgumentException if anyone of the params are null.
	 */
	boolean exist(TypeItemLikedEnum type, Long itemId,Long ownerLikeId);
	
	/**
	 * Save a like object in the database.
	 * 
	 * @param reqLike. object with the data of the Like to be saved.
	 * @throws IllegalArgumentException if the param is null.
	 * @return LikeDto object with Like record info.
	 */
	LikeDto save(ReqLike reqLike);

}
