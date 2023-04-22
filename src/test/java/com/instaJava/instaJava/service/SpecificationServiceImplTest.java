package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.enums.GlobalOperationEnum;
import com.instaJava.instaJava.enums.OperationEnum;
import com.instaJava.instaJava.util.MessagesUtils;

@ExtendWith(MockitoExtension.class)
class SpecificationServiceImplTest {

	@Mock private MessagesUtils messUtils;
	@InjectMocks private SpecificationServiceImpl<Follow> specificationService;
	
	@Test
	void getSpecificationSearchRequestDtoNullThrow() {
		List<ReqSearch> searchRequestDtoList = null;
		assertThrows(IllegalArgumentException.class,() -> specificationService.getSpecification(searchRequestDtoList, GlobalOperationEnum.AND));
	}

	@Test
	void getSpecificationSearchRequestDtoEmptyThrow() {
		List<ReqSearch> searchRequestDtoList = Collections.emptyList();
		assertThrows(IllegalArgumentException.class, () -> specificationService.getSpecification(searchRequestDtoList, GlobalOperationEnum.AND));
	}
	
	@Test
	void getSpecificationGlobalOperationEnumNullThrow() {
		ReqSearch searchRequestDto = new ReqSearch();
		List<ReqSearch> searchRequestDtoList = List.of(searchRequestDto);
		assertThrows(IllegalArgumentException.class,() -> specificationService.getSpecification(searchRequestDtoList, null));
	}
	
	@Test
	void getSpecification() {
		ReqSearch reqSearch = ReqSearch.builder()
				.column("followerId")
				.value("1")
				.dateValue(false)
				.operation(OperationEnum.EQUAL)
				.build();
		List<ReqSearch> reqSearchList = List.of(reqSearch);
		assertNotNull(specificationService.getSpecification(reqSearchList, GlobalOperationEnum.AND));
		
	}
	
	@Test
	void getSpecificationOnlyOneReqSearchNullArgThrow() {
		assertThrows(IllegalArgumentException.class, () -> specificationService.getSpecification(null));
	}
	
	@Test
	void getSpecificationOnlyOneReqSearchReturnNotNull() {
		ReqSearch reqSearch = ReqSearch.builder()
				.column("followerId")
				.value("1")
				.dateValue(false)
				.operation(OperationEnum.EQUAL)
				.build();
		assertNotNull(specificationService.getSpecification(reqSearch));
	}
}