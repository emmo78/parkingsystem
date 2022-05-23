package com.parkit.parkingsystem.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

/**
 * 
 * @author Olivier MOREL
 *
 */
public class TicketDAOSIT {
    private DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private DataBasePrepareService dataBasePrepareService; //static for @BeforeAll and @AfterAll
	private ParkingSpot parkingSpot;
    private List<Ticket> tickets;
	private Calendar inTimeCal;
	private Calendar outTimeCal;
	private TicketDAO ticketDAO; //SIT
    
    /**
     * Before Each Test initialize Class Under Test and 
     */
	@BeforeEach
    private void setUpPerTest() {
    	dataBaseTestConfig = new DataBaseTestConfig();
		ticketDAO = new TicketDAO();
		ticketDAO.setDataBaseConfig(dataBaseTestConfig);
		dataBasePrepareService  = new DataBasePrepareService();
		dataBasePrepareService.clearDataBaseEntries(); // "update parking set available = true" , "truncate table ticket"
    	parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
		tickets = new ArrayList<>();
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
    	dataBaseTestConfig = null;
		ticketDAO.setDataBaseConfig(null);
    	ticketDAO = null;
		dataBasePrepareService  = null;
    	tickets = null;
    	parkingSpot = null;
    	inTimeCal = null;
    	outTimeCal = null;
     }
    
    /**
     * Tests if method calculateFare gives corrects results
     * with nominal cases
     * @param min : how long the vehicle parks (minutes)
     * @param coefFare : = min/60 because rate/h
     * @param type : vehicle's type
     */
    @ParameterizedTest(name = "{0} times for user FID in park last month should be {1} for recurrent user {2}")
    @CsvSource({"11,true,FID","10,false,FID","11,false,DIF"})
    @DisplayName("Nominal cases")
    public void isRecurrentUserTicketTestShouldBeTrueIfMoreTenTimesLastMonth(int times, String isRecurrentS, String regNum) {
    	
    	//GIVEN
		inTimeCal = GregorianCalendar.getInstance();
		inTimeCal.setTimeInMillis(inTimeCal.getTimeInMillis()-3600*1000);
		outTimeCal = GregorianCalendar.getInstance();
		inTimeCal.set(Calendar.DATE, 1); //set date at the begin of month
		outTimeCal.set(Calendar.DATE, 1);
		inTimeCal.add(Calendar.MONTH, -1); //One month ago
		outTimeCal.add(Calendar.MONTH, -1); //Add rule example : 01/01/2022 Calling add(Calendar.MONTH, -1) sets the calendar to 01/12/2021
		Ticket ticket;
		for(int i=1; i<=times; i++) { //loops = times
			ticket = new Ticket(); //Declare and initialize a new pointer (reference value to object)
			ticket.setParkingSpot(parkingSpot);
			ticket.setVehicleRegNumber(regNum);
			ticket.setPrice(0);
			ticket.setInTime(inTimeCal.getTime());
			ticket.setOutTime(outTimeCal.getTime());
			tickets.add(ticket); //The pointer (reference value to object) is added in the List
			ticket = null; //Nullify pointer to avoid usage in the next loop 
			inTimeCal.roll(Calendar.DATE, 2); //add 2 days so 11*2 = 22 days
			outTimeCal.roll(Calendar.DATE, 2); //Roll rule : Larger fields (here MONTH) are unchanged after the call.
		}
		
		Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection(); //throws ClassNotFoundException, SQLException will be caught see catch
            PreparedStatement psT = con.prepareStatement("insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(?,?,?,?,?)");
            tickets.forEach(t -> {
            	try {
		            psT.setInt(1, t.getParkingSpot().getId());
		            psT.setString(2, t.getVehicleRegNumber());
		            psT.setDouble(3, t.getPrice());
		            psT.setTimestamp(4, new Timestamp(t.getInTime().getTime()));
		            psT.setTimestamp(5, new Timestamp(t.getInTime().getTime()));
		            psT.execute();
            	} catch (Exception ex){
            		ex.printStackTrace();
            	}
            });
            dataBaseTestConfig.closePreparedStatement(psT);
        } catch (Exception ex){
        	ex.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(con);
        }
        con = null;
        tickets.clear(); // Clear the list
        
        //WHEN
        Boolean isRecurent = ticketDAO.isRecurrentUserTicket();
        
        //THEN
        assertThat(isRecurent).isEqualTo(Boolean.valueOf(isRecurrentS));
    }
}
