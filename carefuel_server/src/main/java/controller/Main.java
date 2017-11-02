package controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * 
 * Main is the entry class of the application. It starts the webserver (with automatically embedded
 * tomcat servlet container, which means this code can be run as a standalone application) and searches
 * for classes with the Controller annotation in this package and should therefore use the
 * RequestController class for handling all requests.
 *
 */
@SpringBootApplication
public class Main {
	public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
