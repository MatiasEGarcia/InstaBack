package com.instaJava.instaJava.entity;

import com.instaJava.instaJava.enums.FollowStatus;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "followers", schema="insta_java")
public class Follower {

	@EmbeddedId
	private FollowerId id;
	
	@ManyToOne
	@MapsId("followed")
	@JoinColumn(name = "followed")
	private User userFollowed; 
	
	@ManyToOne
	@MapsId("follower")
	@JoinColumn(name = "follower")
	private User userFollower;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private FollowStatus followStatus;
}
