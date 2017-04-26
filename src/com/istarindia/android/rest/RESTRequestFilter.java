package com.istarindia.android.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.istarindia.apps.services.AppEncryptionService;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class RESTRequestFilter implements ContainerRequestFilter{

	private String serverConfig;
	private static final String LOGIN_AUTHORIZATION_HEADER = "TALENTIFY ROCKS";
	public RESTRequestFilter(){
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
	public void filter(ContainerRequestContext requestContext) throws IOException {		
        System.out.println("Request Recieved-> Decrypting");
		long previousTime = System.currentTimeMillis();
		String httpAuthorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
		System.out.println(httpAuthorizationHeader);
		if(serverConfig.equals("prod")){
			try{
				if(httpAuthorizationHeader==null){
					throw new Exception();
				}
					AppEncryptionService appEncryptionService = new AppEncryptionService();
					String decryptedValue = appEncryptionService.decrypt(httpAuthorizationHeader);
										
					if(!LOGIN_AUTHORIZATION_HEADER.equals(decryptedValue)){
						String token = decryptedValue.substring(0, 20); //authorizationToken
						String istarUserIdAsString = decryptedValue.substring(20); //IStarUserId
						Integer istarUserId = Integer.parseInt(istarUserIdAsString);
						
						boolean isAuthorized = validateToken(istarUserId, token);
						
						if(!isAuthorized){
							throw new Exception();
						}
					}
										
					System.err.println("REST REQUEST FILTER FOR AUTHORIZATION " + "Time->"+(System.currentTimeMillis()-previousTime));	
					Scanner encryptedStream = new Scanner(requestContext.getEntityStream());
					String encryptedString = encryptedStream.hasNext() ? encryptedStream.next() : null;
					encryptedStream.close();
					System.out.println("encryptedString->"+encryptedString);
					String decryptedString = appEncryptionService.decrypt(encryptedString);
					System.out.println("decryptedString->"+decryptedString);
					
					InputStream decryptedStream = new ByteArrayInputStream(decryptedString.getBytes());
					requestContext.setEntityStream(decryptedStream);	
		
			}catch(Exception e){
				e.printStackTrace();
				requestContext.abortWith(
		                Response.status(Response.Status.UNAUTHORIZED).build());
			}
		}else{
			System.out.println("Server is DEv");
		}
		System.err.println("REST REQUEST FILTER FOR ENCRYPTION " + "Time->"+(System.currentTimeMillis()-previousTime));
	}

	private boolean validateToken(int istarUserId, String authenticationToken) throws Exception {

		boolean isValid = true;
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(istarUserId);

		System.out.println(istarUser.getEmail());
		System.out.println("DB Token: "+istarUser.getAuthToken() + "REQUEST Token->" + authenticationToken);
		if (istarUser == null || !istarUser.getAuthToken().equals(authenticationToken)) {
			throw new Exception();
		}
		
		System.out.println("Request Validated");
		return isValid;
	}
}
