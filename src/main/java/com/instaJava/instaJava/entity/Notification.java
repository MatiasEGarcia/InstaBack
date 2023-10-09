package com.instaJava.instaJava.entity;

import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "notifications")
public class Notification {

	@Id
	@GeneratedValue
	@Column(name="id")
	private Long notiId;
	
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "from_who")
	private User fromWho;
	
	
	@ManyToOne(fetch =FetchType.LAZY)//normally this user will want the notifications, so we won't really need this one
	@JoinColumn(name = "to_who")
	private User toWho;
	
	
	private ZonedDateTime createdAt;
	
}
