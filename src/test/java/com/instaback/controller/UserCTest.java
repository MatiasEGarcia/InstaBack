package com.instaback.controller;

import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instaback.dao.InvTokenDao;
import com.instaback.dao.PersonalDetailsDao;
import com.instaback.dao.UserDao;
import com.instaback.dto.PersonalDetailsDto;
import com.instaback.dto.request.ReqLogout;
import com.instaback.dto.request.ReqSearch;
import com.instaback.dto.request.ReqSearchList;
import com.instaback.entity.PersonalDetails;
import com.instaback.entity.User;
import com.instaback.enums.FollowStatus;
import com.instaback.enums.GlobalOperationEnum;
import com.instaback.enums.OperationEnum;
import com.instaback.enums.RolesEnum;
import com.instaback.service.JwtService;
import com.instaback.util.MessagesUtils;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
class UserCTest {
	
	@SuppressWarnings("unused")
	private static MockHttpServletRequest request;

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private JwtService jwtService;
	@Autowired
	private MessagesUtils messUtils;
	@Autowired
	private JdbcTemplate jdbc;
	@Autowired
	private UserDao userDao;
	@Autowired
	private InvTokenDao invTokenDao;
	@Autowired
	private PersonalDetailsDao personalDetailsDao;
	@Autowired
	private ObjectMapper objectMapper;
	
	@Value("${sql.script.create.user.1}")
	private String sqlAddUser1;
	@Value("${sql.script.create.user.2}")
	private String sqlAddUser2;
	@Value("${sql.script.create.publicatedImage.2}")
	private String sqlAddPublicatedImage2;
	@Value("${sql.script.create.follow.statusInProcess}")
	private String sqlAddFollow;
	@Value("${sql.script.truncate.users}")
	private String sqlTruncateUsers;
	@Value("${sql.script.truncate.follow}")
	private String sqlTruncateFollow;
	@Value("${sql.script.truncate.publicatedImages}")
	private String sqlTruncatePublicatedImages;
	@Value("${sql.script.truncate.personalDetails}")
	private String sqlTruncatePersonalDetails;
	@Value("${sql.script.ref.integrity.false}")
	private String sqlRefIntegrityFalse;
	@Value("${sql.script.ref.integrity.true}")
	private String sqlRefIntegrityTrue;
	
	private static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON;
	//this user is in the bdd , because we save it with sqlAddUser1
	private User matiAuth = User.builder()
			.id(1L)
			.username("matias")
			.password("123456")
			.role(RolesEnum.ROLE_USER)
			.build();
	
	
	@BeforeAll
	static void mockSetup() throws IOException {
		request = new MockHttpServletRequest();
	}

	@BeforeEach
	void dbbSetUp() {
		jdbc.update(sqlAddUser1);
		jdbc.update(sqlAddUser2);
		jdbc.update(sqlAddPublicatedImage2);
		jdbc.update(sqlAddFollow);
	}

	@Test
	void getUserBasicInfoOk() throws Exception{
		String token = jwtService.generateToken(matiAuth);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/userBasicInfo")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username", is(matiAuth.getUsername())))
				.andExpect(jsonPath("$.visible", is(true)));
		
	}
	
	
	@Test
	void postUploadImageOk() throws Exception {
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", 
				 MediaType.IMAGE_JPEG_VALUE, 
		        "Hello, World!".getBytes()
		      );
		String imgBase64 = Base64.getEncoder().encodeToString(img.getBytes());
		String token = jwtService.generateToken(matiAuth);
		
		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/users/image")
				.file(img)
				.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
				.header("Authorization","Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.image64",equalToIgnoringCase(imgBase64)));
	}	
	

	@Test
	void postUploadImageWrongArchiveBadRequst() throws Exception {
		//THe type of the archive is wrong
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", 
				"text/plain", 
		        "Hello, World!".getBytes()
		      );
		String token = jwtService.generateToken(matiAuth);
		
		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/users/image")
				.file(img)
				.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
				.header("Authorization","Bearer " + token))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.error",is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("client.type-incorrect"))))
				.andExpect(jsonPath("$.details.file",is(messUtils.getMessage("vali.image"))));
	}
	
	@Test
	void getDownloadImageOk() throws Exception {
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", 
				 MediaType.IMAGE_JPEG_VALUE, 
		        "Hello, World!".getBytes()
		      );
		String imgBase64 = Base64.getEncoder().encodeToString(img.getBytes());
		String token = jwtService.generateToken(matiAuth);
		matiAuth.setImage(imgBase64);
		userDao.save(matiAuth);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/image")
				.header("Authorization","Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.image64",is(imgBase64)));
	}
	
	@Test
	void postLogoutOk() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		ReqLogout reqLogout=ReqLogout.builder()
			.token("SomeStringToken")
			.refreshToken("SomeRefreshToken")
			.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/logout")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(reqLogout)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("user.logout-ok"))));
		
		assertTrue(invTokenDao.existsByToken(reqLogout.getToken()));
		assertTrue(invTokenDao.existsByToken(reqLogout.getRefreshToken()));
	}
	
	@Test
	void postLogoutWithoutTokensBadRequest() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		ReqLogout reqLogout=ReqLogout.builder().build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/logout")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(reqLogout)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error",is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("client.body-not-fulfilled"))))
				.andExpect(jsonPath("$.details.token",is(messUtils.getMessage("vali.token-not-blank"))))
				.andExpect(jsonPath("$.details.refreshToken", is(messUtils.getMessage("vali.refreshToken-not-blank"))));
	}
	
	@Test
	void postSavePersonalDetailsOk() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		Byte age = 23;
		PersonalDetailsDto perDetDto = PersonalDetailsDto.builder()
				.name("Mati")
				.lastname("Gar")
				.age(age)
				.email("matig@gmail.com")
				.build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/personalDetails")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsBytes(perDetDto)))
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name",is(perDetDto.getName())))
				.andExpect(jsonPath("$.lastname",is(perDetDto.getLastname())))
				.andExpect(jsonPath("$.age",is(23)))
				.andExpect(jsonPath("$.email",is(perDetDto.getEmail())));
		
		assertNotNull(personalDetailsDao.findByUser(matiAuth));
	}
	
	@Test
	void postSavePersonalDetailsBodyAtributesBlankBadRequest() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		PersonalDetailsDto perDetDto = PersonalDetailsDto.builder().build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/personalDetails")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(perDetDto)))
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error",is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("client.body-not-fulfilled"))))
				.andExpect(jsonPath("$.details.name",is(messUtils.getMessage("vali.name-not-blank"))))
				.andExpect(jsonPath("$.details.lastname",is(messUtils.getMessage("vali.lastname-not-blank"))))
				.andExpect(jsonPath("$.details.age",is(messUtils.getMessage("vali.age-range"))))
				.andExpect(jsonPath("$.details.email",is(messUtils.getMessage("vali.email-not-blank"))));
	
		assertNull(personalDetailsDao.findByUser(matiAuth));
	}
	
	@Test
	void putVisibleOk() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/users/visible")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.visible", is(false))); //before user was visible = true
	}
	
	@Test
	void getPersonalDetailsNoExistNotFound() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/personalDetails")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", is(HttpStatus.NOT_FOUND.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("perDet.not-found"))));
	}
	
	@Test
	void getPersonalDetailsExistReturnPersonDetOk() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		Byte age = 23;
		PersonalDetails perDet = PersonalDetails.builder()
				.name(token)
				.lastname(token)
				.age(age)
				.email("matig@gmail.com")
				.user(matiAuth)
				.build();
		personalDetailsDao.save(perDet);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/personalDetails")
				.header("Authorization", "Bearer " + token))
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name",is(perDet.getName())))
				.andExpect(jsonPath("$.lastname",is(perDet.getLastname())))
				.andExpect(jsonPath("$.age",is(23)))
				.andExpect(jsonPath("$.email",is(perDet.getEmail())));
	}
	
	
	@Test
	void postSearchUserWithOneConditionNoMatchesNotFound() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		ReqSearch reqSearch = ReqSearch.builder().column("username").value("random")
				.dateValue(false).operation(OperationEnum.EQUAL).build();
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/searchOne/oneCondition")
				.header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(reqSearch))
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", is(HttpStatus.NOT_FOUND.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("user.not-found"))));
		
	}
	@Test
	void postSearchUserWithOneConditionReqSearchNullBlankValuesBlankBadRequest() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		ReqSearch reqSearch = ReqSearch.builder().build();
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/searchOne/oneCondition")
				.header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(reqSearch))
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error",is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("client.body-not-fulfilled"))))
				.andExpect(jsonPath("$.details.column", is(messUtils.getMessage("vali.ReqSearch.column-not-blank"))))
				.andExpect(jsonPath("$.details.value", is(messUtils.getMessage("vali.ReqSearch.value-not-blank"))))
				.andExpect(jsonPath("$.details.dateValue", is(messUtils.getMessage("vali.ReqSearch.dateValue-not-null"))))
				.andExpect(jsonPath("$.details.operation", is(messUtils.getMessage("vali.ReqSearch.operation-not-null"))));
	}
	@Test
	void postSearchUserWithOneConditionMatchesOk() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		ReqSearch reqSearch = ReqSearch.builder().column("username").value(matiAuth.getUsername())
				.dateValue(false).operation(OperationEnum.EQUAL).build();
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/searchOne/oneCondition")
				.header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(reqSearch))
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.username" , is(matiAuth.getUsername())))
				.andExpect(jsonPath("$.image" , is(matiAuth.getImage())))
				.andExpect(jsonPath("$.visible" , is(true)));
		//this user data we save it as sqlAddUser1 in dbSetUp method
	}
	
	
	@Test
	void postSearchUserWithManyConditionsNoMatchesNotFound() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		ReqSearch reqSearch = ReqSearch.builder().column("username").value("random")
				.dateValue(false).operation(OperationEnum.EQUAL).build();
		ReqSearchList reqSearchList = ReqSearchList.builder().reqSearchs(List.of(reqSearch))
				.globalOperator(GlobalOperationEnum.AND).build();
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/searchOne/manyConditions")
				.header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(reqSearchList))
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", is(HttpStatus.NOT_FOUND.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("user.not-found"))));
				
		
	}
	@Test
	void postSearchUserWithManyConditionsMatchesOk() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		ReqSearch reqSearch = ReqSearch.builder().column("username").value(matiAuth.getUsername())
				.dateValue(false).operation(OperationEnum.EQUAL).build();
		ReqSearchList reqSearchList = ReqSearchList.builder().reqSearchs(List.of(reqSearch))
				.globalOperator(GlobalOperationEnum.AND).build();
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/searchOne/manyConditions")
				.header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(reqSearchList))
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.username" , is(matiAuth.getUsername())))
				.andExpect(jsonPath("$.image" , is(matiAuth.getImage())))
				.andExpect(jsonPath("$.visible" , is(true)));
		//this user data we save it as sqlAddUser1 in dbSetUp method
	}
	@Test
	void postSearchUserWithManyConditionsReqSearchListNullValuesBadRequest() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		ReqSearchList reqSearchList = ReqSearchList.builder().build();
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/searchOne/manyConditions")
				.header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(reqSearchList))
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error",is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("client.body-not-fulfilled"))))
				.andExpect(jsonPath("$.details.reqSearchs", is(messUtils.getMessage("vali.reqSearchs-not-null"))))
				.andExpect(jsonPath("$.details.globalOperator", is(messUtils.getMessage("vali.globalOperator-not-null"))));
	}
	
	
	@Test
	void postSearchUsersWithOneConditionNoParamsMatchesOk() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		ReqSearch reqSearch = ReqSearch.builder().column("username").value(matiAuth.getUsername())
				.dateValue(false).operation(OperationEnum.EQUAL).build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/searchAll/oneCondition")
				.header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(reqSearch))
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.list" , hasSize(1)))
				.andExpect(jsonPath("$.pageInfoDto.pageNo", is(0))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.pageSize", is(20))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.totalPages", is(1))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.totalElements", is(1))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.sortField", is("id"))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.sortDir", is(Direction.ASC.toString()))); //default value if the user don't pass any param
		//we only save 1 user with that username, we save in dbbSetUp method
	}
	@Test
	void postSearchUsersWithOneConditionWithParamsMatchesOk() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		ReqSearch reqSearch = ReqSearch.builder().column("username").value(matiAuth.getUsername())
				.dateValue(false).operation(OperationEnum.EQUAL).build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/searchAll/oneCondition")
				.header("Authorization", "Bearer " + token)
				.param("sortField", "username")
				.param("sortDir", Direction.DESC.toString())
				.content(objectMapper.writeValueAsString(reqSearch))
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.list" , hasSize(1)))
				.andExpect(jsonPath("$.pageInfoDto.pageNo", is(0))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.pageSize", is(20))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.totalPages", is(1))) 
				.andExpect(jsonPath("$.pageInfoDto.totalElements", is(1))) 
				.andExpect(jsonPath("$.pageInfoDto.sortField", is("username")))
				.andExpect(jsonPath("$.pageInfoDto.sortDir", is(Direction.DESC.toString())));
		//we only save 1 user with that username, we save in dbbSetUp method
	}
	@Test
	void postSearchUsersWithOneConditionNoMatchesNoContent() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		ReqSearch reqSearch = ReqSearch.builder().column("username").value("random")
				.dateValue(false).operation(OperationEnum.EQUAL).build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/searchAll/oneCondition")
				.header("Authorization", "Bearer " + token)
				.param("sortField", "username")
				.param("sortDir", Direction.DESC.toString())
				.content(objectMapper.writeValueAsString(reqSearch))
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isNoContent())
				.andExpect(header().string(messUtils.getMessage("key.header-detail-exception"),
						is(messUtils.getMessage("user.group-not-found"))));
	}
	@Test
	void postSearchUsersWithOneConditionreqSearchNullBlankValuesBadRequest() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		ReqSearch reqSearch = ReqSearch.builder().build();
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/searchAll/oneCondition")
				.header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(reqSearch))
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error",is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("client.body-not-fulfilled"))))
				.andExpect(jsonPath("$.details.column", is(messUtils.getMessage("vali.ReqSearch.column-not-blank"))))
				.andExpect(jsonPath("$.details.value", is(messUtils.getMessage("vali.ReqSearch.value-not-blank"))))
				.andExpect(jsonPath("$.details.dateValue", is(messUtils.getMessage("vali.ReqSearch.dateValue-not-null"))))
				.andExpect(jsonPath("$.details.operation", is(messUtils.getMessage("vali.ReqSearch.operation-not-null"))));
	}
	
	
	@Test
	void postSearchUsersWithManyConditionsNoParamsOk() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		ReqSearch reqSearch = ReqSearch.builder().column("username").value(matiAuth.getUsername())
				.dateValue(false).operation(OperationEnum.EQUAL).build();
		ReqSearchList reqSearchList = ReqSearchList.builder().reqSearchs(List.of(reqSearch))
				.globalOperator(GlobalOperationEnum.AND).build();
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/searchAll/manyConditions")
				.header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(reqSearchList))
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.list", hasSize(1)))
				.andExpect(jsonPath("$.pageInfoDto.pageNo", is(0))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.pageSize", is(20))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.totalPages", is(1))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.totalElements", is(1))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.sortField", is("id"))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.sortDir", is(Direction.ASC.toString()))); //default value if the user don't pass any param;
		//this user data we save it as sqlAddUser1 in dbSetUp method
	}
	@Test
	void postSearchUsersWithManyConditionsWithParamsOk() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		ReqSearch reqSearch = ReqSearch.builder().column("username").value(matiAuth.getUsername())
				.dateValue(false).operation(OperationEnum.EQUAL).build();
		ReqSearchList reqSearchList = ReqSearchList.builder().reqSearchs(List.of(reqSearch))
				.globalOperator(GlobalOperationEnum.AND).build();
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/searchAll/manyConditions")
				.header("Authorization", "Bearer " + token)
				.param("sortField", "username")
				.param("sortDir", Direction.DESC.toString())
				.content(objectMapper.writeValueAsString(reqSearchList))
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.list", hasSize(1)))
				.andExpect(jsonPath("$.pageInfoDto.pageNo", is(0))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.pageSize", is(20))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.totalPages", is(1))) 
				.andExpect(jsonPath("$.pageInfoDto.totalElements", is(1))) 
				.andExpect(jsonPath("$.pageInfoDto.sortField", is("username")))
				.andExpect(jsonPath("$.pageInfoDto.sortDir", is(Direction.DESC.toString())));
		//this user data we save it as sqlAddUser1 in dbSetUp method
	}
	@Test
	void postSearchUsersWithManyConditionsNoMatchesNoContent() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		ReqSearch reqSearch = ReqSearch.builder().column("username").value("random")
				.dateValue(false).operation(OperationEnum.EQUAL).build();
		ReqSearchList reqSearchList = ReqSearchList.builder().reqSearchs(List.of(reqSearch))
				.globalOperator(GlobalOperationEnum.AND).build();
		
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/searchAll/manyConditions")
				.header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(reqSearchList))
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isNoContent())
				.andExpect(header().string(messUtils.getMessage("key.header-detail-exception"),
						is(messUtils.getMessage("user.group-not-found"))));
	}
	@Test
	void postSearchUsersWithManyConditionsReqSearchListNullValuesBadRequest() throws Exception {
		String token = jwtService.generateToken(matiAuth);
		ReqSearchList reqSearchList = ReqSearchList.builder().build();
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/searchAll/manyConditions")
				.header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(reqSearchList))
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error",is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("client.body-not-fulfilled"))))
				.andExpect(jsonPath("$.details.reqSearchs", is(messUtils.getMessage("vali.reqSearchs-not-null"))))
				.andExpect(jsonPath("$.details.globalOperator", is(messUtils.getMessage("vali.globalOperator-not-null"))));
	}
	
	@Test
	void getGetUserGeneralInfoByIdUserNoExistsNotFound() throws Exception{
		String token = jwtService.generateToken(matiAuth);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/generalInfoById/{id}",100)
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", is(HttpStatus.NOT_FOUND.toString())))
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("user.not-found"))));//ya no existe este mensaje
	}
	
	@Test
	void getGetUserGeneralInfoByIdOk() throws Exception{
		String token = jwtService.generateToken(matiAuth);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/generalInfoById/{id}",2)
				.header("Authorization", "Bearer " + token))
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.user.id", is("2")))
				.andExpect(jsonPath("$.social.numberPublications", is("1")))
				.andExpect(jsonPath("$.social.numberFollowers", is("0")))
				.andExpect(jsonPath("$.social.numberFollowed", is("0")))// is in IN_PROCESS status , so is 0 followed
				.andExpect(jsonPath("$.social.followerFollowStatus", is(FollowStatus.NOT_ASKED.toString())))
				.andExpect(jsonPath("$.social.followedFollowStatus", is(FollowStatus.IN_PROCESS.toString())));
	}
	
	
	
	@Test
	void getWebSocketToken() throws Exception{
		String token = jwtService.generateToken(matiAuth);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/webSocketToken")
				.header("Authorization", "Bearer " + token))
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.webSocketAuthToken", instanceOf(String.class)));
		
	}
	
	
	@AfterEach
	void setUpAfterTransaction() {
		jdbc.execute(sqlRefIntegrityFalse);
		jdbc.execute(sqlTruncateUsers);
		jdbc.execute(sqlTruncateFollow);
		jdbc.execute(sqlTruncatePublicatedImages);
		jdbc.execute(sqlTruncatePersonalDetails);
		jdbc.execute(sqlRefIntegrityTrue);
	}

}
