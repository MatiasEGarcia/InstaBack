package com.instaJava.instaJava.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.instaJava.instaJava.entity.PublicatedImage;

public interface PublicatedImagesDao extends JpaRepository<PublicatedImage, Long> , JpaSpecificationExecutor<PublicatedImage>{
	

}
