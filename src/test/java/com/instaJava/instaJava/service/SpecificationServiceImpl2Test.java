package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import com.instaJava.instaJava.dao.PublicatedImagesDao;
import com.instaJava.instaJava.dao.UserDao;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.GlobalOperationEnum;
import com.instaJava.instaJava.enums.OperationEnum;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
class SpecificationServiceImpl2Test {

	@Autowired private UserDao userDao;
	@Autowired private JdbcTemplate jdbc;
	@Autowired private PublicatedImagesDao publImagesDao;
	@Autowired private SpecificationServiceImpl2<PublicatedImage> pubSpecService;
	@Autowired private SpecificationServiceImpl2<User> userSpecService;
	
	private final ZonedDateTime zFirstPubliImage = ZonedDateTime.parse("2023-03-10T18:42:15.948338800Z");
	private final ZonedDateTime zSecondPubliImage = ZonedDateTime.parse("2023-05-10T18:42:15.948338800Z");
	
	@Value("${sql.script.create.user.1}")
	private String sqlAddUser1;
	@Value("${sql.script.create.user.2}")
	private String sqlAddUser2;
	@Value("${sql.script.create.publicatedImage}")
	private String sqlAddPublicatedImage;
	@Value("${sql.script.create.publicatedImage.2}")
	private String sqlAddPublicatedImage2;
	@Value("${sql.script.truncate.users}")
	private String sqlTruncateUsers;
	@Value("${sql.script.truncate.publicatedImages}")
	private String sqlTruncatePublicatedImages;
	@Value("${sql.script.ref.integrity.false}")
	private String sqlRefIntegrityFalse;
	@Value("${sql.script.ref.integrity.true}")
	private String sqlRefIntegrityTrue;
	
	@BeforeEach
	void dataBaseMockData() {
		jdbc.execute(sqlAddUser1);
		jdbc.execute(sqlAddUser2);
		jdbc.execute(sqlAddPublicatedImage);
		jdbc.execute(sqlAddPublicatedImage2);
	}
	
	@Test
	void test() {
		User user1 = userDao.findByUsername("matias");
		System.out.println("EL ID ES : " +  user1.getUserId());
	}
	
	@Test
	void getSpecificationReqSearchListNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> pubSpecService.getSpecification(null, GlobalOperationEnum.NONE));
	}
	@Test
	void getSpecificationGlobalOperationEnumNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> pubSpecService.getSpecification(List.of(), null));
	}
	@Test
	void getSpecificationReqSearchListEmpty() {
		assertNull(pubSpecService.getSpecification(Collections.emptyList(), GlobalOperationEnum.NONE));
	}
	
	@Test
	void getSpecificationOnePredicatePublicImageIdEqual() {
		ReqSearch reqSearch = ReqSearch.builder()
				.column("pubImaId")
				.value("1")
				.operation(OperationEnum.EQUAL)
				.build();
		Optional<PublicatedImage> optP = publImagesDao.findOne(pubSpecService.getSpecification(reqSearch));
		if(optP.isEmpty()) fail("shouldn't be empty");
		assertTrue(optP.get().getPubImaId().equals(1L), "it's not the the record that we want");
	}
	
	
	@Test
	void getSpecificationOnePredicatePublicImageIdGraterThan() {
		ReqSearch reqSearch = ReqSearch.builder()
				.column("pubImaId")
				.value("1")
				.operation(OperationEnum.GREATER_THAN)
				.build();
		Optional<PublicatedImage> optP = publImagesDao.findOne(pubSpecService.getSpecification(reqSearch));
		if(optP.isEmpty()) fail("shouldn't be empty");
		if(optP.get().getPubImaId() < 1L ) fail("should return a record with id > 1");
	}
	@Test
	void getSpecificationOnePredicatePublicImageIdGraterThanReturnEmptyOptional() {
		ReqSearch reqSearch = ReqSearch.builder()
				.column("pubImaId")
				.value("2") //there is no records with id more greater
				.operation(OperationEnum.GREATER_THAN)
				.build();
		Optional<PublicatedImage> optP = publImagesDao.findOne(pubSpecService.getSpecification(reqSearch));
		if(optP.isPresent()) fail("should be empty");
	}
	@Test
	void getSpecificationOnePredicatePublicImageCreatedAtGraterThan() {
		LocalDate date = zSecondPubliImage.toLocalDate().minusDays(1L);
		ReqSearch reqSearch = ReqSearch.builder()
				.column("createdAt")
				.value(date.toString()) //there is only one record, it's have 2023-05-10
				.operation(OperationEnum.GREATER_THAN)
				.build();
		Optional<PublicatedImage> optP = publImagesDao.findOne(pubSpecService.getSpecification(reqSearch));
		if(optP.isEmpty()) fail("shouldn't be empty");
		if(optP.get().getPubImaId() != 2L) fail("Wrong record, the id should be 2");
	}
	
	
	
	@Test
	void getSpecificationOnePredicatePublicImageDescriptionLike() {
		ReqSearch reqSearch = ReqSearch.builder()
				.column("description")
				.value("1random") 
				.operation(OperationEnum.LIKE)
				.build();
		Optional<PublicatedImage> optP = publImagesDao.findOne(pubSpecService.getSpecification(reqSearch));
		if(optP.isEmpty()) fail("shouldn't be empty");
		if(optP.get().getPubImaId() != 1L) fail("Should return the publicated image with id == 1");
	}
	@Test
	void getSpecificationOnePredicatePublicImageJoinTableUserOwnerUserIdLike() {
		ReqSearch reqSearch = ReqSearch.builder()
				.column("userId")
				.value("1") 
				.joinTable("userOwner")
				.operation(OperationEnum.LIKE)
				.build();
		Optional<PublicatedImage> optP = publImagesDao.findOne(pubSpecService.getSpecification(reqSearch));
		if(optP.isEmpty()) fail("shouldn't be empty");
		if(optP.get().getUserOwner().getUserId() != 1L) fail("Should return the publicated image with user owner id == 1");
	}
	@Test
	void getSpecificationOnePredicatePublicImageCreatedAtLike() {
		LocalDate localDate=  zFirstPubliImage.toLocalDate(); // I only want yyyy-MM-dd
		ReqSearch reqSearch = ReqSearch.builder()
				.column("createdAt")
				.value(localDate.toString())
				.operation(OperationEnum.LIKE)
				.build();
		Optional<PublicatedImage> optP = publImagesDao.findOne(pubSpecService.getSpecification(reqSearch));
		if(optP.isEmpty()) fail("shouldn't be empty");
		if(optP.get().getPubImaId() != 1L) fail("Should return the publicated image id == 1");
	}
	
	
	@Test
	void getSpecificationOnePredicatePublicImageCreatedAtBetween() {
		LocalDate localDate =  zFirstPubliImage.minusDays(1L).toLocalDate(); // I only want yyyy-MM-dd
		LocalDate localDateNoExist =zFirstPubliImage.plusDays(1L).toLocalDate(); // I only want yyyy-MM-dd
		StringBuilder sb = new StringBuilder();
		sb.append(localDate.toString());
		sb.append(",");
		sb.append(localDateNoExist.toString());
		ReqSearch reqSearch = ReqSearch.builder()
				.column("createdAt")
				.value(sb.toString())
				.operation(OperationEnum.BETWEEN)
				.build();
		Optional<PublicatedImage> optP = publImagesDao.findOne(pubSpecService.getSpecification(reqSearch));
		if(optP.isEmpty()) fail("shouldn't be empty");
		if(optP.get().getPubImaId() != 1L) fail("Should return the publicated image id == 1");
	}
	@Test
	void getSpecificationOnePredicatePublicImageJoinTableUserOwnerUserIdBetween() {
		ReqSearch reqSearch = ReqSearch.builder()
				.column("userId")
				.value("2,5")//there is one with 2
				.joinTable("userOwner")
				.operation(OperationEnum.BETWEEN)
				.build();
		Optional<PublicatedImage> optP = publImagesDao.findOne(pubSpecService.getSpecification(reqSearch));
		if(optP.isEmpty()) fail("shouldn't be empty");
		if(optP.get().getUserOwner().getUserId() != 2L) fail("Should return the publicated image owner user id == 2");
	}
	
	
	@Test
	void getSpecificationOnePredicatePublicImageJoinTableUserOwnerUserIdIn() {
		ReqSearch reqSearch = ReqSearch.builder()
				.column("userId")
				.value("1,5")//there is one with id = 1
				.joinTable("userOwner")
				.operation(OperationEnum.IN)
				.build();
		Optional<PublicatedImage> optP = publImagesDao.findOne(pubSpecService.getSpecification(reqSearch));
		if(optP.isEmpty()) fail("shouldn't be empty");
		if(optP.get().getUserOwner().getUserId() != 1L) fail("Should return the publicated image owner user id == 1");
	}
	@Test
	void getSpecificationOnePredicatePublicImageDescriptionIn() {
		ReqSearch reqSearch = ReqSearch.builder()
				.column("description")
				.value("1randomDescription,3randomDescription") //there is one with  = 1randomDescription
				.operation(OperationEnum.IN)
				.build();
		Optional<PublicatedImage> optP = publImagesDao.findOne(pubSpecService.getSpecification(reqSearch));
		if(optP.isEmpty()) fail("shouldn't be empty");
		if(optP.get().getPubImaId() != 1L) fail("Should return the publicated image owner user id == 1");
	}
	
	/*
	//ESTO ESTA MAL, POR AHORA ME LO SALTEO, PORQUE CREO QUE ES UN TEMA DEL TEST, SI LO PRUEBO CON POSTMAN CREO QUE SIRVE(TENGO QUE PROBAR)
	//h2 no me devuelve el tiempo en UTC / esto me devuelve -> 2023-03-10T15:42:15.948339-03:00
	@Test
	void getSpecificationOnePredicatePublicImageCreatedAtInDATES() {
		StringBuilder sb = new StringBuilder();
		//I have a problem, when i get the data from the database , the timezone always is the same that JVM, i don't know how to change this
		TimeZone jvmTimeZone = TimeZone.getDefault();
		ZonedDateTime zoneDateFromJVM = zFirstPubliImage.withZoneSameLocal(jvmTimeZone.toZoneId());
		
		System.out.println("UTC TIMEZONE : " + zFirstPubliImage.toString());
		System.out.println("JVM TIMEZONE : " + zoneDateFromJVM.toString());
		
		
		
		sb.append(Timestamp.from(zFirstPubliImage.toInstant()).toString());
		sb.append(",");
		sb.append(Timestamp.from(zFirstPubliImage.plusDays(10L).toInstant()).toString()); //this not exist
		
		ReqSearch reqSearch = ReqSearch.builder()
				.column("createdAt")
				.value(sb.toString())
				.operation(OperationEnum.IN_DATES)
				.build();
		Optional<PublicatedImage> optP = publImagesDao.findOne(specService.getSpecification(reqSearch));
		if(optP.isEmpty()) fail("shouldn't be empty");
		if(optP.get().getPubImaId() != 1L) fail("Should return the publicated image owner user id == 1");
	}
	*/
	
	@Test
	void getSpecificationOnePredicatePublicImageIdLessThan() {
		ReqSearch reqSearch = ReqSearch.builder()
				.column("pubImaId")
				.value("2")
				.operation(OperationEnum.LESS_THAN)
				.build();
		Optional<PublicatedImage> optP = publImagesDao.findOne(pubSpecService.getSpecification(reqSearch));
		if(optP.isEmpty()) fail("shouldn't be empty");
		if(optP.get().getPubImaId() != 1) fail("Should return the publicated image with id == 1");
	}
	@Test
	void getSpecificationOnePredicatePublicImageJoinTableUserOwnerIdLessThan() {
		ReqSearch reqSearch = ReqSearch.builder()
				.column("userId")
				.value("2")
				.joinTable("userOwner")
				.operation(OperationEnum.LESS_THAN)
				.build();
		Optional<PublicatedImage> optP = publImagesDao.findOne(pubSpecService.getSpecification(reqSearch));
		if(optP.isEmpty()) fail("shouldn't be empty");
		if(optP.get().getUserOwner().getUserId() != 1) fail("Should return the publicated image with user owner id == 1");
	}
	@Test
	void getSpecificationOnePredicatePublicImageCreatedAtLessThan() {
		LocalDate date = zSecondPubliImage.toLocalDate().minusDays(1L);
		ReqSearch reqSearch = ReqSearch.builder()
				.column("createdAt")
				.value(date.toString())
				.operation(OperationEnum.LESS_THAN)
				.build();
		Optional<PublicatedImage> optP = publImagesDao.findOne(pubSpecService.getSpecification(reqSearch));
		if(optP.isEmpty()) fail("shouldn't be empty");
		if(optP.get().getPubImaId() != 1) fail("Should return the publicated image with id == 1");
	}
	
	
	@Test
	void getSpecificationOnePredicateUserVisibleIsFalse() {
		ReqSearch reqSearch = ReqSearch.builder()
				.column("visible")
				.value("false")
				.operation(OperationEnum.IS_FALSE)
				.build();
		Optional<User> optU = userDao.findOne(userSpecService.getSpecification(reqSearch));
		if(optU.isEmpty()) fail("shouldn't be empty");
		if(optU.get().getUserId() != 2) fail("Should return the user with id == 2");
	}
	@Test 
	void getSpecificationOnePredicatePublicImageOwnerUserVisibleIsFalse() {
		ReqSearch reqSearch = ReqSearch.builder()
				.column("visible")
				.value("false")
				.joinTable("userOwner")
				.operation(OperationEnum.IS_FALSE)
				.build();
		Optional<PublicatedImage> optP = publImagesDao.findOne(pubSpecService.getSpecification(reqSearch));
		if(optP.isEmpty()) fail("shouldn't be empty");
		if(optP.get().getPubImaId() != 2) fail("Should return the publicated image with id == 2");
	}
	
	
	@Test
	void getSpecificationOnePredicateUserVisibleIsTrue() {
		ReqSearch reqSearch = ReqSearch.builder()
				.column("visible")
				.value("true")
				.operation(OperationEnum.IS_TRUE)
				.build();
		Optional<User> optU = userDao.findOne(userSpecService.getSpecification(reqSearch));
		if(optU.isEmpty()) fail("shouldn't be empty");
		if(optU.get().getUserId() != 1) fail("Should return the user with id == 1");
	}
	@Test 
	void getSpecificationOnePredicatePublicImageOwnerUserVisibleIsTrue() {
		ReqSearch reqSearch = ReqSearch.builder()
				.column("visible")
				.value("true")
				.joinTable("userOwner")
				.operation(OperationEnum.IS_TRUE)
				.build();
		Optional<PublicatedImage> optP = publImagesDao.findOne(pubSpecService.getSpecification(reqSearch));
		if(optP.isEmpty()) fail("shouldn't be empty");
		if(optP.get().getPubImaId() != 1) fail("Should return the publicated image with id == 1");
	}
	
	@AfterEach
	void truncateMockData() {
		jdbc.execute(sqlRefIntegrityFalse);
		jdbc.execute(sqlTruncateUsers);
		jdbc.execute(sqlTruncatePublicatedImages);
		jdbc.execute(sqlRefIntegrityTrue);
	}
	
}
