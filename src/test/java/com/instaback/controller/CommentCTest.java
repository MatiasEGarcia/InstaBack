package com.instaback.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instaback.dao.CommentDao;
import com.instaback.dto.request.ReqComment;
import com.instaback.dto.request.ReqUpdateComment;
import com.instaback.entity.Comment;
import com.instaback.entity.PublicatedImage;
import com.instaback.entity.User;
import com.instaback.enums.RolesEnum;
import com.instaback.service.JwtService;
import com.instaback.util.MessagesUtils;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
class CommentCTest {

	@Autowired
	private Clock clock;
	@Autowired
	private CommentDao commentDao;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private JwtService jwtService;
	@Autowired
	private MessagesUtils messUtils;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private JdbcTemplate jdbc;

	@Value("${sql.script.create.user.1}")
	private String sqlAddUser1;
	@Value("${sql.script.create.user.2}")
	private String sqlAddUser2;
	@Value("${sql.script.create.publicatedImage}")
	private String sqlAddPublicatedImage;
	@Value("${sql.script.create.comment.1}")
	private String sqlAddComment;
	@Value("${sql.script.create.comment.2}")
	private String sqlAddComment2;
	@Value("${sql.script.truncate.users}")
	private String sqlTruncateUsers;
	@Value("${sql.script.truncate.publicatedImages}")
	private String sqlTruncatePublicatedImages;
	@Value("${sql.script.truncate.notifications}")
	private String sqlTruncateNotifications;
	@Value("${sql.script.truncate.comments}")
	private String sqlTruncateComments;
	@Value("${sql.script.ref.integrity.false}")
	private String sqlRefIntegrityFalse;
	@Value("${sql.script.ref.integrity.true}")
	private String sqlRefIntegrityTrue;

	// this user is in the bdd , because we save it with sqlAddUser1
	private User matiasUserAuth = User.builder().id(1L).username("matias").password("123456")
			.role(RolesEnum.ROLE_USER).build();

	private User rociUserAuth = User.builder().id(2L).username("rocio").password("123456").role(RolesEnum.ROLE_USER)
			.build();

	@BeforeEach
	void dbbSetUp() {
		jdbc.update(sqlAddUser1);
		jdbc.update(sqlAddUser2);
		jdbc.update(sqlAddPublicatedImage); // owner matiasUserAuth
	}

	// saved
	@Test
	void postSaveRequestBodyImcompleteBadRequest() throws Exception {
		String token = jwtService.generateToken(rociUserAuth);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/comments").header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error", is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("client.value-missing-incorrect"))));
	}

	@Test
	void postSaveReqCommentIncompleteBadRequest() throws Exception {
		String token = jwtService.generateToken(rociUserAuth);
		ReqComment reqComment = new ReqComment();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/comments").header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(reqComment)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error", is(HttpStatus.BAD_REQUEST.toString())))
				.andExpect(jsonPath("$.message", is(messUtils.getMessage("client.body-not-fulfilled"))));
	}

	@Test
	void postSaveOk() throws Exception {
		String token = jwtService.generateToken(rociUserAuth);
		ReqComment reqComment = ReqComment.builder().body("new comment").publImgId("1").build();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/comments").header("Authorization", "Bearer " + token)
				.content(objectMapper.writeValueAsString(reqComment)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpectAll(jsonPath("$.body", is(reqComment.getBody())))
				.andExpectAll(jsonPath("$.ownerUser.id", is(rociUserAuth.getId().toString())))
				.andExpectAll(jsonPath("$.associatedImg.id", is("1")));
	}

	// getManyByPublicationId
	@Test
	void getGetManyByPublicationIdNoContent() throws Exception {
		String token = jwtService.generateToken(rociUserAuth);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/comments/manyByPublicationId")
				.header("Authorization", "Bearer " + token).param("id", "1"))
				.andExpect(status().isNoContent());
	}

	@Test
	void getGetManyByPublicationIdPublicationIdNoGivenBadRequest() throws Exception {
		String token = jwtService.generateToken(rociUserAuth);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/comments/manyByPublicationId").header("Authorization",
				"Bearer " + token)).andExpect(status().isBadRequest());
	}

	@Test
	void getGetManyByPublicationIdOk() throws Exception {
		String token = jwtService.generateToken(rociUserAuth);
		jdbc.update(sqlAddComment); // adding a comment on publication.

		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/comments/manyByPublicationId")
				.header("Authorization", "Bearer " + token).param("id", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.list", hasSize(1)))
				.andExpect(jsonPath("$.pageInfoDto.pageNo", is(0))) 																					
				.andExpect(jsonPath("$.pageInfoDto.pageSize", is(20))) 
				.andExpect(jsonPath("$.pageInfoDto.totalPages", is(1)))
				.andExpect(jsonPath("$.pageInfoDto.totalElements", is(1)))
				.andExpect(jsonPath("$.pageInfoDto.sortField", is("id")))
				.andExpect(jsonPath("$.pageInfoDto.sortDir", is(Direction.ASC.toString())));
	}
	
	//getManyByParentId
	@Test 
	void getGetManyByParentIdNoContent() throws Exception {
		String token = jwtService.generateToken(rociUserAuth);
		jdbc.update(sqlAddComment); // adding a comment on publication.
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/comments/manyByParentId")
				.header("Authorization", "Bearer " + token).param("id", "1"))
				.andExpect(status().isNoContent());
	}
	
	@Test
	void getGetManyByParentIdParentNoExistsNotFound() throws Exception {
		String token = jwtService.generateToken(rociUserAuth);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/comments/manyByParentId")
				.header("Authorization","Bearer " + token)
				.param("id", "1"))
				.andExpect(status().isNotFound());
	}
	@Test
	void getGetManyByParentIdNotGivenBadRequest() throws Exception {
		String token = jwtService.generateToken(rociUserAuth);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/comments/manyByParentId")
				.header("Authorization","Bearer " + token))
				.andExpect(status().isBadRequest());
	}
	
	@Test 
	void getGetManyByParentIdOk() throws Exception {
		String token = jwtService.generateToken(rociUserAuth);
		jdbc.update(sqlAddComment); // adding a comment on publication.
		jdbc.update(sqlAddComment2); // adding a comment having parent the first comment.
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/comments/manyByParentId")
				.header("Authorization", "Bearer " + token)
				.param("id", "1"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.list", hasSize(1)))
				.andExpect(jsonPath("$.pageInfoDto.pageNo", is(0))) 																					
				.andExpect(jsonPath("$.pageInfoDto.pageSize", is(20))) 
				.andExpect(jsonPath("$.pageInfoDto.totalPages", is(1)))
				.andExpect(jsonPath("$.pageInfoDto.totalElements", is(1)))
				.andExpect(jsonPath("$.pageInfoDto.sortField", is("id")))
				.andExpect(jsonPath("$.pageInfoDto.sortDir", is(Direction.ASC.toString())));;
	}
	

	// DeleteById
	@Test
	void deleteDeleteByIdOk() throws Exception {
		ZonedDateTime now = ZonedDateTime.now(clock);
		String token = jwtService.generateToken(matiasUserAuth);
		Comment newComment = Comment.builder().associatedImg(new PublicatedImage(1L)).ownerUser(matiasUserAuth)
				.body("random commentary").createdAt(now.minusMinutes(2L)) // I need to have this precision, with a static query I cannot have it.
				.build();
		commentDao.save(newComment); // comment to delete

		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/comments/byId/{commentId}", 1).header("Authorization",
				"Bearer " + token)).andExpect(status().isOk());
	}

	@Test
	void deleteDeleteByIdBadRequest() throws Exception {
		ZonedDateTime now = ZonedDateTime.now(clock);
		String token = jwtService.generateToken(matiasUserAuth);
		Comment newComment = Comment.builder().associatedImg(new PublicatedImage(1L)).ownerUser(matiasUserAuth)
				.body("random commentary").createdAt(now.minusMinutes(6L)) // I need to have this precision, with a static query I cannot have it.
				.build();
		commentDao.save(newComment); // comment to delete

		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/comments/byId/{commentId}", 1).header("Authorization",
				"Bearer " + token)).andExpect(status().isBadRequest());
	}

	//updateById
	@Test
	void putUpdateByIdOk() throws Exception{
		ZonedDateTime now = ZonedDateTime.now(clock);
		String token = jwtService.generateToken(matiasUserAuth);
		Comment newComment = Comment.builder().associatedImg(new PublicatedImage(1L)).ownerUser(matiasUserAuth)
				.body("comment to save").createdAt(now.minusMinutes(2L)) // I need to have this precision, with a static query I cannot have it.
				.build();
		commentDao.save(newComment); // comment to update
		ReqUpdateComment reqUpdateComment = new ReqUpdateComment(1L,"comemnt to udpate");
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/comments/byId")
				.header("Authorization","Bearer " + token)
				.content(objectMapper.writeValueAsString(reqUpdateComment))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id" , is("1")))
				.andExpect(jsonPath("$.body" , is(reqUpdateComment.getBody())));
	}
	
	@Test
	void putUpdateById5AfterBadRequest() throws Exception{
		ZonedDateTime now = ZonedDateTime.now(clock);
		String token = jwtService.generateToken(matiasUserAuth);
		Comment newComment = Comment.builder().associatedImg(new PublicatedImage(1L)).ownerUser(matiasUserAuth)
				.body("comment to save").createdAt(now.minusMinutes(6L)) // I need to have this precision, with a static query I cannot have it.
				.build();
		commentDao.save(newComment); // comment to update
		ReqUpdateComment reqUpdateComment = new ReqUpdateComment(1L,"comemnt to udpate");
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/comments/byId")
				.header("Authorization","Bearer " + token)
				.content(objectMapper.writeValueAsString(reqUpdateComment))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	void putUpdateByIdNotOwnerBadRequest() throws Exception{
		ZonedDateTime now = ZonedDateTime.now(clock);
		String token = jwtService.generateToken(rociUserAuth);
		Comment newComment = Comment.builder().associatedImg(new PublicatedImage(1L)).ownerUser(matiasUserAuth)
				.body("comment to save").createdAt(now.minusMinutes(1L)) // I need to have this precision, with a static query I cannot have it.
				.build();
		commentDao.save(newComment); // comment to update
		ReqUpdateComment reqUpdateComment = new ReqUpdateComment(1L,"comemnt to udpate");
		
		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/comments/byId")
				.header("Authorization","Bearer " + token)
				.content(objectMapper.writeValueAsString(reqUpdateComment))
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest());
	}
	@AfterEach
	void setUpAfterTransaction() {
		jdbc.execute(sqlRefIntegrityFalse);
		jdbc.execute(sqlTruncateComments);
		jdbc.execute(sqlTruncatePublicatedImages);
		jdbc.execute(sqlTruncateNotifications);
		jdbc.execute(sqlTruncateUsers);
		jdbc.execute(sqlRefIntegrityTrue);
	}

}
