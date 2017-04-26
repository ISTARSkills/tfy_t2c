package com.istarindia.android.notification;

import java.io.InputStream;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@WebListener
public class MyAppServletContextListener implements ServletContextListener{

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("ServletContextListener destroyed");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		try {
			System.out.println("ServletContextListener starting");
			InputStream targetStream = MyAppServletContextListener.class.getClassLoader().getResourceAsStream("Viksit-ac716147c574.json");
			FirebaseOptions options = new FirebaseOptions.Builder().setDatabaseUrl("https://fir-viksit.firebaseio.com/").setServiceAccount(targetStream).build();	
			FirebaseApp.initializeApp(options);
			System.out.println("ServletContextListener started");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
