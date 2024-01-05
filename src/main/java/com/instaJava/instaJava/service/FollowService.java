package com.instaJava.instaJava.service;

import com.instaJava.instaJava.dto.FollowDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.exception.AlreadyExistsException;

public interface FollowService {

	/**
	 * 
	 * Get the user wanted to follow, set as follower the autheticated user and save
	 * the Follower record.
	 * 
	 * @param FollowedId. user id to follow.
	 * @throws IllegalArgumentException - if @param FollowedId is null
	 * @throws AlreadyExistsException - if follow record already exists.
	 * @return FollowDto object with Follow record created info.
	 */
	FollowDto save(Long FollowedId);

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
	ResPaginationG<FollowDto> search(PageInfoDto pageInfoDto, ReqSearchList reqSearchList);


	/**
	 * Update followStatus in the Follow record.
	 * 
	 * @return FollowDto object with Follow record updated info.
	 * @throws IllegalArgumentException if any of the params are null.
	 * @throws IllegalArgumentException if the user authenticated and who wants to
	 *                                  change the follow status are not the
	 *                                  followed user in the Follow record.
	 */
	FollowDto updateFollowStatusById(Long id, FollowStatus newStatus);
	
	/**
	 * Method to update follow status on follow record where auth user is the followed and the otherUser follower.
	 * 
	 * @param followerUserId - follower user
	 * @param flag - flag to know if the other user is follower or followed. other user is follower = true, otherwise false.
	 * @param newStatus - new follow status.
	 * @return FollowDto object with Follow record updated info.
	 */
	FollowDto updateFollowStatusByFollower(Long followerUserId, FollowStatus newStatus);
	
	/**
	 * 
	 * Find Follow record by id.
	 * 
	 * @param id. Id of the Follow record.
	 * @return FollowDto object with follow record info.
	 * @throws IllegalArgumentException if the follow record no exists
	 */
	FollowDto findById(Long id);

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
	 * Get Follow record by id and compare the owner with the user authenticated, if
	 * are same user then delete the follow record.
	 * 
	 * @param id. Id of the Follow record to delete
	 * @throws IllegalArgumentException if @param id is null.
	 * @throws InvalidActionException if follower != auth user
	 */
	void deleteById(Long id);

	/**
	 * 
	 * Get the User {@link com.instaJava.instaJava.enums.FollowStatus} by id of the
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
