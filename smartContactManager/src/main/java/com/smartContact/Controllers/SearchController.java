package com.smartContact.Controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.smartContact.Repositories.ContactRepository;
import com.smartContact.Repositories.UserRepositiory;
import com.smartContact.entities.Contact;
import com.smartContact.entities.User;

@RestController
public class SearchController {

	@Autowired
	private UserRepositiory userRepository;

	@Autowired
	private ContactRepository contactRepository;

	// Search handler
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query, Principal principal) {

		User user = this.userRepository.getUserByUserName(principal.getName());
		List<Contact> contacts = new ArrayList<>();

		if (user != null) {
			List<Contact> con = user.getContacts();
			if (con != null) {
				Contact userContact = new Contact();
				userContact.setEmail(principal.getName());
				contacts.add(userContact);
				for (Contact contact : con) {
					if ((contact.getName()).toLowerCase().contains(query.toLowerCase())) {
						
						contacts.add(contact);
					}
				}
				
			}
		}
		return ResponseEntity.ok(contacts);
	}
}
