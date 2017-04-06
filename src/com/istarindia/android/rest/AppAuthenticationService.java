package com.istarindia.android.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.istarindia.android.pojo.StudentProfile;
import com.istarindia.android.utility.AppPOJOUtility;
import com.istarindia.android.utility.AppUtility;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.utils.user.IstarUserServices;
import com.viksitpro.core.pojo.recruiter.IstarUserPOJO;

@Path("AuthenticateUser")
public class AppAuthenticationService {

	@POST
	@Path("login")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response loginUser(@FormParam("email") String email, @FormParam("password") String password) {

		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUserByEmail(email);

		try {
			if (istarUser == null || !istarUser.getPassword().equals(password)) {
				throw new Exception();
			}

			istarUser = assignToken(istarUser);

			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
			StudentProfile studentProfile = appPOJOUtility.getStudentProfile(istarUser);

			return Response.ok(studentProfile).build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	@POST
	@Path("login/social")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response loginUserWithSocialMedia(@FormParam("email") String email, @FormParam("mobile") Long mobile,
			@FormParam("token") String token, @FormParam("socialMedia") String socialMedia) {

		System.out.println("Logged In from Social Media--> Mobile Number is:" + mobile);

		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUserByEmail(email);

		try {
			if (istarUser == null) {
				istarUser = istarUserServices.createIstarUser(email, "test123", mobile, token, socialMedia);
			}

			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
			StudentProfile studentProfile = appPOJOUtility.getStudentProfile(istarUser);

			return Response.ok(studentProfile).build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	@POST
	@Path("test")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response test(IstarUserPOJO istarUserPOJO) {

		System.out.println("Accepting JSON method");

		System.out.println(istarUserPOJO.getEmail());
		System.out.println(istarUserPOJO.getAuthenticationToken());

		System.out.println("Returning JSON");
		return Response.ok(istarUserPOJO).build();
	}

	private IstarUser assignToken(IstarUser istarUser) {

		String authenticationToken = AppUtility.getRandomString(20);

		IstarUserServices istarUserServices = new IstarUserServices();
		istarUser = istarUserServices.updateAuthenticationTokenForIstarUser(istarUser, authenticationToken);

		return istarUser;
	}
}
