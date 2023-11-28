package com.instaJava.instaJava.service;

import java.util.Optional;

import com.instaJava.instaJava.dto.FollowDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.enums.FollowStatus;

public interface FollowService {

	/**
	 * 
	 * Get the user wanted to follow, set as follower the autheticated user and save
	 * the Follower record.
	 * 
	 * @param FollowedId. user id to follow.
	 * @throws IllegalArgumentException - if @param FollowedId is null
	 * @throws EntityNotFoundException - if user to follow not exist.
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
	 * @return Optional with Follow object if exists, if not optional empty.
	 */
	Optional<Follow> findByFollowedAndFollower(Long followedId);// follower is the current user authenticated

	/**
	 * Get Follow record by id and compare the owner with the user authenticated, if
	 * are same user then delete the follow record.
	 * 
	 * @param id. Id of the Follow record to delete
	 * @throws IllegalArgumentException if @param id is null
	 */
	void deleteById(Long id);

	/**
	 * 
	 * Get the User {@link com.instaJava.instaJava.enums.FollowStatus} by id of the
	 * user.
	 * 
	 * @throws IllegalArgumentException if @param id is null;
	 * @throws IllegalArgumentException if followed user no exists
	 * @return FollowStatus.ACCEPTED if followed user.Visible is true. Else if
	 *         followed user.Visible is false and there is not a follow record
	 *         return. And if none of the others condition is met return the current
	 *         FollowStatus.
	 */
	FollowStatus getFollowStatusByFollowedId(Long followedId);

	/**
	 * How many users a user follow, by id. (only if the followed accept the follow)
	 * 
	 * @param id. id of the user that want to know how many users it follow
	 * @return the number of users that are followed by the user searched
	 * @throws IllegalArgumentException if @param id is null
	 */
	Long countAcceptedFollowedByUserId(Long id);

	/**
	 * How many users follow another user by id.(only if the followed accept the
	 * follow)
	 * 
	 * @param id. id of the user wanted to know how many followers have
	 * @return the number of users that follow the user searched
	 * @throws IllegalArgumentException if @param id is null
	 */
	Long countAcceptedFollowerByUserId(Long id);

}
