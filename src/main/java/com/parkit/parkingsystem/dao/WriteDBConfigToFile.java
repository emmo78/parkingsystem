package com.parkit.parkingsystem.dao;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WriteDBConfigToFile implements DBConfigIO{

	private static final Logger logger = LogManager.getLogger("WriteDBConfigToFile");
	private Writer fileWriter;
	private final String userDir = System.getProperty("user.dir"); //parkingsystem
	private final String fileToWrite = "dbProperties.txt";
	private String filePath = userDir+"/"+ fileToWrite;
	public WriteDBConfigToFile() {
		try {
			fileWriter = new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8");
		} catch(IOException e) {
			logger.error("Error opening file writer",e);
		}
	}
	
	@Override
	public void closeResource() {
		try {
			fileWriter.close();
		} catch(IOException e) {
			logger.error("Error closing file writer",e);
		} finally {
			closeResource();
		}
	}
	
	@Override
	public void setDBProperties(Properties dbProperties) {
		try {
			dbProperties.store(fileWriter, null);
		} catch(IOException e) {
			logger.error("Error writing dbProperties.txt",e);
		}
	}
	
	@Override
	public Properties getDBProperties() {return null;}
}
