package com.instaback.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.instaback.entity.Follow;
import com.instaback.enums.FollowStatus;

public interface FollowDao extends JpaRepository<Follow, Long>, JpaSpecificationExecutor<Follow> {

	/**
	 * 
	 * @param followedId - followed id.
	 * @param followerId - follower id.
	 * @return true if follow record exists , otherwise false.
	 */
	boolean existsByFollowedIdAndFollowerId(Long followedId, Long followerId);
	
	Optional<Follow> findOneByFollowedIdAndFollowerId(Long followedId, Long followerId);
	
	Long countByFollowerIdAndFollowStatus(Long follower, FollowStatus followStatus);
	
	Long countByFollowedIdAndFollowStatus(Long followed, FollowStatus followStatus);
}
