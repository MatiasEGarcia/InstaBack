package com.instaJava.instaJava.service;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Base64;
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
import com.instaJava.instaJava.enums.OperationEnum;
import com.instaJava.instaJava.exception.IllegalActionException;
import com.instaJava.instaJava.exception.ImageException;
import com.instaJava.instaJava.util.MessagesUtils;
import com.instaJava.instaJava.util.PageableUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicatedImagesServiceImpl implements PublicatedImageService {

	private final Clock clock;
	private final PublicatedImagesDao publicatedImagesDao;
	private final MessagesUtils messUtils;
	private final SpecificationService<PublicatedImage> specService;
	private final PageableUtils pagUtils;

	/**
	 * Convert MuliparFile to Base64 and try to save a PublicatedImage in the database
	 * 
	 * @param description. Description of the image.
	 * @param file. Image to save.
	 * @return PublicatedImage record that was saved.
	 * @throws IllegalArgumentException if @param file is null or empty.
	 * @throws ImageException if there was an error when was tried to encode the image to Base64
	 */
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

	/**
	 * Delete a PublicatedImage record by its id(pubImaId). If PublicatedImage none record will be deleted.
	 * 
	 * @param id. is the pubImaId of the PublicatedImage record wanted to delete.
	 * @throws IllegalArgumentException if @param id is null.
	 * @throws IllegalActionException if the user authenticated is not the same owner of the PublicatedImage record.
	 */
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

	/**
	 * Find PublicatedImage by its id(pubImaId)
	 * 
	 * @return Optional of PublicatedImage
	 * @throws IllegalArgumentException if @param id is null.
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<PublicatedImage> getById(Long id) {
		if (id == null)
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		return publicatedImagesDao.findById(id);
	}
	
	/**
	 * Create a ReqSearch to get a specification object and 
	 * then search PublicatedImages records by User authenticated.
	 * 
	 * @param pageInfoDto. It has pagination info.
	 * @return Page of PublicatedImages.
	 * @throws IllegalArgumentException if PageInfoDto or pageInfoDto.getSortDir or pageInfoDto.sortField are null.
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<PublicatedImage> getAllByUser(PageInfoDto pageInfoDto){
		if (pageInfoDto == null || pageInfoDto.getSortDir() == null || pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		}
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		ReqSearch reqSearchPubliImaByOwnerEqual = ReqSearch.builder().column("userId")
				.dateValue(false).value(authUser.getUserId().toString()).joinTable("userOwner")
				.operation(OperationEnum.EQUAL).build();
		return publicatedImagesDao.findAll(specService.getSpecification(reqSearchPubliImaByOwnerEqual), pagUtils.getPageable(pageInfoDto));
	};

	/**
	 * Create a ReqSearch to get a specification object and then 
	 * search PublicatedImages records by User.visible = true.
	 * 
	 * @param pageInfoDto. It has pagination info.
	 * @return Page of PublicatedImages.
	 * @throws IllegalArgumentException if PageInfoDto or pageInfoDto.getSortDir or pageInfoDto.sortField are null.
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<PublicatedImage> getAllByOwnersVisibles(PageInfoDto pageInfoDto) {
		if (pageInfoDto == null || pageInfoDto.getSortDir() == null || pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		}
		ReqSearch reqSearchuserOwnersVisibleTrue = ReqSearch.builder().column("visible")
				.value("true").joinTable("userOwner").operation(OperationEnum.IS_TRUE).build();
		return publicatedImagesDao.findAll(specService.getSpecification(reqSearchuserOwnersVisibleTrue),pagUtils.getPageable(pageInfoDto));
	}

	/**
	 * Create a ReqSearch to get a specification object and then 
	 * search PublicatedImages records by ownerId.
	 * 
	 * @param pageInfoDto. It has pagination info.
	 * @param ownerId. Id of the owner who will have his PublicatedImages records fetched.
	 * @throws IllegalArgumentException if ownerId or PageInfoDto or pageInfoDto.getSortDir or pageInfoDto.sortField are null.
	 * @return Page of PublicatedImages.
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<PublicatedImage> getAllByOwnerId(PageInfoDto pageInfoDto, Long ownerId) {
		if (pageInfoDto == null || ownerId == null || pageInfoDto.getSortDir() == null || pageInfoDto.getSortField() == null) {
			throw new IllegalArgumentException(messUtils.getMessage("exception.argument-not-null"));
		}
		ReqSearch reqSearchuserOwnerIdEqual = ReqSearch.builder().column("userId").dateValue(false)
				.value(ownerId.toString()).joinTable("userOwner").operation(OperationEnum.EQUAL).build();
		return publicatedImagesDao.findAll(specService.getSpecification(reqSearchuserOwnerIdEqual),pagUtils.getPageable(pageInfoDto));
	}	
}
