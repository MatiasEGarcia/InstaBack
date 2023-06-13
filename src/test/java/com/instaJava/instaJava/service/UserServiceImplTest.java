package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
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
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.dto.request.ReqSearchList;
import com.instaJava.instaJava.entity.PersonalDetails;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.GlobalOperationEnum;
import com.instaJava.instaJava.exception.ImageException;
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
	static private PersonalDetails personalDetails;
	static private PersonalDetailsDto personalDetailsDto;
	
	@BeforeAll
	static void entitiesSetUp() throws IOException {
		multipartFile = new MockMultipartFile("file", "test.txt",
			      "text/plain", "testing".getBytes());
		user = User.builder()
				.userId(1L)
				.username("Mati")
				.image(Base64.getEncoder().encodeToString(multipartFile.getBytes()))
				.visible(true)
				.build();
		
		personalDetails = PersonalDetails.builder().build();
		personalDetailsDto = PersonalDetailsDto.builder().build();
		
	}
	
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
	
	@Test
	void updateImageArgumentNullThrow() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		assertThrows(ImageException.class,() -> {userService.updateImage(null);});
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
	
	@Test
	void getImageReturnImage() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		assertEquals(user.getImage(), userService.getImage());
	}
	
	@Test
	void getPersonalDetailsUserNotHaveReturnEmptyOptional() {
		user.setPersonalDetails(null);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		Optional<PersonalDetails> perDet = userService.getPersonalDetailsByUser();
		if(perDet.isPresent()) fail("should return empty optional if the user don't have personalDetails");
	}
	
	@Test
	void getPersonalDetailsReturnOptionalPresent() {
		user.setPersonalDetails(personalDetails);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		Optional<PersonalDetails> perDet = userService.getPersonalDetailsByUser();
		if(perDet.isEmpty()) fail("should return present optional if the user have personalDetails");
	}
	
	@Test
	void savePersonalDetailsArgumentNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> {userService.savePersonalDetails(null);});
		verify(personalDetailsMapper,never()).personalDetailsDtoAndUserToPersonalDetails(personalDetailsDto, user);
		verify(personalDetailsDao,never()).save(personalDetails);
	}
	
	@Test
	void savePersonalDetailsArgumentNotNull() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);
		when(personalDetailsMapper.
				personalDetailsDtoAndUserToPersonalDetails(personalDetailsDto, user))
				.thenReturn(personalDetails);
		when(personalDetailsDao.save(personalDetails)).thenReturn(personalDetails);
		
		userService.savePersonalDetails(personalDetailsDto);
		
		verify(personalDetailsMapper)
			.personalDetailsDtoAndUserToPersonalDetails(personalDetailsDto, user);
		verify(personalDetailsDao).save(personalDetails);
	}
	
	@Test
	void changeVisible() {
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(user);//here the user is visible = true
		user.setVisible(false);
		when(userDao.save(user)).thenReturn(user);
		assertEquals(user, userService.changeVisible());
		verify(userDao).save(user);
	}
	
	@Test
	void getByIdArgNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> userService.getById(null));
	}
	
	@Test
	void getByIdNoMatchesReturnEmptyOptional() {
		when(userDao.findById(1L)).thenReturn(Optional.empty());
		Optional<User> optUser = userService.getById(1L);
		if(optUser.isPresent()) fail("if there is no matches should return empty Optional");
	}
	
	@Test
	void getByIdMatchesReturnPresentOptional() {
		when(userDao.findById(1L)).thenReturn(Optional.of(user));
		Optional<User> optUser = userService.getById(1L);
		if(optUser.isEmpty()) fail("if there is  matches should return present Optional");
	}
	
	@Test
	void getByUsernameArgNullThrow() {
	 	assertThrows(IllegalArgumentException.class, () -> userService.getByUsername(null));
	}
	
	@Test
	void getByUsernameNoExistReturnEmptyOptional() {
		when(userDao.findByUsername(user.getUsername())).thenReturn(null);
		Optional<User> optUser = userService.getByUsername(user.getUsername());
		if(optUser.isPresent()) fail("if user not found by username the should return empty Optional");
		verify(userDao).findByUsername(any(String.class));
	}
	
	@Test
	void getByUsernameExistReturnPresentOptional() {
		when(userDao.findByUsername(user.getUsername())).thenReturn(user);
		Optional<User> optUser = userService.getByUsername(user.getUsername());
		if(optUser.isEmpty()) fail("if user was found by username the should return present Optional");
		verify(userDao).findByUsername(any(String.class));
	}
	
	
	@Test
	void getOneUserOneConditionArgumentNullThrow() {
		assertThrows(IllegalArgumentException.class,() -> userService.getOneUserOneCondition(null));
	}
	
	@Test
	void getOneUserOneConditionColumnIsPasswordThrow() {
		ReqSearch reqSearch = ReqSearch.builder()
				.column("password")
				.build();
		assertThrows(IllegalArgumentException.class,() -> userService.getOneUserOneCondition(reqSearch)
				,"search cannot be realized with User's password attribute");
	}
	
	@Test
	void getOneUserOneConditionNoMatchesReturnEmptyOptional() {
		ReqSearch reqSearch = ReqSearch.builder().build();
		Specification<User> spec = (root,query,criteriaBuilder) -> criteriaBuilder.equal(root.get("username"), user.getUsername());
		when(specService.getSpecification(reqSearch)).thenReturn(spec);
		when(userDao.findOne(spec)).thenReturn(Optional.empty());
		Optional<User> optUser = userService.getOneUserOneCondition(reqSearch);
		if(optUser.isPresent()) fail("should return optional empty if there is no matches");
		verify(specService).getSpecification(reqSearch);
		verify(userDao).findOne(spec);
	}
	
	@Test
	void getOneUserOneConditionMatchesReturnPresentOptional() {
		ReqSearch reqSearch = ReqSearch.builder().build();
		//spec for example only, does not match reqSearch
		Specification<User> spec = (root,query,criteriaBuilder) -> criteriaBuilder.equal(root.get("username"), user.getUsername());
		when(specService.getSpecification(reqSearch)).thenReturn(spec);
		when(userDao.findOne(spec)).thenReturn(Optional.of(user));
		Optional<User> optUser = userService.getOneUserOneCondition(reqSearch);
		if(optUser.isEmpty()) fail("should return optional present if there is matches");
		verify(specService).getSpecification(reqSearch);
		verify(userDao).findOne(spec);
	}
	
	@Test
	void getOneUserManyConditionsArgNullThrow() {
		assertThrows(IllegalArgumentException.class ,() -> userService.getOneUserManyConditions(null));
	}
	
	@Test
	void getOneUserManyConditionsNoMatchesReturnEmptyOptional() {
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
		Optional<User> optUser = userService.getOneUserManyConditions(reqSearchList);
		if(optUser.isPresent()) fail("should return optional empty if matches not found");
		verify(specService).getSpecification(reqSearchList.getReqSearchs(), reqSearchList.getGlobalOperator());
		verify(userDao).findOne(spec);
	}
	
	@Test
	void getOneUserManyConditionsMatchesReturnPresentOptional() {
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
		Optional<User> optUser = userService.getOneUserManyConditions(reqSearchList);
		if(optUser.isEmpty()) fail("should return optional present if there is matches");
		verify(specService).getSpecification(reqSearchList.getReqSearchs(), reqSearchList.getGlobalOperator());
		verify(userDao).findOne(spec);
	}
	
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
	void getManyUsersOneCondition() {
		PageInfoDto pageInfoDto = PageInfoDto.builder()
				.pageNo(0)
				.pageSize(10)
				.sortDir(Direction.ASC)
				.sortField("username")
				.build();
		ReqSearch reqSearch = ReqSearch.builder().build();
		//spec for example only, does not match reqSearch
		Specification<User> spec = (root,query,criteriaBuilder) -> criteriaBuilder.equal(root.get("username"), user.getUsername());
		when(specService.getSpecification(reqSearch)).thenReturn(spec);
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		when(userDao.findAll(eq(spec), any(Pageable.class))).thenReturn(Page.empty());
		assertNotNull(userService.getManyUsersOneCondition(pageInfoDto, reqSearch));
		verify(specService).getSpecification(reqSearch);
		verify(userDao).findAll(eq(spec), any(Pageable.class));
	}
	
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
	void getManyUsersManyConditionsReturnNotNull() {
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
		when(pageUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		when(userDao.findAll(eq(spec), any(Pageable.class))).thenReturn(Page.empty());
		assertNotNull(userService.getManyUsersManyConditions(pageInfoDto,reqSearchList));
		verify(specService).getSpecification(reqSearchList.getReqSearchs(),reqSearchList.getGlobalOperator());
		verify(userDao).findAll(eq(spec), any(Pageable.class));
	}
	
	@Test
	void existByUsernameArgNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> userService.existsByUsername(null));
	}
	
	@Test
	void existByUsernameReturnNotNull() {
		assertNotNull(userService.existsByUsername(user.getUsername()));
	}
	
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
	
	
	
	
	
	
	
	
	
	
	
}
