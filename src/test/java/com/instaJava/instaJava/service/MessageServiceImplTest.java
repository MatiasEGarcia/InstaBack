package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.instaJava.instaJava.dao.MessageDao;
import com.instaJava.instaJava.dto.ChatDto;
import com.instaJava.instaJava.dto.MessageDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.UserDto;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Message;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.mapper.MessageMapper;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

	@Mock
	private Authentication auth;
	@Mock
	SecurityContext securityContext;

	@Mock
	private Clock clock;
	@Mock
	private MessageDao msgDao;
	@Mock
	private MessageMapper msgMapper;
	@Mock
	private ChatService chatService;
	@Mock
	private MessagesUtils messUtils;
	@Mock
	private PageableUtils pagUtils;
	@Mock
	private NotificationService notifSerivce;
	@InjectMocks
	private MessageServiceImpl messageService;
	private final User user = User.builder().userId(1L).username("Mati").password("random").role(RolesEnum.ROLE_USER)
			.build();

	// create
	@Test
	void createParamChatIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> messageService.create("random", null));
	}

	@Test
	void createParamMessageNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> messageService.create(null, 1L));
	}

	@Test
	void createParamMessageBlankThrow() {
		assertThrows(InvalidActionException.class, () -> messageService.create("", 1L));
	}
	@Test
	void createAuthUserIsNotFromChatThrow() {
		Long chatId = 1L;
		UserDto userDto = UserDto.builder().username("random").build();// differente user than auth user.
		ChatDto chatDto = ChatDto.builder().users(List.of(userDto)).build();

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// chatService
		when(chatService.getById(chatId)).thenReturn(chatDto);
		
		assertThrows(InvalidActionException.class, () -> messageService.create("randomMessage", chatId));
		
		verify(chatService).getById(chatId);
	}
	@Test
	void createAuthUserIsInChatReturnsNotNull() {
		Long id = 1L;
		String messageToSend = "randomMessage";
		UserDto userDto = UserDto.builder().username("Mati").build();// same user than auth user.
		ChatDto chatDto = ChatDto.builder().users(List.of(userDto)).build();
		Message message = new Message();
		MessageDto messageDto = new MessageDto();
		//auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		//chatService
		when(chatService.getById(id)).thenReturn(chatDto);
		//clock
		when(clock.getZone()).thenReturn(ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(Instant.parse("2020-12-01T10:05:23.653Z"));
		//dao
		when(msgDao.save(any(Message.class))).thenReturn(message);
		//mapper
		when(msgMapper.messageToMessageDto(message)).thenReturn(messageDto);
		
		assertNotNull(messageService.create(messageToSend, id));
		
		verify(chatService).getById(id);
		verify(msgDao).save(any(Message.class));
		verify(notifSerivce).saveNotificationOfMessage(chatDto,messageDto);
	}
	

	// getMessagesByChat
	@Test
	void getMessagesByChatParamChatIdNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("randomSorfield").sortDir(Direction.ASC).build();
		assertThrows(IllegalArgumentException.class, () -> messageService.getMessagesByChat(null, pageInfoDto));
	}

	@Test
	void getMessagesByChatParamPageInfoDtoNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> messageService.getMessagesByChat(1L, null));
	}

	@Test
	void getMessagesByChatParamPageInfoDtoSortFieldNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortDir(Direction.ASC).build();
		assertThrows(IllegalArgumentException.class, () -> messageService.getMessagesByChat(1L, pageInfoDto));
	}

	@Test
	void getMessagesByChatParamPageInfoDtoSortDirNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("randomSorfield").build();
		assertThrows(IllegalArgumentException.class, () -> messageService.getMessagesByChat(1L, pageInfoDto));
	}

	@Test
	void getMessagesByChatUserIsNotInChatThrow() {
		Long chatId = 1L;
		UserDto userDto = UserDto.builder().username("random").build();
		ChatDto chatDto = ChatDto.builder().users(List.of(userDto)).build();
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("randomSorfield").sortDir(Direction.ASC).build();

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// chatService
		when(chatService.getById(chatId)).thenReturn(chatDto);

		assertThrows(InvalidActionException.class, () -> messageService.getMessagesByChat(chatId, pageInfoDto));

		verify(chatService).getById(chatId);
		verify(msgDao, never()).findByChatChatId(eq(chatId), any(Pageable.class));
	}

	@Test
	void getMessagesByChatNoContentThrow() {
		Long chatId = 1L;
		UserDto userDto = UserDto.builder().username(user.getUsername()).build();
		ChatDto chatDto = ChatDto.builder().users(List.of(userDto)).build();
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("randomSorfield").sortDir(Direction.ASC).build();
		Pageable pageable = Pageable.unpaged();
		Page<Message> page = Page.empty();

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// chatService
		when(chatService.getById(chatId)).thenReturn(chatDto);
		// pageUtils
		when(pagUtils.getPageable(pageInfoDto)).thenReturn(pageable);
		// dao
		when(msgDao.findByChatChatId(chatId, pageable)).thenReturn(page);

		assertThrows(RecordNotFoundException.class, () -> messageService.getMessagesByChat(chatId, pageInfoDto));

		verify(chatService).getById(chatId);
		verify(msgDao).findByChatChatId(chatId, pageable);
	}

	@Test
	void getMessagesByChatReturnsNotNull() {
		Long chatId = 1L;
		Message message = new Message();
		UserDto userDto = UserDto.builder().username(user.getUsername()).build();
		ChatDto chatDto = ChatDto.builder().users(List.of(userDto)).build();
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("randomSorfield").sortDir(Direction.ASC).build();
		Pageable pageable = Pageable.unpaged();
		Page<Message> page = new PageImpl<>(List.of(message));
		ResPaginationG<MessageDto> resPaginationG = new ResPaginationG<MessageDto>();

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// chatService
		when(chatService.getById(chatId)).thenReturn(chatDto);
		// pageUtils
		when(pagUtils.getPageable(pageInfoDto)).thenReturn(pageable);
		// dao
		when(msgDao.findByChatChatId(chatId, pageable)).thenReturn(page);
		// Mapper
		when(msgMapper.pageAndPageInfoDtoToResPaginationG(page, pageInfoDto)).thenReturn(resPaginationG);

		assertNotNull(messageService.getMessagesByChat(chatId, pageInfoDto));

		verify(chatService).getById(chatId);
		verify(msgDao).findByChatChatId(chatId, pageable);
	}

}
