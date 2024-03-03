package com.instaback.application;

import org.springframework.data.domain.Sort.Direction;

import com.instaback.dto.CommentDto;
import com.instaback.dto.request.ReqComment;
import com.instaback.dto.response.ResPaginationG;

public interface CommentApplication {

	/**
	 * To save a Comment record
	 * @param ReqComment - comment data transfer object
	 * @return a comment data transfer object with info of comment record saved.
	 */
	CommentDto save(ReqComment reqComment);
	
	/**
	 * TO get comments by publication id.
	 * @param publicationImageId publication's id.
	 * @param pageNo.    - For pagination, number of the page.
	 * @param pageSize.  - For pagination, size of the elements in the same page.
	 * @param sortField. - For pagination, sorted by..
	 * @param sortDir.   - In what direction is sorted, asc or desc.
	 * @return ResPagination with CommentDto objects(having comments record info)
	 */
	ResPaginationG<CommentDto> getRootCommentsByPublicationImageId(Long publicationImageId, int pageNo, int pageSize, String sortField, Direction sortDir);
	
	
	/**
	 * To get a paginated list of comments by parent id.
	 * 
	 * @param publicationImageId - to know from what publicated image search
	 * @param pageInfoDto - pagination info
	 * @return ResPaginationG of CommentDto with Comment records info and pagination info.
	 */
	ResPaginationG<CommentDto> getAssociatedCommentsByParentCommentId(Long parentId,int pageNo, int pageSize, String sortField, Direction sortDir);
	
	/**
	 * To delete comment by id.
	 * @param commentId - comments'id.
	 * returns Comment record deleted.
	 */
	CommentDto deleteById(Long commentId);
	
	/**
	 * Update comment's body by comment's id.
	 * @param commentId - comment's id.
	 * @param newCommentBOdy - new comment's body.
	 * @return
	 */
	CommentDto updateBodyById(Long commentId , String newCommentBOdy);
	
}
