package com.instaJava.instaJava.application;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.http.HttpStatus;
import com.instaJava.instaJava.dto.LikeableDto;
import com.instaJava.instaJava.dto.request.ReqLike;
import com.instaJava.instaJava.dto.response.PublicatedImageDto;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.mapper.PublicatedImageMapper;
import com.instaJava.instaJava.service.LikeService;
import com.instaJava.instaJava.service.PublicatedImageService;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeApplicationImpl implements LikeApplication {

	private final LikeService lService;
	private final PublicatedImageService pImaService;
	private final PublicatedImageMapper pMapper;
	private final MessagesUtils messUtils;

	@Override
	public LikeableDto save(ReqLike reqLike) {
		if (reqLike == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		}
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		//check if like already exists.
		if (lService.exist(reqLike.getType(), reqLike.getItemId(), authUser.getId())) {
			throw new InvalidActionException(messUtils.getMessage("like.already-exists"), HttpStatus.BAD_REQUEST);
		}
		//get the item if exists and save like
		switch (reqLike.getType()) {
		case PULICATED_IMAGE: {
			PublicatedImageDto pDto;
			PublicatedImage p = pImaService.getById(reqLike.getItemId());
			
			lService.save(reqLike.getItemId(), reqLike.getDecision(), reqLike.getType(), authUser);
			pDto = pMapper.publicatedImageToPublicatedImageDto(p);
			pDto.setLiked(reqLike.getDecision().toString());
			pDto.setNumberPositiveLikes(lService.getLikesNumberByItemIdAndDecision(p.getId(), true).toString());
			pDto.setNumberNegativeLikes(lService.getLikesNumberByItemIdAndDecision(p.getId(), false).toString());
			return pDto;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + reqLike.getType());
		}
	}

	@Override
	public PublicatedImageDto deleteByPublicatedImageId(Long publicatedImageId) {
		PublicatedImage publicatedImage = pImaService.getById(publicatedImageId);
		lService.deleteByItemId(publicatedImageId);// now in theory if all was ok the like from the auth user is
													// deleted, so return publicatedImage with like = null
		return pMapper.publicatedImageToPublicatedImageDto(publicatedImage);
	}

}
