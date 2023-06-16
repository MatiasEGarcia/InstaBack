package com.instaJava.instaJava.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.instaJava.instaJava.dto.request.ReqLike;
import com.instaJava.instaJava.entity.Like;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;

public interface LikeService {
	
	int deleteById(Long likeId);
	
	boolean exist(TypeItemLikedEnum type, Long itemId,Long ownerLikeId);
	
	Optional<Like> save(ReqLike reqLike);
	
	List<Like> saveAll(List<ReqLike> reqLikeList);
	
	Map<String,String> getPositiveAndNegativeLikesByItemId(Long id);
}
