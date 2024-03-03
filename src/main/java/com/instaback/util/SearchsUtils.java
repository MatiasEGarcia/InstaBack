package com.instaback.util;

import java.util.List;

import org.springframework.stereotype.Component;

import com.instaback.entity.IBaseEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SearchsUtils {
	
	private final MessagesUtils messUtils;
	
	public int bynarySearchById(List<? extends IBaseEntity> elements, Long elementIdToFind) {
		if(elements == null || elements.isEmpty() || elementIdToFind == null) 
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		
		double low = 0;
		double high = elements.size() - 1;
		
		while(low <= high) {
			int middlePosition = (int) Math.ceil((low + high)/2.0);
			IBaseEntity element = elements.get(middlePosition);
			if(element.getBaseEntityId() == elementIdToFind) {
				return middlePosition;
			}else if(element.getBaseEntityId() < elementIdToFind) {
				low = middlePosition + 1.0;
			}else {
				high = middlePosition - 1.0;
			}
		}
		return -1;
	}
	
	public int bynarySearchByIdReverse(List<? extends IBaseEntity> elements, Long elementIdToFind) { 
		if(elements == null || elements.isEmpty() || elementIdToFind == null) 
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		
		double bottom = 0;
		double top = elements.size() - 1;
		
		while(bottom <= top) {
			int middlePosition = (int) Math.ceil(bottom + top/2.0);
			IBaseEntity element = elements.get(middlePosition);
			if(element.getBaseEntityId() == elementIdToFind) {
				return middlePosition;
			}else if(element.getBaseEntityId() < elementIdToFind) {
				top = middlePosition - 1.0;
			}else {
				bottom = middlePosition + 1.0;
			}
		}
		return -1;
		
	}

	public int linealSearch(List<? extends IBaseEntity> elements , Long elementIdToFind) {
		for(int i = 0 ; i < elements.size(); i++) {
			if(elements.get(i).getBaseEntityId() == elementIdToFind) return i;
		}
		return -1;
	}
}













