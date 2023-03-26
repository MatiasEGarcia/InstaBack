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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.instaJava.instaJava.dao.PublicatedImagesDao;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.service.JwtService;
import com.instaJava.instaJava.util.MessagesUtils;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
class PublicatedImageCTest {
	private static MockHttpServletRequest request;

	@Autowired private MockMvc mockMvc;
	@Autowired private PublicatedImagesDao publicatedImagesDao;
	@Autowired private MessagesUtils messUtils;
	@Autowired private JdbcTemplate jdbc;
	@Autowired private JwtService jwtService;
	
	@Value("${sql.script.create.user.1}")
	private String sqlAddUser1;
	@Value("${sql.script.create.user.2}")
	private String sqlAddUser2;
	@Value("${sql.script.create.publicatedImage}")
	private String sqlAddPublicatedImage;
	@Value("${sql.script.truncate.users}")
	private String sqlTruncateUsers;
	@Value("${sql.script.truncate.publicatedImages}")
	private String sqlTruncatePublicatedImages;
	@Value("${sql.script.ref.integrity.false}")
	private String sqlRefIntegrityFalse;
	@Value("${sql.script.ref.integrity.true}")
	private String sqlRefIntegrityTrue;
	
	
	private static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON;
	//same user that sqlAddUser1
	private User userAuthMati = User.builder()
			.userId(1L)
			.username("matias")
			.password("123456")
			.role(RolesEnum.ROLE_USER)
			.build();
	//same user that sqlAddUser2
	private User userAuthRoci = User.builder()
			.userId(2L)
			.username("rocio")
			.password("123456")
			.role(RolesEnum.ROLE_USER)
			.build();
	
	
	@BeforeAll
	static void mockSetup() {
		request = new MockHttpServletRequest();
	}
	
	@BeforeEach
	void bddSetUp() {
		jdbc.execute(sqlAddUser1);
		jdbc.execute(sqlAddUser2);
		jdbc.execute(sqlAddPublicatedImage); //this has as user -> sqlAddUser1
		
	}
	
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
				.header("authorization", "Bearer " + token)
				.param("description", description))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id",is(2)))
				.andExpect(jsonPath("$.createdAt",instanceOf(String.class)))
				.andExpect(jsonPath("$.image", is(imgBase64)))
				.andExpect(jsonPath("$.description",is(description)))
				.andExpect(jsonPath("$.userOwner",is(userAuthMati.getUsername())));
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
				.header("authorization", "Bearer " + token)
				.param("description", description))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.file",is(messUtils.getMessage("vali.image"))));
	}
	
	@Test
	void postSaveStatusBadRequestImgNotGiven() throws Exception {
		String token = jwtService.generateToken(userAuthMati);
		String description = "description";
		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/publicatedImages/save")
				.header("authorization", "Bearer " + token)
				.param("description", description))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.field",is("img")))
				.andExpect(jsonPath("$.errorMessage",is(messUtils.getMessage("vali.part.not.present"))));
	}
	
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
				.andExpect(jsonPath("$.message",is(messUtils.getMessage("mess.publi-image-deleted"))));
		
		Optional<PublicatedImage> PublicatedImage = publicatedImagesDao.findById(1L);
		if(PublicatedImage.isPresent()) fail();
	}
	
	///
	
	@Test
	void getGetByUserWithoutArgStatusOk() throws Exception {
		String token = jwtService.generateToken(userAuthMati);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/publicatedImages/byUser")
				.header("authorization", "Bearer "+token))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.list",hasSize(1)));
	}
	
	@Test
	void getGetByUserWithArgStatusOk() throws Exception {
		String token = jwtService.generateToken(userAuthMati);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/publicatedImages/byUser")
				.header("authorization", "Bearer "+token)
				.param("page", "1")
				.param("pageSize", "2")
				.param("sortField", "createdAt")
				.param("sortDir", "asc"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.list",hasSize(1)));
	}
	
	@Test
	void getGetByUserWithArgSortFieldAndSortDirNoExistBadRequest() throws Exception {
		String token = jwtService.generateToken(userAuthMati);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/publicatedImages/byUser")
				.header("authorization", "Bearer "+token)
				.param("page", "1")
				.param("pageSize", "2")
				.param("sortField", "created") //this field no exist
				.param("sortDir", "ascssss")) //this not exist
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.sortField",is(messUtils.getMessage("vali.wrong-field-name"))))
				.andExpect(jsonPath("$.sortDir",is(messUtils.getMessage("vali.string-no-valid"))));
	}
	
	@Test
	void getGetByUserWithoutArgStatusNoContent() throws Exception {
		String token = jwtService.generateToken(userAuthRoci);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/publicatedImages/byUser")
				.header("authorization", "Bearer "+token))
				.andExpect(status().isNoContent())
				.andExpect(header().string("Info-header",messUtils.getMessage("mess.not-publi-image")));
	}
	
	@Test
	void getGetByVisibleUserhArgSortFieldAndSortDirNoExistStatusBadRequest() throws Exception {
		String token = jwtService.generateToken(userAuthMati);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/publicatedImages/byVisibleUser")
				.header("authorization", "Bearer "+token)
				.param("page", "1")
				.param("pageSize", "2")
				.param("sortField", "created") //this field no exist
				.param("sortDir", "ascssss")) //this not exist
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.sortField",is(messUtils.getMessage("vali.wrong-field-name"))))
				.andExpect(jsonPath("$.sortDir",is(messUtils.getMessage("vali.string-no-valid"))));
	}
	
	@Test
	void getGetByVisibleUserStatusIsOk() throws Exception {
		String token = jwtService.generateToken(userAuthMati);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/publicatedImages/byVisibleUser")
				.header("authorization", "Bearer "+token)
				.param("page", "1")
				.param("pageSize", "2")
				.param("sortField", "createdAt") 
				.param("sortDir", "asc"))
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.list", hasSize(1)));
	}
	
	@Test
	void getGetByVisibleUserStatusIsNoContent() throws Exception {
		publicatedImagesDao.deleteById(1L); //I delete the only publicated image in the db
		String token = jwtService.generateToken(userAuthMati);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/publicatedImages/byVisibleUser")
				.header("authorization", "Bearer "+token)
				.param("page", "1")
				.param("pageSize", "2")
				.param("sortField", "createdAt") 
				.param("sortDir", "asc"))
				.andExpect(status().isNoContent())
				.andExpect(header().string("Info-header",messUtils.getMessage("mess.not-publi-image")));
	}
	
	@AfterEach
	void bddDataDelete() {
		jdbc.execute(sqlRefIntegrityFalse);
		jdbc.execute(sqlTruncateUsers);
		jdbc.execute(sqlTruncatePublicatedImages);
		jdbc.execute(sqlRefIntegrityTrue);
	}
	
}
