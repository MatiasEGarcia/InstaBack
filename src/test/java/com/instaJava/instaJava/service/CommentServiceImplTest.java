package com.instaJava.instaJava.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.instaJava.instaJava.dao.CommentDao;
import com.instaJava.instaJava.dto.CommentDto;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqComment;
import com.instaJava.instaJava.dto.response.ResPaginationG;
import com.instaJava.instaJava.entity.Comment;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.RolesEnum;
import com.instaJava.instaJava.exception.InvalidActionException;
import com.instaJava.instaJava.exception.RecordNotFoundException;
import com.instaJava.instaJava.mapper.CommentMapper;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

	@Mock
	private Authentication auth;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private CommentDao commentDao;
	@Mock
	private MessagesUtils messUtils;
	@Mock
	private PageableUtils pagUtils;
	@Mock
	private CommentMapper commentMapper;
	@Mock
	private NotificationService notiService;
	@Mock
	private PublicatedImageService publicatedImageService;
	@Mock
	private Clock clock;
	@InjectMocks
	private CommentServiceImpl commentService;

	// auth user.
	private final User user = User.builder().userId(1L).username("Mati").role(RolesEnum.ROLE_USER).build();

	// Save
	@Test
	void saveParamReqCommentBodyNullthrow() {
		ReqComment reqComment = ReqComment.builder().publImgId("1").build();
		assertThrows(IllegalArgumentException.class, () -> commentService.save(reqComment));
	}

	@Test
	void saveParamReqCommentBodyBlankthrow() {
		ReqComment reqComment = ReqComment.builder().publImgId("1").body("").build();
		assertThrows(IllegalArgumentException.class, () -> commentService.save(reqComment));
	}

	@Test
	void saveParamReqCommentPublImgIdNullthrow() {
		ReqComment reqComment = ReqComment.builder().body("random").build();
		assertThrows(IllegalArgumentException.class, () -> commentService.save(reqComment));
	}

	@Test
	void saveParamReqCommentPublImgIdBlankthrow() {
		ReqComment reqComment = ReqComment.builder().publImgId("").body("random").build();
		assertThrows(IllegalArgumentException.class, () -> commentService.save(reqComment));
	}

	@Test
	void savePublicatedImageNoExistsThrow() {
		ReqComment reqComment = ReqComment.builder().publImgId("1").body("random").build();
		when(publicatedImageService.findById(1L)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> commentService.save(reqComment));

	}

	@Test
	void saveWithParentNotFoundThrow() {
		ReqComment reqComment = ReqComment.builder().publImgId("1").body("random").parentId("2").build();

		PublicatedImage p = new PublicatedImage();
		Comment commentSaved = new Comment();

		// service
		when(publicatedImageService.findById(1L)).thenReturn(Optional.of(p));
		// dao search of parent comment
		when(commentDao.findById(anyLong())).thenReturn(Optional.empty());

		assertThrows(RecordNotFoundException.class, () -> commentService.save(reqComment));

		verify(commentDao, never()).save(any(Comment.class));
		verify(notiService, never()).saveNotificationOfComment(eq(commentSaved), anyString());
	}

	@Test
	void saveWithParentReturnNotNull() {
		ReqComment reqComment = ReqComment.builder().publImgId("1").body("random").parentId("2").build();

		PublicatedImage p = new PublicatedImage();
		Comment commentSaved = new Comment();
		CommentDto commentDto = new CommentDto();

		// service
		when(publicatedImageService.findById(1L)).thenReturn(Optional.of(p));
		// dao search of parent comment
		when(commentDao.findById(anyLong())).thenReturn(Optional.of(new Comment()));
		// clock
		when(clock.getZone()).thenReturn(ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(Instant.parse("2020-12-01T10:05:23.653Z"));
		// auth
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// dao
		when(commentDao.save(any(Comment.class))).thenReturn(commentSaved);
		// mapping
		when(commentMapper.commentToCommentDto(commentSaved)).thenReturn(commentDto);

		assertNotNull(commentService.save(reqComment));

		verify(notiService).saveNotificationOfComment(eq(commentSaved), anyString());
	}

	@Test
	void saveWhitoutParentReturnsNotNull() {
		ReqComment reqComment = ReqComment.builder().publImgId("1").body("random").build();
		PublicatedImage p = new PublicatedImage();
		Comment commentSaved = new Comment();
		CommentDto commentDto = new CommentDto();

		// service
		when(publicatedImageService.findById(1L)).thenReturn(Optional.of(p));
		// clock
		when(clock.getZone()).thenReturn(ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(Instant.parse("2020-12-01T10:05:23.653Z"));
		// auth
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// dao
		when(commentDao.save(any(Comment.class))).thenReturn(commentSaved);
		// mapping
		when(commentMapper.commentToCommentDto(commentSaved)).thenReturn(commentDto);

		assertNotNull(commentService.save(reqComment));

		verify(notiService).saveNotificationOfComment(eq(commentSaved), anyString());
		verify(commentDao, never()).findById(anyLong());
	}

	// getCommentsByPublicationImageId
	@Test
	void getCommentsByPublicationImageIdParamPublicationImageIdNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("random").sortDir(Direction.ASC).build();
		assertThrows(IllegalArgumentException.class,
				() -> commentService.getRootCommentsByPublicationImageId(null, pageInfoDto));
	}

	@Test
	void getCommentsByPublicationImageIdParamPageInfoDtoNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> commentService.getRootCommentsByPublicationImageId(1L, null));
	}

	@Test
	void getCommentsByPublicationImageIdParamPageInfoDtoSortFieldNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortDir(Direction.ASC).build();
		assertThrows(IllegalArgumentException.class,
				() -> commentService.getRootCommentsByPublicationImageId(1L, pageInfoDto));
	}

	@Test
	void getCommentsByPublicationImageIdParamPageInfoDtoSortDirThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("random").build();
		assertThrows(IllegalArgumentException.class,
				() -> commentService.getRootCommentsByPublicationImageId(1L, pageInfoDto));
	}

	@Test
	void getCommentsByPublicationImageIdNoContentThrow() {
		Long publicationImageId = 1L;
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("random").sortDir(Direction.ASC).build();

		// pageUtils
		when(pagUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		// dao
		when(commentDao.getRootCommentsByAssociatedImage(eq(publicationImageId), any(Pageable.class)))
				.thenReturn(Page.empty());

		assertThrows(RecordNotFoundException.class,
				() -> commentService.getRootCommentsByPublicationImageId(publicationImageId, pageInfoDto));
		verify(commentMapper, never()).commentToCommentDto(any(Comment.class));
	}

	@Test
	void getCommentsByPublicationImageIdReturnsNotNull() {
		Long publicationImageId = 1L;
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("random").sortDir(Direction.ASC).build();
		Comment comment = new Comment();
		Page<Comment> page = new PageImpl<>(List.of(comment));
		ResPaginationG<CommentDto> resPaginationG = new ResPaginationG<>();

		when(pagUtils.getPageable(pageInfoDto)).thenReturn(Pageable.unpaged());
		when(commentDao.getRootCommentsByAssociatedImage(eq(publicationImageId), any(Pageable.class))).thenReturn(page);
		when(commentMapper.pageAndPageInfoDtoToResPaginationG(page, pageInfoDto)).thenReturn(resPaginationG);

		assertNotNull(commentService.getRootCommentsByPublicationImageId(publicationImageId, pageInfoDto));
	}

	// getAssociatedCommentsByParentCommentid
	@Test
	void getAssociatedCommentsByParentCommentIdParamParentIdNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("random").sortDir(Direction.ASC).build();
		assertThrows(IllegalArgumentException.class,
				() -> commentService.getAssociatedCommentsByParentCommentId(null, pageInfoDto));
	}

	@Test
	void getAssociatedCommentsByParentCommentIdParamPageInfoDtoNullThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> commentService.getAssociatedCommentsByParentCommentId(1L, null));
	}

	@Test
	void getAssociatedCommentsByParentCommentIdParamPageInfoDtoSortFieldNullThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortDir(Direction.ASC).build();
		assertThrows(IllegalArgumentException.class,
				() -> commentService.getAssociatedCommentsByParentCommentId(1L, pageInfoDto));
	}

	@Test
	void getAssociatedCommentsByParentCommentIdParamPageInfoDtoSortDirThrow() {
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("random").build();
		assertThrows(IllegalArgumentException.class,
				() -> commentService.getAssociatedCommentsByParentCommentId(1L, pageInfoDto));
	}

	@Test
	void getAssociatedCommentsByParentCommentIdCommentNotFoundThrow() {
		Long parentId = 1L;
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("random").sortDir(Direction.ASC).build();
		Pageable pageable = Pageable.unpaged();
		Page<Comment> page = Page.empty();

		// dao
		when(commentDao.findById(parentId)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class,
				() -> commentService.getAssociatedCommentsByParentCommentId(parentId, pageInfoDto));
		verify(commentDao, never()).findByParent(any(Comment.class), eq(pageable));
		verify(commentMapper, never()).pageAndPageInfoDtoToResPaginationG(page, pageInfoDto);
	}

	@Test
	void getAssociatedCommentsByParentCommentIdNoContentThrow() {
		Long parentId = 1L;
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("random").sortDir(Direction.ASC).build();
		Pageable pageable = Pageable.unpaged();
		Page<Comment> page = Page.empty();
		Comment comment = new Comment();

		// dao
		when(commentDao.findById(parentId)).thenReturn(Optional.of(comment));
		// pagUtils
		when(pagUtils.getPageable(pageInfoDto)).thenReturn(pageable);
		// dao
		when(commentDao.findByParent(comment, pageable)).thenReturn(page);
		assertThrows(RecordNotFoundException.class,
				() -> commentService.getAssociatedCommentsByParentCommentId(parentId, pageInfoDto));
		verify(commentMapper, never()).pageAndPageInfoDtoToResPaginationG(page, pageInfoDto);
	}

	@Test
	void getAssociatedCommentsByParentCommentIdReturnsnotNull() {
		Long parentId = 1L;
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("random").sortDir(Direction.ASC).build();
		Pageable pageable = Pageable.unpaged();
		Comment comment = new Comment();
		Page<Comment> page = new PageImpl<Comment>(List.of(comment));
		ResPaginationG<CommentDto> resPaginationG = new ResPaginationG<>();

		// dao
		when(commentDao.findById(parentId)).thenReturn(Optional.of(comment));
		// pagUtils
		when(pagUtils.getPageable(pageInfoDto)).thenReturn(pageable);
		// dao
		when(commentDao.findByParent(comment, pageable)).thenReturn(page);
		// mapper
		when(commentMapper.pageAndPageInfoDtoToResPaginationG(page, pageInfoDto)).thenReturn(resPaginationG);
		assertNotNull(commentService.getAssociatedCommentsByParentCommentId(parentId, pageInfoDto));
	}

	// delete
	@Test
	void deleteParamCommentIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> commentService.deleteById(null));
	}

	@Test
	void deleteCommentNotFoundThrow() {
		Long commentId = 1L;
		when(commentDao.findById(commentId)).thenReturn(Optional.empty());

		assertThrows(RecordNotFoundException.class, () -> commentService.deleteById(commentId));

		verify(commentDao, never()).deleteById(commentId);
	}

	@Test
	void deleteCommentAuthUserNoOwnerThrow() {
		Comment comment = Comment.builder().commentId(1L).ownerUser(new User()) // different user than auht user.
				.build();

		// find comment
		when(commentDao.findById(comment.getCommentId())).thenReturn(Optional.of(comment));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		assertThrows(InvalidActionException.class, () -> commentService.deleteById(comment.getCommentId()));

		verify(commentDao, never()).deleteById(comment.getCommentId());
	}

	@Test
	void delete() {
		ZonedDateTime commentCreationDate = ZonedDateTime.parse("2020-12-01T10:04:23.653Z[Europe/Prague]");

		Comment comment = Comment.builder().commentId(1L).ownerUser(user) // same user than auht user.
				.createdAt(commentCreationDate).build();

		// find comment
		when(commentDao.findById(comment.getCommentId())).thenReturn(Optional.of(comment));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);
		// clock
		when(clock.getZone()).thenReturn(ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(Instant.parse("2020-12-01T10:05:23.653Z"));// just a 1 min after creation time

		commentService.deleteById(comment.getCommentId());

		verify(commentDao).deleteById(comment.getCommentId());
	}

}
