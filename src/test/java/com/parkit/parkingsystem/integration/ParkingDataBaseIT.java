package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.DBConstants;
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
import java.util.ArrayList;
import java.util.List;

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
    @DisplayName("check that a ticket is actually saved in DB and Parking table is updated with availabilities (false, true, true, true, true)")
    public void testParkingACar(){
        //GIVEN
    	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, viewer);

    	List<Boolean> resultAvailables;
    	
    	//WHEN
        parkingService.processIncomingVehicle();
        
        
        //THEN
        Connection con = null;
        resultAvailables = new ArrayList<>();
        
/*        try {
            con = dataBaseTestConfig.getConnection(); //throws ClassNotFoundException, SQLException will be caught see catch
            PreparedStatement ps = con.prepareStatement("select p.AVAILABLE from parking p");
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                resultAvailables.add(rs.getBoolean("AVAILABLE"));
            }
            dataBaseTestConfig.closeResultSet(rs);
            dataBaseTestConfig.closePreparedStatement(ps);
        }catch (Exception ex){
            viewer.println(ex.toString());
        }finally {
            dataBaseTestConfig.closeConnection(con);
        }       
*/        
        
        try {
            con = dataBaseTestConfig.getConnection(); //throws ClassNotFoundException, SQLException will be caught see catch
            PreparedStatement ps = con.prepareStatement("select p.AVAILABLE from parking p");
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                resultAvailables.add(rs.getBoolean("AVAILABLE"));
            }
            dataBaseTestConfig.closeResultSet(rs);
            dataBaseTestConfig.closePreparedStatement(ps);
        }catch (Exception ex){
            viewer.println(ex.toString());
        }finally {
            dataBaseTestConfig.closeConnection(con);
        }
        
        assertThat(resultAvailables).containsExactly(false, true, true, true, true);
    }

    @Test
    @Disabled
    public void testParkingLotExit(){
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, viewer);
        parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database
    }

}
