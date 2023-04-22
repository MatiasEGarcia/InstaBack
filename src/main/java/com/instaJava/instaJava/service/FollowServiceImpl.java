package com.instaJava.instaJava.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService{
	
	private final UserService userService;
	private final FollowDao followDao;
	private final MessagesUtils messUtils;
	private final SpecificationService<Follow> specService;
	
	@Override
	@Transactional
	public Follow save(Long FollowedId) {
		if(FollowedId == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		}
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

	@Override
	@Transactional(readOnly = true)
	public Page<Follow> search(PageInfoDto pageInfoDto, ReqSearchList reqSearchList) {
		if(pageInfoDto == null || reqSearchList == null || 
				pageInfoDto.getSortField() == null || pageInfoDto.getSortDir() == null) throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument-not-null-empty"));
		//as sortfield I can pass attributes from another entity that is related - > user_username
		Sort sort = pageInfoDto.getSortDir().equalsIgnoreCase(Sort.Direction.ASC.name()) ? 
				Sort.by(pageInfoDto.getSortField()).ascending() : Sort.by(pageInfoDto.getSortField()).descending();
		//first page for the most people is 1 , but for us is 0
		Pageable pag = PageRequest.of(pageInfoDto.getPageNo() == 0 ? pageInfoDto.getPageNo() : pageInfoDto.getPageNo() - 1, pageInfoDto.getPageSize(),sort);
		Specification<Follow> spec = specService.getSpecification(reqSearchList.getReqSearchs()
				, reqSearchList.getGlobalOperator());
		return followDao.findAll(spec, pag);
	}

	//the only one that can change the follow status is the user followed, 
	@Override
	@Transactional
	public Follow updateFollowStatusById(Long id, FollowStatus newStatus){
		if(newStatus == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		User userFollowed;
		Follow follower = findById(id);
		userFollowed = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(!follower.getFollowed().equals(userFollowed)) throw new IllegalArgumentException(messUtils.getMessage("exception.followed-is-not-same"));
		follower.setFollowStatus(newStatus);
		return followDao.save(follower);
	}

	@Override
	@Transactional(readOnly = true)
	public Follow findById(Long id) {
		Optional<Follow> followerOpt = followDao.findById(id);
		if(followerOpt.isEmpty()) throw new IllegalArgumentException(messUtils.getMessage("exception.follow-id-not-found"));
		return followerOpt.get();
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		Follow foll = this.findById(id);
		User userFollower = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(!foll.getFollower().equals(userFollower)) throw new IllegalArgumentException(messUtils.getMessage("exception.follower-is-not-same"));
		followDao.delete(foll);;
	}
	
	/* if the followed user no exist throw exception
	 * if the user is visible/public the return FollowStatus.ACCEPTED
	 * if the user is not visible/public and there is not a follow record then return FollowStatus.NOT_ASKED
	 * if none of the above conditions is met return the current FollowStatus
	 * */
	@Override
	@Transactional(readOnly = true)
	public FollowStatus getFollowStatusByFollowedId(Long id) {
		if(id == null) throw new IllegalArgumentException();
		Optional<User> userFollowed;
		Optional<Follow> optFollow;
		User userFollower;
		ReqSearch followedSearchEqual;
		ReqSearch followerSearchEqual;
		userFollowed = userService.getById(id);
		if(userFollowed.isEmpty()) throw new IllegalArgumentException();
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

	/*
	 * return how many users a user follow
	 * */
	@Override
	@Transactional(readOnly = true)
	public Long countFollowedByUserId(Long id) {
		if(id == null)  throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		ReqSearch searchFollowByFollowerIdEqual = ReqSearch.builder().column("userId").value(id.toString()).dateValue(false)
				.joinTable("follower").operation(OperationEnum.EQUAL).build();
		return followDao.count(specService.getSpecification(searchFollowByFollowerIdEqual));
	}

	/*
	 * return how many followers have a user
	 * */
	@Override
	@Transactional(readOnly = true)
	public Long countFollowerByUserId(Long id) {
		if(id == null)  throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		ReqSearch searchFollowByFollowerIdEqual = ReqSearch.builder().column("userId").value(id.toString()).dateValue(false)
				.joinTable("followed").operation(OperationEnum.EQUAL).build();
		return followDao.count(specService.getSpecification(searchFollowByFollowerIdEqual));
	}

	


	
}
