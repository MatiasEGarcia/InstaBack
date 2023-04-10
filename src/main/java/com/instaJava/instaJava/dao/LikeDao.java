package com.instaJava.instaJava.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.instaJava.instaJava.entity.Like;

public interface LikeDao extends JpaRepository<Like, Long> ,JpaSpecificationExecutor<Like> {

}
