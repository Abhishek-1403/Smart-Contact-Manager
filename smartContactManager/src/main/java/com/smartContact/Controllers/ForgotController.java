package com.smartContact.Controllers;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartContact.Repositories.UserRepositiory;
import com.smartContact.entities.User;
import com.smartContact.service.EmailService;

import jakarta.servlet.http.HttpSession;


@Controller
public class ForgotController {

	Random random = new Random(100000);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepositiory userRepositiory;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	// Email id form open handler
	@GetMapping("/forgot")
	public String openEmailForm() {
		
		
		return "forgot_email_form";
	}
	
	
	@PostMapping("/send-otp")
	public String sendOtp(@RequestParam("email") String email,HttpSession session) {
		
		System.out.println("Email   "+email);
		//generating otp of 6 digit
		
		
		User user = this.userRepositiory.getUserByUserName(email);
		if(user==null) {
			session.setAttribute("message", "User does not exists with this email !!");
			return "forgot_email_form";
		}
		
		int TempOtp = random.nextInt(999999);
		Long tempotp =  (TempOtp+System.currentTimeMillis()-100000);
		String conversionOfOTP = String.valueOf(tempotp);
		int otp = 546879;
		if(conversionOfOTP.length()>6) {
			 otp = Integer.parseInt(conversionOfOTP.substring(conversionOfOTP.length()-6));
		}
		else {
			otp = Integer.parseInt(conversionOfOTP);
		}
		System.out.println("otp is "+otp);
		// sending otp to email
		String subject = "OTP From SCM";
		String message = ""
				+"<div style='border:1px solid #e2e2e2; padding:20px'>"
				+"As per your request for forgot password your OTP is "
				+"<h3>"
				+"<b>"+otp
				+"</n>"
				+"</h3>"
				+"</b>"
				+"If you are not requested for the same then please do not share the otp with anyone."
				+"<div class='mt-3' style='font-weight:16px'>"
				+"<b>"
				+"Regards SCM Support Helpdesk.."
				+"</b>"
				+"</div>"
				+"</div>";
		String to = email;
		
		
		//System.out.println("sub "+subject+"msg      "+message+"to     "+to);
		
		boolean flag = this.emailService.sendEmail(subject, message, to);
		if(flag) {
			
			session.setAttribute("serverOtp",otp);
			session.setAttribute("email",email);
			return "veryfy_otp";
			
		}
		else {
			session.setAttribute("message", "Check your email id !!");
			return "forgot_email_form";
		}
	}
	
	// veryfy otp
	@PostMapping("/veryfy-otp")
	public String veryfyOtp(@RequestParam("otp") int otp,HttpSession session) {
		int serverOtp = (int) session.getAttribute("serverOtp");
		
		String email = (String)session.getAttribute("email");
		if(otp==serverOtp) {
			//password change form
			
			User user = this.userRepositiory.getUserByUserName(email);
			System.out.println("user is   "+user);
			if(user==null) {
				//send error message
				session.setAttribute("message", "User does not exists with this email !!");
				return "forgot_email_form";
			}
			else {
				//send change password form
				
				
			}
			return "password_change_form";
			
		}
		else {
			session.setAttribute("message","You have entered wrong otp !!");
			return "veryfy_otp";
		}	
	}
	
	// change password 
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPass") String newPass,@RequestParam("rePass") String rePass,HttpSession session) {
		
		String email = (String)session.getAttribute("email");
		System.out.println("email is "+email);
		User user = this.userRepositiory.getUserByUserName(email);
		if(newPass.equals(rePass)) {
			user.setPassword(this.bCryptPasswordEncoder.encode(newPass));
			this.userRepositiory.save(user);
			return "redirect:/signin?change=password changed succesfully..";
		}else {
			// through error
			session.setAttribute("message","Enter same password in both fields !!");
			return "password_change_form";
			
		}
		
		
		
	}
}
