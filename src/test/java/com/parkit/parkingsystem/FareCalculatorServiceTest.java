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

/**
 * Unit Test Class for FareCalculatorService
 *  
 * @author Olivier MOREL
 *
 */
public class FareCalculatorServiceTest {

    private FareCalculatorService fareCalculatorService; //System Under Test
    private Ticket ticket; //Model = only data so don't mock 
    private ParkingSpot parkingSpot; //Model = only data so don't mock
	ParkingType parkingType; //enumeration can't be mocked

    /**
     * Before Each Test initialize Class Under Test and a Ticket's model pointer
     */
	@BeforeEach
    private void setUpPerTest() {
    	fareCalculatorService = new FareCalculatorService();
    	ticket = new Ticket();
    }

	/**
     * After Each Test nullify :
     *  - Class Under Test
     *  - Ticket's model pointer
     *  - ParkingSpot's model pointer (initialized in test methods)
     *  - ParkingType's enumeration valor;
     */
    @AfterEach
    private void undefPerTest() {
    	fareCalculatorService = null;
    	ticket = null;
    	parkingSpot = null;
    	parkingType = null;
     }

    /**
     * Tests if method calculateFare gives corrects results
     * with nominal cases
     * @param min : how long the vehicle parks (minutes)
     * @param coefFare : = min/60 because rate/h
     * @param type : vehicle's type
     */
    @ParameterizedTest(name = "{0} minutes in park should cost {1} x fare/h for {2} type")
    @CsvSource({"60,1,CAR","60,1,BIKE","45,0.75,CAR","45,0.75,BIKE","1440,24,CAR","1440,24,BIKE"}) //24*60=1440
    @DisplayName("Nominal cases")
    public void calculateFareNominalCasesTest(int min, double coefFare, String type){
    	
    	//GIVEN
    	Date inTime = new Date(System.currentTimeMillis() - (min * 60 * 1000));
    	Date outTime = new Date();
    	double fareTypeRate = 0;
        
        switch(type) {
        	case "CAR" : {
        		parkingType = ParkingType.CAR;
        		fareTypeRate = Fare.CAR_RATE_PER_HOUR;
        		break;
        	}
        	case "BIKE" : {
        		parkingType = ParkingType.BIKE;
        		fareTypeRate = Fare.BIKE_RATE_PER_HOUR;
        		break;
        	}
        }
        
        parkingSpot = new ParkingSpot(1, parkingType, false);
        ticket.setParkingSpot(parkingSpot);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        
        //WHEN
        fareCalculatorService.calculateFare(ticket);
        
        //THEN
        assertThat(ticket.getPrice()).isEqualTo(coefFare*fareTypeRate); // or isCloseTo(double, withinPercentage(0.01)). 
    }

    /**
     * Nested Class for corner case's tests
     * @author Olivier MOREL
     *
     */
    @Nested
    @Tag("CornerCases")
    @DisplayName("Corner cases")
    class cornerCases {
        /**
         * Calculate fare for a unknown vehicle's Type
         * should throw an illegal argument exception
         */
    	@Test
        @DisplayName("Unknown vehicle's type")
        public void calculateFareUnknownTypeShouldThrowsIllegalArgumentException(){
        	//GIVEN
        	Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        	Date outTime = new Date();
        	parkingSpot = new ParkingSpot(1, null, false);
            ticket.setParkingSpot(parkingSpot);
            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            
            //WHEN
            
            //THEN
            assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
        }
        
        /**
         * Bike with InTime after OutTime should throw
         * an illegal argument exception
         */
    	@Test
        @DisplayName("Bike with InTime after OutTime")
        public void calculateFareBikeWithFutureInTime(){
        	//GIVEN
        	Date inTime = new Date(System.currentTimeMillis() + (60 * 60 * 1000));
        	Date outTime = new Date();
        	parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
            ticket.setInTime(inTime);
            ticket.setOutTime(outTime);
            ticket.setParkingSpot(parkingSpot);
            
            //WHEN
            
            //THEN
            assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
        }
    }
}
