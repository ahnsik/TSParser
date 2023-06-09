package com.aircode.util;
import java.io.File;
import java.net.URI;
import java.net.URL;

import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogManager {
    private final static String NAME_LOG4J_PROPERTIES = "log4j.properties";
    private static LogManager instance  = null;
    private LogManager () {        
        try {
            URL url = ClassLoader.getSystemResource(LogManager.NAME_LOG4J_PROPERTIES);
            URI uri = url.toURI();
            System.out.println(LogManager.NAME_LOG4J_PROPERTIES + " uri : " + uri);
            File file = new File(uri);
            System.out.println(LogManager.NAME_LOG4J_PROPERTIES + " exist : " + file.exists());
            Configurator.initialize("LogManager", ClassLoader.getSystemClassLoader(), ClassLoader.getSystemResource("log4j.properties").toURI()); 
        } catch (Exception e) {
            e.printStackTrace();
        }
               
    }
    public static LogManager getInstance () {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }
    public Logger getLogger (Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}
