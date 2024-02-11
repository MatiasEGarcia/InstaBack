package com.instaJava.instaJava.application;

import org.springframework.data.domain.Sort.Direction;

import com.instaJava.instaJava.dto.FollowDto;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.enums.FollowStatus;

public interface FollowApplication {

	/**
	 * 
	 * Save Follow record by followed's id.
	 * 
	 * @param FollowedId. user id to follow.
	 * 
	 */
	FollowDto save(Long followedId);
	
	/**
	 * * It gets a Follow page collection with the records that met the requirements
	 * on @param reqSearchList.
	 * @param pageNo.    - For pagination, number of the page.
	 * @param pageSize.  - For pagination, size of the elements in the same page.
	 * @param sortField. - For pagination, sorted by..
	 * @param sortDir.   - In what direction is sorted, asc or desc.
	 * @param reqSearchList
	 * @return
	 */
	ResPaginationG<FollowDto> search(int pageNo, int pageSize, String sortField, Direction sortDir, ReqSearchList reqSearchList);
	
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
	 * Delete follow record by id.
	 * 
	 * @param id. Id of the Follow record to delete
	 * @return deleted follow.
	 */
	FollowDto deleteById(Long id);
	
	/**
	 * To delete a follow record by it's followed id, and auth user as follower
	 * @param followedId - followed's id.
	 * @return deleted follow.
	 */
	FollowDto deleteByFollwedId(Long followedId);
	
	/**
	 * Method to update follow status on follow record where auth user is the followed and the otherUser follower.
	 * 
	 * @param followerUserId - follower user's id
	 * @param flag - flag to know if the other user is follower or followed. other user is follower = true, otherwise false.
	 * @param newStatus - new follow status.
	 * @return Updated Follow record;
	 */
	FollowDto updateFollowStatusByFollowerId(Long followerUserId, FollowStatus newStatus);

}
