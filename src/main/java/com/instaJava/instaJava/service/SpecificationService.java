package com.instaJava.instaJava.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.instaJava.instaJava.dto.SearchRequestDto;
import com.instaJava.instaJava.enums.GlobalOperationEnum;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public interface SpecificationService<T> {

	public List<Predicate> getPredicates(List<SearchRequestDto> searchRequestDto,
			Root<T> root, CriteriaBuilder criteriaBuilder);
	
	public List<Predicate> getPredicatesJoin(List<SearchRequestDto> searchRequestDto,
			Root<T> root, CriteriaBuilder criteriaBuilder);
	
	public Specification<T> getSpecification (List<SearchRequestDto> searchRequestDto,
			GlobalOperationEnum globalOperator);
}
