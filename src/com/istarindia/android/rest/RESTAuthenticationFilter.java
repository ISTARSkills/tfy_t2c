package com.istarindia.android.rest;

import java.io.IOException;

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

@AppSecured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class RESTAuthenticationFilter implements ContainerRequestFilter{

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {		
        
		String httpAuthorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
		System.out.println(httpAuthorizationHeader);
		try{
			if(httpAuthorizationHeader==null){
				throw new Exception();
			}
				AppEncryptionService appEncryptionService = new AppEncryptionService();
				String decryptedValue = appEncryptionService.decrypt(httpAuthorizationHeader);
				
				String token = decryptedValue.substring(0, 20); //authorizationToken
				String istarUserIdAsString = decryptedValue.substring(20); //IStarUserId
				Integer istarUserId = Integer.parseInt(istarUserIdAsString);
				
				boolean isAuthorized = validateToken(istarUserId, token);
				
				if(!isAuthorized){
					throw new Exception();
				}				
	
		}catch(Exception e){
			e.printStackTrace();
			requestContext.abortWith(
	                Response.status(Response.Status.UNAUTHORIZED).build());
		}
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
