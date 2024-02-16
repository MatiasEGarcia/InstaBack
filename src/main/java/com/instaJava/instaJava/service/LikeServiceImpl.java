package com.instaJava.instaJava.service;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.LikeDao;
import com.instaJava.instaJava.dto.dao.IdValueDto;
import com.instaJava.instaJava.entity.IBaseEntity;
import com.instaJava.instaJava.entity.Like;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.SearchsUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

	private final Clock clock;
	private final LikeDao likeDao;
	private final MessagesUtils messUtils;
	private final SearchsUtils searchsUtils;

	
	@Override
	@Transactional
	public void deleteById(Long likeId) {
		if (likeId == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
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
	@Transactional
	public void deleteByItemId(Long itemId) {
		if(itemId == null) 
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		Like likeToDelete;
		User authUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		likeToDelete = likeDao.getByItemIdAndOwnerLikeId(itemId, authUser.getId()).orElseThrow(() -> 
		new RecordNotFoundException(messUtils.getMessage("like.not-found"), HttpStatus.NOT_FOUND));
		likeDao.delete(likeToDelete);
	}
	
	@Override
	@Transactional(readOnly = true)
	public boolean exist(TypeItemLikedEnum type, Long itemId, Long ownerLikeId) {
		if (type == null || itemId == null || ownerLikeId == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));

		return likeDao.existsByItemTypeAndItemIdAndOwnerLikeId(type, itemId, ownerLikeId);
	}

	@Override
	@Transactional
	public Like save(Long itemId,Boolean decision, TypeItemLikedEnum type, User userOwner) {
		if(itemId == null || decision == null || type == null || userOwner == null || userOwner.getId() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		}
		Like likeToSave = new Like(type, itemId, decision, userOwner, ZonedDateTime.now(clock));
		return likeDao.save(likeToSave);
	}

	//some correction, should return a new list,immutability
	@Override
	@Transactional(readOnly = true)
	public void setItemDecisions(List<? extends IBaseEntity> listItems) {
		if(listItems == null || listItems.isEmpty()) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		}
		List<IdValueDto<Boolean>> decisions;
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Set<Long> setPublicatedImageIds = new HashSet<Long>();
		for(IBaseEntity p : listItems) {
			setPublicatedImageIds.add(p.getBaseEntityId());
		}
		//searching decisions
		decisions = likeDao.getDecisionsByItemIdAndOwnerLikeId(setPublicatedImageIds,authUser.getId());
		//seting liked opinion
		for(IdValueDto<Boolean> idValueDto : decisions) {
			Boolean value = idValueDto.getValue();
			if(value != null) {
				int itemIndex = searchsUtils.bynarySearchById(listItems, idValueDto.getId());
				listItems.get(itemIndex).setItemEntityLiked(value.booleanValue());
			}
		}
	}

	//TESTSS
	@Override
	public void setItemDecision(IBaseEntity item) {
		this.setItemDecisions(List.of(item));
	}

	@Override
	public Long getLikesNumberByItemIdAndDecision(Long itemId,boolean decision) {
		if(itemId == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		return likeDao.countByItemIdAndDecision(itemId,decision);
	}
}
