package com.istarindia.android.core;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.istarindia.android.utility.AppUtility;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

@Path("AuthenticateUser")
public class AppAuthenticationService {

	@POST
	@Path("issueToken")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response loginUser(@FormParam("email") String email, @FormParam("password") String password) {

		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUserByEmail(email);

		try {
			if (istarUser == null || !istarUser.getPassword().equals(password)) {
				throw new Exception();
			}

			String token = assignToken(istarUser);
			return Response.ok(token).build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	private String assignToken(IstarUser istarUser) {

		String authenticationToken = AppUtility.getRandomString(10);

		IstarUserServices istarUserServices = new IstarUserServices();
		istarUser = istarUserServices.updateAuthenticationTokenForIstarUser(istarUser, authenticationToken);

		return authenticationToken;
	}
}
