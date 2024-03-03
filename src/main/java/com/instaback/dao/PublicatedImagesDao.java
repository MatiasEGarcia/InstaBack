package com.instaback.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.instaback.entity.PublicatedImage;

public interface PublicatedImagesDao extends JpaRepository<PublicatedImage, Long> , JpaSpecificationExecutor<PublicatedImage>{
	
	Page<PublicatedImage> findByUserOwnerVisible(Boolean visible, Pageable pageable);
	
	Page<PublicatedImage> findByUserOwnerId(Long ownerId, Pageable pageable);
	
	Long countByUserOwnerId(Long userOwnerId);
	
	/**
	 * To get publications from users followed by authenticated user.
	 * 
	 * @param authUserId - auth user id.
	 * @param pageable - pageable info, number page, number of elements, sort field and sort direction.
	 * @return Page with the publication in this iteration.
	 */
	@Query("SELECT p "
			+ "FROM PublicatedImage p "
			+ "JOIN Follow f "
			+ "ON p.userOwner = f.followed "
			+ "WHERE f.follower.Id = :authUserId "
			+ "AND f.followStatus = ACCEPTED")
	Page<PublicatedImage> findPublicationsFromUsersFollowed(@Param(value = "authUserId")Long authUserId, Pageable pageable);
}
