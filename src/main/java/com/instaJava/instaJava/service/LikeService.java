package com.instaJava.instaJava.service;

import java.util.List;

import com.instaJava.instaJava.dto.request.ReqLike;
import com.instaJava.instaJava.dto.response.LikeDto;
import com.instaJava.instaJava.entity.IBaseEntity;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;

public interface LikeService {
	
	/**
	 * Check if Like record exist and Delete a Like record by likeId.
	 * @param likeId. id of the Like record.
	 * @throws IllegalArgumentException if @param likeId is null.
	 * @throws RecordNotFoundException if record to delete was not found.
	 * @throws InvalidActionException if auth user is not he owner of the like wanted to delete.
	 */
	void deleteById(Long likeId);
	
	/**
	 * Delete like record by its publication id and auth user id.
	 * @param publicationId - publication's id.
	 */
	void deleteByPublicationId(Long publicationId);
	
	
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
	 * @Throws InvalidActionException if item trying to like no exist, or if like record already exist
	 * @return LikeDto object with Like record info.
	 */
	LikeDto save(ReqLike reqLike);

	/**
	 * get like decisions by it's item and auth user.(basically to know if auth user likes or not the item)
	 * @param listPublicatedImage
	 */
	void setItemDecisions(List<? extends IBaseEntity> listItems);
	
	void setItemDecision(IBaseEntity item);
}
