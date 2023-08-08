package com.instaJava.instaJava.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.FollowDao;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.enums.GlobalOperationEnum;
import com.instaJava.instaJava.enums.OperationEnum;
import com.instaJava.instaJava.exception.InvalidException;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService{
	
	private final UserService userService;
	private final FollowDao followDao;
	private final MessagesUtils messUtils;
	private final SpecificationService<Follow> specService;
	private final PageableUtils pagUtils;
	
	/**
	 * 
	 * Get the user wanted to follow, set as follower the autheticated user and 
	 * save the Follower record.
	 * 
	 * @param FollowedId. user id to follow.
	 * @throws IllegalArgumentException if @param FollowedId is null
	 * @throws IllegalArgumentException if user to follow not exist.
	 * @return Follow record created.
	 */
	@Override
	@Transactional
	public Follow save(Long FollowedId) {
		if(FollowedId == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		Follow follower = Follow.builder().build();
		Optional<User> optUserFollowed = userService.getById(FollowedId);
		if(optUserFollowed.isEmpty()) throw new IllegalArgumentException(messUtils.getMessage("exception.followed-no-exist"));
		User userFollower = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		follower.setFollowed(optUserFollowed.get());
		follower.setFollower((userService.getById(userFollower.getUserId()).get()));//persistence context?If I don't do this user is detached
		if(optUserFollowed.get().isVisible()) {
			follower.setFollowStatus(FollowStatus.ACCEPTED);
		}else {
			follower.setFollowStatus(FollowStatus.IN_PROCESS);
		}
		return followDao.save(follower);
	}

	
	/**
	 * 
	 * It gets a Follow page collection with the records that met the requirements on @param reqSearchList.
	 * 
	 * @param reqSearchList. Contain ReqSearch collection with info to create specification 
	 * object and a {@link com.instaJava.instaJava.enums.GlobalOperatorEnum} to combine queries.
	 * @param pageInfoDto. It has pagination info.
	 * @return Page collection with Follow records.
	 * @throws IllegalArgumentException if @param reqSearchList or @param pageInfoDto or pageInfoDto.SortField or
	 * pageInfoDto.SortDir are null.
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<Follow> search(PageInfoDto pageInfoDto, ReqSearchList reqSearchList) {
		if(pageInfoDto == null || reqSearchList == null || 
				pageInfoDto.getSortField() == null || pageInfoDto.getSortDir() == null) throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument-not-null-empty"));
		Specification<Follow> spec = specService.getSpecification(reqSearchList.getReqSearchs()
				, reqSearchList.getGlobalOperator());
		return followDao.findAll(spec, pagUtils.getPageable(pageInfoDto));
	}
	
	/**
	 * Update followStatus in the Follow record.
	 * 
	 * @return Follow record that was updated.
	 * @throws IllegalArgumentException if any of the params are null.
	 * @throws IllegalArgumentException if the user authenticated and who wants to change
	 * the follow status are not the followed user in the Follow record. 	 
	 * */
	
	@Override
	@Transactional
	public Follow updateFollowStatusById(Long id, FollowStatus newStatus){
		if(newStatus == null || id == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		User userFollowed;
		Follow follower = findById(id);
		userFollowed = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(!follower.getFollowed().equals(userFollowed)) throw new IllegalArgumentException(messUtils.getMessage("exception.followed-is-not-same"));
		follower.setFollowStatus(newStatus);
		return followDao.save(follower);
	}

	/**
	 * 
	 * Find Follow record by id.
	 * 
	 * @param id. Id of the Follow record.
	 * @return Follow record.
	 * @throws IllegalArgumentException if the follow record no exists
	 */
	@Override
	@Transactional(readOnly = true)
	public Follow findById(Long id) {
		Optional<Follow> followerOpt = followDao.findById(id);
		if(followerOpt.isEmpty()) throw new InvalidException(messUtils.getMessage("exception.follow-id-not-found"));
		return followerOpt.get();
	}

	/**
	 * Get Follow record by id and compare the owner with the user authenticated, 
	 * if are same user then delete the follow record.
	 * 
	 * @param id. Id of the Follow record to delete
	 * @throws IllegalArgumentException if @param id is null
	 */
	@Override
	@Transactional
	public void deleteById(Long id) {
		if(id == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		Follow foll = this.findById(id);
		User userFollower = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(!foll.getFollower().equals(userFollower)) throw new InvalidException(messUtils.getMessage("exception.follower-is-not-same"));
		followDao.delete(foll);;
	}
	
	/**
	 * 
	 * Get the User {@link com.instaJava.instaJava.enums.FollowStatus} by id of the user.
	 * 
	 * @throws IllegalArgumentException if @param id is null;
	 * @throws IllegalArgumentException if followed user no exists
	 * @return FollowStatus.ACCEPTED if followed user.Visible is true. Else
	 * if followed user.Visible is false and there is not a follow record return. And if
	 * none of the others condition is met return the current FollowStatus.
	 */
	@Override
	@Transactional(readOnly = true)
	public FollowStatus getFollowStatusByFollowedId(Long id) {
		if(id == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		Optional<User> userFollowed;
		Optional<Follow> optFollow;
		User userFollower;
		ReqSearch followedSearchEqual;
		ReqSearch followerSearchEqual;
		userFollowed = userService.getById(id);
		if(userFollowed.isEmpty()) throw new IllegalArgumentException(messUtils.getMessage("exception.followed-no-exist"));
		if(userFollowed.get().isVisible()) return FollowStatus.ACCEPTED; // if the user is public/visible, then we return accepted
		userFollower = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		followedSearchEqual = ReqSearch.builder().column("userId").value(id.toString())
				.joinTable("followed").dateValue(false).operation(OperationEnum.EQUAL).build();
		followerSearchEqual = ReqSearch.builder().column("userId").value(userFollower.getUserId().toString())
				.joinTable("follower").dateValue(false).operation(OperationEnum.EQUAL).build();
		optFollow = followDao.findOne(specService.getSpecification(List.of(followedSearchEqual,followerSearchEqual), GlobalOperationEnum.AND));
		if(optFollow.isEmpty()) return FollowStatus.NOT_ASKED;
		return optFollow.get().getFollowStatus();
	}

	/**
	 * How many users a user follow, by id.
	 * 
	 * @param id. id of the user that want to know how many users it follow
	 * @return the number of users that are followed by the user searched
	 * @throws IllegalArgumentException if @param id is null
	 */
	@Override
	@Transactional(readOnly = true)
	public Long countFollowedByUserId(Long id) {
		if(id == null)  throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		ReqSearch searchFollowByFollowerIdEqual = ReqSearch.builder().column("userId").value(id.toString()).dateValue(false)
				.joinTable("follower").operation(OperationEnum.EQUAL).build();
		return followDao.count(specService.getSpecification(searchFollowByFollowerIdEqual));
	}

	/**
	 * How many users follow another user by id.
	 * 
	 * @param id. id of the user wanted to know how many followers have
	 * @return the number of users that follow the user searched
	 * @throws IllegalArgumentException if @param id is null
	 */
	@Override
	@Transactional(readOnly = true)
	public Long countFollowerByUserId(Long id) {
		if(id == null)  throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		ReqSearch searchFollowByFollowerIdEqual = ReqSearch.builder().column("userId").value(id.toString()).dateValue(false)
				.joinTable("followed").operation(OperationEnum.EQUAL).build();
		return followDao.count(specService.getSpecification(searchFollowByFollowerIdEqual));
	}

	


	
}
