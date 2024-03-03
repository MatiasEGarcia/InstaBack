package com.instaback.service;

import java.util.List;

import com.instaback.entity.IBaseEntity;
import com.instaback.entity.Like;
import com.instaback.entity.User;
import com.instaback.enums.TypeItemLikedEnum;
import com.instaback.exception.InvalidActionException;
import com.instaback.exception.RecordNotFoundException;

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
	 * Delete like record by its item id and auth user id.
	 * @param item - item's id.
	 */
	void deleteByItemId(Long itemId);
	
	
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
	 * @param itemId - liked item's id.
	 * @param decision - like decision(true = liked, false = dislike).
	 * @param type - liked item's type(a publicatedImage, a comment, etc) 
	 * @param userOwner - like's owner.
	 * @throws IllegalArgumentException if one  param is null.
	 * @return created Like.
	 */
	Like save(Long itemId,Boolean decision, TypeItemLikedEnum type, User userOwner);

	/**
	 * get like decisions by it's item and auth user.(basically to know if auth user likes or not the item)
	 * @param listPublicatedImage
	 */
	void setItemDecisions(List<? extends IBaseEntity> listItems);
	
	void setItemDecision(IBaseEntity item);
	
	/**
	 * Search number of likes for a specific item.
	 * @param itemId - item's id.
	 * @return number of likes for item selected
	 */
	Long getLikesNumberByItemIdAndDecision(Long itemId, boolean decision);
	
	
}
