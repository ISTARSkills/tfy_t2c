package com.istarindia.android.notification;

import java.io.InputStream;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.viksitpro.core.logger.ViksitLogger;

@WebListener
public class MyAppServletContextListener implements ServletContextListener{

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		ViksitLogger.logMSG(this.getClass().getName(),"ServletContextListener destroyed");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		try {
			////ViksitLogger.logMSG(this.getClass().getName(),"ServletContextListener starting");
			InputStream targetStream = MyAppServletContextListener.class.getClassLoader().getResourceAsStream("istartv2-fde8335fd63d.json");
			FirebaseOptions options = new FirebaseOptions.Builder().setDatabaseUrl("https://istartv2.firebaseio.com/").setServiceAccount(targetStream).build();	
			FirebaseApp.initializeApp(options);
			//ViksitLogger.logMSG(this.getClass().getName(),"ServletContextListener started t2c");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
