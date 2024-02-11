package com.instaJava.instaJava.dao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.instaJava.instaJava.dto.dao.IdValueDto;
import com.instaJava.instaJava.entity.Like;
import com.instaJava.instaJava.enums.TypeItemLikedEnum;

public interface LikeDao extends JpaRepository<Like, Long> ,JpaSpecificationExecutor<Like> {

	boolean existsByItemTypeAndItemIdAndOwnerLikeId(TypeItemLikedEnum itemType, Long itemId, Long onwerLike);
	
	@Query("SELECT NEW com.instaJava.instaJava.dto.dao.IdValueDto(p.id AS id , l.decision AS value) "
			+ "FROM PublicatedImage p "
			+ "LEFT JOIN Like l ON p.id = l.itemId   AND l.ownerLike.id = :ownerLikeId "
			+ "WHERE p.id IN(:publicationsId)")
	List<IdValueDto<Boolean>> getDecisionsByItemIdAndOwnerLikeId(@Param(value="publicationsId")Set<Long> publicationsId,
			@Param(value = "ownerLikeId") Long id);
	
	Optional<Like> getByItemIdAndOwnerLikeId(Long itemId, Long ownerLikeId);
	
	
}
