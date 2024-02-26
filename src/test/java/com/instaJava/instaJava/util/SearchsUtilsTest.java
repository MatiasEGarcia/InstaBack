package com.instaJava.instaJava.util;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.instaJava.instaJava.entity.IBaseEntity;
import com.instaJava.instaJava.entity.Message;

@ExtendWith(MockitoExtension.class)
class SearchsUtilsTest {

	@Mock
	private MessagesUtils messUtils;
	@InjectMocks
	private SearchsUtils searchsUtils;
	
	//bynarySearchById
	@Test
	void bynarySearchByIdElementsParamNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> searchsUtils.bynarySearchById(null, 1L));
	}
	
	@Test
	void bynarySearchByIdElementsParamEmptyThrow() {
		List<IBaseEntity> elements = Collections.emptyList();
		assertThrows(IllegalArgumentException.class, () -> searchsUtils.bynarySearchById(elements, 1L));
	}
	
	@Test
	void bynarySearchByIdElementIdToFindParamNullThrow() {
		Message message = new Message();
		List<IBaseEntity> elements = List.of(message);
		assertThrows(IllegalArgumentException.class, () -> searchsUtils.bynarySearchById(elements, null));
	}
	
	@Test
	void bynarySearchByIdReturnCorrectIndex() {
		Message message1 = new Message(1L);
		Message message2 = new Message(2L);
		Message message3 = new Message(3L);
		Long idSearched = 3L;
		List<Message> listMessages = List.of(message1,message2,message3);
		int index = searchsUtils.bynarySearchById(listMessages, idSearched);
		assertEquals(2, index);
	}
	
	@Test
	void bynarySearchByIdReturnOneNegativeIndexNotFoundReverseOrder() {
		Message message1 = new Message(1L);
		Message message2 = new Message(2L);
		Message message3 = new Message(3L);
		Long idSearched = 3L;
		List<Message> listMessages = List.of(message3,message2,message1);		//reverse, from bigger to lower
		int index = searchsUtils.bynarySearchById(listMessages, idSearched);
		assertEquals(-1, index);
	}
	
	@Test
	void bynarySearchByIdReturnOneNegativeIndexNotFoundNoExists() {
		Message message1 = new Message(1L);
		Message message2 = new Message(2L);
		Message message3 = new Message(3L);
		Long idSearched = 100L; // there are no messages with that id.
		List<Message> listMessages = List.of(message1,message2,message3);		
		int index = searchsUtils.bynarySearchById(listMessages, idSearched);
		assertEquals(-1, index);
	}

	//bynarySearchByIdReverse
	@Test
	void bynarySearchByIdReverseElementsParamNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> searchsUtils.bynarySearchByIdReverse(null, 1L));
	}
	
	@Test
	void bynarySearchByIdReverseElementsParamEmptyThrow() {
		List<IBaseEntity> elements = Collections.emptyList();
		assertThrows(IllegalArgumentException.class, () -> searchsUtils.bynarySearchByIdReverse(elements, 1L));
	}
	
	@Test
	void bynarySearchByIdReverseElementIdToFindParamNullThrow() {
		Message message = new Message();
		List<IBaseEntity> elements = List.of(message);
		assertThrows(IllegalArgumentException.class, () -> searchsUtils.bynarySearchByIdReverse(elements, null));
	}
	
	@Test
	void bynarySearchByIdReverseReturnCorrectIndex() {
		Message message1 = new Message(1L);
		Message message2 = new Message(2L);
		Message message3 = new Message(3L);
		Long idSearched = 3L;
		List<Message> listMessages = List.of(message3,message2,message1);
		int index = searchsUtils.bynarySearchByIdReverse(listMessages, idSearched);
		assertEquals(0, index);
	}
	
	@Test
	void bynarySearchByIdReverseReturnOneNegativeIndexNotFoundReverseOrder() {
		Message message1 = new Message(1L);
		Message message2 = new Message(2L);
		Message message3 = new Message(3L);
		Long idSearched = 3L;
		List<Message> listMessages = List.of(message1,message2,message3);		//Reverse, from lower to bigger
		int index = searchsUtils.bynarySearchByIdReverse(listMessages, idSearched);
		assertEquals(-1, index);
	}
	
	@Test
	void bynarySearchByIdReverseReturnOneNegativeIndexNotFoundNoExists() {
		Message message1 = new Message(1L);
		Message message2 = new Message(2L);
		Message message3 = new Message(3L);
		Long idSearched = 100L; // there are no messages with that id.
		List<Message> listMessages = List.of(message3,message2,message1);		
		int index = searchsUtils.bynarySearchByIdReverse(listMessages, idSearched);
		assertEquals(-1, index);
	}
	
}
