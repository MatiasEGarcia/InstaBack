package com.instaback.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

import com.instaback.dao.CommentDao;
import com.instaback.dto.PageInfoDto;
import com.instaback.entity.Comment;
import com.instaback.entity.PublicatedImage;
import com.instaback.entity.User;
import com.instaback.enums.RolesEnum;
import com.instaback.exception.InvalidActionException;
import com.instaback.exception.RecordNotFoundException;
import com.instaback.mapper.CommentMapper;
import com.instaback.util.MessagesUtils;
import com.instaback.util.PageableUtils;

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
	private final User user = User.builder().id(1L).username("Mati").role(RolesEnum.ROLE_USER).build();

	// Save
	@Test
	void saveBodyNullThrow() {
		String body = null;
		PublicatedImage pImage = new PublicatedImage(1L);

		assertThrows(IllegalArgumentException.class, () -> commentService.save(body, null, pImage));
	}

	@Test
	void savepBodyBlankThrow() {
		String body = "";
		PublicatedImage pImage = new PublicatedImage(1L);

		assertThrows(IllegalArgumentException.class, () -> commentService.save(body, null, pImage));
	}

	@Test
	void savePImageThrow() {
		String body = "random";
		PublicatedImage pImage = null;

		assertThrows(IllegalArgumentException.class, () -> commentService.save(body, null, pImage));
	}

	@Test
	void savePImageWithoutIdThrow() {
		String body = "random";
		PublicatedImage pImage = new PublicatedImage();

		assertThrows(IllegalArgumentException.class, () -> commentService.save(body, null, pImage));
	}

	@Test
	void saveParentNotFoundThrow() {
		String body = "random";
		String parentId = "1";
		PublicatedImage pImage = new PublicatedImage(1L);

		when(commentDao.findById(1L)).thenReturn(Optional.empty());

		assertThrows(RecordNotFoundException.class, () -> commentService.save(body, parentId, pImage));
	}

	@Test
	void saveReturnNotNull() {
		String body = "random";
		String parentId = "1";
		PublicatedImage pImage = new PublicatedImage(1L);
		Comment parentComment = new Comment();
		Comment commentSaved = new Comment();

		when(commentDao.findById(1L)).thenReturn(Optional.of(parentComment));
		// clock
		when(clock.getZone()).thenReturn(ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(Instant.parse("2020-12-01T10:05:23.653Z"));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		when(commentDao.save(any(Comment.class))).thenReturn(commentSaved);
		assertNotNull(commentService.save(body, parentId, pImage));
	}

	@Test
	void getRootCommentsByAssociatedImgIdParamAssociatedImgIdNullThrow() {
		Long associatedImgId = null;
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortDir(Direction.ASC).sortField("random").build();

		assertThrows(IllegalArgumentException.class,
				() -> commentService.getRootCommentsByAssociatedImgId(associatedImgId, pageInfoDto));
		verify(commentDao, never()).getRootCommentsByAssociatedImage(eq(null), any(Pageable.class));
	}

	@Test
	void getRootCommentsByAssociatedImgIdParamPageInfoDtoSortDirNullThrow() {
		Long associatedImgId = 1L;
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("random").build();

		assertThrows(IllegalArgumentException.class,
				() -> commentService.getRootCommentsByAssociatedImgId(associatedImgId, pageInfoDto));
		verify(commentDao, never()).getRootCommentsByAssociatedImage(eq(1L), any(Pageable.class));
	}

	@Test
	void getRootCommentsByAssociatedImgIdParamPageInfoDtoSortFieldNullThrow() {
		Long associatedImgId = 1L;
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortDir(Direction.ASC).build();

		assertThrows(IllegalArgumentException.class,
				() -> commentService.getRootCommentsByAssociatedImgId(associatedImgId, pageInfoDto));
		verify(commentDao, never()).getRootCommentsByAssociatedImage(eq(1L), any(Pageable.class));
	}

	@Test
	void getRootCommentsByAssociatedImgIdNoContentThrow() {
		Long associatedImgId = 1L;
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("random").sortDir(Direction.ASC).build();

		when(pagUtils.getPageable(any(PageInfoDto.class))).thenReturn(Pageable.unpaged());
		when(commentDao.getRootCommentsByAssociatedImage(eq(associatedImgId), any(Pageable.class)))
				.thenReturn(Page.empty());

		assertThrows(RecordNotFoundException.class,
				() -> commentService.getRootCommentsByAssociatedImgId(associatedImgId, pageInfoDto));
	}

	@Test
	void getRootCommentsByAssociatedImgIdReturnNotNull() {
		Long associatedImgId = 1L;
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("random").sortDir(Direction.ASC).build();
		Comment comment = new Comment();
		Page<Comment> pageComments = new PageImpl<>(List.of(comment));

		when(pagUtils.getPageable(any(PageInfoDto.class))).thenReturn(Pageable.unpaged());
		when(commentDao.getRootCommentsByAssociatedImage(eq(associatedImgId), any(Pageable.class)))
				.thenReturn(pageComments);

		assertNotNull(commentService.getRootCommentsByAssociatedImgId(associatedImgId, pageInfoDto));
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

		when(commentDao.findById(parentId)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class,
				() -> commentService.getAssociatedCommentsByParentCommentId(parentId, pageInfoDto));
		verify(commentDao, never()).findByParent(any(Comment.class), eq(pageable));
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
	}

	@Test
	void getAssociatedCommentsByParentCommentIdReturnsnotNull() {
		Long parentId = 1L;
		PageInfoDto pageInfoDto = PageInfoDto.builder().sortField("random").sortDir(Direction.ASC).build();
		Pageable pageable = Pageable.unpaged();
		Comment comment = new Comment();
		Page<Comment> page = new PageImpl<Comment>(List.of(comment));

		// dao
		when(commentDao.findById(parentId)).thenReturn(Optional.of(comment));
		// pagUtils
		when(pagUtils.getPageable(pageInfoDto)).thenReturn(pageable);
		// dao
		when(commentDao.findByParent(comment, pageable)).thenReturn(page);
		assertNotNull(commentService.getAssociatedCommentsByParentCommentId(parentId, pageInfoDto));
	}

	// deleteById

	@Test
	void deleteByIdParamCommentIdNullThrow() {
		assertThrows(IllegalArgumentException.class, () -> commentService.deleteById(null));
	}

	@Test
	void deleteByIdCommentNotFoundThrow() {
		Long commentId = 1L;
		when(commentDao.findById(commentId)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> commentService.deleteById(commentId));
		verify(commentDao, never()).deleteById(commentId);
	}

	@Test
	void deleteByIdCommentAuthUserNoOwnerThrow() {
		Long commentId = 1L;
		Comment comment = Comment.builder().id(1L).ownerUser(new User()).build();// no auth user

		when(commentDao.findById(commentId)).thenReturn(Optional.of(comment));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		assertThrows(InvalidActionException.class, () -> commentService.deleteById(commentId));
	}

	@Test
	void deleteByIdAfter5MinThrow() {
		Long commentId = 1L;
		ZonedDateTime commentCreationDate = ZonedDateTime.parse("2020-12-01T10:04:23.653Z[Europe/Prague]");
		Comment comment = Comment.builder().id(1L).ownerUser(user).createdAt(commentCreationDate).build();// no auth
																											// user

		when(commentDao.findById(commentId)).thenReturn(Optional.of(comment));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		when(clock.getZone()).thenReturn(ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(Instant.parse("2020-12-01T10:20:23.653Z"));// more than 5 minuts after comment
																					// creation

		assertThrows(InvalidActionException.class, () -> commentService.deleteById(commentId));

	}

	@Test
	void deleteByIReturnNotNull() {
		Long commentId = 1L;
		ZonedDateTime commentCreationDate = ZonedDateTime.parse("2020-12-01T10:04:23.653Z[Europe/Prague]");
		Comment comment = Comment.builder().id(1L).ownerUser(user).createdAt(commentCreationDate).build();// no auth
																											// user

		when(commentDao.findById(commentId)).thenReturn(Optional.of(comment));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		when(clock.getZone()).thenReturn(ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(Instant.parse("2020-12-01T10:05:23.653Z"));// just 1 minuts after comment
																					// creation

		assertNotNull(commentService.deleteById(commentId));
	}

	// updateById

	@Test
	void updateByIdParamCommentIdNullThrow() {
		Long commentId = null;
		String newCommentBody = "random";

		assertThrows(IllegalArgumentException.class, () -> commentService.updateById(commentId, newCommentBody));
	}

	@Test
	void updateByIdParamNewCommentBodyNullThrow() {
		Long commentId = 1L;
		String newCommentBody = null;

		assertThrows(IllegalArgumentException.class, () -> commentService.updateById(commentId, newCommentBody));
	}

	@Test
	void updateByIdCommentNotFoundThrow() {
		Long commentId = 1L;
		String newCommentBody = "random";
		when(commentDao.findById(commentId)).thenReturn(Optional.empty());
		assertThrows(RecordNotFoundException.class, () -> commentService.updateById(commentId, newCommentBody));
	}

	@Test
	void updateByIdOwnerNotSameThrow() {
		Long commentId = 1L;
		String newCommentBody = "random";
		Comment comment = Comment.builder().id(1L).ownerUser(new User()).build();// no auth user

		when(commentDao.findById(commentId)).thenReturn(Optional.of(comment));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		assertThrows(InvalidActionException.class, () -> commentService.updateById(commentId, newCommentBody));
	}

	@Test
	void updateByIdAfter5MinThrow() {
		Long commentId = 1L;
		String newCommentBody = "random";
		ZonedDateTime commentCreationDate = ZonedDateTime.parse("2020-12-01T10:04:23.653Z[Europe/Prague]");
		Comment comment = Comment.builder().id(1L).ownerUser(user).createdAt(commentCreationDate).build();// authUser

		when(commentDao.findById(commentId)).thenReturn(Optional.of(comment));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		when(clock.getZone()).thenReturn(ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(Instant.parse("2020-12-01T10:20:23.653Z"));// more than 5 minuts after comment
																					// creation

		assertThrows(InvalidActionException.class, () -> commentService.updateById(commentId, newCommentBody));
	}

	@Test
	void updateByIdReturnNotNull() {
		Long commentId = 1L;
		String newCommentBody = "random";
		ZonedDateTime commentCreationDate = ZonedDateTime.parse("2020-12-01T10:04:23.653Z[Europe/Prague]");
		Comment comment = Comment.builder().id(1L).ownerUser(user).createdAt(commentCreationDate).build();// authUser

		when(commentDao.findById(commentId)).thenReturn(Optional.of(comment));
		// auth user
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		when(clock.getZone()).thenReturn(ZoneId.of("Europe/Prague"));
		when(clock.instant()).thenReturn(Instant.parse("2020-12-01T10:05:23.653Z"));// just 1 minute after

		assertNotNull(commentService.updateById(commentId, newCommentBody));
	}
}
