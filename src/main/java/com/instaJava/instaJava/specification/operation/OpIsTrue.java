package com.instaJava.instaJava.specification.operation;

import com.instaJava.instaJava.dto.request.ReqSearch;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class OpIsTrue implements OpI {

	@Override
	public Predicate getPredicate(Root<?> root, CriteriaBuilder cb,ReqSearch reqSearch) {
		if(reqSearch.getJoinTable() != null) {
			return cb.isTrue(root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()).as(Boolean.class));
		}else {
			return cb.isTrue(root.get(reqSearch.getColumn()).as(Boolean.class));
		}
	}

}
