package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.view.Viewer;
import com.parkit.parkingsystem.view.ViewerImpl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig(); //static for All methods
    private static ParkingSpotDAO parkingSpotDAO; //static for All methods
    private static TicketDAO ticketDAO; //static for All methods
    private static DataBasePrepareService dataBasePrepareService; //static for All methods

    @Mock
    private static InputReaderUtil inputReaderUtil;
    
    private final Viewer viewer = new ViewerImpl(); // Viewer instance    

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.setDataBaseConfig(dataBaseTestConfig);
        ticketDAO = new TicketDAO();
        ticketDAO.setDataBaseConfig(dataBaseTestConfig);
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){
        parkingSpotDAO.setDataBaseConfig(null);
        parkingSpotDAO = null;
        ticketDAO.setDataBaseConfig(null);
        ticketDAO = null;
        dataBasePrepareService = null;
    }

    @Test
    @DisplayName("check that a ticket is actually saved in DB and Parking table is updated with availability (false)")
    public void testParkingACar(){
        //GIVEN
    	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, viewer);
    	TestResult tResult = new TestResult(); //use nested class, see below
        Date expectedInTime = new Date();
    	
    	//WHEN
        parkingService.processIncomingVehicle();
        
        //THEN
        Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection(); //throws ClassNotFoundException, SQLException will be caught see catch
            PreparedStatement ps = con.prepareStatement("select p.PARKING_NUMBER, p.TYPE, p.AVAILABLE, "
            		+ "t.PARKING_NUMBER, t.VEHICLE_REG_NUMBER, t.PRICE, t.IN_TIME, t.OUT_TIME "
            		+ "from parking p inner join ticket t on p.PARKING_NUMBER = t.PARKING_NUMBER");
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
            	tResult.parkingNumber = rs.getInt(1); // = 1
            	tResult.type = rs.getString(2); // = "CAR"
            	tResult.available = rs.getBoolean(3); // = false
            	tResult.parkingSpot = rs.getInt(4); // = 1
            	tResult.vehicleRegNumber = rs.getString(5); // = "ABCDEF" 
            	tResult.price = rs.getDouble(6); // = 0,00 (double)
            	tResult.inTime = new Date(rs.getTimestamp(7).getTime()); // = expectedInTime in java.util.Date format
            	tResult.outTime = rs.getTimestamp(8); // = null
            }
            dataBaseTestConfig.closeResultSet(rs);
            dataBaseTestConfig.closePreparedStatement(ps);
        }catch (Exception ex){
            viewer.println(ex.toString());
        }finally {
            dataBaseTestConfig.closeConnection(con);
        }
        
        assertThat(tResult)
        	.extracting(
        			tR -> tR.parkingNumber,
        			tR -> tR.type,
        			tR -> tR.available,
        			tR -> tR.parkingSpot,
        			tR -> tR.vehicleRegNumber,
        			tR -> tR.price,
        			tR -> tR.inTime.toString().substring(0,17), //To avoid imprecision on few seconds
        			tR -> tR.outTime).
        	containsExactly(
        			1,
        			"CAR",
        			false,
        			1,
        			"ABCDEF",
        			0D, //D to cast to double because can't use ','
        			expectedInTime.toString().substring(0,17), // = "dow mon dd hh:mm:"
        			null);
    }

    @Test
    @Disabled
    public void testParkingLotExit(){
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, viewer);
        parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database
    }
    
    private class TestResult {
        int parkingNumber; //Primary Key
        String type;
        boolean available;

        int parkingSpot; //Foreign Key
        String vehicleRegNumber;
        double price;
        Date inTime;
        Date outTime;
    }
}
