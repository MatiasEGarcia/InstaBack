package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.instaJava.instaJava.dao.PersonalDetailsDao;
import com.instaJava.instaJava.dao.UserDao;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.PersonalDetailsDto;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.PersonalDetails;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.GlobalOperationEnum;
import com.instaJava.instaJava.exception.InvalidImageException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.mapper.PersonalDetailsMapper;
import com.instaJava.instaJava.mapper.UserMapper;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock private Authentication auth;
	@Mock private SecurityContext securityContext;
	@Mock private UserDao userDao;
	@Mock private PersonalDetailsDao personalDetailsDao;
	@Mock private MessagesUtils messUtils;
	@Mock private PageableUtils pageUtils;
	@Mock private UserMapper userMapper;
	@Mock private PersonalDetailsMapper personalDetailsMapper;
	@Mock private SpecificationService<User> specService;
	@InjectMocks private UserServiceImpl userService;
	static private MockMultipartFile multipartFile;
	static private User user;
	static private UserDto userDto;
	static private PersonalDetails personalDetails;
	static private PersonalDetailsDto personalDetailsDto;
	
	@BeforeAll
	static void entitiesSetUp() throws IOException {
		multipartFile = new MockMultipartFile("file", "test.txt",
			      "text/plain", "testing".getBytes());
		String image64 = Base64.getEncoder().encodeToString(multipartFile.getBytes());
		
		user = User.builder()
				.id(1L)
				.username("Mati")
				.image(image64)
				.visible(true)
				.build();
		
		userDto = UserDto.builder()
				.id("1")
				.username("Mati")
				.image(image64)
				.visible(true)
				.build();
		personalDetails = new PersonalDetails();
		personalDetailsDto = new PersonalDetailsDto();
		
	}
	
	//save
	@Test
	void saveArgumentNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> userService.save(null));
	}
	
	@Test
	void saveReturnNotNull() {
		when(userDao.save(user)).thenReturn(user);
		assertNotNull(userService.save(user));
		verify(userDao).save(user);
	}

	//loadByUsername
	@Test
	void loadUserByUsernameUserNull() {
		when(userDao.findByUsername(user.getUsername())).thenReturn(null);
		assertThrows(UsernameNotFoundException.class,() -> {userService.loadUserByUsername(user.getUsername());});
		verify(userDao).findByUsername(user.getUsername());
	}

	@Test
	void loadUserByUsernameUserNotNull() {
		when(userDao.findByUsername(user.getUsername())).thenReturn(user);
		assertEquals(user, userService.loadUserByUsername(user.getUsername()));
		verify(userDao).findByUsername(user.getUsername());
	}
	
	//udpateImage
	@Test
	void updateImageArgumentNullThrow() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		assertThrows(InvalidImageException.class,() -> {userService.updateImage(null);});
		verify(userDao,never()).save(user);
	}
	
	@Test
	void updateImageArgumentNotNullNotThrow() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		userService.updateImage(multipartFile);
		verify(userDao).save(user);
	}
	
	//getImage
	@Test
	void getImageReturnImage() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		assertEquals(user.getImage(), userService.getImage());
	}
	
	//getPersonalDetailsByUser
	@Test
	void getPersonalDetailsUserNotHaveThrow() {
		user.setPersonalDetails(null);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		assertThrows(RecordNotFoundException.class, () -> userService.getPersonalDetailsByUser());
	}
	
	@Test
	void getPersonalDetailsReturnNotNull() {
		user.setPersonalDetails(personalDetails);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		when(personalDetailsMapper.personalDetailsToPersonalDetailsDto(personalDetails)).thenReturn(personalDetailsDto);
		
		assertNotNull(userService.getPersonalDetailsByUser());
	}
	
	//savePersonalDetails
	@Test
	void savePersonalDetailsParamPesonalDetailsDtoNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> {userService.savePersonalDetails(null);});
		verify(personalDetailsMapper,never()).personalDetailsDtoAndUserToPersonalDetails(personalDetailsDto, user);
		verify(personalDetailsDao,never()).save(personalDetails);
	}
	
	@Test
	void savePersonalDetailsReturnsNotNull() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		when(personalDetailsMapper.
				personalDetailsDtoAndUserToPersonalDetails(personalDetailsDto, user))
				.thenReturn(personalDetails);
		when(personalDetailsDao.save(personalDetails)).thenReturn(personalDetails);
		when(personalDetailsMapper.personalDetailsToPersonalDetailsDto(personalDetails)).thenReturn(personalDetailsDto);
		
		assertNotNull(userService.savePersonalDetails(personalDetailsDto));
		
		verify(personalDetailsMapper)
			.personalDetailsDtoAndUserToPersonalDetails(personalDetailsDto, user);
		verify(personalDetailsDao).save(personalDetails);
	}
	
	//changeVisible
	@Test
	void changeVisible() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);//here the user is visible = true
		user.setVisible(false);
		when(userDao.save(user)).thenReturn(user);
		when(userMapper.userToUserDto(user)).thenReturn(userDto);
		assertNotNull(userService.changeVisible());
		verify(userDao).save(user);
	}
	
	//getByPrincipal
	@Test
	void getByPrincipalReturnsNotNull() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);//here the user is visible = true
		when(userMapper.userToUserDto(user)).thenReturn(userDto);
		assertNotNull(userService.getByPrincipal());
	}
	
	//getById
	@Test
	void getByIdArgNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> userService.getById(null));
	}
	
	@Test
	void getByIdNoMatchesThrow() {
		when(userDao.findById(1L)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> userService.getById(1L));
	}
	
	@Test
	void getByIdMatchesReturnsNotNull() {
		when(userDao.findById(1L)).thenReturn(Optional.of(user));
		when(userMapper.userToUserDto(user)).thenReturn(userDto);
		assertNotNull(userService.getById(1L));
	}
	
	//getOneUserOneCondition
	@Test
	void getOneUserOneConditionParamReqSearchNullThrow() {
		assertThrows(IllegalArgumentException.class,() -> userService.getOneUserOneCondition(null));
	}
	
	@Test
	void getOneUserOneConditionReturnsNotNull() {
		ReqSearch reqSearch = ReqSearch.builder()
				.column("password")
				.build();
		UserDto userDto = new UserDto();
		
		UserServiceImpl spyUserServiceImpl = spy(userService);
		doReturn(userDto).when(spyUserServiceImpl).getOneUserManyConditions(any(ReqSearchList.class));
		
		assertNotNull(spyUserServiceImpl.getOneUserOneCondition(reqSearch));
		
		verify(spyUserServiceImpl).getOneUserManyConditions(any(ReqSearchList.class));
	}
	
	//getOneUserManyConditions
	@Test
	void getOneUserManyConditionsParamReqSearchListNullThrow() {
		assertThrows(IllegalArgumentException.class ,() -> userService.getOneUserManyConditions(null));
	}
	
	@Test
	void getOneUserManyConditionsNoMatchesThrow() {
		ReqSearch reqSearch = ReqSearch.builder().build();
		List<ReqSearch> reqSearchs = List.of(reqSearch);
		ReqSearchList reqSearchList = ReqSearchList.builder()
				.reqSearchs(reqSearchs)
				.globalOperator(GlobalOperationEnum.AND)
				.build();
		//spec for example only, does not match reqSearchList
		Specification<User> spec = (root,query,criteriaBuilder) -> criteriaBuilder.equal(root.get("username"), user.getUsername());
		when(specService.getSpecification(reqSearchList.getReqSearchs(), reqSearchList.getGlobalOperator())).thenReturn(spec);
		when(userDao.findOne(spec)).thenReturn(Optional.empty());
		
		assertThrows(RecordNotFoundException.class,() -> userService.getOneUserManyConditions(reqSearchList));
		
		verify(specService).getSpecification(reqSearchList.getReqSearchs(), reqSearchList.getGlobalOperator());
		verify(userDao).findOne(spec);
	}
	
	@Test
	void getOneUserManyConditionsMatchesReturnsNotNull() {
		ReqSearch reqSearch = ReqSearch.builder().build();
		List<ReqSearch> reqSearchs = List.of(reqSearch);
		ReqSearchList reqSearchList = ReqSearchList.builder()
				.reqSearchs(reqSearchs)
				.globalOperator(GlobalOperationEnum.AND)
				.build();
		//spec for example only, does not match reqSearchList
		Specification<User> spec = (root,query,criteriaBuilder) -> criteriaBuilder.equal(root.get("username"), user.getUsername());
		when(specService.getSpecification(reqSearchList.getReqSearchs(), reqSearchList.getGlobalOperator())).thenReturn(spec);
		when(userDao.findOne(spec)).thenReturn(Optional.of(user));
		when(userMapper.userToUserDto(user)).thenReturn(userDto);
		
		assertNotNull(userService.getOneUserManyConditions(reqSearchList));
		
		verify(specService).getSpecification(reqSearchList.getReqSearchs(), reqSearchList.getGlobalOperator());
		verify(userDao).findOne(spec);
	}
	
	
	//getManyUsersOneCondition
	@Test
	void getManyUsersOneConditionArgPageInfoDtoNullThrow() {
		assertThrows(IllegalArgumentException.class , () -> userService.getManyUsersOneCondition(null, new ReqSearch()));
	}
	@Test
	void getManyUsersOneConditionArgReqSearchNullThrow() {
		assertThrows(IllegalArgumentException.class , () -> userService.getManyUsersOneCondition(new PageInfoDto(), null));
	}
	
	@Test
	void getManyUsersOneConditionArgPageInfoDtoSortDirNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder()
				.pageNo(0)
				.pageSize(10)
				.sortField("username")
				.build();
		assertThrows(IllegalArgumentException.class , () -> userService.getManyUsersOneCondition(pageInfoDto, new ReqSearch()));
	}
	
	@Test
	void getManyUsersOneConditionArgPageInfoDtoSortFieldNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder()
				.pageNo(0)
				.pageSize(10)
				.sortDir(Direction.ASC)
				.build();
		assertThrows(IllegalArgumentException.class , () -> userService.getManyUsersOneCondition(pageInfoDto, new ReqSearch()));
	}
	
	@Test
	void getManyUsersOneConditionReturnsNotNull() {
		PageInfoDto pageInfoDto = PageInfoDto.builder()
				.pageNo(0)
				.pageSize(10)
				.sortDir(Direction.ASC)
				.sortField("username")
				.build();
		ReqSearch reqSearch = ReqSearch.builder().build();
		ResPaginationG<UserDto> resPaginationG = new ResPaginationG<UserDto>();
		UserServiceImpl spyUserService = spy(userService);
		doReturn(resPaginationG).when(spyUserService).getManyUsersManyConditions(eq(pageInfoDto), any(ReqSearchList.class));
		
		assertNotNull(spyUserService.getManyUsersOneCondition(pageInfoDto, reqSearch));
		
		verify(spyUserService).getManyUsersManyConditions(eq(pageInfoDto), any(ReqSearchList.class));
		
	}
	
	//getManyUsersManyConditions
	@Test
	void getManyUsersManyConditionsArgPageInfoDtoNullThrow() {
		assertThrows(IllegalArgumentException.class , () -> userService.getManyUsersManyConditions(null, new ReqSearchList()));
	}
	@Test
	void getManyUsersManyConditionsArgReqSearchNullThrow() {
		assertThrows(IllegalArgumentException.class , () -> userService.getManyUsersOneCondition(new PageInfoDto(), null));
	}
	
	@Test
	void getManyUsersManyConditionsPageInfoDtoSortDirNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder()
				.pageNo(0)
				.pageSize(10)
				.sortField("username")
				.build();
		assertThrows(IllegalArgumentException.class , () -> userService.getManyUsersManyConditions(pageInfoDto, new ReqSearchList()));
	}
	
	@Test
	void getManyUsersManyConditionsrgPageInfoDtoSortFieldNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder()
				.pageNo(0)
				.pageSize(10)
				.sortDir(Direction.ASC)
				.build();
		assertThrows(IllegalArgumentException.class , () -> userService.getManyUsersManyConditions(pageInfoDto, new ReqSearchList()));
	}
	
	
	@Test
	void getManyUsersManyConditionsNoRecordFoundThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder()
				.pageNo(0)
				.pageSize(10)
				.sortDir(Direction.ASC)
				.sortField("username")
				.build();
		ReqSearch reqSearch = ReqSearch.builder().build();
		ReqSearchList reqSearchList = ReqSearchList.builder().reqSearchs(List.of(reqSearch)).globalOperator(GlobalOperationEnum.AND).build();
		//spec for example only, does not match reqSearch
		Specification<User> spec = (root,query,criteriaBuilder) -> criteriaBuilder.equal(root.get("username"), user.getUsername());
		when(specService.getSpecification(reqSearchList.getReqSearchs(),reqSearchList.getGlobalOperator())).thenReturn(spec);
		//pageable
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		//dao
		when(userDao.findAll(eq(spec), any(Pageable.class))).thenReturn(Page.empty());
		
		assertThrows(RecordNotFoundException.class, () -> userService.getManyUsersManyConditions(pageInfoDto,reqSearchList));
		
		verify(specService).getSpecification(reqSearchList.getReqSearchs(),reqSearchList.getGlobalOperator());
		verify(userDao).findAll(eq(spec), any(Pageable.class));
	}
	
	@Test
	void getManyUsersManyConditionsReturnNotNull() {
		PageInfoDto pageInfoDto = PageInfoDto.builder()
				.pageNo(0)
				.pageSize(10)
				.sortDir(Direction.ASC)
				.sortField("username")
				.build();
		ReqSearch reqSearch = ReqSearch.builder().build();
		ReqSearchList reqSearchList = ReqSearchList.builder().reqSearchs(List.of(reqSearch)).globalOperator(GlobalOperationEnum.AND).build();
		Page<User> page = new PageImpl<>(List.of(user));
		ResPaginationG<UserDto> resPaginationG = new ResPaginationG<>();
		
		//spec for example only, does not match reqSearch
		Specification<User> spec = (root,query,criteriaBuilder) -> criteriaBuilder.equal(root.get("username"), user.getUsername());
		when(specService.getSpecification(reqSearchList.getReqSearchs(),reqSearchList.getGlobalOperator())).thenReturn(spec);
		//pageable
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		//dao
		when(userDao.findAll(eq(spec), any(Pageable.class))).thenReturn(page);
		//mapper
		when(userMapper.pageAndPageInfoDtoToResPaginationG(page, pageInfoDto)).thenReturn(resPaginationG);
		
		assertNotNull(userService.getManyUsersManyConditions(pageInfoDto,reqSearchList));
		
		verify(specService).getSpecification(reqSearchList.getReqSearchs(),reqSearchList.getGlobalOperator());
		verify(userDao).findAll(eq(spec), any(Pageable.class));
	}
	
	
	//existByUsername
	@Test
	void existByUsernameArgNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> userService.existsByUsername(null));
	}
	
	@Test
	void existByUsernameReturnNotNull() {
		assertNotNull(userService.existsByUsername(user.getUsername()));
	}
	
	//existsOneCondition
	@Test
	void existsOneConditionArgNullThrow() {
		assertThrows(IllegalArgumentException.class , () -> userService.existsOneCondition(null));
	}
	
	@Test
	void existsOneConditionColumnPasswordThrow() {
		ReqSearch reqSearch = ReqSearch.builder().column("password").build();
		assertThrows(IllegalArgumentException.class , () -> userService.existsOneCondition(reqSearch),
				"If in the column the value is 'password' then the research is not allowed");
	}

	@Test
	void existsOneConditionReturnNotNull() {
		ReqSearch reqSearch = ReqSearch.builder().build();
		//spec for example only, does not match reqSearch
		Specification<User> spec = (root,query,criteriaBuilder) -> criteriaBuilder.equal(root.get("username"), user.getUsername());
		when(specService.getSpecification(reqSearch)).thenReturn(spec);
		when(userDao.exists(spec)).thenReturn(true);
		assertNotNull(userService.existsOneCondition(reqSearch));
		verify(userDao).exists(spec);
	}
	
	//existsManyConditions
	@Test
	void existsManyConditionsArgNullThrow() {
		assertThrows(IllegalArgumentException.class , () -> userService.existsManyConditions(null));
	}
	
	@Test
	void existsManyConditionsColumnPasswordThrow() {
		ReqSearch reqSearch = ReqSearch.builder().column("password").build();
		ReqSearchList reqSearchList = ReqSearchList.builder().reqSearchs(List.of(reqSearch)).build();
		assertThrows(IllegalArgumentException.class , () -> userService.existsManyConditions(reqSearchList),
				"If in the column the value is 'password' then the research is not allowed");
	}
	
	@Test
	void existsManyConditionsReturnNotNull() {
		ReqSearchList reqSearchList = ReqSearchList.builder()
				.reqSearchs(List.of())
				.globalOperator(GlobalOperationEnum.AND)
				.build();
		//spec for example only, does not match reqSearch
		Specification<User> spec = (root,query,criteriaBuilder) -> criteriaBuilder.equal(root.get("username"), user.getUsername());
		when(specService.getSpecification(reqSearchList.getReqSearchs(),reqSearchList.getGlobalOperator())).thenReturn(spec);
		when(userDao.exists(spec)).thenReturn(true);
		assertNotNull(userService.existsManyConditions(reqSearchList));
		verify(userDao).exists(spec);
	}
	
	//getByUsernameIn
	@Test
	void getByUsernameInParamUsernamelistNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> userService.getByUsernameIn(null),
				"if usernameList is null an Exception should be throw");
		verify(userDao,never()).findByUsernameIn(null);
	}
	
	@Test
	void getByUsernameInParamUsernameSetEmptyThrow() {
		Set<String> emptySet = Collections.emptySet();
		assertThrows(IllegalArgumentException.class,() -> userService.getByUsernameIn(emptySet));
		verify(userDao,never()).findByUsernameIn(emptySet);
	}
	
	@Test
	void getByUsernameInNoUserFoundThrow() {
		Set<String> usernameList = Set.of("Mati");
		when(userDao.findByUsernameIn(anySet())).thenReturn(Collections.emptyList());
		
		assertThrows(RecordNotFoundException.class,() -> userService.getByUsernameIn(usernameList));
		verify(userDao).findByUsernameIn(anySet());
	}
	
	@Test
	void getByUsernameInReturnsNotNull() {
		Set<String> usernameList = Set.of("username1", "username2");
		List<User> userList = List.of(user);
		
		when(userDao.findByUsernameIn(usernameList)).thenReturn(userList);
		
		assertNotNull(userService.getByUsernameIn(usernameList));
		verify(userDao).findByUsernameIn(usernameList);
	}
}
