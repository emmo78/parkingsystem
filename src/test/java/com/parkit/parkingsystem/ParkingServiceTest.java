package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * Unit Test Class for ParkingServiceTest
 *  
 * @author Olivier MOREL
 *
 */
@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

	@InjectMocks
	private ParkingService parkingService; //Class Under Test

    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private ParkingSpotDAO parkingSpotDAO;
    @Mock
    private TicketDAO ticketDAO;
    @Mock
    private FareCalculatorService fareCalculatorService; //Will be injected by setter in ParkingService
    // For Isolated, FareCalculatorService has a unit test class
    
    private Viewer viewer; //Can't mock console display
    
    ArgumentCaptor<ParkingType> parkingTypeCaptor;
	ArgumentCaptor<ParkingSpot> parkingSpotCaptor;
	ArgumentCaptor<Ticket> ticketCaptor;
	ArgumentCaptor<String> stringCaptor;

    /**
     * Before Each Test initialize viewer, Class Under Test,
     * and ArgumentCaptor's objects
     */
	@BeforeEach
    private void setUpPerTest() {
        viewer = new ViewerImpl();
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, viewer);
        parkingService.setFareCalculatorService(fareCalculatorService);
		parkingTypeCaptor = ArgumentCaptor.forClass(ParkingType.class);
		parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
        ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        stringCaptor = ArgumentCaptor.forClass(String.class);
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
     * Nested Class for nominal case's tests
     * @author Olivier MOREL
     *
     */
    @Nested
    @Tag("NominalCases")
    @DisplayName("Nominal cases")
    class NominalrCases {
    	/**
	     * Tests if method processIncomingVehicle calls mocks and uses correct arguments     
	     * with nominal cases
	     * @param input : the user choice when asked for vehicle's type
	     * @param type : so the expected vehicle's type
	     * @param regNumber : the vehicle's registered number
	     */
	    @ParameterizedTest(name ="Incomming vehicle, input = {0} so Type = {1} and RegistrationNumber = {2}")
	    @CsvSource({"1,CAR,CARREG" , "2,BIKE,BIKEREG"})
	    @Tag("NominalCasesIncomingVehicle")
	    @DisplayName("Nominal cases Incoming Vehicle")
	    public void processIncomingVehicleNominalTests(int input, String type, String regNumber){
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
	        
	        //Asserts the arguments are good
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
	        			ticket -> ticket.getInTime().toString().substring(0,17), //to avoid imprecision on few seconds
	        			ticket -> ticket.getOutTime())
	        		.containsExactly(
	        			1,
	        			ParkingType.valueOf(type),
	        			false,
	        			regNumber,
	        			0D, //D cast to double because can't use ','
	        			expectedInTime.toString().substring(0,17), // = "dow mon dd hh:mm:", 
	        			null);
	        }
	    }

    	/**
    	 * Tests if method processExitingVehicle calls mocks and uses correct arguments     
	     * with nominal cases
    	 */
	    @Test
    	@Tag("NominalCaseExitingVehicle")
        @DisplayName("Nominal case exiting vehicle")
        public void processExitingVehicleNominalTest(){
        	//GIVEN
        	int inputReaderUtilReadRegNumTimes = 0;
        	int ticketDAOGetTimes = 0;
        	int fareCalculatorServiceTimes = 0;
        	int ticketDAOUpdateTimes = 0;
        	int parkingSpotDAOUpdateTimes = 0;
        	
			try {
				when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("REGNUM");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
	    	inputReaderUtilReadRegNumTimes++; //=1
	    	
	    	Ticket ticketGiven = new Ticket();
    		ticketGiven.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
            ticketGiven.setVehicleRegNumber("REGNUM");
            ticketGiven.setPrice(0);
            ticketGiven.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
            ticketGiven.setOutTime(null);
            when(ticketDAO.getTicket(any(String.class))).thenReturn(ticketGiven);
            ticketDAOGetTimes++; //= 1;
            //stringCaptor picked up "REGNUM"
            
            doAnswer(invocation -> {
            	Ticket ticket = invocation.getArgument(0, Ticket.class);
            	ticket.setPrice( ( (ticket.getOutTime().getTime() - ticket.getInTime().getTime()) / (1000*3600D) ) * Fare.CAR_RATE_PER_HOUR);
            	return null;})
            	.when(fareCalculatorService).calculateFare(any(Ticket.class));
            fareCalculatorServiceTimes++; //=1
            //ticketCaptor picked up ticket with out time set by CUT and price set by doAnswer
            
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            ticketDAOUpdateTimes++; //= 1
            //ticketCaptor picked up ticket from another method
            
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
            parkingSpotDAOUpdateTimes++; //=1
            //parkingSpotCaptor picked up the ParkingSpot's object with available set to true
            
            //WHEN
            parkingService.processExitingVehicle();
            
            //THEN
            //Verify mocks are used
	        try {
				verify(inputReaderUtil, times(inputReaderUtilReadRegNumTimes)).readVehicleRegistrationNumber();
			} catch (Exception e) {
				e.printStackTrace();
			}
	        verify(ticketDAO, times(ticketDAOGetTimes)).getTicket(any(String.class));
	        verify(fareCalculatorService, times(fareCalculatorServiceTimes)).calculateFare(any(Ticket.class));
	        verify(ticketDAO, times(ticketDAOUpdateTimes)).updateTicket(any(Ticket.class));
	        verify(parkingSpotDAO, times(parkingSpotDAOUpdateTimes)).updateParking(any(ParkingSpot.class));

	        //Asserts the arguments are good
	        verify(ticketDAO, times(ticketDAOUpdateTimes)).updateTicket(ticketCaptor.capture());
        	assertThat(ticketCaptor.getValue())
        		.extracting(
        			ticket -> ticket.getParkingSpot().getId(),
        			ticket -> ticket.getParkingSpot().getParkingType(),
        			ticket -> ticket.getParkingSpot().isAvailable(),
        			ticket -> ticket.getVehicleRegNumber())
        		.containsExactly(
        			1,
        			ParkingType.valueOf("CAR"),
        			true, // (1)
        			"REGNUM");
	        /* (1) ticket parkingSpot field is a pointer to the object which is set from false to true
	         * after ticket's update to SGBD but there is no persistence for objects in code ...*/	        
    	}
	}

	/**
     * Nested Class for corner case's tests
     * @author Olivier MOREL
     *
     */
    @Nested
    @Tag("CornerCasesIncomingVehicle")
    @DisplayName("Corner cases incoming vehicle")
    class cornerCasesIncomingVehicle {
        /**
         * For an unknown vehicle's type, method processIncomingVehicle should only use
         * one time InputReaderUtil and nothing else
         */
    	@Test
        @DisplayName("Unknown vehicle's type")
        public void processIncomingVehicleForUnknownTypeShouldUseOnly1TimeInputReaderUtil(){
        	//GIVEN
    		int inputReaderUtilReadSelectTimes = 0;
        	int parkingSpotDAOGetTimes = 0;
        	int inputReaderUtilReadRegNumTimes = 0;
        	int parkingSpotDAOUpdateTimes = 0;
        	int ticketDAOSaveTimes = 0;

    		when(inputReaderUtil.readSelection()).thenReturn(-1);
        	inputReaderUtilReadSelectTimes++; //=1
    		/*Else shouldn't be used
        	 *and returns to menu, doesn't use DAOs at all and IllegalArgumentException caught*/

            //WHEN & Asserts that Exception was caught
            assertDoesNotThrow(() -> parkingService.processIncomingVehicle());
            
            //THEN
            //Verify if mocks are used or never
            verify(inputReaderUtil, times(inputReaderUtilReadSelectTimes)).readSelection();
            verify(parkingSpotDAO, times(parkingSpotDAOGetTimes)).getNextAvailableSlot(any(ParkingType.class));
            try {
    			verify(inputReaderUtil, times(inputReaderUtilReadRegNumTimes)).readVehicleRegistrationNumber();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
            verify(parkingSpotDAO, times(parkingSpotDAOUpdateTimes)).updateParking(any(ParkingSpot.class));
            verify(ticketDAO, times(ticketDAOSaveTimes)).saveTicket(any(Ticket.class));
    	}
    	
        /**
         * For parking's slots full, method processIncomingVehicle should only use
         * one time InputReaderUtil, ParkingSpotDAO and nothing else
         */
    	@Test
        @DisplayName("Parking's slots full")
        public void processIncomingVehicleParkingSlotsFull(){
        	//GIVEN
    		int inputReaderUtilReadSelectTimes = 0;
        	int parkingSpotDAOGetTimes = 0;
        	int inputReaderUtilReadRegNumTimes = 0;
        	int parkingSpotDAOUpdateTimes = 0;
        	int ticketDAOSaveTimes = 0;

    		when(inputReaderUtil.readSelection()).thenReturn(1); // type = CAR
        	inputReaderUtilReadSelectTimes++; //=1
        	
    		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);
        	parkingSpotDAOGetTimes++; //=1
    		//parkingTypeCaptor picked up 1 ParkingType's element
        	
    		/*Else shouldn't be used
        	 *and returns to menu, use DAO to read data and Exception caught*/

            //WHEN & Asserts that Exception was caught
            assertDoesNotThrow(() -> parkingService.processIncomingVehicle());
            
            //THEN
            //Verify mocks are used or never
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
            	assertThat(parkingTypeCaptor.getValue().toString()).isEqualTo("CAR");
            }
    	}
    	
        /**
         * For invalid vehicle's registration number, method processIncomingVehicle should only use
         * one time InputReaderUtil, ParkingSpotDAO and nothing else
         */
    	@Test
        @DisplayName("Vehicle's registration number is invalid")
        public void processIncomingVehicleRegNumberInvalid(){
        	//GIVEN
    		int inputReaderUtilReadSelectTimes = 0;
        	int parkingSpotDAOGetTimes = 0;
        	int inputReaderUtilReadRegNumTimes = 0;
        	int parkingSpotDAOUpdateTimes = 0;
        	int ticketDAOSaveTimes = 0;

    		when(inputReaderUtil.readSelection()).thenReturn(1); // type = CAR
        	inputReaderUtilReadSelectTimes++; //=1
        	
    		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        	parkingSpotDAOGetTimes++; //=1
    		//parkingTypeCaptor picked up 1 ParkingType's element
        	
        	try {
				when(inputReaderUtil.readVehicleRegistrationNumber()).thenThrow(new IllegalArgumentException("Invalid input provided"));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
        	inputReaderUtilReadRegNumTimes++; //=1
        	
       		/*Else shouldn't be used
        	 *and returns to menu, use DAO to read data and Exception caught*/

            //WHEN & Asserts that Exception was caught
            assertDoesNotThrow(() -> parkingService.processIncomingVehicle());
            
            //THEN
            //Verify mocks are used or never
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
            	assertThat(parkingTypeCaptor.getValue().toString()).isEqualTo("CAR");
            }
    	}	
    }
    
    @Nested
    @Tag("CornerCasesExitingVehicle")
    @DisplayName("Corner cases exiting vehicle")
    class cornerCasesExitingVehicle {
    	
    }
}
/*  , " ", "FareThrowsE" , "UptDAOTicketFail" ,

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

//verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
} */
