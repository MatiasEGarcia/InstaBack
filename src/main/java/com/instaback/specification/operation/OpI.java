package com.instaback.specification.operation;

import com.instaback.dto.request.ReqSearch;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public interface OpI {
	
	Predicate getPredicate(Root<?> root, CriteriaBuilder cb,ReqSearch reqSearch);
	
}
