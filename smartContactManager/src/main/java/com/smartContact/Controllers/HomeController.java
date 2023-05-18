package com.smartContact.Controllers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smartContact.Repositories.UserRepositiory;
import com.smartContact.entities.User;
import com.smartContact.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder paaswordEncoder;
	
	
	@Autowired
	private UserRepositiory userRepo;
	
	
	String pass = null;
	
//	// for testing purpose only...
//	@GetMapping("/test")
//	@ResponseBody
//	public String test() {
//		User user = new User();
//		user.setName("Abhishek Gupta");
//		
//		user.setEmail("hello@gmail.com");
//		user.setAbout("Cool Person");
//		userRepo.save(user);
//		return "working";
//	}
//	
	// 
	@RequestMapping(value="/")
	public String home(Model model) {
		model.addAttribute("title","Home - Smart Contact Manager");
		
		return "home";
	}
	@RequestMapping(value="/about")
	public String about(Model model) {
		model.addAttribute("title","About - Smart Contact Manager");
		
		return "about";
	}
	
	@RequestMapping(value="/signup")
	public String signup(Model model) {
		model.addAttribute("title","Register - Smart Contact Manager");
		model.addAttribute("user",new User());
		
		return "signup";
	}
	
	//this is for registering user
	@RequestMapping(value="/do_register",method = RequestMethod.POST)
	public String registerUser(@ModelAttribute("user") User user,@RequestParam("profileImage") MultipartFile file,
			@RequestParam(value="agreement",defaultValue="false") boolean agreement,@RequestParam("pass") String pass,Model model,HttpSession session) {
		
		try {
			if(!agreement) {
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
				
			}
			List<User> forCheck = new ArrayList<>();
			
			forCheck = this.userRepo.findAll();
			
			for(int i=0;i<forCheck.size();i++) {
				if(forCheck.get(i).getEmail().equals(user.getEmail())) {
					throw new Exception("The Mail already is in use..");
				}
			}
			
			model.addAttribute("pass",pass);
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			if(file.isEmpty()) {	
				user.setImageUrl("default.png");
			}
			else {
				
				// file save to folder and update the name to user
				user.setImageUrl(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
			}
			
			
		// save end
			user.setPassword(this.paaswordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement "+agreement);
//			System.out.println("User "+ user);
			
			User result = this.userRepo.save(user);
			
			model.addAttribute("user",new User());
			session.setAttribute("message",new Message("Sucessfully Registerd !! Now you can login to your account","alert-success"));
			
			return "signup";
		}
		catch(Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message",new Message("something went worng !!"+e.getMessage(),"alert-danger"));
			
			return "signup";
		}
	}
	
	// handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title","Login page");
		return "login";
	}
	
	
}
