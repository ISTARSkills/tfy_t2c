package com.istarindia.android.rest;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

@AppSecured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AppAuthenticationFilter implements ContainerRequestFilter{

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {		
        
		String httpAuthorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
		System.out.println(httpAuthorizationHeader);
		try{
		if(httpAuthorizationHeader==null || !httpAuthorizationHeader.startsWith("oxygen")){
			System.out.println("Invalid Header");
			throw new Exception();
		}
		
		String authenticationToken = httpAuthorizationHeader.substring("oxygen".length()).trim();
		System.out.println(authenticationToken);
		validateToken(4013, authenticationToken);
		
		}catch(Exception e){
			requestContext.abortWith(
	                Response.status(Response.Status.UNAUTHORIZED).build());
		}
	}

	private boolean validateToken(int istarUserId, String authenticationToken) throws Exception {

		boolean isValid = true;
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(istarUserId);

		System.out.println(istarUser.getEmail());
		System.out.println("DB Token: "+istarUser.getAuthToken());
		if (istarUser == null || !istarUser.getAuthToken().equals(authenticationToken)) {
			throw new Exception();
		}
		
		System.out.println("Validated");
		return isValid;
	}
}
