package com.instaJava.instaJava.service;

import java.util.Optional;

import com.instaJava.instaJava.entity.Like;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;

public interface LikeService {

	Optional<Like> save(TypeItemLikedEnum type, Long itemId,boolean decision);
}
