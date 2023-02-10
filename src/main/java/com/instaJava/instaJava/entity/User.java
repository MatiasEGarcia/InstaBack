package com.instaJava.instaJava.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Entity
@Table(name = "users" , schema="insta_java")
public class User implements UserDetails{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "username")
	private String username;
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "image")
	private String image;
	
	@Column(name = "visible")
	private boolean visible;
	
	@OneToMany(mappedBy = "user",
			fetch = FetchType.EAGER)
	private List<Rol> roles;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		final Set<SimpleGrantedAuthority> grntdAuths = new HashSet<>();
		List<Rol> roles = this.getRoles();
		if(roles != null) {
			for(Rol rol:roles) {
				grntdAuths.add(new SimpleGrantedAuthority(rol.getName().toString()));
			}
		}
		return grntdAuths;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	
}
