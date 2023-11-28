package com.instaJava.instaJava.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.FollowDao;
import com.instaJava.instaJava.dto.FollowDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.enums.GlobalOperationEnum;
import com.instaJava.instaJava.enums.OperationEnum;
import com.instaJava.instaJava.exception.AlreadyExistsException;
import com.instaJava.instaJava.exception.InvalidException;
import com.instaJava.instaJava.mapper.FollowMapper;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

	private final UserService userService;
	private final FollowDao followDao;
	private final MessagesUtils messUtils;
	private final FollowMapper followMapper;
	private final SpecificationService<Follow> specService;
	private final PageableUtils pagUtils;
	private final NotificationService notificationService;
	
	
	@Override
	@Transactional
	public FollowDto save(Long followedId) {
		if (followedId == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		User userFollower;
		Follow follow;
		Optional<Follow> optionalFollow;
		String customMessage;
		// check if the user wanted to follow exists.
		Optional<User> optUserFollowed = userService.getById(followedId);
		if (optUserFollowed.isEmpty())
			throw new EntityNotFoundException(messUtils.getMessage("exception.followed-no-exist"));
		// check if the follow record already exists.
		userFollower = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		optionalFollow = findByFollowedAndFollower(followedId);
		if (optionalFollow.isPresent()) {
			throw new AlreadyExistsException(messUtils.getMessage("exception.follow-already-exists"));
		}
		follow = Follow.builder().followed(optUserFollowed.get()).follower(userFollower).build();
		// set follow status by visible state of the user wanted to follow
		if (optUserFollowed.get().isVisible()) {
			follow.setFollowStatus(FollowStatus.ACCEPTED);
			customMessage = "A new user is following you";
		} else {
			follow.setFollowStatus(FollowStatus.IN_PROCESS);
			customMessage = "A new user wants to follow you";
		}
		//save notification for the followed user
		notificationService.saveNotificationOfFollow(follow, customMessage);
		Follow followCreated = followDao.save(follow);
		return followMapper.followToFollowDto(followCreated);
	}

	
	@Override
	@Transactional(readOnly = true)
	public ResPaginationG<FollowDto> search(PageInfoDto pageInfoDto, ReqSearchList reqSearchList) {
		if (pageInfoDto == null || reqSearchList == null || pageInfoDto.getSortField() == null
				|| pageInfoDto.getSortDir() == null)
			throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument-not-null-empty"));
		Specification<Follow> spec = specService.getSpecification(reqSearchList.getReqSearchs(),
				reqSearchList.getGlobalOperator());
		Page<Follow> page = followDao.findAll(spec, pagUtils.getPageable(pageInfoDto));
		return followMapper.pageAndPageInfoDtoToResPaginationG(page, pageInfoDto);
	}

	@Override
	@Transactional
	public FollowDto updateFollowStatusById(Long id, FollowStatus newStatus) {
		if (newStatus == null || id == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		User userFollowed;
		FollowDto follower = findById(id);
		userFollowed = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!follower.getFollowed().getUserId().equalsIgnoreCase(userFollowed.getUserId().toString()))
			throw new IllegalArgumentException(messUtils.getMessage("exception.followed-is-not-same"));
		follower.setFollowStatus(newStatus);
		Follow followCreated = followDao.save(followMapper.followDtoToFollow(follower));
		return followMapper.followToFollowDto(followCreated);
	}

	
	@Override
	@Transactional(readOnly = true)
	public FollowDto findById(Long id) {
		Optional<Follow> followerOpt = followDao.findById(id);
		if (followerOpt.isEmpty())
			throw new InvalidException(messUtils.getMessage("exception.follow-id-not-found"));
		Follow followFound = followerOpt.get();
		return followMapper.followToFollowDto(followFound);
	}

	
	@Override
	@Transactional(readOnly = true)
	public Optional<Follow> findByFollowedAndFollower(Long followedId) {
		if (followedId == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		User follower = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		ReqSearch equalFollowed = ReqSearch.builder().column("followed").value(followedId.toString()).dateValue(false)
				.operation(OperationEnum.EQUAL).build();
		ReqSearch equalFollower = ReqSearch.builder().column("follower").value(follower.getUserId().toString())
				.dateValue(false).operation(OperationEnum.EQUAL).build();
		return followDao
				.findOne(specService.getSpecification(List.of(equalFollowed, equalFollower), GlobalOperationEnum.AND));
	}

	
	@Override
	@Transactional
	public void deleteById(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		FollowDto foll = this.findById(id);
		User userFollower = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!foll.getFollower().getUserId().equalsIgnoreCase(userFollower.getUserId().toString()))
			throw new InvalidException(messUtils.getMessage("exception.follower-is-not-same"));
		followDao.delete(followMapper.followDtoToFollow(foll));
	}

	
	@Override
	@Transactional(readOnly = true)
	public FollowStatus getFollowStatusByFollowedId(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		Optional<User> userFollowed;
		Optional<Follow> optFollow;
		User userFollower;
		ReqSearch followedSearchEqual;
		ReqSearch followerSearchEqual;
		userFollowed = userService.getById(id);
		if (userFollowed.isEmpty())
			throw new IllegalArgumentException(messUtils.getMessage("exception.followed-no-exist"));
		if (userFollowed.get().isVisible())
			return FollowStatus.ACCEPTED; // if the user is public/visible, then we return accepted
		userFollower = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		followedSearchEqual = ReqSearch.builder().column("userId").value(id.toString()).joinTable("followed")
				.dateValue(false).operation(OperationEnum.EQUAL).build();
		followerSearchEqual = ReqSearch.builder().column("userId").value(userFollower.getUserId().toString())
				.joinTable("follower").dateValue(false).operation(OperationEnum.EQUAL).build();
		optFollow = followDao.findOne(specService.getSpecification(List.of(followedSearchEqual, followerSearchEqual),
				GlobalOperationEnum.AND));
		if (optFollow.isEmpty())
			return FollowStatus.NOT_ASKED;
		return optFollow.get().getFollowStatus();
	}

	
	@Override
	@Transactional(readOnly = true)
	public Long countAcceptedFollowedByUserId(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		
		ReqSearch searchFollowByFollowerIdEqual = ReqSearch.builder().column("userId").value(id.toString())
				.dateValue(false).joinTable("follower").operation(OperationEnum.EQUAL).build();
		ReqSearch searchFollowByStatusAccepted = ReqSearch.builder().column("followStatus")
				.value(FollowStatus.ACCEPTED.toString()).dateValue(false).operation(OperationEnum.EQUAL).build();
		
		Specification<Follow> spec = specService.getSpecification(List.of(searchFollowByFollowerIdEqual,searchFollowByStatusAccepted), 
				GlobalOperationEnum.AND);
		
		return followDao.count(spec);
	}

	
	@Override
	@Transactional(readOnly = true)
	public Long countAcceptedFollowerByUserId(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		
		ReqSearch searchFollowByFollowerIdEqual = ReqSearch.builder().column("userId").value(id.toString())
				.dateValue(false).joinTable("followed").operation(OperationEnum.EQUAL).build();
		ReqSearch searchFollowByStatusAccepted = ReqSearch.builder().column("followStatus")
				.value(FollowStatus.ACCEPTED.toString()).dateValue(false).operation(OperationEnum.EQUAL).build();
		
		Specification<Follow> spec = specService.getSpecification(List.of(searchFollowByFollowerIdEqual,searchFollowByStatusAccepted), 
				GlobalOperationEnum.AND);
		
		return followDao.count(spec);
	}

}
