package com.instaJava.instaJava.specification.operation;

import java.util.Arrays;

import com.instaJava.instaJava.dto.request.ReqSearch;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class OpIn implements OpI{

	/**
	 * Predicate to search records that column values(casted to string) is in @param reqSearch.value.
	 * 
	 * @param root. Type object: User, Like , etc...
	 * @param cb. object to create predicates.
	 * @param reqSearch. has the condition to search.
	 * @return in predicate
	 */
	@Override
	public Predicate getPredicate(Root<?> root, CriteriaBuilder cb,ReqSearch reqSearch) {
		String[] splitIn = reqSearch.getValue().split(",");
		
		if(reqSearch.getJoinTable() != null) {
			return root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()).as(String.class).in(Arrays.asList(splitIn));
		}else {
			return root.get(reqSearch.getColumn()).as(String.class).in(Arrays.asList(splitIn));
		}
	}

}
