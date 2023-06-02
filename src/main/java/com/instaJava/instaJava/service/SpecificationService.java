package com.instaJava.instaJava.service;

import static java.util.Map.entry;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.domain.Specification;

import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.enums.GlobalOperationEnum;
import com.instaJava.instaJava.specification.operation.OpBetween;
import com.instaJava.instaJava.specification.operation.OpEqual;
import com.instaJava.instaJava.specification.operation.OpGreaterThan;
import com.instaJava.instaJava.specification.operation.OpI;
import com.instaJava.instaJava.specification.operation.OpIn;
import com.instaJava.instaJava.specification.operation.OpInZonedDateTime;
import com.instaJava.instaJava.specification.operation.OpIsFalse;
import com.instaJava.instaJava.specification.operation.OpIsTrue;
import com.instaJava.instaJava.specification.operation.OpLessThan;
import com.instaJava.instaJava.specification.operation.OpLike;

public interface SpecificationService<T> {
	
	Map<String,OpI> OPERATIONS = Map.ofEntries(
			entry("equal", new OpEqual()),
			entry("like" , new OpLike()),
			entry("in", new OpIn()),
			entry("inDates", new OpInZonedDateTime()),
			entry("greaterThan", new OpGreaterThan()),
			entry("lessThan", new OpLessThan()),
			entry("between", new OpBetween()),
			entry("isTrue", new OpIsTrue()),
			entry("isFalse", new OpIsFalse())
			);
	
	public Specification<T> getSpecification (List<ReqSearch> reqSearchList,
			GlobalOperationEnum globalOperator);
	
	public Specification<T> getSpecification (ReqSearch reqSearch);
}
