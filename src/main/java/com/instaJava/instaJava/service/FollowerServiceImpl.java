package com.instaJava.instaJava.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.FollowerDao;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.entity.Follower;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowerServiceImpl implements FollowerService{
	
	private final UserService userService;
	private final FollowerDao followerDao;
	private final MessagesUtils messUtils;
	private final SpecificationService<Follower> specService;
	
	@Override
	@Transactional
	public Follower save(Long FollowedId) {
		if(FollowedId == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument.not.null"));
		}
		Follower follower = Follower.builder().build();
		User userFollowed = userService.findById(FollowedId); //if not exist throw exception
		User userFollower = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		follower.setUserFollowed(userFollowed);
		follower.setUserFollower((userService.findById(userFollower.getUserId())));//persistence context?If I don't do this user is detached
		if(userFollowed.isVisible()) {
			follower.setFollowStatus(FollowStatus.ACCEPTED);
		}else {
			follower.setFollowStatus(FollowStatus.IN_PROCESS);
		}
		return followerDao.save(follower);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<Follower> search(int pageNo, int pageSize, String sortField, String sortDir, ReqSearch reqSearch) {
		if(sortField == null || sortField.isBlank() 
				|| sortDir == null || sortDir.isBlank() || reqSearch == null) throw new IllegalArgumentException(messUtils.getMessage("exepcion.argument-not-null-empty"));
		//as sortfield I can pass attributes from another entity that is related - > user_username
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
		//first page for the most people is 1 , but for us is 0
		Pageable pag = PageRequest.of(pageNo-1, pageSize,sort);
		Specification<Follower> spec = specService.getSpecification(reqSearch.getSearchRequestDtos()
				, reqSearch.getGlobalOperator());
		return followerDao.findAll(spec, pag);
	}


	
}
