package com.instaJava.instaJava.service;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.NotificationDao;
import com.instaJava.instaJava.dto.ChatDto;
import com.instaJava.instaJava.dto.MessageDto;
import com.instaJava.instaJava.dto.NotificationChatDto;
import com.instaJava.instaJava.dto.NotificationDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.entity.Chat;
import com.instaJava.instaJava.entity.Comment;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.entity.Notification;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.NotificationType;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.mapper.ChatMapper;
import com.instaJava.instaJava.mapper.NotificationMapper;
import com.instaJava.instaJava.mapper.UserMapper;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private final Clock clock;
	private final NotificationDao notiDao;
	private final SimpMessagingTemplate messTemplate;
	private final UserMapper userMapper;
	private final NotificationMapper notificationMapper;
	private final ChatMapper chatMapper;
	private final PageableUtils pagUtils;
	private final MessagesUtils messUtils;

	@Override
	@Transactional
	public void saveNotificationOfFollow(Follow follow, String customMessage) {
		if (follow == null || follow.getFollowed() == null || follow.getFollower() == null || customMessage == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		}
		NotificationDto notiDto;
		ZonedDateTime znDate = ZonedDateTime.now(clock);
		Notification newNoti = Notification.builder().fromWho(follow.getFollower()).toWho(follow.getFollowed())
				.type(NotificationType.FOLLOW).createdAt(znDate).notiMessage(customMessage)
				.elementId(follow.getId())
				.build();
		newNoti = notiDao.save(newNoti);
		
		// web socket event message.
		notiDto = NotificationDto.builder().id(newNoti.getId().toString())
				.notificationType(NotificationType.FOLLOW).createdAt(znDate).watched(newNoti.isWatched())
				.toWho(userMapper.userToUserDto(follow.getFollowed()))
				.fromWho(userMapper.userToUserDto(follow.getFollower())).notiMessage(customMessage)
				.elementId(follow.getId().toString()).build();
		messTemplate.convertAndSendToUser(follow.getFollowed().getId().toString(), "/private", notiDto);
	}

	
	@Override
	@Transactional
	public void saveNotificationOfMessage(Chat chat, MessageDto messageDto) {
		if(chat == null || chat.getUsers().isEmpty()  || messageDto == null || 
				messageDto.getBody() == null || messageDto.getBody().isBlank()) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		}
		ChatDto chatDto;
		Long elementId = Long.parseLong(messageDto.getId());
		List<NotificationDto> notificationsDto;
		List<Notification> notifications = new ArrayList<>();
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		//creating notifications to save, to each user in chatDto.
		chat.getUsers().forEach((user) -> {
			//authenticated user don't need the notification of new message.
			if(user.getId() != authUser.getId()) {
				Notification newNoti = Notification.builder().fromWho(authUser).toWho(user)
						.type(NotificationType.MESSAGE).createdAt(ZonedDateTime.now(clock)).notiMessage(messUtils.getMessage("socket.new-message"))
						.elementId(elementId)
						.build();	
				notifications.add(newNoti);
			}
		});
		
		//saving new notifications
		notificationsDto = notificationMapper.notificationListToNotificationDtoListWithToWho(notiDao.saveAll(notifications));
		chatDto = chatMapper.chatToChatDto(chat);
		
		//sending notifications to each user subscribed by sockets.
		notificationsDto.forEach((notificationDto) -> {
			//chat topic
			String destination = "/chat/" + notificationDto.getToWho().getId();//   /chat/userId
			NotificationChatDto notiChatDto = new NotificationChatDto(chatDto, messageDto);
			messTemplate.convertAndSend(destination, notiChatDto);
			
			//notification topic
			messTemplate.convertAndSendToUser(notificationDto.getToWho().getId(), "/private", notificationDto);
		});

	}

	
	@Override
	@Transactional
	public void saveNotificationOfComment(Comment comment, String customMessage) {
		if(comment.getOwnerUser() == null || comment.getOwnerUser().getId() == null || comment.getAssociatedImg() == null ||
				comment.getAssociatedImg().getUserOwner() == null || comment.getAssociatedImg().getUserOwner().getId() == null ||
				customMessage == null || customMessage.isBlank()) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		}
		NotificationDto notiDto;
		Notification notification = Notification.builder()
				.fromWho(comment.getOwnerUser())
				.toWho(comment.getAssociatedImg().getUserOwner())
				.type(NotificationType.COMMENT)
				.createdAt(ZonedDateTime.now(clock))
				.notiMessage(customMessage)
				.elementId(comment.getAssociatedImg().getId()) //in this case will save the publication, so the client will know where is the comemnt
				.build();
		//dao
		notification = notiDao.save(notification);
		//mapper
		notiDto = notificationMapper.notificationToNotificationDtoWithToWho(notification);
		//socket
		messTemplate.convertAndSendToUser(notiDto.getToWho().getId(),"/private", notiDto);
		
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<Notification> getNotificationsByAuthUser(PageInfoDto pageInfoDto) {
		if (pageInfoDto == null || pageInfoDto.getSortDir() == null || pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Page<Notification> notiPage = notiDao.findByToWhoId(user.getId(), pagUtils.getPageable(pageInfoDto));
		if (!notiPage.hasContent()) {
			throw new RecordNotFoundException(messUtils.getMessage("notif.group-not-found"), HttpStatus.NO_CONTENT);
		}
		return notiPage;
	}

	
	@Override
	@Transactional
	public Notification deleteNotificationById(Long notiId) {
		if (notiId == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		User authUser;
		Notification notiToDelete = findNotificationById(notiId).orElseThrow(() -> 
				new RecordNotFoundException(messUtils.getMessage("notif.not-found"), HttpStatus.NOT_FOUND));
		authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!notiToDelete.getToWho().equals(authUser)) {
			throw new InvalidActionException(messUtils.getMessage("notif.owner-not-same"), HttpStatus.BAD_REQUEST);
		}
		notiDao.delete(notiToDelete);
		return notiToDelete;
	}

	@Override
	@Transactional
	public void deleteAllByAuthUser() {
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		notiDao.deleteAllByToWho(authUser);
	}
	
	
	@Override
	@Transactional(readOnly = true)
	public Optional<Notification> findNotificationById(Long notiId) {
		if (notiId == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		return notiDao.findById(notiId);
	}
}
