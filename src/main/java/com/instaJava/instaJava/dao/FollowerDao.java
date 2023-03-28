package com.instaJava.instaJava.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.instaJava.instaJava.entity.Follower;

public interface FollowerDao extends JpaRepository<Follower, Long> {

}
