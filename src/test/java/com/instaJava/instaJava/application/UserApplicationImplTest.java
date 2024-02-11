package com.instaJava.instaJava.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.PersonalDetailsDto;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.PersonalDetails;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.enums.GlobalOperationEnum;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.mapper.PersonalDetailsMapper;
import com.instaJava.instaJava.mapper.UserMapper;
import com.instaJava.instaJava.service.FollowService;
import com.instaJava.instaJava.service.PublicatedImageService;
import com.instaJava.instaJava.service.SpecificationService;
import com.instaJava.instaJava.service.UserService;
import com.instaJava.instaJava.util.MessagesUtils;

@ExtendWith(MockitoExtension.class)
class UserApplicationImplTest {

	@Mock
	private Authentication auth;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private UserService uService;
	@Mock
	private FollowService fService;
	@Mock
	private PublicatedImageService pImaService;
	@Mock
	private SpecificationService<User> specService;
	@Mock
	private UserMapper uMapper;
	@Mock
	private PersonalDetailsMapper pMapper;
	@Mock
	private MessagesUtils messUtils;
	@InjectMocks
	private UserApplicationImpl userApplication;

	// getByPrincipal
	@Test
	void getByPrincipalReturnNotNull() {
		User authUser = new User();
		UserDto authUserDto = new UserDto();
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		// mapper
		when(uMapper.userToUserDto(authUser)).thenReturn(authUserDto);

		assertNotNull(userApplication.getByPrincipal());
	}

	// updateImage
	@Test
	void updateImage() {
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", MediaType.IMAGE_JPEG_VALUE,
				"Hello, World!".getBytes());

		userApplication.updateImage(img);
		verify(uService).updateImage(img);
	}

	// getImage
	@Test
	void getImageReturnNotNull() {
		when(uService.getImage()).thenReturn("base64image");
		assertNotNull(userApplication.getImage());
	}

	// savePersonalDetails
	@Test
	void savePersonalDetailsParamPersonalDetailsDtoNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> userApplication.savePersonalDetails(null));
	}

	@Test
	void savePersonalDetailsReturnNotNull() {
		byte age = 20;
		PersonalDetails p = new PersonalDetails();
		PersonalDetailsDto personalDetailsDto = new PersonalDetailsDto();
		personalDetailsDto.setName("random");
		personalDetailsDto.setLastname("random");
		personalDetailsDto.setEmail("randomEmail");
		personalDetailsDto.setAge(age);

		when(uService.savePersonalDetails(personalDetailsDto.getName(), personalDetailsDto.getLastname(),
				personalDetailsDto.getAge(), personalDetailsDto.getEmail())).thenReturn(p);
		when(pMapper.personalDetailsToPersonalDetailsDto(p)).thenReturn(personalDetailsDto);
		assertNotNull(userApplication.savePersonalDetails(personalDetailsDto));
	}

	// getPersonalDetailsByUser
	@Test
	void getPersonalDetailsByUserReturnNotNull() {
		PersonalDetails p = new PersonalDetails();
		when(uService.getPersonalDetailsByUser()).thenReturn(p);
		when(pMapper.personalDetailsToPersonalDetailsDto(p)).thenReturn(new PersonalDetailsDto());

		assertNotNull(userApplication.getPersonalDetailsByUser());
	}

	// changeVisible
	@Test
	void changeVisibleReturnNotNull() {
		User user = new User();
		when(uService.changeVisible()).thenReturn(user);
		when(uMapper.userToUserDto(user)).thenReturn(new UserDto());
		assertNotNull(userApplication.changeVisible());
	}

	// getOneUserOneCondition
	@SuppressWarnings("unchecked")
	@Test
	void getOneUserOneConditionColumnPasswordThrow() {
		ReqSearch reqSearch = ReqSearch.builder().column("password").build();
		assertThrows(InvalidActionException.class, () -> userApplication.getOneUserOneCondition(reqSearch));
		verify(specService, never()).getSpecification(reqSearch);
		verify(uService, never()).getOneUserManyConditions(any(Specification.class));
	}

	@Test
	void getOneUserOneConditionReturnNotNull() {
		User user = new User();
		ReqSearch reqSearch = ReqSearch.builder().column("username").build();
		// spec for example only, does not match reqSearchList
		Specification<User> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("username"),
				"random");
		// spec
		when(specService.getSpecification(reqSearch)).thenReturn(spec);
		// getting user
		when(uService.getOneUserManyConditions(spec)).thenReturn(user);
		// mapping
		when(uMapper.userToUserDto(user)).thenReturn(new UserDto());
		assertNotNull(userApplication.getOneUserOneCondition(reqSearch));
	}

	// getOneUserManyConditions
	
	@SuppressWarnings("unchecked")
	@Test
	void getOneUserManyConditionsColumnPasswordThrow() {
		ReqSearch reqSearch = ReqSearch.builder().column("password").build();
		ReqSearchList req = new ReqSearchList();
		req.setReqSearchs(List.of(reqSearch));
		req.setGlobalOperator(GlobalOperationEnum.OR);

		assertThrows(InvalidActionException.class, () -> userApplication.getOneUserManyConditions(req));

		verify(specService, never()).getSpecification(anyList(), eq(GlobalOperationEnum.OR));
		verify(uService, never()).getOneUserManyConditions(any(Specification.class));
	}

	@Test
	void getOneUserManyConditionsReturnNotNull() {
		User user = new User();
		ReqSearch reqSearch = ReqSearch.builder().column("username").build();
		ReqSearchList req = new ReqSearchList();
		req.setReqSearchs(List.of(reqSearch));
		req.setGlobalOperator(GlobalOperationEnum.OR);
		// spec for example only, does not match reqSearchList
		Specification<User> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("username"),
				"random");
		// spec
		when(specService.getSpecification(anyList(), eq(GlobalOperationEnum.OR))).thenReturn(spec);
		//getting user
		when(uService.getOneUserManyConditions(spec)).thenReturn(user);
		//mapping
		when(uMapper.userToUserDto(user)).thenReturn(new UserDto());
		
		assertNotNull(userApplication.getOneUserManyConditions(req));
	}

	//getManyUsersOneCondition
	@Test
	void getManyUsersOneConditionColumnPasswordThrow() {
		ReqSearch req = ReqSearch.builder().column("password").build();
		assertThrows(InvalidActionException.class, () -> userApplication.getManyUsersOneCondition(0, 0, null, null, req));
		
		verify(specService, never()).getSpecification(req);
		verify(uService,never()).getManyUsersManyConditions(any(PageInfoDto.class), eq(null));
	}
	
	@Test
	void getManyUsersOneConditionReturnNotNull() {
		ReqSearch reqSearch = ReqSearch.builder().column("username").build();
		Page<User> page = Page.empty();
		// spec for example only, does not match reqSearchList
		Specification<User> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("username"),
				"random");
		
		when(specService.getSpecification(reqSearch)).thenReturn(spec);
		when(uService.getManyUsersManyConditions(any(PageInfoDto.class), eq(spec))).thenReturn(page);
		when(uMapper.pageAndPageInfoDtoToResPaginationG(eq(page), any(PageInfoDto.class))).thenReturn(new ResPaginationG<UserDto>());
		
		assertNotNull(userApplication.getManyUsersOneCondition(0, 0, null, null, reqSearch));
	}
	
	//getManyUsersManyConditions
	
	@Test
	void getManyUsersManyConditionsColumnPasswordThrow() {
		GlobalOperationEnum g = GlobalOperationEnum.NONE;
		ReqSearch reqSearch = ReqSearch.builder().column("password").build();
		ReqSearchList reqSearchList = new ReqSearchList();
		reqSearchList.setGlobalOperator(g);
		reqSearchList.setReqSearchs(List.of(reqSearch));
		
		assertThrows(InvalidActionException.class, () -> userApplication.getManyUsersManyConditions(0, 0, null, null, reqSearchList));
		
		verify(specService,never()).getSpecification(anyList(), eq(g));
		verify(uService,never()).getManyUsersManyConditions(any(PageInfoDto.class), eq(null));
	}
	
	@Test
	void getManyUsersManyConditionsReturnNotNull() {
		GlobalOperationEnum g = GlobalOperationEnum.NONE;
		ReqSearch reqSearch = ReqSearch.builder().column("username").build();
		ReqSearchList reqSearchList = new ReqSearchList();
		reqSearchList.setGlobalOperator(g);
		reqSearchList.setReqSearchs(List.of(reqSearch));
		// spec for example only, does not match reqSearchList
				Specification<User> spec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("username"),
						"random");
		Page<User> page = Page.empty();
		//spec
		when(specService.getSpecification(anyList(), eq(g))).thenReturn(spec);
		//gettin users
		when(uService.getManyUsersManyConditions(any(PageInfoDto.class), eq(spec))).thenReturn(page);
		//mapping
		when(uMapper.pageAndPageInfoDtoToResPaginationG(eq(page), any(PageInfoDto.class))).thenReturn(new ResPaginationG<UserDto>());
		assertNotNull(userApplication.getManyUsersManyConditions(0, 0, null, null, reqSearchList));
	}
	
	//getUserGeneralInfoById
	
	@Test
	void getUserGeneralInfoByIdReturnNotNull() {
		Long id =2L; 
		Long count = 50L;
		User user = new User(id);
		FollowStatus followStatus = FollowStatus.ACCEPTED;
		
		when(uService.findById(id)).thenReturn(user);
		when(fService.getFollowStatusByFollowedId(id)).thenReturn(followStatus);
		when(fService.getFollowStatusByFollowerId(id)).thenReturn(followStatus);
		when(fService.countByFollowStatusAndFollower(followStatus, id)).thenReturn(count);
		when(fService.countByFollowStatusAndFollowed(followStatus, id)).thenReturn(count);
		when(pImaService.countPublicationsByOwnerId(id)).thenReturn(count);
		when(uMapper.userToUserDto(user)).thenReturn(new UserDto());
		
		assertNotNull(userApplication.getUserGeneralInfoById(id));
	}
	
	
	
}


















