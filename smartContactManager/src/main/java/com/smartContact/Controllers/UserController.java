package com.smartContact.Controllers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smartContact.Repositories.UserRepositiory;
import com.smartContact.entities.Contact;
import com.smartContact.entities.User;
import com.smartContact.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepositiory userRepository;
	
	// method for adding common data to  response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String userName = principal.getName();
		
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER"+user);
		model.addAttribute("user",user);
	}
	
	// dashboard
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		model.addAttribute("title","User Dashboard");
	
		return "user/user_dashboard";
	}
	
	
	
	
	//user by name
	@GetMapping("/userbyname/{name}")
	public List<User> getUserByName(@PathVariable String name){
		return (List<User>) userRepository.getUserByUserName(name);
	}
	
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","add ontact");
		model.addAttribute("contact",new Contact());
		return "user/add_contact_form";
	}
	
	// handler for submission of add contact
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file, 
			Principal principal,
			HttpSession session){
		try {
			
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			if(file.isEmpty()) {
				// if file is empty 
				contact.setImage("contact.svg");
				System.out.println("image not uploaded");
			}
			else {
				// file save to folder and update the name to contact
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
			}
			
			user.getContacts().add(contact);
			
			this.userRepository.save(user);
			// message for success
			session.setAttribute("message",new Message("Your contact is added !! Add more...","success"));
		}
		catch(Exception e) {
			// message for error
			session.setAttribute("message", new Message("Something went wrong !! Try again..","danger"));
			System.out.println("ERROR"+e.getMessage());
			e.printStackTrace();
		}
		return "user/add_contact_form";
	}
	
	
	// show contact handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model model,Principal principal) {
		
		model.addAttribute("title","show User Contacts");
		//model.addAttribute(name);
		// Contact list fetching and sending to view page..
		
		String name = principal.getName();
		
		
//		Pageable pageable = PageRequest.of(page, 5);
//		Page<Contact> contact = this.userRepository.getUserByUserName(userName,pageable);
//		
//		List<Contact> list= contact.getContent();
//		
		
		User user = this.userRepository.getUserByUserName(name);
		model.addAttribute("name",user.getEmail());
		
		List<Contact> con = user.getContacts();
		List<Contact> pagination = new ArrayList<>();
		
		model.addAttribute("page",page);
		if(con.size()/5==0) {
			model.addAttribute("size",con.size()/5);
		}
		else {
			model.addAttribute("size",con.size()/5+1);
		}
		if(con.size()>=page*5+5) {
			pagination = con.subList(page*5, page*5+5);
		}
		else {
			int sizeOfRequiredList = con.size()-page*5;
			if(sizeOfRequiredList>0) {	
				for(int i=0;i<sizeOfRequiredList;i++) {
					Contact temp = con.get(page*5+i);
					pagination.add(temp);
				}	
			}
			else {
				return "user/show_contacts";
			}
		}
		
		
		
		model.addAttribute("contact",pagination);
		
		
		
		
		
		
		
//		List<Contact> contacts =contact.getContent();
//		System.out.println(contacts);
//	    //model.addAttribute("contacts",contacts);
//		model.addAttribute("currentPage",page);
//		model.addAttribute("totalPages",contact.getTotalPages());
		
		return "user/show_contacts";
	}
	
	
	
	// Showing particular contact details.
	
	@GetMapping("/{email}/{name}/contact")
	public String showContactDetail(@PathVariable("email") String email,@PathVariable("name") String name,Model model ) {

		model.addAttribute("title","Contact Detail page");
		
		User user = this.userRepository.getUserByUserName(name);
		
		List<Contact> contact =   user.getContacts();
		
		Contact requiredContact = new Contact();
		
		for(int i=0;i<contact.size();i++) {
			String tempMail = contact.get(i).getEmail();
			if(email.equals(tempMail)) {
				 requiredContact = contact.get(i);
				 break;
			}
		}
		 model.addAttribute("contact",requiredContact);
		return "user/contact_detail";
	}
	
	
	// Delete contact handler
	@GetMapping("/delete/{phone}/{name}/{page}")
	public String deleteContact(@PathVariable("phone") String phone,@PathVariable("name") String name,@PathVariable("page") Integer page,Model model,HttpSession session) {

		User user = this.userRepository.getUserByName(name);
		System.out.println(user);
		List<Contact> contact =   user.getContacts();
		List<Contact> updatedList = new ArrayList<>();
		
		Contact requiredContact = new Contact();
		
		for(int i=0;i<contact.size();i++) {
			String tempPhone = contact.get(i).getPhone();
			if(phone.equals(tempPhone)) {
				 requiredContact = contact.get(i);
				 continue;
			}
			updatedList.add(contact.get(i));
		}

		user.setContacts(updatedList);
		
		User updatedUser = this.userRepository.save(user);
		session.setAttribute("message",new Message("Contact deleted succesfully...","success"));
		return "redirect:/user/show-contacts/{page}";
	}
	
	// Update contact open  handler
	@PostMapping("/update-contact/{phone}/{name}")
	public String updateForm(@PathVariable("phone") String phone,@PathVariable("name") String name,Model model) {
		
		model.addAttribute("title","Update Contact page");
		
		User user = this.userRepository.getUserByUserName(name);

		List<Contact> contact =   user.getContacts();
		Contact requiredContact = new Contact();
		for(int i=0;i<contact.size();i++) {
			String tempPhone = contact.get(i).getPhone();
			if(phone.equals(tempPhone)) {
				 requiredContact = contact.get(i);
				 break;
			}
		}
		model.addAttribute("contact",requiredContact);
		model.addAttribute("contactName",requiredContact.getName());
		model.addAttribute("contactEmail",requiredContact.getEmail());
		model.addAttribute("contactImage",requiredContact.getImage());
		model.addAttribute("name",name);
		return "user/update_form";	
	}
	
	//Update contact response data handler
	@PostMapping("/process-update")
	public String processUpdate(@ModelAttribute Contact contact,
	@RequestParam("profileImage") MultipartFile file,
	@RequestParam("contactName") String cName,
	@RequestParam("contactEmail") String cEmail,
	@RequestParam("contactImage") String cImage,
	HttpSession session) {
		System.out.println(contact.getCid());
		User user = this.userRepository.getUserByUserName(contact.getCid());
		System.out.println("fetched user is.... "+user);

		List<Contact> userContactList = user.getContacts();
		List<Contact> updatedList = new ArrayList<>();
		try {
			
			for(int i=0;i<userContactList.size();i++) {
				
				
				if(userContactList.get(i).getName().equals(cName) && userContactList.get(i).getEmail().equals(cEmail)) {
					if(!file.isEmpty()) {
						
						// delete previous photo
						File deleteFile = new ClassPathResource("static/img").getFile();
						File tempFile  = new File(deleteFile, cImage);
						tempFile.delete();
						
						// add new photo to database
						contact.setImage(file.getOriginalFilename());
						File saveFile = new ClassPathResource("static/img").getFile();
						Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
						Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
					
					}
					else {
						contact.setImage(cImage);
					}
					updatedList.add(contact);
				}
				else {
					updatedList.add(userContactList.get(i));
				}
			}
			
			user.setContacts(updatedList);
			this.userRepository.save(user);
			
			
		}
		catch (Exception e) {
			// TODO: handle exception
			
			session.setAttribute("message", new Message("Something went wrong !! Try again..","danger"));
			System.out.println("ERROR"+e.getMessage());
			e.printStackTrace();
		}
		

		
		
		return "user/update_form";
	}
	
	// Your profile page handler
	@GetMapping("/profile")
	public String showUserProfile(Model model) {
		
		model.addAttribute("title","Profile Page");
		return "user/profile";
	}
	
	//delete user account handler
	@GetMapping("/delete/{email}/{name}")
	public String deleteUserAccount(@PathVariable("email") String email,@PathVariable("name") String name,HttpSession session) {
	
		User user = this.userRepository.getUserByUserName(email);
		
		this.userRepository.delete(user);
		session.setAttribute("message",new Message("You deleted your account successfully... Looking forword to see you again...","alert-success"));
		return "/signup";
	}
	
	//update user account details form handler
	@PostMapping("/update_account/{email}")
	public String updateUserAccount(Model model,@PathVariable("email") String email) {
		
		User user = this.userRepository.getUserByUserName(email);
		model.addAttribute("user",user);
		return "user/update_user_account";
	}
	
	// update user account response handler
	
	@Autowired
	private BCryptPasswordEncoder paaswordEncoder;
	
	
	@PostMapping("/save_account")
	public String updateAccount(@ModelAttribute User user,@RequestParam("profileImage") MultipartFile file,@RequestParam("oldEmail") String oldEmail
			,@RequestParam("oldImage") String oldImage,HttpSession session) {
		
		
		
		User temp = this.userRepository.getUserByUserName(oldEmail);
		List<Contact> contacts = temp.getContacts();
		
		user.setContacts(contacts);
		try {
			if(this.paaswordEncoder.matches(user.getPassword(),temp.getPassword())  ) {
				if(!file.isEmpty()) {
					
					// delete previous photo
					File deleteFile = new ClassPathResource("static/img").getFile();
					File tempFile  = new File(deleteFile, oldImage);
					tempFile.delete();
					
					// add new photo to database
					user.setImageUrl(file.getOriginalFilename());
					File saveFile = new ClassPathResource("static/img").getFile();
					Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
					Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
					
				}
				else {
					user.setImageUrl(oldImage);
				}
				
				user.setPassword(this.paaswordEncoder.encode(user.getPassword()));
				
				this.userRepository.delete(temp);
				this.userRepository.save(user);
				session.setAttribute("message",new Message("Your Account is updated successfully... Login again...","alert alert-success"));
			}
			else {
				session.setAttribute("message", new Message("Something went wrong !! Try again..","alert alert-danger"));
				return "user/profile";
			}
			
			
			
		}
		catch (Exception e) {
			// TODO: handle exception
			session.setAttribute("message", new Message("Something went wrong !! Try again..","alert -alert-danger"));
			System.out.println("ERROR"+e.getMessage());
		}
		

		return "redirect:/logout";
	}
	
	//Open setting handler
	@GetMapping("/settings")
	public String openSettings() {	
		return "user/settings";
	}
	
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPass,@RequestParam("newPassword") String newPass,@RequestParam("rePassword") String rePass,@RequestParam("email") String email,HttpSession session) {
		
		User user = this.userRepository.getUserByUserName(email);
		String prevPass = user.getPassword();
		
		if(this.paaswordEncoder.matches(oldPass,prevPass) && newPass.equals(rePass)) {
		
			user.setPassword(this.paaswordEncoder.encode(newPass));
			this.userRepository.save(user);
			session.setAttribute("message",new Message("Your Password is updated successfully...","alert alert-success"));
			
		}
		else {
			session.setAttribute("message", new Message("Something went wrong !! Try again..","alert alert-danger"));
			return "user/settings";
		}
		
		
		return "redirect:/user/index";
	}
}

