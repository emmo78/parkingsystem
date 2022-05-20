package com.parkit.parkingsystem.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.view.Viewer;
import com.parkit.parkingsystem.view.ViewerImpl;

import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

@ExtendWith(MockitoExtension.class)
public class FreeThirtyMinutesOrLessSteps {

    private DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private ParkingSpotDAO parkingSpotDAO= new ParkingSpotDAO();
    private TicketDAO ticketDAO = new TicketDAO();
    private DataBasePrepareService dataBasePrepareService  = new DataBasePrepareService();
    
    private InputReaderUtil inputReaderUtil = mock(InputReaderUtil.class); //To mock user input (this class itself uses final class Scanner)
    
    private Viewer viewer = new ViewerImpl();
    ParkingService parkingService;
	Date expectedInTime;
    
	@Given("utilisateur {string} est garé depuis {int} minutes;")
	public void userRegNumParkedSince(String regNum, int min) {
		parkingSpotDAO.setDataBaseConfig(dataBaseTestConfig);
		ticketDAO.setDataBaseConfig(dataBaseTestConfig);
 		dataBasePrepareService.clearDataBaseEntries(); // "update parking set available = true" , "truncate table ticket"
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, viewer);
		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNum);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	expectedInTime = new Date(System.currentTimeMillis() - (min*60 * 1000));
    	TestResult tR = new TestResult(1, "CAR", false, 1, regNum, 0d, expectedInTime, null);
    	// <regNum> in park <min> minutes ago on spot 1
        Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection(); //throws ClassNotFoundException, SQLException will be caught see catch
            PreparedStatement psT = con.prepareStatement("insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(?,?,?,?,?)");
            PreparedStatement psP = con.prepareStatement("update parking set available = ? where PARKING_NUMBER = ?");
        	try {
	            psT.setInt(1,tR.parkingSpot);
	            psT.setString(2, tR.vehicleRegNumber);
	            psT.setDouble(3, tR.price);
	            psT.setTimestamp(4, new Timestamp(tR.inTime.getTime()));
	            psT.setTimestamp(5, (tR.outTime == null)?null: (new Timestamp(tR.outTime.getTime())));
	            psT.execute();
	            if(!tR.available) { // if availability false
	                psP.setBoolean(1, tR.available);
	                psP.setInt(2, tR.parkingNumber);
	                psP.executeUpdate();
	            }
        	} catch (Exception ex){
        		ex.printStackTrace();
        	}
            dataBaseTestConfig.closePreparedStatement(psT);
            dataBaseTestConfig.closePreparedStatement(psP);
        } catch (Exception ex){
        	ex.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(con);
        }  	
        tR=null; // Nullify tR
        con = null;
	}
	
	@When("il sort;")
	public void userParkingExit() {
		parkingService.processExitingVehicle();
	}
	
	@Then("le ticket persisté a une plaque {string}, un tarif à {double} et la place persistée a une disponibilité {string};")
	public void test(String regNum, double fare, String availability) {
    	Date expectedOutTime = new Date();
    	TestResult tResult = new TestResult();
    	Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection(); //throws ClassNotFoundException, SQLException will be caught see catch
            PreparedStatement ps = con.prepareStatement("select p.PARKING_NUMBER, p.TYPE, p.AVAILABLE, "
            		+ "t.PARKING_NUMBER, t.VEHICLE_REG_NUMBER, t.PRICE, t.IN_TIME, t.OUT_TIME "
            		+ "from parking p inner join ticket t on p.PARKING_NUMBER = t.PARKING_NUMBER "
            		+ "where t.VEHICLE_REG_NUMBER = ?");
            ps.setString(1, regNum);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                tResult.parkingNumber = rs.getInt(1);
            	tResult.type = rs.getString(2);
            	tResult.available = rs.getBoolean(3);
            	tResult.parkingSpot = rs.getInt(4);
            	tResult.vehicleRegNumber = rs.getString(5);
            	tResult.price = rs.getDouble(6);
            	tResult.inTime = new Date(rs.getTimestamp(7).getTime());
            	tResult.outTime = (rs.getTimestamp(8) == null)?null: new Date(rs.getTimestamp(8).getTime());
            }
            dataBaseTestConfig.closeResultSet(rs);
            dataBaseTestConfig.closePreparedStatement(ps);
        }catch (Exception ex){
        	ex.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(con);
        }
 
        try {
			verify(inputReaderUtil, times(1)).readVehicleRegistrationNumber(); // 1 time used
		} catch (Exception e) {
			e.printStackTrace();
		}
        assertThat(tResult)
        	.extracting(
        			tR -> tR.parkingNumber,
        			tR -> tR.type,
        			tR -> tR.available,
        			tR -> tR.parkingSpot,
        			tR -> tR.vehicleRegNumber,
        			tR -> tR.price) 
        	.containsExactly(
        			1,
        			"CAR",
        			Boolean.valueOf(availability).booleanValue(),
        			1,
        			regNum,
        			fare);
        assertThat(tResult.inTime).isCloseTo(expectedInTime, 2000);
        assertThat(tResult.outTime).isCloseTo(expectedOutTime, 2000);
        	/* Verifies that the output Dates are close to the expected Dates by less than delta (expressed in milliseconds),
        	 * if difference is equal to delta it's ok. */
        
        parkingSpotDAO.setDataBaseConfig(null);
        parkingSpotDAO = null;
        ticketDAO.setDataBaseConfig(null);
        ticketDAO = null;
        dataBasePrepareService = null;
        viewer = null;
        parkingService = null;
    }

	
    /**
     * Nested class with fields to collect ResulSet fields
     * @author Olivier MOREL
     *
     */
    class TestResult {
		int parkingNumber; //Primary Key
        String type;
        boolean available;

        int parkingSpot; //Foreign Key
        String vehicleRegNumber;
        double price;
        Date inTime;
        Date outTime;
 
        TestResult() {
 			this.parkingNumber = 0;
 			this.type = null;
 			this.available = false;
 			this.parkingSpot = 0;
 			this.vehicleRegNumber = null;
 			this.price = 0d;
 			this.inTime = null;
 			this.outTime = null;
        }
 		
        TestResult(int parkingNumber, String type, boolean available, int parkingSpot, String vehicleRegNumber,
				double price, Date inTime, Date outTime) {
			this.parkingNumber = parkingNumber;
			this.type = type;
			this.available = available;
			this.parkingSpot = parkingSpot;
			this.vehicleRegNumber = vehicleRegNumber;
			this.price = price;
			this.inTime = inTime;
			this.outTime = outTime;
		}
    }
}
