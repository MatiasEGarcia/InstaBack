package com.instaJava.instaJava.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Optional;

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

import com.instaJava.instaJava.dao.PublicatedImagesDao;
import com.instaJava.instaJava.dao.UserDao;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.service.JwtService;
import com.instaJava.instaJava.util.MessagesUtils;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
class PublicatedImageCTest {
	@SuppressWarnings("unused")
	private static MockHttpServletRequest request;

	@Autowired private MockMvc mockMvc;
	@Autowired private PublicatedImagesDao publicatedImagesDao;
	@Autowired private UserDao userDao;
	@Autowired private MessagesUtils messUtils;
	@Autowired private JdbcTemplate jdbc;
	@Autowired private JwtService jwtService;
	
	@Value("${sql.script.create.user.1}")
	private String sqlAddUser1;
	@Value("${sql.script.create.user.2}")
	private String sqlAddUser2;
	@Value("${sql.script.update.user.1.visible.false}")
	private String sqlUpdateUser1;
	@Value("${sql.script.create.publicatedImage}")
	private String sqlAddPublicatedImage;
	@Value("${sql.script.create.publicatedImage.2}")
	private String sqlAddPublicatedImage2;
	@Value("${sql.script.create.follow.statusInProcess}")
	private String sqlAddFollow;
	@Value("${sql.script.update.follow.statusAccepted.on.follow1}")
	private String updateFollow1ToAccepted;
	@Value("${sql.script.truncate.users}")
	private String sqlTruncateUsers;
	@Value("${sql.script.truncate.publicatedImages}")
	private String sqlTruncatePublicatedImages;
	@Value("${sql.script.truncate.follow}")
	private String sqlTruncateFollow;
	@Value("${sql.script.ref.integrity.false}")
	private String sqlRefIntegrityFalse;
	@Value("${sql.script.ref.integrity.true}")
	private String sqlRefIntegrityTrue;
	
	
	private static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON;
	//same user that sqlAddUser1
	private User userAuthMati = User.builder()
			.userId(1L)
			.username("matias")
			.password("$2a$10$Z/mAWx8fyvjzn2V.xDDge.SnMkyVyFfAcLlEJUHQ0DqXfqrao8wke")
			.visible(true)
			.role(RolesEnum.ROLE_USER)
			.build();
	//same user that sqlAddUser2
	private User userAuthRoci = User.builder()
			.userId(2L)
			.username("rocio")
			.password("$2a$10$Z/mAWx8fyvjzn2V.xDDge.SnMkyVyFfAcLlEJUHQ0DqXfqrao8wke")
			.visible(false)
			.role(RolesEnum.ROLE_USER)
			.build();
	
	
	@BeforeAll
	static void mockSetup() {
		request = new MockHttpServletRequest();
	}
	
	@BeforeEach
	void bddSetUp() { 
		jdbc.execute(sqlAddUser1); //userAuthMati
		jdbc.execute(sqlAddUser2); //userAuthRoci
		jdbc.execute(sqlAddPublicatedImage); //this has as ownerUser -> sqlAddUser1
		jdbc.execute(sqlAddPublicatedImage2); // this has as ownerUser -> sqlAddUser2
		jdbc.execute(sqlAddFollow); //sqlAddUser1 is the followed and sqlAddUser2 is the follower
		
	}
	
	//save
	@Test
	void postSaveStatusOk() throws Exception {
		String token = jwtService.generateToken(userAuthMati);
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", 
				 MediaType.IMAGE_JPEG_VALUE, 
		        "Hello, World!".getBytes()
		      );
		String imgBase64 = Base64.getEncoder().encodeToString(img.getBytes());
		String description = "description";
		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/publicatedImages/save")
				.file(img)
				.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
				.header("authorization", "Bearer " + token)
				.param("description", description))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id",is("3")))
				.andExpect(jsonPath("$.createdAt",instanceOf(String.class)))
				.andExpect(jsonPath("$.image", is(imgBase64)))
				.andExpect(jsonPath("$.description",is(description)))
				.andExpect(jsonPath("$.userOwner.userId",is(userAuthMati.getUserId().toString())));
		assertNotNull(publicatedImagesDao.findById(1L));
	}
	@Test
	void postSaveStatusBadRequestImageTypeInvalid() throws Exception {
		String token = jwtService.generateToken(userAuthMati);
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", 
				"text/plain", 
		        "Hello, World!".getBytes()
		      );
		String description = "description";
		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/publicatedImages/save")
				.file(img)
				.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
				.header("authorization", "Bearer " + token)
				.param("description", description))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.error",is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("client.type-incorrect"))))
				.andExpect(jsonPath("$.details.file",is(messUtils.getMessage("vali.image"))));
	}
	@Test
	void postSaveStatusBadRequestImgNotGiven() throws Exception {
		String token = jwtService.generateToken(userAuthMati);
		String description = "description";
		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/publicatedImages/save")
				.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
				.header("authorization", "Bearer " + token)
				.param("description", description))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.error",is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("vali.part.not.present"))));
	}

	//deleteById
	@Test
	void deleteDeleteByIdStatusOk() throws Exception {
		String token = jwtService.generateToken(userAuthMati);
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", 
				 MediaType.IMAGE_JPEG_VALUE, 
		        "Hello, World!".getBytes()
		      );
		String imgBase64 = Base64.getEncoder().encodeToString(img.getBytes());
		publicatedImagesDao.save(PublicatedImage.builder()
				.image(imgBase64)
				.createdAt(ZonedDateTime.now(Clock.systemUTC()))
				.description("random")
				.userOwner(userAuthMati)
				.build());
		
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/publicatedImages/{id}",1)
				.header("authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("generic.delete-ok"))));
		
		Optional<PublicatedImage> PublicatedImage = publicatedImagesDao.findById(1L);
		if(PublicatedImage.isPresent()) fail();
	}
	
	//getById
	@Test
	void getGetByIdBadRequest() throws Exception {///////////////////////////////////////////////////////////////////////////////////////
		jdbc.execute(sqlUpdateUser1); //now mati is not visible.
		String token = jwtService.generateToken(userAuthRoci);//this user don't have follow status accepted with the publication owner.
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/publicatedImages/{id}",1)
				.header("Authorization", "Bearer " + token))
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	void getGetByIdOk() throws Exception {
		jdbc.execute(updateFollow1ToAccepted);//now follow has status accepted, so roci can get mati publications
		String token = jwtService.generateToken(userAuthRoci);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/publicatedImages/{id}",1)
				.header("Authorization", "Bearer " + token))
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is("1")))
				.andExpect(jsonPath("$.userOwner.userId", is("1")));
	}
	
	@Test
	void getGetByIdNotFound() throws Exception {
		String token = jwtService.generateToken(userAuthRoci);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/publicatedImages/{id}",10000)
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound());
	}
	
	//getAllByOwnerVisible
	@Test
	void getGetAllByOwnerVisibleWithoutParamsOk() throws Exception {
		String token = jwtService.generateToken(userAuthMati); //this user is public and has publicated images
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/publicatedImages/byVisiblesOwners")
				.header("Authorization", "Bearer " + token))
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.list", hasSize(1)))
				.andExpect(jsonPath("$.pageInfoDto.pageNo", is(0))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.pageSize", is(20))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.totalPages", is(1))) 
				.andExpect(jsonPath("$.pageInfoDto.totalElements", is(1))) 
				.andExpect(jsonPath("$.pageInfoDto.sortField", is("publImgId")))  //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.sortDir", is(Direction.ASC.toString())));   //default value if the user don't pass any param
	}
	@Test
	void getGetAllByOwnerVisibleWithParamsOk() throws Exception {
		String token = jwtService.generateToken(userAuthMati); //this user is public and has publicated images
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/publicatedImages/byVisiblesOwners")
				.header("Authorization", "Bearer " + token)
				.param("sortField", "userOwner_username")
				.param("sortDir", Direction.DESC.toString()))
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.list", hasSize(1)))
				.andExpect(jsonPath("$.pageInfoDto.pageNo", is(0))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.pageSize", is(20))) //default value if the user don't pass any param
				.andExpect(jsonPath("$.pageInfoDto.totalPages", is(1))) 
				.andExpect(jsonPath("$.pageInfoDto.totalElements", is(1))) 
				.andExpect(jsonPath("$.pageInfoDto.sortField", is("userOwner_username")))  
				.andExpect(jsonPath("$.pageInfoDto.sortDir", is(Direction.DESC.toString()))); 
	}
	@Test
	void getGetAllByOwnerVisibleWithoutParamsNoContent() throws Exception {
		publicatedImagesDao.deleteById(1L); //we delete the only publicated image belong to the only visible/public user
		String token = jwtService.generateToken(userAuthRoci); //this user hasn't publicated images
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/publicatedImages/byVisiblesOwners")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isNoContent())
				.andExpect(header().string(messUtils.getMessage("key.header-detail-exception"), messUtils.getMessage("publiImage.group-not-found")));
	}
	
	//getAllByOwner
	@Test
	void getGetAllByOwnerOk() throws Exception{
		String token = jwtService.generateToken(userAuthRoci);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/publicatedImages/byOwnerId/{ownerId}",1)//the sqlAddUser1 id
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.list", hasSize(1)))//should have at least 1 publication
		        .andExpect(jsonPath("$.pageInfoDto.pageNo", is(0))) //default value if the user don't pass any param
		        .andExpect(jsonPath("$.pageInfoDto.pageSize", is(20))) //default value if the user don't pass any param
		        .andExpect(jsonPath("$.pageInfoDto.totalPages", is(1))) 
		        .andExpect(jsonPath("$.pageInfoDto.totalElements", is(1))) 
		        .andExpect(jsonPath("$.pageInfoDto.sortField", is("publImgId")))  //default value if the user don't pass any param
		        .andExpect(jsonPath("$.pageInfoDto.sortDir", is(Direction.ASC.toString())));   //default value if the user don't pass any param
	}
	@Test
	void getGetAllByOwnerNoContentThereIsNotPublicaitons() throws Exception{
		String token = jwtService.generateToken(userAuthRoci);
		publicatedImagesDao.deleteById(1L);//now the sqlAddUser1 don't have publications
		
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/publicatedImages/byOwnerId/{ownerId}",1)//the sqlAddUser1 id
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isNoContent())
				.andExpect(header().string(messUtils.getMessage("key.header-detail-exception"), messUtils.getMessage("publiImage.group-not-found")));   
	}
	@Test
	void getGetAllByOwnerNoContentUserPrivateAndStatusInProcess() throws Exception {
		userAuthMati.setVisible(false);//we make sqlAddUser1 private
		userDao.save(userAuthMati);
		String token = jwtService.generateToken(userAuthRoci);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/publicatedImages/byOwnerId/{ownerId}",1)//the sqlAddUser1 id
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error", is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("follow.followStatus-in-process"))));
	}
	
	
	//getAllByUsersFollowed
	@Test
	void getGetAllByUsersFollowedWithoutParamsOk() throws Exception{
		String token = jwtService.generateToken(userAuthRoci);
		jdbc.execute(updateFollow1ToAccepted);//now follow has status accepted, so roci can get mati publications
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/publicatedImages/byUsersFollowed")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.list", hasSize(1)))//should have at least 1 publication
        		.andExpect(jsonPath("$.pageInfoDto.pageNo", is(0))) //default value if the user don't pass any param
        		.andExpect(jsonPath("$.pageInfoDto.pageSize", is(20))) //default value if the user don't pass any param
        		.andExpect(jsonPath("$.pageInfoDto.totalPages", is(1))) 
        		.andExpect(jsonPath("$.pageInfoDto.totalElements", is(1))) 
        		.andExpect(jsonPath("$.pageInfoDto.sortField", is("publImgId")))  //default value if the user don't pass any param
        		.andExpect(jsonPath("$.pageInfoDto.sortDir", is(Direction.ASC.toString())));   //default value if the user don't pass any param
	}
	
	@Test
	void getGetAllByUsersFollowedWithoutParamsNoContent() throws Exception{
		String token = jwtService.generateToken(userAuthRoci);
		//roci only tries to follow mati, but follow status is in process , so there should be none publication
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/publicatedImages/byUsersFollowed")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isNoContent())
				.andExpect(header().string(messUtils.getMessage("key.header-detail-exception"), 
						messUtils.getMessage("publiImage.group-not-found")));   
	}
	
	
	@AfterEach
	void bddDataDelete() {
		jdbc.execute(sqlRefIntegrityFalse);
		jdbc.execute(sqlTruncateUsers);
		jdbc.execute(sqlTruncatePublicatedImages);
		jdbc.execute(sqlTruncateFollow);
		jdbc.execute(sqlRefIntegrityTrue);
	}
}
