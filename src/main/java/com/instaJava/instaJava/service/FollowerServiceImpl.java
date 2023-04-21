package com.instaJava.instaJava.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.FollowerDao;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.entity.Follower;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.enums.GlobalOperationEnum;
import com.instaJava.instaJava.enums.OperationEnum;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowerServiceImpl implements FollowerService{
	
	private final UserService userService;
	private final FollowerDao followerDao;
	private final MessagesUtils messUtils;
	private final SpecificationService<Follower> specService;
	
	//retest
	@Override
	@Transactional
	public Follower save(Long FollowedId) {
		if(FollowedId == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument.not.null"));
		}
		Follower follower = Follower.builder().build();
		Optional<User> optUserFollowed = userService.getById(FollowedId);
		if(optUserFollowed.isEmpty()) throw new IllegalArgumentException(messUtils.getMessage("exception.followed-no-exist"));
		User userFollower = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		follower.setUserFollowed(optUserFollowed.get());
		follower.setUserFollower((userService.getById(userFollower.getUserId()).get()));//persistence context?If I don't do this user is detached
		if(optUserFollowed.get().isVisible()) {
			follower.setFollowStatus(FollowStatus.ACCEPTED);
		}else {
			follower.setFollowStatus(FollowStatus.IN_PROCESS);
		}
		return followerDao.save(follower);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<Follower> search(PageInfoDto pageInfoDto, ReqSearchList reqSearchList) {
		if(pageInfoDto == null || reqSearchList == null || 
				pageInfoDto.getSortField() == null || pageInfoDto.getSortDir() == null) throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument-not-null-empty"));
		//as sortfield I can pass attributes from another entity that is related - > user_username
		Sort sort = pageInfoDto.getSortDir().equalsIgnoreCase(Sort.Direction.ASC.name()) ? 
				Sort.by(pageInfoDto.getSortField()).ascending() : Sort.by(pageInfoDto.getSortField()).descending();
		//first page for the most people is 1 , but for us is 0
		Pageable pag = PageRequest.of(pageInfoDto.getPageNo()-1, pageInfoDto.getPageSize(),sort);
		Specification<Follower> spec = specService.getSpecification(reqSearchList.getReqSearchs()
				, reqSearchList.getGlobalOperator());
		return followerDao.findAll(spec, pag);
	}

	//the only one that can change the follow status is the user followed, 
	@Override
	@Transactional
	public Follower updateFollowStatusById(Long id, FollowStatus newStatus){
		if(newStatus == null) throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument-not-null"));
		User userFollowed;
		Follower follower = findById(id);
		userFollowed = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(!follower.getUserFollowed().equals(userFollowed)) throw new IllegalArgumentException(messUtils.getMessage("exception.followed-is-not-same"));
		follower.setFollowStatus(newStatus);
		return followerDao.save(follower);
	}

	@Override
	@Transactional(readOnly = true)
	public Follower findById(Long id) {
		Optional<Follower> followerOpt = followerDao.findById(id);
		if(followerOpt.isEmpty()) throw new IllegalArgumentException(messUtils.getMessage("exception.follower-id-not-found"));
		return followerOpt.get();
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		Follower foll = this.findById(id);
		User userFollower = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(!foll.getUserFollower().equals(userFollower)) throw new IllegalArgumentException(messUtils.getMessage("exception.follower-is-not-same"));
		followerDao.delete(foll);;
	}

	//tengo que testear
	
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
		Optional<Follower> optFollower;
		User userFollower;
		ReqSearch followedSearchEqual;
		ReqSearch followerSearchEqual;
		userFollowed = userService.getById(id);
		if(userFollowed.isEmpty()) throw new UsernameNotFoundException(null);
		if(userFollowed.get().isVisible()) return FollowStatus.ACCEPTED; // if the user is public/visible, then we return accepted
		userFollower = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		followedSearchEqual = ReqSearch.builder().column("userId").value(id.toString())
				.joinTable("userFollowed").dateValue(false).operation(OperationEnum.EQUAL).build();
		followerSearchEqual = ReqSearch.builder().column("userId").value(userFollower.getUserId().toString())
				.joinTable("userFollower").dateValue(false).operation(OperationEnum.EQUAL).build();
		optFollower = followerDao.findOne(specService.getSpecification(List.of(followedSearchEqual,followerSearchEqual), GlobalOperationEnum.AND));
		if(optFollower.isEmpty()) return FollowStatus.NOT_ASKED;
		return optFollower.get().getFollowStatus();
	}

	


	
}
