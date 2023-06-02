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
	
	private Root<?> root;
	@SuppressWarnings("unused")
	private CriteriaBuilder cb;//// in this case I don't need the criteria, but I have to put it anyway,
								//// because I don't know when the client will use it
	private ReqSearch reqSearch;

	public OpInZonedDateTime(Root<?> root, CriteriaBuilder cb, ReqSearch reqSearch) {
		super();
		this.root = root;
		this.cb = cb;
		this.reqSearch = reqSearch;
	}

	@Override
	public Predicate getPredicate() {
		String[] splitIn = reqSearch.getValue().split(",");
		List<ZonedDateTime> listZonedDateTime = Arrays.stream(splitIn).map(ZonedDateTime::parse).collect(toList());
		
		if (reqSearch.getJoinTable() != null) {
			return root.join(reqSearch.getJoinTable()).get(reqSearch.getColumn()).in(listZonedDateTime);
		} else {
			return root.get(reqSearch.getColumn()).in(listZonedDateTime);
		}
	}

}
