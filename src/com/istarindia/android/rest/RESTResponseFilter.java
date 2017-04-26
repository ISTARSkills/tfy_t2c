package com.istarindia.android.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import com.istarindia.apps.services.AppEncryptionService;

@Provider
public class RESTResponseFilter implements ContainerResponseFilter{

	String serverConfig;
	
	public RESTResponseFilter(){
		try{
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
				if (inputStream != null) {
					properties.load(inputStream);
					serverConfig = properties.getProperty("serverConfig");
					
					System.out.println("serverConfig"+serverConfig);
				}
			} catch (IOException e) {
				e.printStackTrace();
				serverConfig = "dev";
			}
	}
	
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		long previousTime = System.currentTimeMillis();
		if(serverConfig.equals("prod") && responseContext.hasEntity()){
			AppEncryptionService appEncryptionService = new AppEncryptionService();
			String encrypted = appEncryptionService.encrypt((String)responseContext.getEntity());
			System.out.println(encrypted);
			responseContext.setEntity(encrypted, responseContext.getEntityAnnotations(), responseContext.getMediaType());
		}
		System.err.println("REST RESPONSE FILTER FOR ENCRYPTION " + "Time->"+(System.currentTimeMillis()-previousTime));
	}	
}
