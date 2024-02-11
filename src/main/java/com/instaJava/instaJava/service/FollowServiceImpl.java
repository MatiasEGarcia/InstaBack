package com.instaJava.instaJava.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.FollowDao;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.exception.AlreadyExistsException;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

	private final FollowDao followDao;
	private final MessagesUtils messUtils;
	private final SpecificationService<Follow> specService;
	private final PageableUtils pagUtils;

	@Override
	@Transactional
	public Follow save(User userFollowed) {
		if (userFollowed == null || userFollowed.getId() == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		User userFollower;
		Follow follow;
		// check if the follow record already exists.
		userFollower = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (existsByFollowedAndFollower(userFollowed.getId())) {
			throw new AlreadyExistsException(messUtils.getMessage("generic.create-record-already.exists"),
					HttpStatus.BAD_REQUEST);
		}
		follow = Follow.builder().followed(userFollowed).follower(userFollower).build();
		// set follow status by visible state of the user wanted to follow
		if (userFollowed.isVisible()) {
			follow.setFollowStatus(FollowStatus.ACCEPTED);
		} else {
			follow.setFollowStatus(FollowStatus.IN_PROCESS);
		}
		return followDao.save(follow);
	}


	@Override
	@Transactional(readOnly = true)
	public Page<Follow> search(PageInfoDto pageInfoDto, ReqSearchList reqSearchList) {
		if (pageInfoDto == null || reqSearchList == null || pageInfoDto.getSortField() == null
				|| pageInfoDto.getSortDir() == null)
			throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument-not-null-empty"));
		Specification<Follow> spec = specService.getSpecification(reqSearchList.getReqSearchs(),
				reqSearchList.getGlobalOperator());
		Page<Follow> page = followDao.findAll(spec, pagUtils.getPageable(pageInfoDto));
		if (page.getContent().isEmpty()) {
			throw new RecordNotFoundException(messUtils.getMessage("follow.not-found"), HttpStatus.NO_CONTENT);
		}
		return page;
	}

	@Override
	@Transactional
	public Follow updateFollowStatusById(Long id, FollowStatus newStatus) {
		if (newStatus == null || id == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		User userFollowed;
		Follow follow = followDao.findById(id).orElseThrow(() -> 
		new RecordNotFoundException(messUtils.getMessage("follow.not-found"), HttpStatus.NOT_FOUND));
		userFollowed = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!follow.getFollowed().equals(userFollowed))
			throw new InvalidActionException(messUtils.getMessage("follow.followed-not-same"),HttpStatus.BAD_REQUEST);
		follow.setFollowStatus(newStatus);
		return follow;
	}

	//**CHCEK TESTSS
	@Override
	@Transactional
	public Follow updateFollowStatusByFollowerId(Long followerUserId, FollowStatus newStatus) {
		if (followerUserId == null || newStatus == null)
			throw new IllegalArgumentException("generic.arg-not-null");
		User authUser;
		Follow follow;
		authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		// getting follow record
		follow = followDao.findOneByFollowedIdAndFollowerId(authUser.getId(), followerUserId).orElseThrow(
				() -> new RecordNotFoundException(messUtils.getMessage("follow.not-found"), HttpStatus.NOT_FOUND));
		// updating follow recod
		follow.setFollowStatus(newStatus);
		return follow;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByFollowedAndFollower(Long followedId) {
		if (followedId == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		User follower = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return followDao.existsByFollowedIdAndFollowerId(followedId, follower.getId());
	}

	@Override
	@Transactional
	public Follow deleteById(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		Follow follow = followDao.findById(id).orElseThrow(() -> 
		new RecordNotFoundException(messUtils.getMessage("follow.not-found"), HttpStatus.NOT_FOUND));
		User userFollower = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!follow.getFollower().equals(userFollower))
			throw new InvalidActionException(messUtils.getMessage("follow.follower-not-same"), HttpStatus.BAD_REQUEST);
		followDao.delete(follow);
		return follow;
	}

	@Override
	@Transactional
	public Follow deleteByFollwedId(Long followedId) {
		if(followedId == null) throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Follow follow = followDao.findOneByFollowedIdAndFollowerId(followedId, authUser.getId()).orElseThrow(() -> 
			new RecordNotFoundException(messUtils.getMessage("follow.not-found"), HttpStatus.NOT_FOUND));
		followDao.deleteById(follow.getId());
		return follow;
	}
	
	@Override
	@Transactional(readOnly = true)
	public FollowStatus getFollowStatusByFollowedId(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		Optional<Follow> optFollow;
		User userFollower;

		userFollower = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		optFollow = followDao.findOneByFollowedIdAndFollowerId(id, userFollower.getId());

		if (optFollow.isEmpty())
			return FollowStatus.NOT_ASKED;
		return optFollow.get().getFollowStatus();
	}
	
	@Override
	@Transactional(readOnly = true)
	public FollowStatus getFollowStatusByFollowerId(Long followerId) {
		if (followerId == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Optional<Follow> optFollow;
		User userFollowed;
		userFollowed = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		optFollow = followDao.findOneByFollowedIdAndFollowerId(userFollowed.getId(), followerId);

		if (optFollow.isEmpty())
			return FollowStatus.NOT_ASKED;
		return optFollow.get().getFollowStatus();
	}

	@Override
	@Transactional(readOnly = true)
	public Long countByFollowStatusAndFollowed(FollowStatus followStatus, Long followedId) {
		if (followedId == null || followStatus == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));

		return followDao.countByFollowedIdAndFollowStatus(followedId, followStatus);
	}

	@Override
	@Transactional(readOnly = true)
	public Long countByFollowStatusAndFollower(FollowStatus followStatus, Long followerId) {
		if (followerId == null || followStatus == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));

		return followDao.countByFollowerIdAndFollowStatus(followerId, followStatus);
	}

}
