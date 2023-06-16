package com.instaJava.instaJava.service;

import static java.util.stream.Collectors.toList;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
	private final SpecificationService<Like> likeSpecService;
	// cuando agrege los comentarios tengo que agregar su service

	/**
	 * Check if Like record exist and Delete a Like record by likeId.
	 * @param likeId. id of the Like record.
	 * @throws IllegalArgumentException if @param likeId is null.
	 * @return 0 if Like record no exist, else 1. 
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

	/**
	 * 
	 * To check if a Like record exist by type, item and owner id.
	 * 
	 * @param type. type of the item that was liked, ej : COMMENT.
	 * @param itemId. id of the item
	 * @param ownerLikeId. id of the user owner of the Like record.
	 * @return true if the Like record already exists, else false.
	 * @throws IllegalArgumentException if anyone of the params are null.
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean exist(TypeItemLikedEnum type, Long itemId, Long ownerLikeId) {
		if (type == null || itemId == null || ownerLikeId == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));

		ReqSearch typeEqual = ReqSearch.builder().column("itemType").value(type.toString()).dateValue(false)
				.operation(OperationEnum.EQUAL).build();
		ReqSearch itemIdEqual = ReqSearch.builder().column("itemId").value(itemId.toString()).dateValue(false)
				.operation(OperationEnum.EQUAL).build();
		ReqSearch ownerEqual = ReqSearch.builder().joinTable("ownerLike").column("userId").value(ownerLikeId.toString())
				.dateValue(false).operation(OperationEnum.EQUAL).build();
		
		Specification<Like> spec = likeSpecService.getSpecification(List.of(typeEqual,itemIdEqual,ownerEqual),GlobalOperationEnum.AND);
		return likeDao.exists(spec);
	}

	/**
	 * 
	 * Save a Like collection in the database if the ReqLike was valid.
	 * 
	 * @param reqLikeList. Collection with ReqLike objects.
	 * @return Empty collection if reqLikeList is empty or none ReqLike was valid, 
	 * else a Like collection with the records saved
	 * @throws IllegalArgumentException if @param reqLikeList is null
	 */
	@Override
	@Transactional
	public List<Like> saveAll(List<ReqLike> reqLikeList) {
		if (reqLikeList == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
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

	/**
	 * Save a like object in the database.
	 * 
	 * @param reqLike. object with the data of the Like to be saved.
	 * @throws IllegalArgumentException if the param is null.
	 * @return Empty optional if the reqLike is not valid, else Like optional.
	 */
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

	
	@Override
	@Transactional(readOnly = true)
	public Map<String, String> getPositiveAndNegativeLikesByItemId(Long id) {
		if(id == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		List<Like> likes;
		Map<Boolean,Long> countByDecision;
		Map<String,String> positiveAndNegativeLikeCount = new HashMap<>();
		ReqSearch reqSearch= ReqSearch.builder().column("itemId").value(id.toString()).operation(OperationEnum.EQUAL).build();
		likes = likeDao.findAll(likeSpecService.getSpecification(reqSearch));
		countByDecision = likes.stream()
				.collect(Collectors.groupingBy(Like::isDecision,Collectors.counting()));
		positiveAndNegativeLikeCount.put("Positive", countByDecision.get(true).toString());
		positiveAndNegativeLikeCount.put("Negative", countByDecision.get(false).toString());
		return positiveAndNegativeLikeCount;
	}
	
	
	/**
	 * @param reqLike. is the object with the data about the item to be liked.
	 * @param ownerId. is the id of the user owner of the like.
	 * @return ReqLike with the valid attribute settled as true or false.
	 * @see {@link #validateReqLikeList(List<ReqLike>, Long) validateReqLikeList} method
	 */
	private ReqLike validateReqLike(ReqLike reqLike, Long ownerId) {
		return this.validateReqLikeList(List.of(reqLike), ownerId).get(0);
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
