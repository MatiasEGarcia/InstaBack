package com.instaJava.instaJava.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.instaJava.instaJava.dto.SearchRequestDto;
import com.instaJava.instaJava.enums.GlobalOperationEnum;

public interface SpecificationService<T> {
	
	public Specification<T> getSpecification (List<SearchRequestDto> searchRequestDto,
			GlobalOperationEnum globalOperator);
}
