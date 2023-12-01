package com.instaJava.instaJava.service;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.LikeDao;
import com.instaJava.instaJava.dto.request.ReqLike;
import com.instaJava.instaJava.dto.response.LikeDto;
import com.instaJava.instaJava.entity.Like;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.mapper.LikeMapper;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

	private final Clock clock;
	private final LikeDao likeDao;
	private final PublicatedImageService publiImaService;
	private final MessagesUtils messUtils;
	private final LikeMapper likeMapper;
	// cuando agrege los comentarios tengo que agregar su service

	
	@Override
	@Transactional
	public void deleteById(Long likeId) {
		if (likeId == null)
			throw new IllegalArgumentException();
		User user;
		Optional<Like> optLike = likeDao.findById(likeId);
		if (optLike.isEmpty()) {
			throw new RecordNotFoundException(messUtils.getMessage("like.not-found"), List.of(likeId.toString()), HttpStatus.NOT_FOUND);
		}
		user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!user.equals(optLike.get().getOwnerLike()))
			throw new InvalidActionException(messUtils.getMessage("generic.auth-user-no-owner"),HttpStatus.BAD_REQUEST);
		likeDao.delete(optLike.get());
	}

	
	@Override
	@Transactional(readOnly = true)
	public boolean exist(TypeItemLikedEnum type, Long itemId, Long ownerLikeId) {
		if (type == null || itemId == null || ownerLikeId == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));

		return likeDao.existsByItemTypeAndItemIdAndOwnerLike(type, itemId, ownerLikeId);
	}

	@Override
	@Transactional
	public LikeDto save(ReqLike reqLike) {
		if (reqLike == null || reqLike.getItemId() == null|| reqLike.getType() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		}
		User userOwner = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		validateReqLike(reqLike, userOwner.getUserId());
		if (!reqLike.isValid())
			throw new InvalidActionException(messUtils.getMessage("like.not-valid"),HttpStatus.BAD_REQUEST);
		
		Like likeToSave = Like.builder()
				.itemId(reqLike.getItemId())
				.decision(reqLike.isDecision())
				.itemType(reqLike.getType())
				.ownerLike(userOwner)
				.likedAt(ZonedDateTime.now(clock))
				.build(); 
		 
		return likeMapper.likeToLikeDto(likeDao.save(likeToSave)); 
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
				if (publiImaService.findById(like.getItemId()).isEmpty()
						|| this.exist(like.getType(), like.getItemId(), ownerId)) {
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
