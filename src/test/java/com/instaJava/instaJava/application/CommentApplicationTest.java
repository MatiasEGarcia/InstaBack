package com.instaJava.instaJava.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

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

@ExtendWith(MockitoExtension.class)
class CommentApplicationTest {

	@Mock private CommentService cService;
	@Mock private NotificationService nService;
	@Mock private PublicatedImageService pImageService;
	@Mock private CommentMapper cMapper;
	@Mock private MessagesUtils messUtils;
	@InjectMocks private CommentApplicationImpl commentApplication;
	
	//save
	@Test
	void saveReqCommentNullThrow() {
		assertThrows(IllegalArgumentException.class , () -> commentApplication.save(null));
	}
	
	@Test
	void saveReqCommentPublImgIdNullThrow() {
		ReqComment reqComment = new ReqComment();
		assertThrows(IllegalArgumentException.class , () -> commentApplication.save(reqComment));
	}
	
	@Test
	void saveReturnNotNull() {
		ReqComment reqComment = ReqComment.builder()
				.publImgId("1")
				.build();
		PublicatedImage pImaged = new PublicatedImage();
		Comment commentSaved = new Comment();
		
		when(pImageService.getById(1L)).thenReturn(pImaged);
		when(cService.save(null, null, pImaged)).thenReturn(commentSaved);
		when(cMapper.commentToCommentDto(commentSaved)).thenReturn(new CommentDto());
		
		assertNotNull(commentApplication.save(reqComment));
		
		verify(nService).saveNotificationOfComment(eq(commentSaved), anyString());
	}
	
	//getRootCommentsByPublicationImageId
	@Test
	void getRootCommentsByPublicationImageIdReturnNotNull() {
		Long publicationImageId = 1L;
		Page<Comment> pageComment = Page.empty();
		ResPaginationG<CommentDto> resP = new ResPaginationG<CommentDto>();
		when(cService.getRootCommentsByAssociatedImgId(eq(publicationImageId), any(PageInfoDto.class))).thenReturn(pageComment);
		when(cMapper.pageAndPageInfoDtoToResPaginationG(eq(pageComment), any(PageInfoDto.class))).thenReturn(resP);
		
		assertNotNull(commentApplication.getRootCommentsByPublicationImageId(publicationImageId, 0, 0, null, null));
	}
	
	//getAssociatedCommentsByParentCommentId
	@Test
	void getAssociatedCommentsByParentCommentIdReturnNotNull() {
		Long parentId = 1L;
		Page<Comment> pageComment = Page.empty();
		ResPaginationG<CommentDto> resP = new ResPaginationG<CommentDto>();
		when(cService.getAssociatedCommentsByParentCommentId(eq(parentId), any(PageInfoDto.class))).thenReturn(pageComment);
		when(cMapper.pageAndPageInfoDtoToResPaginationG(eq(pageComment), any(PageInfoDto.class))).thenReturn(resP);
		
		assertNotNull(commentApplication.getAssociatedCommentsByParentCommentId(parentId, 0, 0, null, null));
	}
	
	//deleteById
	@Test
	void deleteByIdReturnNotNull() {
		Long commentId = 1L;
		Comment commentDeleted = new Comment();
		when(cService.deleteById(commentId)).thenReturn(commentDeleted);
		when(cMapper.commentToCommentDto(commentDeleted)).thenReturn(new CommentDto());
		
		assertNotNull(commentApplication.deleteById(commentId));
	}
	
	//updateBodyById
	@Test
	void updateBodyById() {
		Long commentId = 1L;
		String commentBody ="random";
		Comment commentUpdated = new Comment();
		
		when(cService.updateById(commentId, commentBody)).thenReturn(commentUpdated);
		when(cMapper.commentToCommentDto(commentUpdated)).thenReturn(new CommentDto());
		
		assertNotNull(commentApplication.updateBodyById(commentId, commentBody));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
