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

	@Override
	@Transactional
	public Like save(TypeItemLikedEnum type, Long itemId, boolean decision) {
		if(type == null) throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument.not.null"));
		Like like = Like.builder().itemType(type).decision(decision).build();
		switch (type) {
		case PULICATED_IMAGE:
			publiImaService.findById(itemId); // if no exist, throw exception
			break;
		default:
			throw new IllegalArgumentException(messUtils.getMessage("exception.like-type-no-exist"));
		}
		like.setItemId(itemId);
		like.setLikedAt(ZonedDateTime.now(clock));
		like.setOwnerLike((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		return likeDao.save(like);
	}

}
