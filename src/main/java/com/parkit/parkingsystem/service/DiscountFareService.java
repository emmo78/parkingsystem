package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.model.Ticket;

public class DiscountFareService {

	public void calculateDiscount(Ticket ticket) {
        
        if((ticket.getOutTime().getTime() - ticket.getInTime().getTime())/(1000*60) <= 30) {
        	ticket.setPrice(0.0);
        };
	}

}
