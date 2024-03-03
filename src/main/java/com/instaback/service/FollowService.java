package com.instaback.service;

import org.springframework.data.domain.Page;

import com.instaback.dto.PageInfoDto;
import com.instaback.dto.request.ReqSearchList;
import com.instaback.entity.Follow;
import com.instaback.entity.User;
import com.instaback.enums.FollowStatus;
import com.instaback.exception.AlreadyExistsException;
import com.instaback.exception.InvalidActionException;
import com.instaback.exception.RecordNotFoundException;

public interface FollowService {

	/**
	 * 
	 * Get the user wanted to follow, set as follower the autheticated user and save
	 * the Follower record.
	 * @param userFollowed - user wanted to follow
	 * @throws IllegalArgumentException - if @param userFollowed.id is null
	 * @throws AlreadyExistsException - if follow record already exists.
	 * @return FollowDto object with Follow record created info.
	 */
	Follow save(User userFollowed);

	/**
	 * 
	 * It gets a Follow page collection with the records that met the requirements
	 * on @param reqSearchList.
	 * 
	 * @param reqSearchList. Contain ReqSearch collection with info to create specification object and a
	 * {@link com.instaJava.instaJava.enums.GlobalOperatorEnum} to combine queries.
	 * @param pageInfoDto -  It has pagination info.
	 * @return list of follows with pagination info.
	 * @throws IllegalArgumentException if @param reqSearchList or @param pageInfoDto or pageInfoDto.SortField or 
	 * pageInfoDto.SortDir are null.
	 */
	Page<Follow> search(PageInfoDto pageInfoDto, ReqSearchList reqSearchList);


	/**
	 * Update followStatus in the Follow record. just the followed user can update follow status
	 * 
	 * @return Updated Follow.
	 * @throws IllegalArgumentException if any of the params are null.
	 * @throws InvalidActionException if the user authenticated and who wants to
	 *                                  change the follow status are not the
	 *                                  followed user in the Follow record.
	 */
	Follow updateFollowStatusById(Long id, FollowStatus newStatus);
	
	/**
	 * Method to update follow status on follow record where auth user is the followed and the otherUser follower.
	 * 
	 * @param followerUserId - follower user
	 * @param flag - flag to know if the other user is follower or followed. other user is follower = true, otherwise false.
	 * @param newStatus - new follow status.
	 * @return Updated Follow record;
	 */
	Follow updateFollowStatusByFollowerId(Long followerUserId, FollowStatus newStatus);

	/**
	 * To see if a follow exists by followed and current user
	 * authenticated(follower).
	 * 
	 * @param followedId - id of the user followed.
	 * @return true if exist, otherwise false;
	 * @throws IllegalArgumentException if followedId param is null.
	 */
	boolean existsByFollowedAndFollower(Long followedId);// follower is the current user authenticated

	/**
	 * Delete follow record by id.
	 * 
	 * @param id. Id of the Follow record to delete
	 * @return deleted follow.
	 * @throws IllegalArgumentException if @param id is null.
	 * @throws InvalidActionException if follower != auth user
	 */
	Follow deleteById(Long id);
	
	/**
	 * To delete a follow record by it's followed id, and auth user as follower
	 * @param followedId - followed's id.
	 * @return deleted follow.
	 * @throws RecordNotFoundException if none follow record was found.
	 */
	Follow deleteByFollwedId(Long followedId);

	/**
	 * 
	 * Get the User {@link com.instaback.enums.FollowStatus} by id of the
	 * user.
	 * 
	 * @throws IllegalArgumentException if @param id is null;
	 * @return FollowStatus between authUser and user onwer of id given. Where authenticated user is the follower user.
	 */
	FollowStatus getFollowStatusByFollowedId(Long followedId);
	
	/**
	 * 
	 * @param followerId
	 * @return FollowStatus between authUser and user onwer of id given. Where authenticated user is the followed user.
	 */
	FollowStatus getFollowStatusByFollowerId(Long followerId);

	/**
	 * Count how many follow records there are by followed user and follow status.
	 * 
	 * @param id - id of the user that want to know how many users it follow.
	 * @param followStatus - follow status to use as param to search.
	 * @return number of follow records
	 * @throws IllegalArgumentException if one param is null
	 */
	Long countByFollowStatusAndFollowed(FollowStatus followStatus,Long followedId);

	/**
	 * Count how many follow records there are by follower user and follow status.
	 * 
	 * @param id -  id of the user wanted to know how many followers have
	 * @param followStatus - follow status to use as param to search.
	 * @return number of follow records
	 * @throws IllegalArgumentException  if one param is null
	 */
	Long countByFollowStatusAndFollower(FollowStatus followStatus, Long followerId);
}
