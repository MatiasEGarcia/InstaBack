package com.instaJava.instaJava.service;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.instaJava.instaJava.dao.PublicatedImagesDao;
import com.instaJava.instaJava.dto.PageInfoDto;
import com.instaJava.instaJava.dto.request.ReqSearch;
import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;
import com.instaJava.instaJava.enums.FollowStatus;
import com.instaJava.instaJava.enums.OperationEnum;
import com.instaJava.instaJava.exception.IllegalActionException;
import com.instaJava.instaJava.exception.ImageException;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicatedImagesServiceImpl implements PublicatedImageService {

	private final Clock clock;
	private final PublicatedImagesDao publicatedImagesDao;
	private final MessagesUtils messUtils;
	private final SpecificationService<PublicatedImage> specService;
	private final PageableUtils pagUtils;
	private final FollowService followService;
	private final UserService userService;

	
	@Override
	@Transactional
	public PublicatedImage save(String description, MultipartFile file) {
		if (file == null || file.isEmpty())
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null-empty"));
		PublicatedImage publicatedImage;
		try {
			publicatedImage = PublicatedImage.builder().description(description)
					.image(Base64.getEncoder().encodeToString(file.getBytes()))
					.userOwner((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
					.createdAt(ZonedDateTime.now(clock)).build();
		} catch (Exception e) {
			throw new ImageException(e);
		}

		return publicatedImagesDao.save(publicatedImage);
	}

	
	@Override
	@Transactional
	public void deleteById(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		User authUser;
		Optional<PublicatedImage> optPublImage = publicatedImagesDao.findById(id);
		if (optPublImage.isEmpty())
			return;
		authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!optPublImage.get().getUserOwner().equals(authUser))
			throw new IllegalActionException(messUtils.getMessage("exception.owner-not-same"));
		publicatedImagesDao.deleteById(id);
	}

	
	@Override
	@Transactional(readOnly = true)
	public Optional<PublicatedImage> getById(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		return publicatedImagesDao.findById(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<PublicatedImage> getAllByOwnersVisibles(PageInfoDto pageInfoDto) {
		if (pageInfoDto == null || pageInfoDto.getSortDir() == null || pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		}
		ReqSearch reqSearchuserOwnersVisibleTrue = ReqSearch.builder().column("visible").value("true")
				.joinTable("userOwner").operation(OperationEnum.IS_TRUE).build();
		return publicatedImagesDao.findAll(specService.getSpecification(reqSearchuserOwnersVisibleTrue),
				pagUtils.getPageable(pageInfoDto));
	}

	
	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> getAllByOnwer(Long ownerId, PageInfoDto pageInfoDto) {
		if (ownerId == null || pageInfoDto == null || pageInfoDto.getSortDir() == null
				|| pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		}
		Page<PublicatedImage> publicatedImagePage;
		Optional<User> ownerUser;
		Map<String, Object> mapp = new HashMap<>();
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (authUser.getUserId() != ownerId) {
			ownerUser = userService.getById(ownerId);
			if (ownerUser.isEmpty())
				throw new EntityNotFoundException(messUtils.getMessage("excepcion.record-by-id-not-found"));
			//if the owner user is not visible we need to check the followStatus
			if (!ownerUser.get().isVisible()) {
				// in case that the publications requests are not from the authenticated user.
				FollowStatus followStatus = followService.getFollowStatusByFollowedId(ownerId);
				switch (followStatus) {
				case NOT_ASKED:
					mapp.put("moreInfo", messUtils.getMessage("mess.followStatus-not-asked"));
					return mapp;
				case IN_PROCESS:
					mapp.put("moreInfo", messUtils.getMessage("mess.followStatus-in-process"));
					return mapp;
				case REJECTED:
					mapp.put("moreInfo", messUtils.getMessage("mess.followStatus-rejected"));
					return mapp;
				case ACCEPTED:
					break;
				default:
					throw new IllegalArgumentException("Unexpected value: " + followStatus);
				}
			}

		}

		// if owner is the same than the user is auth then we return publications, and
		// if the user is visible or follow status is Accepted too.
		ReqSearch reSearchUserOwnerIdEqual = ReqSearch.builder().column("userId").dateValue(false)
				.value(ownerId.toString()).joinTable("userOwner").operation(OperationEnum.EQUAL).build();
		publicatedImagePage = publicatedImagesDao.findAll(specService.getSpecification(reSearchUserOwnerIdEqual),
				pagUtils.getPageable(pageInfoDto));
		mapp.put("publications", publicatedImagePage);
		return mapp;
	}

	@Override
	@Transactional(readOnly=true)
	public Long countPublicationsByOwnerId(Long id) {
		if(id == null) throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		ReqSearch reqSearchOwnerUserIdEqual = ReqSearch.builder().column("userId").dateValue(false)
				.value(id.toString()).joinTable("userOwner").operation(OperationEnum.EQUAL).build();
		return publicatedImagesDao.count(specService.getSpecification(reqSearchOwnerUserIdEqual));
	}

}
