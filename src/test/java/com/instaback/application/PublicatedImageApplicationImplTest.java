package com.instaback.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.instaback.dto.CommentDto;
import com.instaback.dto.PageInfoDto;
import com.instaback.dto.response.PublicatedImageDto;
import com.instaback.dto.response.ResPaginationG;
import com.instaback.entity.Comment;
import com.instaback.entity.PublicatedImage;
import com.instaback.entity.User;
import com.instaback.enums.FollowStatus;
import com.instaback.exception.InvalidActionException;
import com.instaback.mapper.CommentMapper;
import com.instaback.mapper.PublicatedImageMapper;
import com.instaback.service.CommentService;
import com.instaback.service.FollowService;
import com.instaback.service.LikeService;
import com.instaback.service.PublicatedImageService;
import com.instaback.service.UserService;
import com.instaback.util.MessagesUtils;

@ExtendWith(MockitoExtension.class)
class PublicatedImageApplicationImplTest {

	@Mock
	private Authentication auth;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private PublicatedImageService pImageService;
	@Mock
	private FollowService fService;
	@Mock
	private CommentService cService;
	@Mock
	private UserService uService;
	@Mock
	private LikeService lService;
	@Mock
	private PublicatedImageMapper pImageMapper;
	@Mock
	private CommentMapper cMapper;
	@Mock
	private MessagesUtils messUtils;
	@InjectMocks
	private PublicatedImageApplicationImpl pImageApplication;

	// save
	@Test
	void saveReturnNotNull() {
		String description = "random";
		PublicatedImage pSaved = new PublicatedImage();
		MockMultipartFile img = new MockMultipartFile("img", "hello.txt", MediaType.IMAGE_JPEG_VALUE,
				"Hello, World!".getBytes());
		when(pImageService.save(description, img)).thenReturn(pSaved);
		when(pImageMapper.publicatedImageToPublicatedImageDto(pSaved)).thenReturn(new PublicatedImageDto());

		assertNotNull(pImageApplication.save(description, img));
	}

	// getById
	@Test
	void getByIdAuthUserNotOwnerAndOwnerNotVisibleFollowStatusNotAskedThrow() {
		User owner = User.builder().id(5L).visible(false).build();
		User authUser = new User(10L);
		PublicatedImage p = PublicatedImage.builder().id(1L).userOwner(owner).build();

		when(pImageService.getById(p.getId())).thenReturn(p);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		when(fService.getFollowStatusByFollowedId(owner.getId())).thenReturn(FollowStatus.NOT_ASKED);

		assertThrows(InvalidActionException.class, () -> pImageApplication.getById(p.getId(), 0, 0, null, null));

		verify(lService, never()).setItemDecision(p);
		verify(lService, never()).getLikesNumberByItemIdAndDecision(p.getId(), true);
		verify(lService, never()).getLikesNumberByItemIdAndDecision(p.getId(), false);
		verify(cService, never()).getRootCommentsByAssociatedImgId(eq(p.getId()), any(PageInfoDto.class));
	}

	@Test
	void getByIdAuthUserNotOwnerAndOwnerNotVisibleFollowStatusInProcessThrow() {
		User owner = User.builder().id(5L).visible(false).build();
		User authUser = new User(10L);
		PublicatedImage p = PublicatedImage.builder().id(1L).userOwner(owner).build();

		when(pImageService.getById(p.getId())).thenReturn(p);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		when(fService.getFollowStatusByFollowedId(owner.getId())).thenReturn(FollowStatus.IN_PROCESS);

		assertThrows(InvalidActionException.class, () -> pImageApplication.getById(p.getId(), 0, 0, null, null));

		verify(lService, never()).setItemDecision(p);
		verify(lService, never()).getLikesNumberByItemIdAndDecision(p.getId(), true);
		verify(lService, never()).getLikesNumberByItemIdAndDecision(p.getId(), false);
		verify(cService, never()).getRootCommentsByAssociatedImgId(eq(p.getId()), any(PageInfoDto.class));
	}

	@Test
	void getByIdAuthUserNotOwnerAndOwnerNotVisibleFollowStatusRejectedThrow() {
		User owner = User.builder().id(5L).visible(false).build();
		User authUser = new User(10L);
		PublicatedImage p = PublicatedImage.builder().id(1L).userOwner(owner).build();

		when(pImageService.getById(p.getId())).thenReturn(p);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		when(fService.getFollowStatusByFollowedId(owner.getId())).thenReturn(FollowStatus.REJECTED);

		assertThrows(InvalidActionException.class, () -> pImageApplication.getById(p.getId(), 0, 0, null, null));

		verify(lService, never()).setItemDecision(p);
		verify(lService, never()).getLikesNumberByItemIdAndDecision(p.getId(), true);
		verify(lService, never()).getLikesNumberByItemIdAndDecision(p.getId(), false);
		verify(cService, never()).getRootCommentsByAssociatedImgId(eq(p.getId()), any(PageInfoDto.class));
	}

	@Test
	void getByIdAuthUserNotOwnerAndOwnerNotVisibleFollowStatusAccepted() {
		User owner = User.builder().id(5L).visible(false).build();
		User authUser = new User(10L);
		PublicatedImage p = PublicatedImage.builder().id(1L).userOwner(owner).build();
		Page<Comment> pageComment = Page.empty();
		PublicatedImageDto pDto = new PublicatedImageDto();
		ResPaginationG<CommentDto> res = new ResPaginationG<>();

		when(pImageService.getById(p.getId())).thenReturn(p);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		when(fService.getFollowStatusByFollowedId(owner.getId())).thenReturn(FollowStatus.ACCEPTED);
		// getting likes number
		when(lService.getLikesNumberByItemIdAndDecision(p.getId(), true)).thenReturn(1L);
		when(lService.getLikesNumberByItemIdAndDecision(p.getId(), false)).thenReturn(1L);
		// getting comments
		when(cService.getRootCommentsByAssociatedImgId(eq(p.getId()), any(PageInfoDto.class))).thenReturn(pageComment);
		// mapping
		when(pImageMapper.publicatedImageToPublicatedImageDto(p)).thenReturn(pDto);
		when(cMapper.pageAndPageInfoDtoToResPaginationG(eq(pageComment), any(PageInfoDto.class))).thenReturn(res);

		assertNotNull(pImageApplication.getById(p.getId(), 0, 0, null, null));

		verify(lService).setItemDecision(p);
	}

	@Test
	void getByIdAuthUserNotOwnerAndOwnerVisibleFollowStatusAcceptedReturnNotNull() {
		User owner = User.builder().id(5L).visible(true).build();
		User authUser = new User(10L);
		PublicatedImage p = PublicatedImage.builder().id(1L).userOwner(owner).build();
		Page<Comment> pageComment = Page.empty();
		PublicatedImageDto pDto = new PublicatedImageDto();
		ResPaginationG<CommentDto> res = new ResPaginationG<>();

		when(pImageService.getById(p.getId())).thenReturn(p);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		when(fService.getFollowStatusByFollowedId(owner.getId())).thenReturn(FollowStatus.ACCEPTED);
		// getting likes number
		when(lService.getLikesNumberByItemIdAndDecision(p.getId(), true)).thenReturn(1L);
		when(lService.getLikesNumberByItemIdAndDecision(p.getId(), false)).thenReturn(1L);
		when(cService.getRootCommentsByAssociatedImgId(eq(p.getId()), any(PageInfoDto.class))).thenReturn(pageComment);
		when(pImageMapper.publicatedImageToPublicatedImageDto(p)).thenReturn(pDto);
		when(cMapper.pageAndPageInfoDtoToResPaginationG(eq(pageComment), any(PageInfoDto.class))).thenReturn(res);

		assertNotNull(pImageApplication.getById(p.getId(), 0, 0, null, null));

		verify(lService).setItemDecision(p);
	}

	@Test
	void getByIdAuthUserIsOwnerReturnNotNull() {
		User authUser = new User(10L);
		PublicatedImage p = PublicatedImage.builder().id(1L).userOwner(authUser).build();
		Page<Comment> pageComment = Page.empty();
		PublicatedImageDto pDto = new PublicatedImageDto();
		ResPaginationG<CommentDto> res = new ResPaginationG<>();

		when(pImageService.getById(p.getId())).thenReturn(p);
		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		// getting likes number
		when(lService.getLikesNumberByItemIdAndDecision(p.getId(), true)).thenReturn(1L);
		when(lService.getLikesNumberByItemIdAndDecision(p.getId(), false)).thenReturn(1L);
		when(cService.getRootCommentsByAssociatedImgId(eq(p.getId()), any(PageInfoDto.class))).thenReturn(pageComment);
		when(pImageMapper.publicatedImageToPublicatedImageDto(p)).thenReturn(pDto);
		when(cMapper.pageAndPageInfoDtoToResPaginationG(eq(pageComment), any(PageInfoDto.class))).thenReturn(res);

		assertNotNull(pImageApplication.getById(p.getId(), 0, 0, null, null));

		verify(lService).setItemDecision(p);
		verify(fService, never()).getFollowStatusByFollowedId(authUser.getId());
	}

	// getAllByOwnersVisibles
	@Test
	void getAllByOwnersVisiblesReturnNotNull() {
		List<PublicatedImage> listPublicatedImage = new ArrayList<>();
		Page<PublicatedImage> pagePImge = new PageImpl<>(listPublicatedImage);
		ResPaginationG<PublicatedImageDto> res = new ResPaginationG<>();

		when(pImageService.getAllByOwnerVisible(any(PageInfoDto.class))).thenReturn(pagePImge);
		when(pImageMapper.pageAndPageInfoDtoToResPaginationG(eq(pagePImge), any(PageInfoDto.class))).thenReturn(res);

		assertNotNull(pImageApplication.getAllByOwnersVisibles(0, 0, null, null));

		verify(lService).setItemDecisions(anyList());
	}

	// getAllByOnwerId
	@Test
	void getAllByOnwerIdNotAuthUserNotVisibleFollowStatusNotAskedThrow() {
		User owner = User.builder().id(1L).visible(false).build();
		User authUser = new User(10L);

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		when(uService.findById(owner.getId())).thenReturn(owner);
		when(fService.getFollowStatusByFollowedId(owner.getId())).thenReturn(FollowStatus.NOT_ASKED);

		assertThrows(InvalidActionException.class,
				() -> pImageApplication.getAllByOnwerId(owner.getId(), 0, 0, null, null));

		verify(pImageService, never()).getAllByOnwerId(eq(owner.getId()), any(PageInfoDto.class));
		verify(lService, never()).setItemDecisions(anyList());
	}

	@Test
	void getAllByOnwerIdNotAuthUserNotVisibleFollowStatusRejectedThrow() {
		User owner = User.builder().id(1L).visible(false).build();
		User authUser = new User(10L);

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		when(uService.findById(owner.getId())).thenReturn(owner);
		when(fService.getFollowStatusByFollowedId(owner.getId())).thenReturn(FollowStatus.REJECTED);

		assertThrows(InvalidActionException.class,
				() -> pImageApplication.getAllByOnwerId(owner.getId(), 0, 0, null, null));

		verify(pImageService, never()).getAllByOnwerId(eq(owner.getId()), any(PageInfoDto.class));
		verify(lService, never()).setItemDecisions(anyList());
	}

	@Test
	void getAllByOnwerIdNotAuthUserNotVisibleFollowStatusInProcessThrow() {
		User owner = User.builder().id(1L).visible(false).build();
		User authUser = new User(10L);

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		when(uService.findById(owner.getId())).thenReturn(owner);
		when(fService.getFollowStatusByFollowedId(owner.getId())).thenReturn(FollowStatus.IN_PROCESS);

		assertThrows(InvalidActionException.class,
				() -> pImageApplication.getAllByOnwerId(owner.getId(), 0, 0, null, null));

		verify(pImageService, never()).getAllByOnwerId(eq(owner.getId()), any(PageInfoDto.class));
		verify(lService, never()).setItemDecisions(anyList());
	}

	@Test
	void getAllByOnwerIdNotAuthUserNotVisibleFollowStatusAcceptedReturnNotNull() {
		User owner = User.builder().id(1L).visible(false).build();
		User authUser = new User(10L);
		List<PublicatedImage> listPublicatedImage = new ArrayList<>();
		Page<PublicatedImage> pImagePage = new PageImpl<>(listPublicatedImage);
		ResPaginationG<PublicatedImageDto> res = new ResPaginationG<>();

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		when(uService.findById(owner.getId())).thenReturn(owner);
		when(fService.getFollowStatusByFollowedId(owner.getId())).thenReturn(FollowStatus.ACCEPTED);
		when(pImageService.getAllByOnwerId(eq(owner.getId()), any(PageInfoDto.class))).thenReturn(pImagePage);
		when(pImageMapper.pageAndPageInfoDtoToResPaginationG(eq(pImagePage), any(PageInfoDto.class))).thenReturn(res);

		assertNotNull(pImageApplication.getAllByOnwerId(owner.getId(), 0, 0, null, null));

		verify(lService).setItemDecisions(anyList());
	}

	@Test
	void getAllByOnwerIdIsAuthUserReturnNotNull() {
		User authUser = new User(10L);
		List<PublicatedImage> listPublicatedImage = new ArrayList<>();
		Page<PublicatedImage> pImagePage = new PageImpl<>(listPublicatedImage);
		ResPaginationG<PublicatedImageDto> res = new ResPaginationG<>();

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		when(pImageService.getAllByOnwerId(eq(authUser.getId()), any(PageInfoDto.class))).thenReturn(pImagePage);
		when(pImageMapper.pageAndPageInfoDtoToResPaginationG(eq(pImagePage), any(PageInfoDto.class))).thenReturn(res);

		assertNotNull(pImageApplication.getAllByOnwerId(authUser.getId(), 0, 0, null, null));

		verify(lService).setItemDecisions(anyList());
	}

	@Test
	void getAllByOnwerIdNotAuthUserVisibleReturnNotNull() {
		User owner = User.builder().id(1L).visible(true).build();
		User authUser = new User(10L);
		List<PublicatedImage> listPublicatedImage = new ArrayList<>();
		Page<PublicatedImage> pImagePage = new PageImpl<>(listPublicatedImage);
		ResPaginationG<PublicatedImageDto> res = new ResPaginationG<>();

		when(securityContext.getAuthentication()).thenReturn(auth);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(authUser);
		when(uService.findById(owner.getId())).thenReturn(owner);
		when(pImageService.getAllByOnwerId(eq(owner.getId()), any(PageInfoDto.class))).thenReturn(pImagePage);
		when(pImageMapper.pageAndPageInfoDtoToResPaginationG(eq(pImagePage), any(PageInfoDto.class))).thenReturn(res);

		assertNotNull(pImageApplication.getAllByOnwerId(owner.getId(), 0, 0, null, null));

		verify(lService).setItemDecisions(anyList());
	}

	// getPublicationsFromUsersFollowed
	@Test
	void getPublicationsFromUsersFollowedReturnNotNull() {
		List<PublicatedImage> listP = new ArrayList<>();
		Page<PublicatedImage> pagePImages = new PageImpl<>(listP);
		ResPaginationG<PublicatedImageDto> res = new ResPaginationG<>();

		when(pImageService.getPublicationsFromUsersFollowed(any(PageInfoDto.class))).thenReturn(pagePImages);
		when(pImageMapper.pageAndPageInfoDtoToResPaginationG(eq(pagePImages), any(PageInfoDto.class))).thenReturn(res);

		assertNotNull(pImageApplication.getPublicationsFromUsersFollowed(0, 0, null, null));

		verify(lService).setItemDecisions(listP);

	}

}
