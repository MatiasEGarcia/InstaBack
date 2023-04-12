package com.instaJava.instaJava.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.enums.GlobalOperationEnum;

public interface SpecificationService<T> {
	
	public Specification<T> getSpecification (List<ReqSearch> reqSearchList,
			GlobalOperationEnum globalOperator);
	
	public Specification<T> getSpecification (ReqSearch reqSearch);
}
