package com.instaJava.instaJava.specification.operation;

import static java.util.stream.Collectors.toList;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import com.instaJava.instaJava.dto.request.ReqSearch;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class OpInZonedDateTime implements OpI {

	@Override
	public Predicate getPredicate(Root<?> root, CriteriaBuilder cb,ReqSearch reqSearch) {
		String[] splitIn = reqSearch.getValue().split(",");
		List<ZonedDateTime> listZonedDateTime = Arrays.stream(splitIn).map(ZonedDateTime::parse).collect(toList());
		
		if (reqSearch.getJoinTable() != null) {
			return root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()).in(listZonedDateTime);
		} else {
			return root.get(reqSearch.getColumn()).in(listZonedDateTime);
		}
	}

}
