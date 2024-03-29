package com.instaback.application;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.instaback.dto.PageInfoDto;
import com.instaback.dto.PersonalDetailsDto;
import com.instaback.dto.UserDto;
import com.instaback.dto.request.ReqSearch;
import com.instaback.dto.request.ReqSearchList;
import com.instaback.dto.response.ResImageString;
import com.instaback.dto.response.ResPaginationG;
import com.instaback.dto.response.SocialInfoDto;
import com.instaback.dto.response.UserGeneralInfoDto;
import com.instaback.entity.PersonalDetails;
import com.instaback.entity.User;
import com.instaback.enums.FollowStatus;
import com.instaback.exception.InvalidActionException;
import com.instaback.mapper.PersonalDetailsMapper;
import com.instaback.mapper.UserMapper;
import com.instaback.service.FollowService;
import com.instaback.service.PublicatedImageService;
import com.instaback.service.SpecificationService;
import com.instaback.service.UserService;
import com.instaback.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserApplicationImpl implements UserApplication {

	private final UserService uService;
	private final FollowService fService;
	private final PublicatedImageService pImaService;
	private final SpecificationService<User> specService;
	private final UserMapper uMapper;
	private final PersonalDetailsMapper pMapper;
	private final MessagesUtils messUtils;
	
	@Override
	public UserDto getByPrincipal() {
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return uMapper.userToUserDto(authUser);
	}

	@Override
	public void updateImage(MultipartFile file) {
		uService.updateImage(file);
		
	}

	@Override
	public ResImageString getImage() {
		ResImageString rIString = new ResImageString();
		rIString.setImage64(uService.getImage());
		return rIString;
	}

	@Override
	public PersonalDetailsDto savePersonalDetails(PersonalDetailsDto personalDetailsDto) {
		if(personalDetailsDto == null) throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		PersonalDetails pDetails = uService.savePersonalDetails(personalDetailsDto.getName(), 
				personalDetailsDto.getLastname(), personalDetailsDto.getAge(), personalDetailsDto.getEmail());
		return pMapper.personalDetailsToPersonalDetailsDto(pDetails);
	}

	@Override
	public PersonalDetailsDto getPersonalDetailsByUser() {
		PersonalDetails pDetails = uService.getPersonalDetailsByUser();
		return pMapper.personalDetailsToPersonalDetailsDto(pDetails);
	}
	
	
	@Override
	public UserDto changeVisible() {
		User user = uService.changeVisible();
		return uMapper.userToUserDto(user);
	}


	@Override
	public UserDto getOneUserOneCondition(ReqSearch reqSearch) {
		this.passNotAvailableForSearch(reqSearch);
		Specification<User> specification = specService.getSpecification(reqSearch);
		User user = uService.getOneUserManyConditions(specification);
		return uMapper.userToUserDto(user);
	}

	
	@Override
	public UserDto getOneUserManyConditions(ReqSearchList reqSearchList) {
		this.passNotAvailableForSearch(reqSearchList.getReqSearchs());
		Specification<User> specification = specService.getSpecification(reqSearchList.getReqSearchs(), reqSearchList.getGlobalOperator());
		User user = uService.getOneUserManyConditions(specification);
		return uMapper.userToUserDto(user);
	}

	@Override
	public ResPaginationG<UserDto> getManyUsersOneCondition(int pageNo, int pageSize, String sortField,
			Direction sortDir, ReqSearch reqSearch) {
		Page<User> pageUsers;
		Specification<User> specification;
		PageInfoDto pageInfoDto;
		this.passNotAvailableForSearch(reqSearch);
		pageInfoDto = new PageInfoDto(pageNo, pageSize, 0, 0, sortField, sortDir);
		specification = specService.getSpecification(reqSearch);
		pageUsers = uService.getManyUsersManyConditions(pageInfoDto, specification);
		return uMapper.pageAndPageInfoDtoToResPaginationG(pageUsers, pageInfoDto);
	}

	@Override
	public ResPaginationG<UserDto> getManyUsersManyConditions(int pageNo, int pageSize, String sortField,
			Direction sortDir, ReqSearchList reqSearchList) {
		Page<User> pageUsers;
		Specification<User> specification;
		PageInfoDto pageInfoDto;
		this.passNotAvailableForSearch(reqSearchList.getReqSearchs());
		pageInfoDto = new PageInfoDto(pageNo, pageSize, 0, 0, sortField, sortDir);
		specification = specService.getSpecification(reqSearchList.getReqSearchs(), reqSearchList.getGlobalOperator());
		pageUsers = uService.getManyUsersManyConditions(pageInfoDto, specification);
		return uMapper.pageAndPageInfoDtoToResPaginationG(pageUsers, pageInfoDto);
	}

	@Override
	public UserGeneralInfoDto getUserGeneralInfoById(Long id) {
		String numberFollowed;
		String numberFollower;
		String numberPublications;
		UserGeneralInfoDto userGeneralInfo = new UserGeneralInfoDto();
		FollowStatus followStatusToCount = FollowStatus.ACCEPTED;
		SocialInfoDto socialInfoDto = new SocialInfoDto();
		User user = uService.findById(id);
		socialInfoDto.setFollowerFollowStatus(fService.getFollowStatusByFollowedId(id));
		socialInfoDto.setFollowedFollowStatus(fService.getFollowStatusByFollowerId(id));
		numberFollowed = fService.countByFollowStatusAndFollower(followStatusToCount, id).toString();
		numberFollower = fService.countByFollowStatusAndFollowed(followStatusToCount, id).toString();
		socialInfoDto.setNumberFollowed(numberFollowed);
		socialInfoDto.setNumberFollowers(numberFollower);
		numberPublications = pImaService.countPublicationsByOwnerId(id).toString();
		socialInfoDto.setNumberPublications(numberPublications);
		userGeneralInfo.setUser(uMapper.userToUserDto(user));
		userGeneralInfo.setSocial(socialInfoDto);
		return userGeneralInfo;
	}

	
	
	/**
	 * Method to test conditions and see if there is one for password.
	 * 
	 * @param reqSearch. condition to be tested.
	 * @throws IllegalArgumentException if one of the conditions is for password.
	 */
	private void passNotAvailableForSearch(ReqSearch reqSearch) {
		if (reqSearch.getColumn() == null) {
			return;
		} else if (reqSearch.getColumn().equalsIgnoreCase("password"))
			throw new InvalidActionException(messUtils.getMessage("user.password-not-search"),HttpStatus.BAD_REQUEST);
	}

	/**
	 * Method to test conditions and see if there is one for password.
	 * 
	 * @param searchs. Collections of conditions to be tested.
	 * @throws IllegalArgumentException if one of the conditions is for password.
	 */
	private void passNotAvailableForSearch(List<ReqSearch> searchs) {
		boolean isTherePassowrdColumn = searchs.stream().filter(s -> s.getColumn() != null)
				.anyMatch(s -> s.getColumn().equalsIgnoreCase("password"));
		if (isTherePassowrdColumn)
			throw new InvalidActionException(messUtils.getMessage("user.password-not-search"), HttpStatus.BAD_REQUEST);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
}
