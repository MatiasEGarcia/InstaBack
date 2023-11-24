package com.instaJava.instaJava.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dao.ChatDao;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqChat;
import com.instaJava.instaJava.entity.Chat;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.ChatTypeEnum;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.exception.ImageException;
import com.instaJava.instaJava.exception.InvalidException;
import com.instaJava.instaJava.exception.UserNotApplicableForChatException;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

	private final ChatDao chatDao;
	private final PageableUtils pageUtils;
	private final MessagesUtils messUtils;
	private final UserService userService;
	private final FollowService followService;

	/**
	 * Method to get all authenticated user's chats.
	 * @param pageInfoDto, It has pagination info. (must not be null)
	 * @Throw IllegalArgumentException if some parameter is null.
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<Chat> getAuthUserChats(PageInfoDto pageInfoDto) {
		if (pageInfoDto == null || pageInfoDto.getSortDir() == null || pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		}
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return chatDao.findByUsersUserId(authUser.getUserId(), pageUtils.getPageable(pageInfoDto));
	}

	/**
	 * Method to create a chat entity and save it in dbb.
	 * @param reqChat - object with info to create chat entity (must not be null)
	 * @Throws IllegalArgumentException if some parameter is null.
	 * @throws UserNotApplicableForChatException if there one user that cannot be added to the chat.
	 */
	@Override
	@Transactional
	public Chat create(ReqChat reqChat) {
		if (reqChat == null || reqChat.getUsersToAdd() == null 
					 || reqChat.getType() == null ) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		}
		Chat chatToCreate;
		List<String> usersNotApplicable;
		List<User> allUsers;
		List<String> allUsersname = new ArrayList<>();
		allUsersname.addAll(reqChat.getUsersToAdd());
		//if the chat is private then there won't be admins except auth user, thats why I don't check UsersToAddAsAdmins if is null and throw exception.
		if(reqChat.getUsersToAddAsAdmins() != null) {
			allUsersname.addAll(reqChat.getUsersToAddAsAdmins());			
		}
		allUsers = userService.getByUsernameIn(allUsersname);
		// if none user was found.
		if (allUsers.isEmpty()) {
			throw new UserNotApplicableForChatException(messUtils.getMessage("expection.chat-users-not-applicable"),
					allUsersname);
		}
		
		//check if there was some user who couldn't be found.
		checkAllFounded(allUsers, allUsersname);

		// check if all users can be added to the chat.
		usersNotApplicable = new ArrayList<>();
		areNotApplicable(allUsers,usersNotApplicable);
		if (!usersNotApplicable.isEmpty()) {
			throw new UserNotApplicableForChatException(messUtils.getMessage("expection.chat-users-not-applicable"),
					usersNotApplicable);
		}

		chatToCreate = new Chat();
		
		if(reqChat.getType().equals(ChatTypeEnum.GROUP)) {
			createGroupChat(reqChat, chatToCreate, allUsers);
		}else if(reqChat.getType().equals(ChatTypeEnum.PRIVATE)) {
			createPrivateChate(reqChat, chatToCreate, allUsers.get(0)); //there should be only one user.
		}else {
			throw new IllegalArgumentException(messUtils.getMessage("exception.enum-type-incorrect"));
		}
		return chatDao.save(chatToCreate);
	}

	/**
	 * Method to set or update chat image in the case that is a group chat.
	 * 
	 * @param image - image to add to chat record.
	 * @param chatId - chat's id to update.
	 * @throws ImageException - if there was some error in image encode to base64.
	 */
	@Override
	@Transactional
	public Chat setImage(MultipartFile image, Long chatId) {
		if(image == null || chatId == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		}
		Optional<Chat> chatToEdit = chatDao.findById(chatId);
		if(chatToEdit.isEmpty()) {
			throw new EntityNotFoundException(messUtils.getMessage("exception.chat-not-found"));
		}
		//only group chat can edit image attribute.
		if(chatToEdit.get().getType().equals(ChatTypeEnum.PRIVATE)) {
			throw new InvalidException(messUtils.getMessage("exception.chat-private-not-image")); 
		}
		
		try {
			chatToEdit.get().setImage(Base64.getEncoder().encodeToString(image.getBytes()));
		}catch(Exception e) {
			throw new ImageException(e);
		}
		return chatDao.save(chatToEdit.get());
	}
	
	
	/**
	 * Function to check if users are applicable for chat, for example, are visible
	 * or not? the auth user follow it or not?
	 * 
	 * @param users - list off users to check if are applicable or not.
	 * @param usersNotApplicable - list to add user's username when is not applicable.
	 */
	@Transactional(readOnly = true)
	private void areNotApplicable(List<User> users, List<String> usersNotApplicable) {
		users.forEach((user) -> {
			if (!user.isVisible()) {
				FollowStatus status = followService.getFollowStatusByFollowedId(user.getUserId());
				// only if auth user follow the user and the status is accepted can add it to
				// the group.
				if (!status.equals(FollowStatus.ACCEPTED)) {
					usersNotApplicable.add(user.getUsername());
				}
			}
		});
	}

	/**
	 * Function to set private chat attributes.
	 * 
	 * @param reqChat - object from the request with the data to add to Chat entity.
	 * @param chatToCreate - chat entity which will be saved.
	 * @param userNotAuth - in private chat there will be only 2 users, auth user and the other, the userNotAuht, 
	 */
	private void createPrivateChate(ReqChat reqChat, Chat chatToCreate, User userNotAuth) {
		chatToCreate.setUsers(List.of(userNotAuth));//in theory there should be only one user.
		chatToCreate.setType(reqChat.getType());
		setAuthUserInChat(chatToCreate);
	}
	
	/**
	 * Function to set group chat attributes.
	 * @param reqChat - object from the request with the data to add to Chat entity.
	 * @param chatToCreate - chat entity which will be saved.
	 * @param allUsersFound - list of users who need to be added to Chat entity(users and admins)
	 * @throws IllegalArgumentExeption if one parameter is null.
	 */
	private void createGroupChat(ReqChat reqChat, Chat chatToCreate, List<User> allUsersFound) {
		if(reqChat == null || reqChat.getUsersToAddAsAdmins() == null || reqChat.getType() == null || reqChat.getName() == null
				||chatToCreate == null || allUsersFound == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		}
		//divide common users from admins users
		Predicate<User> partition = user -> reqChat.getUsersToAddAsAdmins().contains(user.getUsername());
		Map<Boolean, List<User>> usersToChat= allUsersFound.stream().collect(Collectors.partitioningBy(partition));
		
		chatToCreate.setUsers(usersToChat.get(false));
		chatToCreate.setAdmins(usersToChat.get(true));
		setAuthUserInChat(chatToCreate);
		chatToCreate.setName(reqChat.getName());
		chatToCreate.setType(reqChat.getType());
	}
	
	/**
	 * Method to check if all the users needed to add in Chat entity to save were found.
	 * @param usersFound - users who where found.
	 * @param allUsersString -  list of usernames of the users provided by the request (there must be all the usernames that had been needed to be found).
	 * @throws IllegalArgumentExeption if one parameter is null.
	 * @throws UserNotApplicableForChatException if there was some user who couldn't be found
	 */
	private void checkAllFounded(List<User> usersFound, List<String> allUsersString) {
		if(usersFound == null || allUsersString == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		List<String> usersFoundUsernames= usersFound.stream().map(User::getUsername).toList();
		
		List<String> usersNotFound = allUsersString.stream().filter(username -> !usersFoundUsernames.contains(username)).toList();
	
		if(!usersNotFound.isEmpty()) {
			throw new UserNotApplicableForChatException(messUtils.getMessage("expection.chat-users-not-applicable"), 
					usersNotFound);
		}
	}
	
	/**
	 * Function to add auth user in Chat entity.
	 * @param chatToCreate - chat entity which will be saved.
	 * @throws IllegalArgumentExeption if one parameter is null.
	 */
	private void setAuthUserInChat(Chat chatToCreate) {
		if(chatToCreate == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		//add auth user as admin, because is the one who created the chat.
		if(chatToCreate.getAdmins() == null) {
			chatToCreate.setAdmins(List.of(authUser));
		}else {
			chatToCreate.getAdmins().add(authUser);
		}
		
	}
}
