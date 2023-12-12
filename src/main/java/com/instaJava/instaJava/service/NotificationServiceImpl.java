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
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Follow;
import com.instaJava.instaJava.entity.Notification;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.NotificationType;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
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
				.type(NotificationType.FOLLOW).createdAt(znDate).notiMessage(customMessage).build();
		newNoti = notiDao.save(newNoti);

		// web socket event message.
		notiDto = NotificationDto.builder().notiId(newNoti.getNotiId().toString())
				.notificationType(NotificationType.FOLLOW).createdAt(znDate).watched(newNoti.isWatched())
				.fromWho(userMapper.userToUserDto(follow.getFollower())).notiMessage(customMessage).build();
		messTemplate.convertAndSendToUser(follow.getFollowed().getUserId().toString(), "/private", notiDto);
	}

	@Override
	@Transactional//check tests
	public void saveNotificationOfMessage(ChatDto chatDto, MessageDto messageDto) {
		if(chatDto == null || chatDto.getUsers() == null || chatDto.getUsers().isEmpty() 
				|| messageDto == null || messageDto.getBody() == null || messageDto.getBody().isBlank()) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument.not.null"));
		}
		
		List<NotificationDto> notificationsDto;
		List<Notification> notifications = new ArrayList<>();
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		//creating notifications to save, to each user in chatDto.
		chatDto.getUsers().forEach((userDto) -> {
			//authenticated user don't need the notification of new message.
			if(!userDto.getUserId().equalsIgnoreCase(user.getUserId().toString())) {
				Long userDtoIdLong = Long.parseLong(userDto.getUserId());
				Notification newNoti = Notification.builder().fromWho(user).toWho(new User(userDtoIdLong))
						.type(NotificationType.MESSAGE).createdAt(ZonedDateTime.now(clock)).notiMessage(messUtils.getMessage("socket.new-message"))
						.build();	
				notifications.add(newNoti);
			}
		});
		
		//saving new notifications
		notificationsDto = notificationMapper.notificationListToNotificationDtoListWithToWho(notiDao.saveAll(notifications));
		
		//sending notifications to each user subscribed by sockets.
		notificationsDto.forEach((notificationDto) -> {
			//chat topic
			String destination = "/chat/" + notificationDto.getToWho().getUserId();//   /chat/userId
			NotificationChatDto notiChatDto = new NotificationChatDto(chatDto, messageDto);
			messTemplate.convertAndSend(destination, notiChatDto);
			
			//notification topic
			messTemplate.convertAndSendToUser(notificationDto.getToWho().getUserId(), "/private", notificationDto);
		});

	}

	// check test
	@Override
	@Transactional(readOnly = true)
	public ResPaginationG<NotificationDto> getNotificationsByAuthUser(PageInfoDto pageInfoDto) {
		if (pageInfoDto == null || pageInfoDto.getSortDir() == null || pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Page<Notification> notiPage = notiDao.findByToWhoUserId(user.getUserId(), pagUtils.getPageable(pageInfoDto));
		if (!notiPage.hasContent()) {
			throw new RecordNotFoundException(messUtils.getMessage("notif.group-not-found"), HttpStatus.NO_CONTENT);
		}
		return notificationMapper.pageAndPageInfoDtoToResPaginationG(notiPage, pageInfoDto);
	}

	// check test
	@Override
	@Transactional
	public void deleteNotificationById(Long notiId) {
		if (notiId == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		User authUser;
		Optional<Notification> notiToDelete = findNotificationById(notiId);
		authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!notiToDelete.get().getToWho().equals(authUser)) {
			throw new InvalidActionException(messUtils.getMessage("notif.owner-not-same"), HttpStatus.BAD_REQUEST);
		}
		notiDao.delete(notiToDelete.get());
	}

	// check test
	@Override
	@Transactional(readOnly = true)
	public Optional<Notification> findNotificationById(Long notiId) {
		if (notiId == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		return notiDao.findById(notiId);
	}

}
