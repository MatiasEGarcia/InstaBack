package com.instaJava.instaJava.application;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.instaJava.instaJava.dto.request.ReqLike;
import com.instaJava.instaJava.dto.response.LikeDto;
import com.instaJava.instaJava.dto.response.PublicatedImageDto;
import com.instaJava.instaJava.entity.Like;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.service.LikeService;
import com.instaJava.instaJava.service.PublicatedImageService;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.mapper.LikeMapper;
import com.instaJava.instaJava.mapper.PublicatedImageMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeApplicationImpl implements LikeApplication{
	
	private final LikeService lService;
	private final PublicatedImageService pImaService;
	private final LikeMapper lMapper;
	private final PublicatedImageMapper pMapper;
	private final MessagesUtils messUtils;

	@Override
	public LikeDto save(ReqLike reqLike) {
		if(reqLike == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		}
		User userOwner = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Like likeSaved;
		validateReqLike(reqLike, userOwner.getId());
		if (!reqLike.isValid())
			throw new InvalidActionException(messUtils.getMessage("like.not-valid"),HttpStatus.BAD_REQUEST);
		likeSaved = lService.save(reqLike.getItemId(), reqLike.getDecision(), reqLike.getType(), userOwner);
		return lMapper.likeToLikeDto(likeSaved);
	}
	
	
	@Override
	public PublicatedImageDto deleteByPublicatedImageId(Long publicatedImageId) {
		PublicatedImage publicatedImage = pImaService.getById(publicatedImageId); 
		lService.deleteByItemId(publicatedImageId);//now in theory if all was ok the like from the auth user is deleted, so return publicatedImage with like = null
		return pMapper.publicatedImageToPublicatedImageDto(publicatedImage);
	}

	
	/**
	 * @param reqLike. is the object with the data about the item to be liked.
	 * @param ownerId. is the id of the user owner of the like.
	 * @see {@link #validateReqLikeList(List<ReqLike>, Long) validateReqLikeList} method
	 */
	private void validateReqLike(ReqLike reqLike, Long ownerId) {
		validateReqLikeList(List.of(reqLike), ownerId);
	}
	
	/**
	 * It Validates that an item exists from the id passed in itemId, and that 
	 * a like record with the same owner and item don't exist.
	 * If a like record already exists will set the ReqLike object as valid = false, else true.
	 * If the item to be liked does not exist will set the ReqLike object as valid = false, 
	 * else true.
	 * 
	 * @param reqLike is the object with the data about the item to be liked
	 * @param ownerId is the id of the user owner of the like
	 * @return a list of ReqLike with the valid attribute settled as true or false
	 * @throws illegalArgumentException if TypeItemLikedEnum no exists
	 */
	private void validateReqLikeList(List<ReqLike> reqLikeList, Long ownerId) {
		reqLikeList.forEach((like) -> {
			
			switch (like.getType()) {
			case PULICATED_IMAGE:
				if (pImaService.findById(like.getItemId()).isEmpty()
						|| lService.exist(like.getType(), like.getItemId(), ownerId)) {
					like.setValid(false);
				} else {
					like.setValid(true);
				}
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + like.getType());
			}

			
		});
	}


	
}
