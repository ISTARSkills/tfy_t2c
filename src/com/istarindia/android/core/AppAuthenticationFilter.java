package com.istarindia.android.core;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

public class AppAuthenticationFilter implements ContainerRequestFilter{

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		
		String httpAuthorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
		
		try{
		if(httpAuthorizationHeader==null || !httpAuthorizationHeader.startsWith("oxygen")){
			throw new Exception();
		}
		
		String authenticationToken = httpAuthorizationHeader.substring("oxygen".length()).trim();
		
		validateToken(1, authenticationToken);
		
		}catch(Exception e){
			requestContext.abortWith(
	                Response.status(Response.Status.UNAUTHORIZED).build());
		}
	}

	private boolean validateToken(int istarUserId, String authenticationToken) throws Exception {

		boolean isValid = true;
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(istarUserId);

		if (istarUser == null || !istarUser.getAuthToken().equals(authenticationToken)) {
			throw new Exception();
		}

		return isValid;
	}
}
