package com.instaback.specification.operation;

import com.instaback.dto.request.ReqSearch;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class OpLessThan implements OpI {

	/**
	 * Predicate to search records that column values is less than @param reqSearch.value.
	 * 
	 * @param root. Type object: User, Like , etc...
	 * @param cb. object to create predicates.
	 * @param reqSearch. has the condition to search.
	 * @return Less than predicate.
	 */
	@Override
	public Predicate getPredicate(Root<?> root, CriteriaBuilder cb,ReqSearch reqSearch) {
		if(reqSearch.getJoinTable() != null) {
			return cb.lessThan(root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()).as(String.class),
					reqSearch.getValue());
		}else {
			return cb.lessThan(root.get(reqSearch.getColumn()).as(String.class),
					reqSearch.getValue());
		}
		
	}

}
