package com.smartContact.Repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.smartContact.entities.Contact;
import com.smartContact.entities.User;

@Repository
public interface UserRepositiory extends MongoRepository<User, String> {
	
	@Query ( "{ 'email' : ?0 }" )
	public Page<Contact> getUserByUserName(String userName,Pageable peagable);
	
	@Query ( "{ 'email' : ?0 }" )
	public User getUserByUserName(String username);
	
	
	public User getUserByName(String name);
	
	
	@Query("{'phone': ?0 }")
	public User deleteContactByPhone(Contact requiredPhone);
	
	public User save(User user);	
	
	//@org.springframework.data.mongodb.repository.Query(value = "{ 'contacts': { $elemMatch: { 'name' : ?0 } }}")
	public List<Contact> findByNameContaining(String name);
}
