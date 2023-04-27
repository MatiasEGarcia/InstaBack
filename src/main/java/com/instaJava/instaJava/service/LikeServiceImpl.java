package com.instaJava.instaJava.service;

import static java.util.stream.Collectors.toList;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.LikeDao;
import com.instaJava.instaJava.dto.request.ReqLike;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.entity.Like;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.GlobalOperationEnum;
import com.instaJava.instaJava.enums.OperationEnum;
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
	private final SpecificationService<Like> specService;
	// cuando agrege los comentarios tengo que agregar su service

	/*
	 * @Override
	 * 
	 * @Transactional public Like save(TypeItemLikedEnum type, Long itemId, boolean
	 * decision) { if (type == null || itemId == null) throw new
	 * IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"))
	 * ; Like like; switch (type) { case PULICATED_IMAGE: if
	 * (publiImaService.getById(itemId).isEmpty()) throw new
	 * IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"))
	 * ; break; default: throw new
	 * IllegalArgumentException(messUtils.getMessage("exception.like-type-no-exist")
	 * ); } like = Like.builder().itemType(type).decision(decision).build();
	 * like.setItemId(itemId); like.setLikedAt(ZonedDateTime.now(clock));
	 * like.setOwnerLike((User)
	 * SecurityContextHolder.getContext().getAuthentication().getPrincipal());
	 * return likeDao.save(like); }
	 */

	/*
	 * return 0 if the record wasn't deleted return 1 if the record was deleted
	 */
	@Override
	@Transactional
	public int deleteById(Long likeId) {
		if (likeId == null)
			throw new IllegalArgumentException();
		User user;
		Optional<Like> optLike = likeDao.findById(likeId);
		if (optLike.isEmpty())
			return 0;
		user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!user.equals(optLike.get().getOwnerLike()))
			return 0;
		likeDao.delete(optLike.get());
		return 1;
	}

	// me falta testear de aca para abajo
	@Override
	@Transactional(readOnly = true)
	public boolean exist(TypeItemLikedEnum type, Long itemId, Long ownerLikeId) {
		if (type == null || itemId == null || ownerLikeId == null)
			throw new IllegalArgumentException();

		ReqSearch typeEqual = ReqSearch.builder().column("itemType").value(type.toString()).dateValue(false)
				.operation(OperationEnum.EQUAL).build();
		ReqSearch itemIdEqual = ReqSearch.builder().column("itemId").value(itemId.toString()).dateValue(false)
				.operation(OperationEnum.EQUAL).build();
		ReqSearch ownerEqual = ReqSearch.builder().joinTable("ownerLike").column("userId").value(ownerLikeId.toString())
				.dateValue(false).operation(OperationEnum.EQUAL).build();
		
		Specification<Like> spec = specService.getSpecification(List.of(typeEqual, itemIdEqual, ownerEqual), GlobalOperationEnum.AND);
		boolean bo = likeDao.exists(spec);
		return bo;
	}

	@Override
	@Transactional
	public List<Like> saveAll(List<ReqLike> reqLikeList) {
		if (reqLikeList == null)
			throw new IllegalArgumentException();
		if (reqLikeList.isEmpty())
			return Collections.emptyList();
		User userOwner = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		List<ReqLike> validatedReqLikeList = this.validateReqLikeList(reqLikeList, userOwner.getUserId());
		ZonedDateTime time = ZonedDateTime.now(clock);
		List<Like> likeListToSave = validatedReqLikeList.stream().filter(ReqLike::isValid).map((reqLike) -> {
			return Like.builder().itemId(reqLike.getItemId()).itemType(reqLike.getType()).likedAt(time)
					.ownerLike(userOwner).decision(reqLike.getDecision()).build();
		}).collect(toList());
		if (likeListToSave.isEmpty())
			return Collections.emptyList();
		return likeDao.saveAll(likeListToSave);
	}

	@Override
	@Transactional
	public Optional<Like> save(ReqLike reqLike) {
		if (reqLike == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		User userOwner = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		reqLike = validateReqLike(reqLike, userOwner.getUserId());
		if (!reqLike.isValid())
			return Optional.empty();
		return Optional.of(likeDao.save(Like.builder().itemId(reqLike.getItemId()).decision(reqLike.getDecision())
				.itemType(reqLike.getType()).ownerLike(userOwner).likedAt(ZonedDateTime.now(clock)).build()));
	}

	// we validate that the item exist and that the like no exist, so then can save
	// it
	private ReqLike validateReqLike(ReqLike reqLike, Long ownerId) {
		return this.validateReqLikeList(List.of(reqLike), ownerId).get(0);
	}

	private List<ReqLike> validateReqLikeList(List<ReqLike> reqLikeList, Long ownerId) {
		reqLikeList.forEach((l) -> {
			
			switch (l.getType()) {
			case PULICATED_IMAGE:
				System.out.println("-------------------------------------------------");
				if (publiImaService.getById(l.getItemId()).isEmpty()
						|| this.exist(l.getType(), l.getItemId(), ownerId)) {
					l.setValid(false);
				} else {
					l.setValid(true);
				}
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + l.getType());
			}

			
		});
		return reqLikeList;
	}

}
