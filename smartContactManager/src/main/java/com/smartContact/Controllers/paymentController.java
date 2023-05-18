package com.smartContact.Controllers;

import java.security.Principal;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.razorpay.*;
import com.smartContact.Repositories.MyOrderRepository;
import com.smartContact.Repositories.UserRepositiory;
import com.smartContact.entities.MyOrder;



@Controller
@RequestMapping("/payment")
public class paymentController {
	
	@Autowired
	private MyOrderRepository myOrderRepository;
	
	@Autowired
	private UserRepositiory userRepositiory;
	
	// creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String,Object> data,Principal principal) throws Exception {
		//System.out.println("payment controller method is working..");
		//System.out.println(data);
		
		int amount = Integer.parseInt(data.get("amount").toString());
		
		var client = new RazorpayClient("rzp_test_gA8BZGXzwg9dUP", "BGn13DgfoqdJvkmyUEzaoiMy");
		
		JSONObject ob =new  JSONObject();
		ob.put("amount", amount*100);
		ob.put("currency", "INR");
		ob.put("receipt", "txn_234543");
		
		
		//creating new order
		
		Order order = client.orders.create(ob);
		System.out.println(order);
		
		// if you want you can save orders in your database..
		
	
		
		
		MyOrder myOrder = new MyOrder();
		myOrder.setAmount(order.get("amount")+"");
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("created");
		myOrder.setUser(this.userRepositiory.getUserByUserName(principal.getName()));
		myOrder.setReceipt(order.get("receipt"));
		
		
		
		
		this.myOrderRepository.save(myOrder);
		
		
		return order.toString();
	}
	
	
	@PostMapping("/update_order")
	public  ResponseEntity<?> updateOrder(@RequestBody Map<String,Object>  data){
		
		MyOrder myorder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		
		System.out.println("my order fetched is"+ myorder);
		myorder.setPaymentId(data.get("payment_id").toString());
		myorder.setStatus(data.get("status").toString());
		
	
		
		this.myOrderRepository.save(myorder);
		
		
		//System.out.println(data);
		return ResponseEntity.ok(Map.of("msg","updated"));
	}
	
}