package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class FareCalculatorServiceTest {

    private FareCalculatorService fareCalculatorService; //System Under Test
    private Ticket ticket; //Model = only data so don't mock 
    private ParkingSpot parkingSpot; //Model = only data so don't mock
	ParkingType parkingType; //enumeration can't be mocked


    @BeforeEach
    private void setUpPerTest() {
    	fareCalculatorService = new FareCalculatorService();
    	ticket = new Ticket();
    }
    
    @AfterEach
    private void undefPerTest() {
    	fareCalculatorService = null;
    	ticket = null;
    	parkingSpot = null;
    	parkingType = null;
     }

    @ParameterizedTest(name = "{0} minutes in park cost {1} x fare/h for {2} type")
    @CsvSource({"60,1,CAR","60,1,BIKE","45,0.75,CAR","45,0.75,BIKE","1440,24,CAR","1440,24,BIKE"}) //24*60=1440
    @DisplayName("Nominal cases")
    public void calculateFareCar(int min, double coefFare, String type){
    	
    	//Given
    	Date inTime = new Date();
    	Date outTime = new Date(System.currentTimeMillis() + (min * 60 * 1000));
    	double fareType = 0;
        
        switch(type) {
        	case "CAR" : 
        		parkingType = ParkingType.CAR;
        		fareType = Fare.CAR_RATE_PER_HOUR;
        		break;
        	
        	case "BIKE" : 
        		parkingType = ParkingType.BIKE;
        		fareType = Fare.BIKE_RATE_PER_HOUR;
        		break;
        }
        
        parkingSpot = new ParkingSpot(1, parkingType, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        //When
        fareCalculatorService.calculateFare(ticket);
        
        //Then
        assertThat(ticket.getPrice()).isEqualTo(coefFare*fareType); // or isCloseTo(double, Offset). 
    }

    @Nested
    @Tag("Corner cases")
    @DisplayName("Corner cases")
    class cornerCases {
        @Test
        @DisplayName("Unknown vehicle's type")
        public void calculateFareUnkownType(){
        	//Given
        	Date inTime = new Date();
        	Date outTime = new Date(System.currentTimeMillis() + (60 * 60 * 1000));
        	parkingSpot = new ParkingSpot(1, null, false);

            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);
            
            //When
            
            //Then
            assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
        }
        
        @Test
        @DisplayName("Bike with InTime after OutTime")
        public void calculateFareBikeWithFutureInTime(){
        	//Given
        	Date inTime = new Date(System.currentTimeMillis() + (60 * 60 * 1000));
        	Date outTime = new Date();
        	parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);
            
            //When
            
            //Then
            assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
        }
    }
}
