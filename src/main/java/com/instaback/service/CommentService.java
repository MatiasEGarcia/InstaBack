package com.instaback.service;

import org.springframework.data.domain.Page;

import com.instaback.dto.PageInfoDto;
import com.instaback.entity.Comment;
import com.instaback.entity.PublicatedImage;
import com.instaback.exception.InvalidActionException;
import com.instaback.exception.RecordNotFoundException;

public interface CommentService {
	
	/**
	 * To save a Comment record
	 * @param body - comment's message
	 * @param parentId - comment's parent id. if there is.
	 * @param pImage - commnet's publicated image.
	 * @return a comment data transfer object with info of comment record saved.
	 * @throws RecordNotFoundException if publicated image no exists.
	 */
	Comment save(String body, String parentId, PublicatedImage pImage);
	
	/**
	 * To get a paginated list of comments by publicationImage Id.
	 * @param associatedImgId - to know from what publicated image search
	 * @param pageInfoDto - pagination info
	 * @return Page with comments and pagination info. If there is no comments then return a page with no content(empty list).
	 * @throws IllegalArgumentException if some required param is null.
	 * @throws RecordNotFoundException if no comment was found.
	 */
	Page<Comment> getRootCommentsByAssociatedImgId(Long associatedImgId, PageInfoDto pageInfoDto);
	
	/**
	 * To get a paginated list of comments by parent id
	 * 
	 * @param publicationImageId - to know from what publicated image search
	 * @param pageInfoDto - pagination info
	 * @return Page of Comment records info and pagination info.
	 * @throws IllegalArgumentException if some required param is null.
	 * @throws RecordNotFoundException if parent not found or no comment was found.
	 */
	Page<Comment> getAssociatedCommentsByParentCommentId(Long parentId,PageInfoDto pageInfoDto);
	
	/**
	 * To delete comment by id.
	 * @param commentId - comments'id.
	 * @return deleted comment.
	 * @throws IllegalArgumentException if commentId is null.
	 * @throws RecordNotFoundException if comment record was not found.
	 * @throws InvalidActionException if the auth user is not the owner of the comment or if 5 min has passed from the creation.
	 */
	Comment deleteById(Long commentId);
	
	/**
	 * To update comment by id
	 * @param commentId - comment's id
	 * @param newCommentBody - new comment body(content)
	 * @return updated comment.
	 * @throws IllegalArgumentException if commentId is null.
	 * @throws RecordNotFoundException if comment record was not found.
	 * @throws InvalidActionException if the auth user is not the owner of the comment or if 5 min has passed from the creation
	 */
	Comment updateById(Long commentId, String newCommentBody);
}
