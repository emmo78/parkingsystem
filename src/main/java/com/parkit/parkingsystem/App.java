package com.parkit.parkingsystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.service.InteractiveShell;

/**
 * parking system's entry point for run into Eclipse
 * @author Olivier MOREL
 *
 */
public class App {
    private static final Logger logger = LogManager.getLogger("App");
    
    /**
     * Main method to launch the app.
     * Call static method loadInterface() from InteractiveShell class
     * in service package 
     * @param args not used in this method
     */
    
    public static void main(String args[]){
        logger.info("Initializing Parking System");
        InteractiveShell interactiveShellInstance = InteractiveShell.getInstance();
        interactiveShellInstance.loadInterface();
    }
}
