package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.view.Viewer;
import com.parkit.parkingsystem.view.ViewerImpl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit Test Class for ParkingServiceTest
 *  
 * @author Olivier MOREL
 *
 */
@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private ParkingService parkingService; //Class Under Test

    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private ParkingSpotDAO parkingSpotDAO;
    @Mock
    private TicketDAO ticketDAO;
    @Mock
    private FareCalculatorService fareCalculatorService;
    
    private Viewer viewer; //can't mock console display
    
    ArgumentCaptor<ParkingType> parkingTypeCaptor;
	ArgumentCaptor<ParkingSpot> parkingSpotCaptor;
	ArgumentCaptor<Ticket> ticketCaptor;

    /**
     * Before Each Test initialize viewer, Class Under Test,
     * and ArgumentCaptor's objects
     */
	@BeforeEach
    private void setUpPerTest() {
        try {
            viewer = new ViewerImpl();
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, viewer);
    		parkingTypeCaptor = ArgumentCaptor.forClass(ParkingType.class);
    		parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
            ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
           
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }
	
	/**
     * After Each Test nullify viewer, Class Under Test,
     * and ArgumentCaptor's objects
     */
    @AfterEach
    private void undefPerTest() {
    	viewer = null;
    	parkingService = null;
    	parkingTypeCaptor = null;
    	parkingSpotCaptor = null;
    	ticketCaptor = null;
     }
    
    /**
     * Tests if method processIncomingVehicle calls mocks and uses correct arguments     
     * with nominal cases
     * @param input
     * @param type
     * @param regNumber
     */
    @ParameterizedTest(name ="Incomming vehicle, input = {0} so Type = {1} and RegistrationNumber = {2}")
    @CsvSource({"1,CAR,CARREG" , "2,BIKE,BIKEREG"})
    @DisplayName("Nominal cases")
    public void processIncomingVehicleTest(int input, String type, String regNumber){
    	//GIVEN
    	int inputReaderUtilReadSelectTimes = 0;
    	int parkingSpotDAOGetTimes = 0;
    	int inputReaderUtilReadRegNumTimes = 0;
    	int parkingSpotDAOUpdateTimes = 0;
    	int ticketDAOSaveTimes = 0;

		when(inputReaderUtil.readSelection()).thenReturn(input);
    	inputReaderUtilReadSelectTimes++; //=1
    	
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
    	parkingSpotDAOGetTimes++; //=1
		//parkingTypeCaptor picked up 1 ParkingType's element
    	
		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNumber);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    	inputReaderUtilReadRegNumTimes++; //=1
    	
    	when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
    	parkingSpotDAOUpdateTimes++; //=1
    	//parkingSpotCaptor picked up 1 ParkingSpot's object
    	
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
    	ticketDAOSaveTimes++; //=1
    	//ticketCaptor picked up 1 Ticket's object
    	
        //WHEN
        parkingService.processIncomingVehicle();
        
        //THEN
        //Verify mocks are used
        verify(inputReaderUtil, times(inputReaderUtilReadSelectTimes)).readSelection();
        verify(parkingSpotDAO, times(parkingSpotDAOGetTimes)).getNextAvailableSlot(any(ParkingType.class));
        try {
			verify(inputReaderUtil, times(inputReaderUtilReadRegNumTimes)).readVehicleRegistrationNumber();
		} catch (Exception e) {
			e.printStackTrace();
		}
        verify(parkingSpotDAO, times(parkingSpotDAOUpdateTimes)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, times(ticketDAOSaveTimes)).saveTicket(any(Ticket.class));
        
        //Assert the arguments are good
        if(parkingSpotDAOGetTimes == 1) {
	        verify(parkingSpotDAO, times(parkingSpotDAOGetTimes)).getNextAvailableSlot(parkingTypeCaptor.capture());
        	assertThat(parkingTypeCaptor.getValue().toString()).isEqualTo(type);
        }

        if(parkingSpotDAOUpdateTimes == 1) {
       	verify(parkingSpotDAO, times(parkingSpotDAOUpdateTimes)).updateParking(parkingSpotCaptor.capture());
        	assertThat(parkingSpotCaptor.getValue()).
        		usingRecursiveComparison().isEqualTo(new ParkingSpot(1, ParkingType.valueOf(type), false));
        }
        
        if(ticketDAOSaveTimes == 1) {
	        Date expectedInTime = new Date();
        	verify(ticketDAO, times(ticketDAOSaveTimes)).saveTicket(ticketCaptor.capture());
        	assertThat(ticketCaptor.getValue())
        		.extracting(
        			ticket -> ticket.getParkingSpot().getId(),
        			ticket -> ticket.getParkingSpot().getParkingType(),
        			ticket -> ticket.getParkingSpot().isAvailable(),
        			ticket -> ticket.getVehicleRegNumber(),
        			ticket -> ticket.getPrice(),
        			ticket -> ticket.getInTime().toString(), //toString to avoid imprecision on milliseconds
        			ticket -> ticket.getOutTime())
        		.containsExactly(
        			1,
        			ParkingType.valueOf(type),
        			false,
        			regNumber,
        			0D, //D cast to double because can't use ','
        			expectedInTime.toString(), 
        			null);
        }
        
        /**
         * Nested Class for corner case's tests
         * @author Olivier MOREL
         *
         */
        @Nested
        @Tag("Corner cases")
        @DisplayName("Corner cases")
        class cornerCases {
        	}
        }
        
        /*
 
    }
    
    
/*   , "1,CAR,Invalid" , "-1, , "
    	
    		switch(input) {
	    	case 1 : 
	    		

	    		if(regNumber == "Invalid") {
		    		when(inputReaderUtil.readVehicleRegistrationNumber()).thenThrow(new IllegalArgumentException("Invalid input provided"));
		        	inputReaderUtilReadRegNumTimes = 1;
		    		// -> exception caught then returns to menu, doesn't use DAOs to Create or Update
	    		} else {

		            
		            break;
	    		}
	        

    	
	    	default :
	    		when(inputReaderUtil.readSelection()).thenReturn(input);
	        	inputReaderUtilReadSelectTimes = 1;
	    		// -> then returns to menu, doesn't use DAOs at all
	    		// same if parking is full
        
        try {
       } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
        

    }
    
    
    @ParameterizedTest(name ="Exiting vehicle with registration number = {0} ")
    @ValueSource(strings = {"REGNUM" , "FareThrowsE" , "UptDAOTicketFail" , " "})
    @Disabled
    public void processExitingVehicleTest(String regNumber){
    	//GIVEN
    	int inputReaderUtilReadRegNumTimes = 0;
    	int ticketDAOGetTimes = 0;
    	int fareCalculatorServiceTimes = 0;
    	int ticketDAOUpdateTimes = 0;
    	int parkingSpotDAOUpdateTimes = 0;
        try {
    		switch(regNumber) {
	    	case "REGNUM" :
	    		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNumber);
	    		inputReaderUtilReadRegNumTimes = 1;
	    		parkingSpot = new ParkingSpot(1, ParkingType.CAR,true);
	    		ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(regNumber);
                ticket.setPrice(0);
                ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
                ticket.setOutTime(new Date());
                
	    		when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);
	        	ticketDAOGetTimes = 1;
	            ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

	            fareCalculatorService.calculateFare(ticket); //void methods on mocks do nothing by default https://javadoc.io/static/org.mockito/mockito-core/4.5.0/org/mockito/Mockito.html#doNothing--
	            fareCalculatorServiceTimes = 1;
	            //ticketCaptor already initialized
	            ticket.setPrice(1.5);
	            
	            when(ticketDAO.updateTicket(ticket)).thenReturn(true);
	            ticketDAOUpdateTimes = 1;
	            //ticketCaptor already initialized so ticket picked up 3 times
	            
	            when(parkingSpotDAO.updateParking(parkingSpot)).thenReturn(true);
	            parkingSpotDAOUpdateTimes = 1;
	            parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
	            break;
	    	
	    	case "FareThrowsE" :
	    		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNumber);
	    		inputReaderUtilReadRegNumTimes = 1;
	    		parkingSpot = new ParkingSpot(1, ParkingType.CAR,true);
	    		ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(regNumber);
                ticket.setPrice(0);
                ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
                ticket.setOutTime(new Date());
                
	    		when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);
	        	ticketDAOGetTimes = 1;
	            ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

	            lenient().doThrow(new IllegalArgumentException()).when(fareCalculatorService).calculateFare(ticket);
	            fareCalculatorServiceTimes = 1;
	            /* ticketCaptor already initialized so ticket picked up twice
	               -> exception caught then returns to menu, doesn't use DAOs to Create or Update *//*
	    		break;
	    		
	    	case "UptDAOTicketFail" :
	    		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNumber);
	    		inputReaderUtilReadRegNumTimes = 1;
	    		parkingSpot = new ParkingSpot(1, ParkingType.CAR,true);
	    		ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(regNumber);
                ticket.setPrice(0);
                ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
                ticket.setOutTime(new Date());
                
	    		when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);
	        	ticketDAOGetTimes = 1;
	            ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

	            fareCalculatorService.calculateFare(ticket); //void methods on mocks do nothing by default https://javadoc.io/static/org.mockito/mockito-core/4.5.0/org/mockito/Mockito.html#doNothing--
	            fareCalculatorServiceTimes = 1;
	            //ticketCaptor already initialized
	            ticket.setPrice(1.5);
	            
	            when(ticketDAO.updateTicket(ticket)).thenReturn(false);
	            ticketDAOUpdateTimes = 1;
	            /* ticketCaptor already initialized so ticket picked up 3 times
	               -> then returns to menu, uses DAO to Update but fails *//*
	    		break;
	    		
	    	default :
	    		when(inputReaderUtil.readVehicleRegistrationNumber()).thenThrow(new IllegalArgumentException("Invalid input provided"));
	    		// -> exception caught then return to menu, doesn't use DAOs at all
    		}
        } catch (Exception e) {
            e.printStackTrace();
           // throw  new RuntimeException("Failed to set up test mock objects");
        }
    	
    	//THEN
    	parkingService.processExitingVehicle();
        //verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
    } */

}
