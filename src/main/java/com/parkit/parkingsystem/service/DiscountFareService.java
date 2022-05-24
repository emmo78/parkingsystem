package com.parkit.parkingsystem.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.parkit.parkingsystem.model.Ticket;

public class DiscountFareService {

	public void fareForThirtyOrLessMinutes(Ticket ticket) {
        double duration = (ticket.getOutTime().getTime() - ticket.getInTime().getTime()) / (1000*60d); // long / double so decimal division in minutes
        if(BigDecimal.valueOf(duration).setScale(0, RoundingMode.HALF_UP).intValue() <= 30) {  
        	/* Set integer minutes rounded towards "nearest neighbor" unless both neighbors are equidistant, in which case round up
             * max value = (2^31-1)/60/24/365.24219 = more 4000 years ! */
        	ticket.setPrice(0.0); //0.0 double by default
        }
	}

	public void fivePourcentsOff(Ticket ticket) {
		//ticket.setPrice(BigDecimal.valueOf(ticket.getPrice()*(1-5/100d)).setScale(2, RoundingMode.HALF_UP).doubleValue());		
	}
}
