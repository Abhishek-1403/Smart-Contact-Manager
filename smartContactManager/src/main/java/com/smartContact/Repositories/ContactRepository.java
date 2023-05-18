package com.smartContact.Repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.smartContact.entities.Contact;

@Repository
public interface ContactRepository extends MongoRepository<Contact,Long>{

	//public List<Contact> findByNameContainingAndContact(String query,List<Contact> contacts);

}
