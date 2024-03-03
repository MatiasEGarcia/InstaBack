package com.instaback.specification.operation;

import com.instaback.dto.request.ReqSearch;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class OpIsFalse implements OpI {

	/**
	 * Predicate to search records that column values is false.
	 * 
	 * @param root. Type object: User, Like , etc...
	 * @param cb. object to create predicates.
	 * @param reqSearch. has the condition to search.
	 * @return false predicate.
	 */
	@Override
	public Predicate getPredicate(Root<?> root, CriteriaBuilder cb,ReqSearch reqSearch) {
		if(reqSearch.getJoinTable() != null) {
			return cb.isFalse(root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()).as(Boolean.class));
		}else {
			return cb.isFalse(root.get(reqSearch.getColumn()).as(Boolean.class));
		}
	}

}
