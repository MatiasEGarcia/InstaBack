package com.instaJava.instaJava.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.instaJava.instaJava.dto.request.ReqLike;
import com.instaJava.instaJava.entity.Like;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;

public interface LikeService {
	
	/**
	 * Check if Like record exist and Delete a Like record by likeId.
	 * @param likeId. id of the Like record.
	 * @throws IllegalArgumentException if @param likeId is null.
	 * @return 0 if Like record no exist, else 1. 
	 */
	int deleteById(Long likeId);
	
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
	 * @return Empty optional if the reqLike is not valid, else Like optional.
	 */
	Optional<Like> save(ReqLike reqLike);
	
	
	/**
	 * 
	 * Save a Like collection in the database if the ReqLike was valid.
	 * 
	 * @param reqLikeList. Collection with ReqLike objects.
	 * @return Empty collection if reqLikeList is empty or none ReqLike was valid, 
	 * else a Like collection with the records saved
	 * @throws IllegalArgumentException if @param reqLikeList is null
	 */
	List<Like> saveAll(List<ReqLike> reqLikeList);
	
	/**
	 * Method to know how many likes or dislike has an item by it's id, like a publication or comentary, etc.
	 * 
	 * @param id - item id
	 * @return a Map, with 2 keys, positive and negative, both gives as value the number of likes or dislikes
	 */
	Map<String,String> getPositiveAndNegativeLikesByItemId(Long id);
}
