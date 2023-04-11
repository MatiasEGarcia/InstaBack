package com.instaJava.instaJava.service;

import com.instaJava.instaJava.entity.Like;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;

public interface LikeService {

	Like save(TypeItemLikedEnum type, Long itemId,boolean decision);
}
