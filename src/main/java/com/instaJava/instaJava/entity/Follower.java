package com.instaJava.instaJava.entity;

import com.instaJava.instaJava.enums.FollowStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "followers")
public class Follower {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name= "id")
	private Long FollowerId;
	
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
