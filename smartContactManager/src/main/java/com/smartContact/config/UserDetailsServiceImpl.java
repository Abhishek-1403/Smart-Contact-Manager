package com.smartContact.config;

import java.nio.file.attribute.UserPrincipalNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smartContact.Repositories.UserRepositiory;
import com.smartContact.entities.User;

public class UserDetailsServiceImpl implements UserDetailsService{
	
	@Autowired
	private  UserRepositiory userRepositiory;
	
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		//fetching user from database
		
		User user = userRepositiory.getUserByUserName(username);
		if(user==null) {
			throw new UsernameNotFoundException("Could not found user !!");
			
		}
		CustomUserDetails customUserDetails = new CustomUserDetails(user);
		
		return customUserDetails;
	}


}
