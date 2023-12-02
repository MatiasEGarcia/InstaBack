package com.instaJava.instaJava.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.enums.FollowStatus;

public interface FollowDao extends JpaRepository<Follow, Long>, JpaSpecificationExecutor<Follow> {

	/**
	 * 
	 * @param followedId - followed id.
	 * @param followerId - follower id.
	 * @return true if follow record exists , otherwise false.
	 */
	boolean existsByFollowedUserIdAndFollowerUserId(Long followedId, Long followerId);
	
	Optional<Follow> findOneByFollowedUserIdAndFollowerUserId(Long followedId, Long followerId);
	
	Long countByFollowerUserIdAndFollowStatus(Long follower, FollowStatus followStatus);
	
	Long countByFollowedUserIdAndFollowStatus(Long followed, FollowStatus followStatus);
}
