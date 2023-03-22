package com.instaJava.instaJava.controller;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import com.instaJava.instaJava.entity.RolesEnum;
import com.instaJava.instaJava.entity.User;
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
	@Value("${sql.script.truncate.users}")
	private String sqlTruncateUsers;
	@Value("${sql.script.ref.integrity.false}")
	private String sqlRefIntegrityFalse;
	@Value("${sql.script.ref.integrity.true}")
	private String sqlRefIntegrityTrue;
	
	
	private static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON;
	//same user that sqlAddUser1
	private User userAuth = User.builder()
			.userId(1L)
			.username("matias")
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
	}
	
	@Test
	void postSaveStatusOk() throws Exception {
		String token = jwtService.generateToken(userAuth);
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
				.andExpect(jsonPath("$.id",is(1)))
				.andExpect(jsonPath("$.createdAt",instanceOf(String.class)))
				.andExpect(jsonPath("$.image", is(imgBase64)))
				.andExpect(jsonPath("$.description",is(description)))
				.andExpect(jsonPath("$.userOwner",is(userAuth.getUsername())));
		assertNotNull(publicatedImagesDao.findById(1L));
	}

	@Test
	void postSaveStatusBadRequestImageTypeInvalid() throws Exception {
		String token = jwtService.generateToken(userAuth);
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
		String token = jwtService.generateToken(userAuth);
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
		String token = jwtService.generateToken(userAuth);
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", 
				 MediaType.IMAGE_JPEG_VALUE, 
		        "Hello, World!".getBytes()
		      );
		String imgBase64 = Base64.getEncoder().encodeToString(img.getBytes());
		publicatedImagesDao.save(PublicatedImage.builder()
				.image(imgBase64)
				.createdAt(ZonedDateTime.now(Clock.systemUTC()))
				.description("random")
				.userOwner(userAuth)
				.build());
		
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/publicatedImages/{id}",1)
				.header("authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpectAll(jsonPath("$.message",is(messUtils.getMessage("mess.publi-image-deleted"))));
		
		Optional<PublicatedImage> PublicatedImage = publicatedImagesDao.findById(1L);
		if(PublicatedImage.isPresent()) fail();
	}
	
	
	
	@AfterEach
	void bddDataDelete() {
		jdbc.execute(sqlRefIntegrityFalse);
		jdbc.execute(sqlTruncateUsers);
		jdbc.execute(sqlRefIntegrityTrue);
	}
	
}
