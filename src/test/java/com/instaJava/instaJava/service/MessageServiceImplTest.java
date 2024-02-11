package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

import com.instaJava.instaJava.dao.ChatDao;
import com.instaJava.instaJava.dao.MessageDao;
import com.instaJava.instaJava.dto.MessageDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.entity.Chat;
import com.instaJava.instaJava.entity.ChatUser;
import com.instaJava.instaJava.entity.Message;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.mapper.ChatMapper;
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
	private ChatDao chatDao;
	@Mock
	private MessagesUtils messUtils;
	@Mock
	private PageableUtils pagUtils;
	@Mock
	private NotificationService notifSerivce;
	@Mock
	private ChatMapper chatMapper;
	@InjectMocks
	private MessageServiceImpl messageService;
	private final User user = User.builder().id(1L).username("Mati").password("random").role(RolesEnum.ROLE_USER)
			.build();

	// create
	@Test
	void createParamChatNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> messageService.create("random", null));
	}

	@Test
	void createParamMessageNullThrow() {
		Chat chat = new Chat(1L);
		assertThrows(IllegalArgumentException.class, () -> messageService.create(null, chat));
	}

	@Test
	void createParamMessageBlankThrow() {
		Chat chat = new Chat(1L);
		assertThrows(InvalidActionException.class, () -> messageService.create("", chat));
	}

	@Test
	void createParamChatIdNullThrow() {
		Chat chat = new Chat();
		assertThrows(IllegalArgumentException.class, () -> messageService.create("random", chat));
	}
	
	@Test
	void createAuthUserIsNotFromChatThrow() {
		User randomUser = User.builder().username("random").build();// differente user than auth user.
		ChatUser chatUser = ChatUser.builder().user(randomUser).build();
		Chat chat = Chat.builder().id(1L).chatUsers(List.of(chatUser)).build();

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		assertThrows(InvalidActionException.class, () -> messageService.create("randomMessage", chat));
		verify(msgDao, never()).save(any(Message.class));
		verify(notifSerivce, never()).saveNotificationOfMessage(eq(chat), any(MessageDto.class));
	}

	@Test
	void createAuthUserIsInChatReturnsNotNull() {
		String messageToSend = "randomMessage";
		ChatUser chatUser = ChatUser.builder().user(user)// same user than authUser
				.build();
		Chat chat = Chat.builder().id(1L).chatUsers(List.of(chatUser)).build();
		Message message = new Message();

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// clock
		when(clock.getZone()).thenReturn(ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(Instant.parse("2020-12-01T10:05:23.653Z"));
		// dao
		when(msgDao.save(any(Message.class))).thenReturn(message);

		assertNotNull(messageService.create(messageToSend, chat));
	}

	// getMessagesByChat
	@Test
	void getMessagesByChatParamChatIdNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("randomSorfield").sortDir(Direction.ASC).build();
		assertThrows(IllegalArgumentException.class, () -> messageService.getMessagesByChat(null, pageInfoDto));
	}

	@Test
	void getMessagesByChatParamPageInfoDtoNullThrow() {
		Chat chat = new Chat(1L);
		assertThrows(IllegalArgumentException.class, () -> messageService.getMessagesByChat(chat, null));
	}

	@Test
	void getMessagesByChatParamPageInfoDtoSortFieldNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortDir(Direction.ASC).build();
		Chat chat = new Chat(1L);
		assertThrows(IllegalArgumentException.class, () -> messageService.getMessagesByChat(chat, pageInfoDto));
	}

	@Test
	void getMessagesByChatParamPageInfoDtoSortDirNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("randomSorfield").build();
		Chat chat = new Chat(1L);
		assertThrows(IllegalArgumentException.class, () -> messageService.getMessagesByChat(chat, pageInfoDto));
	}

	@Test
	void getMessagesByChatUserIsNotInChatThrow() {
		User randomUser = User.builder().username("random").build();// differente user than auth user.
		ChatUser chatUser = ChatUser.builder().user(randomUser)// same user than authUser
				.build();
		Chat chat = Chat.builder().id(1L).chatUsers(List.of(chatUser)).build();
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("randomSorfield").sortDir(Direction.ASC).build();

		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		assertThrows(InvalidActionException.class, () -> messageService.getMessagesByChat(chat, pageInfoDto));

		verify(msgDao, never()).findByChatId(eq(chat.getId()), any(Pageable.class));
	}

	@Test
	void getMessagesByChatNoContentThrow() {
		ChatUser chatUser = ChatUser.builder().user(user)// same user than authUser
				.build();
		Chat chat = Chat.builder().id(1L).chatUsers(List.of(chatUser)).build();
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("randomSorfield").sortDir(Direction.ASC).build();
		Pageable pageable = Pageable.unpaged();
		Page<Message> page = Page.empty();
		
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		// pageUtils
		when(pagUtils.getPageable(pageInfoDto)).thenReturn(pageable);
		// dao
		when(msgDao.findByChatId(chat.getId(), pageable)).thenReturn(page);

		assertThrows(RecordNotFoundException.class, () -> messageService.getMessagesByChat(chat, pageInfoDto));
	}

	@Test
	void getMessagesByChatReturnsNotNull() {
		Message message = new Message();
		ChatUser chatUser = ChatUser.builder().user(user)// same user than authUser
				.build();
		Chat chat = Chat.builder().id(1L).chatUsers(List.of(chatUser)).build();
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("randomSorfield").sortDir(Direction.ASC).build();
		Pageable pageable = Pageable.unpaged();
		Page<Message> page = new PageImpl<>(List.of(message));


		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// pageUtils
		when(pagUtils.getPageable(pageInfoDto)).thenReturn(pageable);
		// dao
		when(msgDao.findByChatId(chat.getId(), pageable)).thenReturn(page);

		assertNotNull(messageService.getMessagesByChat(chat, pageInfoDto));
	}

	// messagesWatched
	@Test
	void messagesWatchedParamMessageWatchedIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> messageService.messagesWatched(null));
	}

	@Test
	void messagesWatchedParamMessageWatchedIdEmptyThrow() {
		assertThrows(IllegalArgumentException.class, () -> messageService.messagesWatched(Collections.emptySet()));
	}

	@Test
	void messagesWatchedNotAllMessagesWereFoundThrow() {
		Set<String> messageWatchedIds = Set.of("1", "2");
		// only message found.
		Message message = Message.builder().id(1L).watchedBy("Julio,").build();
		List<Message> listMessages = List.of(message);
		when(msgDao.findAllById(anyList())).thenReturn(listMessages);

		assertThrows(RecordNotFoundException.class, () -> messageService.messagesWatched(messageWatchedIds));

		verify(msgDao, never()).saveAll(listMessages);
	}

	@Test
	void messagesWatchedReturnsNotNullNoMoreMessagesNotWatched() {
		Set<String> messageWatchedIds = Set.of("1", "2");
		Chat chat = Chat.builder().id(1L).build();
		// messages found.
		Message message1 = Message.builder().id(1L).chat(chat).watchedBy("Julio,").build();
		Message message2 = Message.builder().id(2L).chat(chat).watchedBy("Julio,").build();
		List<Message> listMessages = List.of(message1, message2);

		MessageServiceImpl spyMessageService = spy(messageService);

		// getting messages
		when(msgDao.findAllById(anyList())).thenReturn(listMessages);
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// dao
		when(msgDao.saveAll(listMessages)).thenReturn(listMessages);

		doReturn(Collections.emptyList()).when(spyMessageService)
				.getMessagesNotWatchedCountByChatIds(List.of(chat.getId()), user.getUsername());

		assertNotNull(spyMessageService.messagesWatched(messageWatchedIds));

		verify(msgDao).saveAll(listMessages);
	}

	@Test
	void messagesWatchedReturnsNotNullOneMessageAlreadyWatched() {
		Set<String> messageWatchedIds = Set.of("1", "2");
		Chat chat = Chat.builder().id(1L).build();
		// messages found.
		Message message1 = Message.builder().id(1L).chat(chat).watchedBy("Julio,").build();
		Message message2 = Message.builder().id(2L).chat(chat).watchedBy("Mati,").build();// already watched by
																									// auth user.
		List<Message> listMessages = List.of(message1, message2);

		MessageServiceImpl spyMessageService = spy(messageService);

		// getting messages
		when(msgDao.findAllById(anyList())).thenReturn(listMessages);
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		message1.setWatchedBy("Julio,Mati,");
		// dao
		when(msgDao.saveAll(listMessages)).thenReturn(listMessages);

		doReturn(Collections.emptyList()).when(spyMessageService)
				.getMessagesNotWatchedCountByChatIds(List.of(chat.getId()), user.getUsername());

		assertNotNull(spyMessageService.messagesWatched(messageWatchedIds));

		verify(msgDao).saveAll(listMessages);
	}

	// getMessagesNotWatchedCountByChatIds
	@Test
	void getMessagesNotWatchedCountByChatIdsParamChatsIdNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> messageService.getMessagesNotWatchedCountByChatIds(null, "random"));
	}

	@Test
	void getMessagesNotWatchedCountByChatIdsParamChatsIdEmptyThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> messageService.getMessagesNotWatchedCountByChatIds(Collections.emptyList(), "random"));
	}

	@Test
	void getMessagesNotWatchedCountByChatIdsParamUsernameNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> messageService.getMessagesNotWatchedCountByChatIds(List.of(1L), null));
	}

	@Test
	void getMessagesNotWatchedCountByChatIdsParamUsernameBlankThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> messageService.getMessagesNotWatchedCountByChatIds(List.of(1L), ""));
	}

	@Test
	void getMessagesNotWatchedCountByChatIdsReturnsNotNull() {
		List<Long> chatsIds = List.of(1L);
		String username = "random";
		when(msgDao.countByUserNoWatchedAndChatId(chatsIds, username)).thenReturn(Collections.emptyList());
		assertNotNull(messageService.getMessagesNotWatchedCountByChatIds(chatsIds, username));
	}

	// setAllMessagesNotWatchedAsWatchedByChatId
	@Test
	void setAllMessagesNotWatchedAsWatchedByChatIdParamChatNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> messageService.setAllMessagesNotWatchedAsWatchedByChatId(null));
	}
	@Test
	void setAllMessagesNotWatchedAsWatchedByChatIdParamChatIdNullThrow() {
		Chat chat = new Chat();
		assertThrows(IllegalArgumentException.class,
				() -> messageService.setAllMessagesNotWatchedAsWatchedByChatId(chat));
	}

	@Test
	void setAllMessagesNotWatchedAsWatchedByChatIdNoMessages() {
		Chat chat = new Chat(1L);
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// dao
		when(msgDao.findAllByChatAndUserNoWatched(chat, user.getUsername())).thenReturn(Collections.emptyList());
		messageService.setAllMessagesNotWatchedAsWatchedByChatId(chat);

		verify(msgDao, never()).saveAll(anyList());
	}

	@Test
	void setAllMessagesNotWatchedAsWatchedByChatId() {
		Chat chat = new Chat(1L);
		Message mess = Message.builder()
				.watchedBy("julio,")
				.build();
		List<Message> listMess = List.of(mess);
		
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// dao
		when(msgDao.findAllByChatAndUserNoWatched(chat, user.getUsername())).thenReturn(listMess);
		messageService.setAllMessagesNotWatchedAsWatchedByChatId(chat);

		listMess.get(0).setWatchedBy("julio,"+user.getUsername()+",");
		
		verify(msgDao).saveAll(listMess);
	}
	
}
