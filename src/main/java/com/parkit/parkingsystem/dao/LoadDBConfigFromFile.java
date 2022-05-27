package com.parkit.parkingsystem.dao;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoadDBConfigFromFile implements DBConfigIO {
	
	private static final Logger logger = LogManager.getLogger("LoadDBConfigFromFile");
	private Reader fileReader;
	private final String userDir = System.getProperty("user.dir"); //Projet_4
	private final String fileToRead = "dbProperties.txt";
	private String filePath = userDir+"/"+ fileToRead;

	public LoadDBConfigFromFile() {
		try {
			fileReader = new InputStreamReader(new FileInputStream(filePath), "UTF-8");
		} catch(FileNotFoundException e) {
			logger.error("Error opening file reader",e);
		} catch (UnsupportedEncodingException e) {
			logger.error("Error opening file reader : The Character Encoding is not supported",e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void closeResource() {
		try {
			fileReader.close();
		} catch(IOException e) {
			logger.error("Error closing file reader",e);
		}
	}
	
	@Override
	public Properties getDBProperties() {
		Properties dbProperties = new Properties();
		try {
			dbProperties.load(fileReader);
		} catch(IOException e) {
			logger.error("Error loading dbProperties.txt",e);
		} finally {
			closeResource();
		}
		return dbProperties;
	}

	@Override
	public void setDBProperties(Properties dbProperties) {}
}
