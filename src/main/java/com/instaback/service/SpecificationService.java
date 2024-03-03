package com.instaback.service;

import static java.util.Map.entry;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.domain.Specification;

import com.instaback.dto.request.ReqSearch;
import com.instaback.enums.GlobalOperationEnum;
import com.instaback.specification.operation.OpBetween;
import com.instaback.specification.operation.OpEqual;
import com.instaback.specification.operation.OpGreaterThan;
import com.instaback.specification.operation.OpI;
import com.instaback.specification.operation.OpIn;
import com.instaback.specification.operation.OpInZonedDateTime;
import com.instaback.specification.operation.OpIsFalse;
import com.instaback.specification.operation.OpIsTrue;
import com.instaback.specification.operation.OpLessThan;
import com.instaback.specification.operation.OpLike;
import com.instaback.specification.operation.OpNotEqual;

public interface SpecificationService<T> {
	
	Map<String,OpI> OPERATIONS = Map.ofEntries(
			entry("equal", new OpEqual()),
			entry("notEqual", new OpNotEqual()),
			entry("like" , new OpLike()),
			entry("in", new OpIn()),
			entry("inDates", new OpInZonedDateTime()),
			entry("greaterThan", new OpGreaterThan()),
			entry("lessThan", new OpLessThan()),
			entry("between", new OpBetween()),
			entry("isTrue", new OpIsTrue()),
			entry("isFalse", new OpIsFalse())
			);
	
	/**
	 * 
	 * Method to get a specification and then search data that match the requirements
	 * 
	 * @param reqSearchList. Collection of ReqSearch.
	 * @param globalOperator. GLobal operator to unite all the individual queries .
	 * @return Specification with all the queries together.
	 * @throws IllegalArgumentException if @param reqSearchList or @param globalOperator are null.
	 * @throws IllegalArgumentException if globalOperator no exists.
	 * @throws IllegalArgumentException if some ReqSearch is null or ReqSearch.Operation is null.
	 */
	public Specification<T> getSpecification (List<ReqSearch> reqSearchList,
			GlobalOperationEnum globalOperator);
	
	/**
	 * 
	 * 
	 * @see {@link #getSpecification(List<ReqSearch>,GlobalOperationEnum) getSpecification} method
	 */
	public Specification<T> getSpecification (ReqSearch reqSearch);
}
