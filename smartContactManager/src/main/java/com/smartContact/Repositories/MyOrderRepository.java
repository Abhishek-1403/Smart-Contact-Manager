package com.smartContact.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.smartContact.entities.MyOrder;

public interface MyOrderRepository extends MongoRepository<MyOrder, String> {
	
	public MyOrder findByOrderId(String orderId);
}
