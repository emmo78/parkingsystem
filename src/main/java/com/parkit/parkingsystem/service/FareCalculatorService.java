package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

/**
 * This service calculates the fare taking into account the vehicle's type
 * @author Olivier MOREL
 *
 */
public class FareCalculatorService {

    /**
     * From a given ticket calculates the fare taking into account the vehicle's type
     * @param ticket
     * @throws IllegalArgumentException if type is unknown
     */
	public void calculateFare(Ticket ticket) throws IllegalArgumentException{ // Throws optional because RuntimeException but better readability
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        double inHour = ticket.getInTime().getTime(); // Returns the number of milliseconds since
        double outHour = ticket.getOutTime().getTime(); // January 1, 1970, 00:00:00 GMT represented by this Date object

        double duration = (outHour - inHour) / (1000*3600); // from milliseconds to decimal hours with floating number minimal imprecision
        try {
	        switch (ticket.getParkingSpot().getParkingType()){ //When null show up in switch statement, Java will throw NullPointerException
	            case CAR: {
	                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
	                break;
	            }
	            case BIKE: {
	                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
	                break;
	            }
	            default: { //Braces are optional but better readability 
	            	throw new IllegalArgumentException("Unkown Parking Type"); //Throw a java.lang.RuntimeException
	            }
	        }
        } catch(NullPointerException e) {
        	throw new IllegalArgumentException("Type is null");
        }
    }
}