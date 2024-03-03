package com.instaback.application;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.multipart.MultipartFile;

import com.instaback.dto.response.PublicatedImageDto;
import com.instaback.dto.response.ResPaginationG;

public interface PublicatedImageApplication {

	/**
	 * Save a publicatedImage.
	 * @param Description - publicatedImage's description.
	 * @param file - publicatedImage's image.
	 * @return PublicatedImageDto from PublicatedImage record created.
	 */
	PublicatedImageDto save(String Description,MultipartFile file);
	
	/**
	 * Delete a publicatedImage by id
	 * @param publicatedImageId - publicatedImage's id
	 * @return PublicatedImageDto deleted.
	 */
	PublicatedImageDto deleteById(Long publicatedImageId);
	
	/**
	 * Get publicatedImage by id.
	 * @param id - publicatedImage'id
	 * @param pageNo.    - For comments pagination, number of the page.
	 * @param pageSize.  - For comments pagination, size of the elements in the same page.
	 * @param sortField. - For comments pagination, sorted by..
	 * @param sortDir.   - In what direction is sorted, asc or desc.
	 * @return Publicated image with its comments and if was liked by the auth user.
	 * @throws InvalidActionException if the owner is not visible and the follow status is not ACCEPTED.
	 */
	PublicatedImageDto getById(Long id, int pageNo, int pageSize, String sortField, Direction sortDir);
	
	/**
	 * PublicatedImages records by User.visible = true.
	 * @param pageNo.    - For pagination, number of the page.
	 * @param pageSize.  - For pagination, size of the elements in the same page.
	 * @param sortField. - For pagination, sorted by..
	 * @param sortDir.   - In what direction is sorted, asc or desc.
	 * @return ResPaginationG with publications and pagination info.
	 */
	ResPaginationG<PublicatedImageDto> getAllByOwnersVisibles(int pageNo, int pageSize, String sortField, Direction sortDir);
	
	/**
	 * PublicatedImages records by ownerId.
	 * @param ownerId - publicatedImage 's onwer's id.
	 * @param pageInfoDto - pagination info
	 * @return Page with PublicatedImage records.
	 * @throws InvalidActionException if the auth user cannot get the publications from some reason.
	 */
	ResPaginationG<PublicatedImageDto> getAllByOnwerId(Long ownerId,int pageNo, int pageSize, String sortField, Direction sortDir);

	/**
	 * get all publications where the owner user is followed by authenticated user.
	 * @param pageNo.    - For pagination, number of the page.
	 * @param pageSize.  - For pagination, size of the elements in the same page.
	 * @param sortField. - For pagination, sorted by..
	 * @param sortDir.   - In what direction is sorted, asc or desc.
	 * @return  Page with PublicatedImage records with pagination info.
	 */
	ResPaginationG<PublicatedImageDto> getPublicationsFromUsersFollowed(int pageNo, int pageSize, String sortField, Direction sortDir);
}
