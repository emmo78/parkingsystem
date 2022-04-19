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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
    
    private Viewer viewer;
    private ParkingType parkingType; //enumeration can't be mocked
    private ParkingSpot parkingSpot; //Model so don't mock
    private Ticket ticket; //Model so don't mock
    
	ArgumentCaptor<ParkingType> parkingTypeCaptor;
	ArgumentCaptor<ParkingSpot> parkingSpotCaptor;
	ArgumentCaptor<Ticket> ticketCaptor;

    @BeforeEach
    private void setUpPerTest() {
        try {
            //when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            //ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            ticket = new Ticket();
            viewer = new ViewerImpl();
            //ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            //ticket.setParkingSpot(parkingSpot);
            //ticket.setVehicleRegNumber("ABCDEF");
            ////when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            ////when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            //when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, viewer);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @AfterEach
    private void undefPerTest() {
    	parkingType = null;
    	parkingSpot = null;
    	ticket = null;
    	viewer = null;
    	parkingTypeCaptor = null;
    	parkingSpotCaptor = null;
    	ticketCaptor = null;
      }
    
    @ParameterizedTest(name ="Incomming vehicle, input = {0} so Type = {1} and RegistrationNumber = {2}")
    @CsvSource({"1,CAR,CARREG","2,BIKE,BIKEREG","-1, , ","3,CAR, "})
    public void processIncomingVehicleTest(int input, String type, String regNumber){
    	//GIVEN
    	int inputReaderUtilReadSelectTimes = 0;
    	int parkingSpotDAOGetTimes = 0;
    	int inputReaderUtilReadRegNumTimes =0;
    	int parkingSpotDAOUpdateTimes = 0;
    	int ticketDAOSaveTimes = 0;
        try {
    		switch(input) {
	    	case 1 : 
	    		when(inputReaderUtil.readSelection()).thenReturn(1);
	    		parkingType = ParkingType.CAR;
	    		when(parkingSpotDAO.getNextAvailableSlot(parkingType)).thenReturn(1); // 1 to 3 for CAR
	    		parkingTypeCaptor = ArgumentCaptor.forClass(ParkingType.class);
	    		parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
	    		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNumber);
	    		when(parkingSpotDAO.updateParking(parkingSpot)).thenReturn(true);
	    		parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
	            ticket.setParkingSpot(parkingSpot);
	            ticket.setVehicleRegNumber(regNumber);
	            ticket.setPrice(0);
	            ticket.setInTime(new Date()); //System.currentTimeMillis() - (60*60*1000)
	            ticket.setOutTime(null);
	            when(ticketDAO.saveTicket(ticket)).thenReturn(true);
	            ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
	        	inputReaderUtilReadSelectTimes = 1;
	        	parkingSpotDAOGetTimes = 1;
	        	inputReaderUtilReadRegNumTimes = 1;
	        	parkingSpotDAOUpdateTimes = 1;
	        	ticketDAOSaveTimes = 1;
	            break;
	            
	    	case 2 : 
	    		when(inputReaderUtil.readSelection()).thenReturn(2);
	    		parkingType = ParkingType.BIKE;
	    		when(parkingSpotDAO.getNextAvailableSlot(parkingType)).thenReturn(4); // 4 to 5 for BIKE
	    		parkingTypeCaptor = ArgumentCaptor.forClass(ParkingType.class);
	    		parkingSpot = new ParkingSpot(4, ParkingType.BIKE,false);
	    		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNumber);
	    		when(parkingSpotDAO.updateParking(parkingSpot)).thenReturn(true);
	    		parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(regNumber);
                ticket.setPrice(0);
                ticket.setInTime(new Date());//System.currentTimeMillis() - (60*60*1000)
                ticket.setOutTime(null);
                when(ticketDAO.saveTicket(ticket)).thenReturn(true);
	            ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
	        	inputReaderUtilReadSelectTimes = 1;
	        	parkingSpotDAOGetTimes = 1;
	        	inputReaderUtilReadRegNumTimes = 1;
	        	parkingSpotDAOUpdateTimes = 1;
	        	ticketDAOSaveTimes = 1;
	    		break;
	    		
	    	case 3 :
	    		when(inputReaderUtil.readSelection()).thenReturn(1);
	    		parkingType = ParkingType.CAR;
	    		when(parkingSpotDAO.getNextAvailableSlot(parkingType)).thenReturn(2); // 1 to 3 for CAR
	    		parkingTypeCaptor = ArgumentCaptor.forClass(ParkingType.class);
	    		parkingSpot = new ParkingSpot(2, ParkingType.CAR,true); //because throws Exception, doesn't set false
	    		when(inputReaderUtil.readVehicleRegistrationNumber()).thenThrow(new IllegalArgumentException("Invalid input provided"));
	    		// -> exception caught then return to menu, write nothing with DAOs 
	        	inputReaderUtilReadSelectTimes = 1;
	        	parkingSpotDAOGetTimes = 1;
	        	inputReaderUtilReadRegNumTimes = 1;
	    		break;
	    	
	    	default :
	    		when(inputReaderUtil.readSelection()).thenReturn(-1);
	    		parkingType = null;
	    		parkingSpot = null;
	        	inputReaderUtilReadSelectTimes = 1;
	    		// -> then return to menu, write nothing with DAOs
	    		// same if parking is full
	        }
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
        
        //WHEN
        parkingService.processIncomingVehicle();
        
        //THEN
        try {
        	verify(inputReaderUtil, times(inputReaderUtilReadSelectTimes)).readSelection();
	        verify(parkingSpotDAO, times(parkingSpotDAOGetTimes)).getNextAvailableSlot(any(ParkingType.class));
	        verify(inputReaderUtil, times(inputReaderUtilReadRegNumTimes)).readVehicleRegistrationNumber();
	        verify(parkingSpotDAO, times(parkingSpotDAOUpdateTimes)).updateParking(any(ParkingSpot.class));
	        verify(ticketDAO, times(ticketDAOSaveTimes)).saveTicket(any(Ticket.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
        
        if(parkingSpotDAOGetTimes == 1) {
	        verify(parkingSpotDAO, times(parkingSpotDAOGetTimes)).getNextAvailableSlot(parkingTypeCaptor.capture());
        	assertThat(parkingTypeCaptor.getValue()).isEqualTo(parkingType);
        }
        
        if(parkingSpotDAOUpdateTimes == 1) {
	        verify(parkingSpotDAO, times(parkingSpotDAOUpdateTimes)).updateParking(parkingSpotCaptor.capture());
        	assertThat(parkingSpotCaptor.getValue()).usingRecursiveComparison().isEqualTo(parkingSpot);
        }
        
       if(ticketDAOSaveTimes == 1) {
	        verify(ticketDAO, times(ticketDAOSaveTimes)).saveTicket(ticketCaptor.capture());
        	assertThat(ticketCaptor.getValue()).usingRecursiveComparison().ignoringFields("inTime").isEqualTo(ticket);
        } /*field/property 'inTime' differ:
				- actual value  : 2022-04-19T13:06:52.852 (java.util.Date)
				- expected value: 2022-04-19T13:06:52.848 (java.util.Date)*/
    }
    
    
    @ParameterizedTest(name ="Exiting vehicle, ")
    @ValueSource(strings = {"REGNUM"," "})
    public void processExitingVehicleTest(String regNum){
    	//GIVEN
    	
    	
    	
    	
    	parkingService.processExitingVehicle();
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
    }

}
