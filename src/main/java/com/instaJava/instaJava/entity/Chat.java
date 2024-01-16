package com.instaJava.instaJava.entity;

import java.util.ArrayList;
import java.util.List;

import com.instaJava.instaJava.enums.ChatTypeEnum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "chats")
public class Chat{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "image", columnDefinition = "BLOB")
	private String image;

	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private ChatTypeEnum type;

	@OneToMany(fetch = FetchType.LAZY , mappedBy = "chat",cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
	private List<ChatUser> chatUsers;
	
	@OneToMany(fetch = FetchType.LAZY , mappedBy = "chat",cascade = {CascadeType.REMOVE})
	private List<Message> messages;
	
	@Transient
	private String lastMessage;
	
	
	public Chat(Long id) {
		this.id = id;
	}
	
	public Chat(Chat c, String lastMessage) {
		this.id = c.getId();
		this.name = c.getName();
		this.image = c.getImage();
		this.type = c.getType();
		this.chatUsers = c.getChatUsers();
		this.lastMessage = lastMessage;
	}
	
	/**
	 * 
	 * @return list of chat's users. 
	 */
	public List<User> getUsers(){
		List<User> listUser = new ArrayList<>();
		if(chatUsers != null) {
			for(ChatUser chatUser : this.chatUsers) {
				listUser.add(chatUser.getUser());
			}
		}
		return listUser;
	}
}
