package com.instaJava.instaJava.dao;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.instaJava.instaJava.entity.Like;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;

public interface LikeDao extends JpaRepository<Like, Long> ,JpaSpecificationExecutor<Like> {

	boolean existsByItemTypeAndItemIdAndOwnerLikeId(TypeItemLikedEnum itemType, Long itemId, Long onwerLike);
	
	@Query("SELECT p.id , l.decision "
			+ "FROM PublicatedImage p "
			+ "LEFT JOIN Like l ON p.id = l.itemId   AND l.ownerLike.id = :ownerLikeId "
			+ "WHERE p.id IN(:publicationsId)")
	Map<Long, Boolean> getDecisionsByItemIdAndOwnerLikeId(@Param(value="publicationsId")Set<Long> publicationsId,
			@Param(value = "ownerLikeId") Long id);
	
	Optional<Like> deleteByItemIdAndOwnerLikeId(Long itemId, Long ownerLikeId);
	
}
