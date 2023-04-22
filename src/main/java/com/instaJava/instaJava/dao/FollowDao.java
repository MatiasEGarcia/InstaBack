package com.instaJava.instaJava.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.instaJava.instaJava.entity.Follow;

public interface FollowDao extends JpaRepository<Follow, Long>, JpaSpecificationExecutor<Follow> {

}
