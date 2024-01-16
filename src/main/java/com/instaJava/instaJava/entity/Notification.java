package com.instaJava.instaJava.entity;

import java.time.ZonedDateTime;

import com.instaJava.instaJava.enums.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
public class Notification{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "from_who")
	private User fromWho;
	
	@Enumerated(EnumType.STRING)
	@Column(name= "type")
	private NotificationType type;
	
	@Column(name = "element_id")
	private Long elementId;
	
	@Column(name = "noti_message")
	private String notiMessage; //to make more especific notifications
	
	@ManyToOne(fetch =FetchType.LAZY)//normally this user will want the notifications, so we won't really need this one
	@JoinColumn(name = "to_who")
	private User toWho;
	
	@Column(name="created_at")
	private ZonedDateTime createdAt;
	
	@Column(name = "watched")
	private boolean watched; //by default in bdd false

}
