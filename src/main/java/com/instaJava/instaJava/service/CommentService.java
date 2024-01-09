package com.instaJava.instaJava.service;

import com.instaJava.instaJava.dto.CommentDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqComment;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.exception.RecordNotFoundException;

public interface CommentService {
	
	/**
	 * To save a Comment record
	 * @param ReqComment - comment data transfer object
	 * @return a comment data transfer object with info of comment record saved.
	 * @throws RecordNotFoundException if publicated image no exists.
	 */
	CommentDto save(ReqComment reqComment);
	
	/**
	 * To get a paginated list of comments by publicationImage Id.
	 * 
	 * @param publicationImageId - to know from what publicated image search
	 * @param pageInfoDto - pagination info
	 * @return ResPaginationG of CommentDto with Comment records info and pagination info.
	 * @throws IllegalArgumentException if some required param is null.
	 * @throws RecordNotFoundException if no comment was found.
	 */
	ResPaginationG<CommentDto> getRootCommentsByPublicationImageId(Long publicationImageId,PageInfoDto pageInfoDto);
	
	/**
	 * To get a paginated list of comments by parent id
	 * 
	 * @param publicationImageId - to know from what publicated image search
	 * @param pageInfoDto - pagination info
	 * @return ResPaginationG of CommentDto with Comment records info and pagination info.
	 * @throws IllegalArgumentException if some required param is null.
	 * @throws RecordNotFoundException if no comment was found.
	 */
	ResPaginationG<CommentDto> getAssociatedCommentsByParentCommentId(Long parentId,PageInfoDto pageInfoDto);
	
	/**
	 * To delete comment by id.
	 * @param commentId - comments'id.
	 */
	void deleteById(Long commentId);
	
	/**
	 * To update comment by id
	 * @param commentId - comment's id
	 * @param newCommentBody - new comment body(content)
	 */
	CommentDto updateById(Long commentId, String newCommentBody);
	
}
