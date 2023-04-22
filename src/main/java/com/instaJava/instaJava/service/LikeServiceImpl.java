package com.instaJava.instaJava.service;

import java.time.Clock;
import java.time.ZonedDateTime;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.LikeDao;
import com.instaJava.instaJava.entity.Like;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

	private final Clock clock;
	private final LikeDao likeDao;
	private final PublicatedImageService publiImaService;
	private final MessagesUtils messUtils;
	// cuando agrege los comentarios tengo que agregar su service

	/*
	 * if the item liked no exist, throw Exc
	 * If the item liked has a type that no exist throw Exc
	 * if the like is saved return the record saved
	 * */
	@Override
	@Transactional
	public Like save(TypeItemLikedEnum type, Long itemId, boolean decision) {
		if(type == null || itemId == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		Like like; 
		switch (type) {
		case PULICATED_IMAGE:
			if(publiImaService.getById(itemId).isEmpty()) throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
			break;
		default:
			throw new IllegalArgumentException(messUtils.getMessage("exception.like-type-no-exist"));
		}
		like = Like.builder().itemType(type).decision(decision).build();
		like.setItemId(itemId);
		like.setLikedAt(ZonedDateTime.now(clock));
		like.setOwnerLike((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		return likeDao.save(like);
	}

}
