package com.istarindia.android.rest;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

@RESTSecured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class RESTAuthenticationFilter implements ContainerRequestFilter{

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {		
        
/*		String httpAuthorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
		//ViksitLogger.logMSG(this.getClass().getName(),httpAuthorizationHeader);
		try{
		if(httpAuthorizationHeader==null || !httpAuthorizationHeader.startsWith("oxygen")){
			//ViksitLogger.logMSG(this.getClass().getName(),"Invalid Header");
			throw new Exception();
		}
		
		String authenticationToken = httpAuthorizationHeader.substring("oxygen".length()).trim();
		//ViksitLogger.logMSG(this.getClass().getName(),authenticationToken);
		validateToken(4013, authenticationToken);
		
		}catch(Exception e){
			e.printStackTrace();
			requestContext.abortWith(
	                Response.status(Response.Status.UNAUTHORIZED).build());
		}*/
	}

	public boolean validateToken(int istarUserId, String authenticationToken) throws Exception {

		boolean isValid = true;
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(istarUserId);

		//ViksitLogger.logMSG(this.getClass().getName(),istarUser.getEmail());
		//ViksitLogger.logMSG(this.getClass().getName(),"DB Token: "+istarUser.getAuthToken());
		if (istarUser == null || !istarUser.getAuthToken().equals(authenticationToken)) {
			throw new Exception();
		}
		
		//ViksitLogger.logMSG(this.getClass().getName(),"Validated");
		return isValid;
	}
}
