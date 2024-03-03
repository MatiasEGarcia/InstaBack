package com.instaback.service;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaback.dao.CommentDao;
import com.instaback.dto.PageInfoDto;
import com.instaback.entity.Comment;
import com.instaback.entity.PublicatedImage;
import com.instaback.entity.User;
import com.instaback.exception.InvalidActionException;
import com.instaback.exception.RecordNotFoundException;
import com.instaback.util.MessagesUtils;
import com.instaback.util.PageableUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

	private final Clock clock;
	private final CommentDao commentDao;
	private final MessagesUtils messUtils;
	private final PageableUtils pagUtils;

	@Override
	@Transactional
	public Comment save(String body, String parentId, PublicatedImage pImage) {
		if (body == null || body.isBlank() || pImage == null || pImage.getId() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		}
		Comment comment;
		Comment parentComment;
		User authUser;
		comment = new Comment();
		// checking parent comment
		if (parentId != null && !parentId.isBlank()) {
			parentComment = commentDao.findById(Long.parseLong(parentId))
					.orElseThrow(() -> new RecordNotFoundException(messUtils.getMessage("comment.parent-not-found"),
							HttpStatus.NOT_FOUND));
			comment.setParent(parentComment);
		}
		comment.setBody(body);
		comment.setCreatedAt(ZonedDateTime.now(clock));
		// auth user
		authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		comment.setOwnerUser(authUser);
		// image
		comment.setAssociatedImg(pImage);
		return commentDao.save(comment);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<Comment> getRootCommentsByAssociatedImgId(Long associatedImgId, PageInfoDto pageInfoDto){
		if (associatedImgId == null || pageInfoDto == null || pageInfoDto.getSortField() == null
				|| pageInfoDto.getSortDir() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Page<Comment> pageComments = commentDao.getRootCommentsByAssociatedImage(associatedImgId,
				pagUtils.getPageable(pageInfoDto));
		if (!pageComments.hasContent()) {
			throw new RecordNotFoundException(messUtils.getMessage("comment-no-content"), HttpStatus.NO_CONTENT);
		}
		return pageComments;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<Comment> getAssociatedCommentsByParentCommentId(Long parentId,PageInfoDto pageInfoDto){
		if (parentId == null || pageInfoDto == null || pageInfoDto.getSortField() == null
				|| pageInfoDto.getSortDir() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Comment parentComment = commentDao.findById(parentId)
				.orElseThrow(() -> new RecordNotFoundException(messUtils.getMessage("comment.parent-not-found"),
						HttpStatus.NOT_FOUND));
		Page<Comment> pageComments = commentDao.findByParent(parentComment, pagUtils.getPageable(pageInfoDto));
		if (!pageComments.hasContent()) {
			throw new RecordNotFoundException(messUtils.getMessage("comment-no-content"), HttpStatus.NO_CONTENT);
		}
		return pageComments;
	}

	@Override
	@Transactional
	public Comment deleteById(Long commentId) {
		if (commentId == null)
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		Comment commentToDelete;
		User authUser;
		ZonedDateTime whenCommentWasCreated;
		ZonedDateTime now;
		Long durationMinutes;

		commentToDelete = commentDao.findById(commentId).orElseThrow(
				() -> new RecordNotFoundException(messUtils.getMessage("comment.not-found"), HttpStatus.NOT_FOUND));
		authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		// owner is the same than authenticated user?
		if (!commentToDelete.getOwnerUser().equals(authUser)) {
			throw new InvalidActionException(messUtils.getMessage("generic.auth-user-no-owner"),
					HttpStatus.BAD_REQUEST);
		}
		// only can be deleted before 5 min
		now = ZonedDateTime.now(clock);
		whenCommentWasCreated = commentToDelete.getCreatedAt();
		Duration duration = Duration.between(whenCommentWasCreated.toInstant(), now.toInstant());
		durationMinutes = duration.toMinutes() % 60;
		if (durationMinutes > 5) {
			throw new InvalidActionException(messUtils.getMessage("comment.5-minutes-not-delete"),
					HttpStatus.BAD_REQUEST);
		}
		commentDao.deleteById(commentId);
		return commentToDelete;
	}

	
	@Override
	@Transactional
	public Comment updateById(Long commentId, String newCommentBody) {
		if (commentId == null || newCommentBody == null || newCommentBody.isBlank()) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Comment commentToUpdate;
		User authUser;
		ZonedDateTime whenCommentWasCreated;
		ZonedDateTime now;
		Long durationMinutes;

		commentToUpdate = commentDao.findById(commentId).orElseThrow(
				() -> new RecordNotFoundException(messUtils.getMessage("comment.not-found"), HttpStatus.NOT_FOUND));
		authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		// owner is the same than authenticated user?
		if (!commentToUpdate.getOwnerUser().equals(authUser)) {
			throw new InvalidActionException(messUtils.getMessage("generic.auth-user-no-owner"),
					HttpStatus.BAD_REQUEST);
		}

		//only can be updated before 5 min
		now = ZonedDateTime.now(clock);
		whenCommentWasCreated = commentToUpdate.getCreatedAt();
		Duration duration = Duration.between(whenCommentWasCreated.toInstant(), now.toInstant());
		durationMinutes = duration.toMinutes() % 60;
		if (durationMinutes > 5) {
			throw new InvalidActionException(messUtils.getMessage("comment.5-minutes-not-update"),
					HttpStatus.BAD_REQUEST);
		}
		//update
		commentToUpdate.setBody(newCommentBody);
		return commentToUpdate;
	}


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
