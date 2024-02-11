package com.instaJava.instaJava.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.instaJava.instaJava.dto.CommentDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqComment;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Comment;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.mapper.CommentMapper;
import com.instaJava.instaJava.service.CommentService;
import com.instaJava.instaJava.service.NotificationService;
import com.instaJava.instaJava.service.PublicatedImageService;
import com.instaJava.instaJava.util.MessagesUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentApplicationImpl implements CommentApplication{

	private final CommentService commentService;
	private final NotificationService notiService;
	private final PublicatedImageService pImageService;
	private final CommentMapper cMapper;
	private final MessagesUtils messUtils;
	
	@Override
	public CommentDto save(ReqComment reqComment) {
		if(reqComment == null ||reqComment.getPublImgId() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("generic.arg-not-null"));
		}
		Comment commentSaved;
		//publicatedImage exists?
		PublicatedImage pImaged = pImageService.getById(Long.parseLong(reqComment.getPublImgId()));
		//saving comment.
		commentSaved = commentService.save(reqComment.getBody(), reqComment.getParentId(), pImaged);
		// sending notification of new comment.
		notiService.saveNotificationOfComment(commentSaved, "Tienes un nuevo comentario en una publicacion.");
		return cMapper.commentToCommentDto(commentSaved);
	}

	@Override
	public ResPaginationG<CommentDto> getRootCommentsByPublicationImageId(Long publicationImageId, int pageNo,
			int pageSize, String sortField, Direction sortDir) {
		Page<Comment> pageComment;
		PageInfoDto pageInfoDto = new PageInfoDto(pageNo, pageSize, 0, 0, sortField, sortDir);
		pageComment = commentService.getRootCommentsByAssociatedImgId(publicationImageId, pageInfoDto);
		return cMapper.pageAndPageInfoDtoToResPaginationG(pageComment, pageInfoDto);
	}

	@Override
	public ResPaginationG<CommentDto> getAssociatedCommentsByParentCommentId(Long parentId, int pageNo, int pageSize,
			String sortField, Direction sortDir) {
		Page<Comment> pageComment;
		PageInfoDto pageInfoDto = new PageInfoDto(pageNo, pageSize, 0, 0, sortField, sortDir);
		pageComment = commentService.getAssociatedCommentsByParentCommentId(parentId, pageInfoDto);
		return cMapper.pageAndPageInfoDtoToResPaginationG(pageComment, pageInfoDto);
	}

	@Override
	public CommentDto deleteById(Long commentId) {
		Comment commentDeleted = commentService.deleteById(commentId);
		return cMapper.commentToCommentDto(commentDeleted);
	}

	@Override
	public CommentDto updateBodyById(Long commentId, String newCommentBOdy) {
		Comment updatedComment = commentService.updateById(commentId, newCommentBOdy);
		return cMapper.commentToCommentDto(updatedComment);
	}

}
