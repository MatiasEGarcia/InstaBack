package com.instaJava.instaJava.service;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.Duration;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.instaJava.instaJava.dao.CommentDao;
import com.instaJava.instaJava.dto.CommentDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqComment;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Comment;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.mapper.CommentMapper;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

	private final Clock clock;
	private final CommentDao commentDao;
	private final MessagesUtils messUtils;
	private final PageableUtils pagUtils;
	private final CommentMapper commentMapper;
	private final NotificationService notiService;
	private final PublicatedImageService publicatedImageService;

	@Override
	@Transactional
	public CommentDto save(ReqComment reqComment) {
		if (reqComment.getBody() == null || reqComment.getBody().isBlank() || reqComment.getPublImgId() == null
				|| reqComment.getPublImgId().isBlank()) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null-or-empty"));
		}
		Comment comment;
		Comment parentComment;
		User authUser;
		PublicatedImage publicatedImage = publicatedImageService.findById(Long.parseLong(reqComment.getPublImgId()))
				.orElseThrow(() -> new RecordNotFoundException(messUtils.getMessage("publiImage.not-found"),
						HttpStatus.NOT_FOUND));

		comment = new Comment();
		// checking parent comment
		if (reqComment.getParentId() != null && !reqComment.getParentId().isBlank()) {
			parentComment = commentDao.findById(Long.parseLong(reqComment.getParentId()))
					.orElseThrow(() -> new RecordNotFoundException(messUtils.getMessage("comment.parent-not-found"),
							HttpStatus.NOT_FOUND));
			comment.setParent(parentComment);
		}
		comment.setBody(reqComment.getBody());
		comment.setCreatedAt(ZonedDateTime.now(clock));
		// auth user
		authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		comment.setOwnerUser(authUser);
		// image
		comment.setAssociatedImg(publicatedImage);
		// saving
		comment = commentDao.save(comment);
		// sending notification?
		notiService.saveNotificationOfComment(comment, "Tienes un nuevo comentario en una publicacion.");

		return commentMapper.commentToCommentDto(comment);
	}

	@Override
	@Transactional(readOnly = true)
	public ResPaginationG<CommentDto> getRootCommentsByPublicationImageId(Long publicationImageId,
			PageInfoDto pageInfoDto) {
		if (publicationImageId == null || pageInfoDto == null || pageInfoDto.getSortField() == null
				|| pageInfoDto.getSortDir() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Page<Comment> pageComments = commentDao.getRootCommentsByAssociatedImage(publicationImageId,
				pagUtils.getPageable(pageInfoDto));
		if (!pageComments.hasContent()) {
			throw new RecordNotFoundException(messUtils.getMessage("comment-no-content"), HttpStatus.NO_CONTENT);
		}
		return commentMapper.pageAndPageInfoDtoToResPaginationG(pageComments, pageInfoDto);
	}

	@Override
	@Transactional(readOnly = true)
	public ResPaginationG<CommentDto> getAssociatedCommentsByParentCommentId(Long parentId, PageInfoDto pageInfoDto) {
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
		return commentMapper.pageAndPageInfoDtoToResPaginationG(pageComments, pageInfoDto);
	}

	// tests
	@Override
	@Transactional
	public void deleteById(Long commentId) {
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
	}

	//falta testss
	@Override
	@Transactional
	public CommentDto updateById(Long commentId, String newCommentBody) {
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
		commentToUpdate = commentDao.save(commentToUpdate);
		return commentMapper.commentToCommentDto(commentToUpdate);
	}

}
