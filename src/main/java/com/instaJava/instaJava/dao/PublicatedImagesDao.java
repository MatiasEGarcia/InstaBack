package com.instaJava.instaJava.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.instaJava.instaJava.entity.PublicatedImage;
import com.instaJava.instaJava.entity.User;

public interface PublicatedImagesDao extends JpaRepository<PublicatedImage, Long> {
	
	@Query(value="SELECT p FROM PublicatedImage p WHERE p.userOwner = ?1")
	Page<PublicatedImage> findPublicatedImagesByOwner(User user, Pageable pag);
}
