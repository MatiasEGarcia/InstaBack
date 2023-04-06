package com.instaJava.instaJava.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.instaJava.instaJava.dto.SearchRequestDto;
import com.instaJava.instaJava.enums.GlobalOperationEnum;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Service
public class SpecificationServiceImpl<T> implements SpecificationService<T> {

	@Override
	public List<Predicate> getPredicates(List<SearchRequestDto> searchRequestDto,
			Root<T> root, CriteriaBuilder criteriaBuilder) {
		List<Predicate> predicates = new ArrayList<>();

		for (SearchRequestDto requestDto : searchRequestDto) {

			switch (requestDto.getOperation()) {
			case EQUAL:
				if(requestDto.isDateValue()) {
					predicates.add(criteriaBuilder.equal(root.get(requestDto.getColumn()), ZonedDateTime.parse(requestDto.getValue())));
				}
				predicates.add(criteriaBuilder.equal(root.get(requestDto.getColumn()), requestDto.getValue()));
				break;
			case LIKE:
				predicates.add(criteriaBuilder.like(root.get(requestDto.getColumn()), "%" + requestDto.getValue() + "%")); 
				break;
			case IN:
				String[] splitIn = requestDto.getValue().split(",");
				if(requestDto.isDateValue()) {
					List<ZonedDateTime> values = new ArrayList<>();
					for(String value : splitIn) {
						values.add(ZonedDateTime.parse(value));
					}
					predicates.add(root.get(requestDto.getColumn()).in(values));
				}
				predicates.add(root.get(requestDto.getColumn()).in(Arrays.asList(splitIn)));
				break;
			case GREATER_THAN:
				if(requestDto.isDateValue()) {
					predicates.add(criteriaBuilder.greaterThan(root.get(requestDto.getColumn()), ZonedDateTime.parse(requestDto.getValue())));
				}
				predicates.add(criteriaBuilder.greaterThan(root.get(requestDto.getColumn()),
						Long.parseLong(requestDto.getValue())));
				break;
			case LESS_THAN:
				if(requestDto.isDateValue()) {
					predicates.add(criteriaBuilder.lessThan(root.get(requestDto.getColumn()), ZonedDateTime.parse(requestDto.getValue())));
				}
				predicates.add(criteriaBuilder.lessThan(root.get(requestDto.getColumn()),
						Long.parseLong(requestDto.getValue())));
				break;
			case BETWEEN:
				String[] splitBetween = requestDto.getValue().split(",");
				if(requestDto.isDateValue()) {
					predicates.add(criteriaBuilder.between(root.get(requestDto.getColumn()), ZonedDateTime.parse(splitBetween[0]),ZonedDateTime.parse(splitBetween[1])));
				}
				
				predicates.add(criteriaBuilder.between(root.get(requestDto.getColumn()), Long.parseLong(splitBetween[0]),
						Long.parseLong(splitBetween[1])));
				break;
			default:
				throw new IllegalStateException("there was some error");
			}
		}

		return predicates;
	}

	@Override
	public List<Predicate> getPredicatesJoin(List<SearchRequestDto> searchRequestDto,
			Root<T> root, CriteriaBuilder criteriaBuilder) {
		List<Predicate> predicates = new ArrayList<>();
		
		for (SearchRequestDto requestDto : searchRequestDto) {
			
			switch (requestDto.getOperation()) {
			case EQUAL:
				if(requestDto.isDateValue()) {
					predicates.add(criteriaBuilder.equal(root.join(requestDto.getJoinTable()).get(requestDto.getColumn()), ZonedDateTime.parse(requestDto.getValue())));
				}
				predicates.add(criteriaBuilder.equal(root.join(requestDto.getJoinTable()).get(requestDto.getColumn()), requestDto.getValue()));
				break;
			case LIKE:
				predicates.add(
						criteriaBuilder.like(root.join(requestDto.getJoinTable()).get(requestDto.getColumn()), "%" + requestDto.getValue() + "%"));
				break;
			case IN:
				String[] splitIn = requestDto.getValue().split(",");
				if(requestDto.isDateValue()) {
					List<ZonedDateTime> values = new ArrayList<>();
					for(String value : splitIn) {
						values.add(ZonedDateTime.parse(value));
					}
					predicates.add(root.join(requestDto.getJoinTable()).get(requestDto.getColumn()).in(values));
				}
				predicates.add(root.join(requestDto.getJoinTable()).get(requestDto.getColumn()).in(Arrays.asList(splitIn)));
				break;
			case GREATER_THAN:
				if(requestDto.isDateValue()) {
					predicates.add(criteriaBuilder.greaterThan(root.join(requestDto.getJoinTable()).get(requestDto.getColumn()), ZonedDateTime.parse(requestDto.getValue())));
				}
				predicates.add(criteriaBuilder.greaterThan(root.join(requestDto.getJoinTable()).get(requestDto.getColumn()),
						Long.parseLong(requestDto.getValue())));
				break;
			case LESS_THAN:
				if(requestDto.isDateValue()) {
					predicates.add(criteriaBuilder.lessThan(root.join(requestDto.getJoinTable()).get(requestDto.getColumn()), ZonedDateTime.parse(requestDto.getValue())));
				}
				predicates.add(criteriaBuilder.lessThan(root.join(requestDto.getJoinTable()).get(requestDto.getColumn()),
						Long.parseLong(requestDto.getValue())));
				break;
			case BETWEEN:
				String[] splitBetween = requestDto.getValue().split(",");
				if(requestDto.isDateValue()) {
					predicates.add(criteriaBuilder.between(root.join(requestDto.getJoinTable()).get(requestDto.getColumn()), ZonedDateTime.parse(splitBetween[0]),
							ZonedDateTime.parse(splitBetween[1])));
				}
				predicates.add(criteriaBuilder.between(root.join(requestDto.getJoinTable()).get(requestDto.getColumn()), Long.parseLong(splitBetween[0]),
						Long.parseLong(splitBetween[1])));
				break;
			default:
				throw new IllegalStateException("there was some error");
			}
		}
		
		return predicates;
	}

	@Override
	public Specification<T> getSpecification(List<SearchRequestDto> searchRequestDto,
			GlobalOperationEnum globalOperator) {
		return (root, query, criteriaBuilder) -> {
			List<jakarta.persistence.criteria.Predicate> allPredicates = new ArrayList<>();
			List<jakarta.persistence.criteria.Predicate> predicates;
			List<jakarta.persistence.criteria.Predicate> predicatesJoin;
			java.util.function.Predicate<SearchRequestDto> partition = dto-> dto.getJoinTable() == null || dto.getJoinTable().isBlank();
			var map = searchRequestDto.stream().collect(Collectors.partitioningBy(partition));
			predicates = this.getPredicates(map.get(true), root, criteriaBuilder);
			predicatesJoin = this.getPredicatesJoin(map.get(false), root, criteriaBuilder);
			allPredicates.addAll(predicates);
			allPredicates.addAll(predicatesJoin);
			if (globalOperator.equals(GlobalOperationEnum.AND)) {
				return criteriaBuilder.and(allPredicates.toArray(new Predicate[0]));
			} else {
				return criteriaBuilder.or(allPredicates.toArray(new Predicate[0]));
			}
			
		};
	}

}
