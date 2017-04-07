package com.istarindia.android.rest;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.istarindia.android.pojo.StudentProfile;
import com.istarindia.android.utility.AppPOJOUtility;
import com.istarindia.apps.services.AppServices;
import com.istarindia.apps.services.BatchStudentsServices;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

@Path("user")
public class AppIstarUserService {

	@POST
	@Path("create")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createUser(@FormParam("email") String email, @FormParam("password") String password,
			@FormParam("mobile") Long mobile) {

		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.createIstarUser(email, password, mobile);

		if (istarUser == null) {
			// User Email or Mobile already registered
			return Response.status(Response.Status.CONFLICT).build();
		} else {
			return Response.status(Response.Status.CREATED).build();
		}
	}

	@GET
	@Path("{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserProfile(@PathParam("userId") int userId) {
		System.out.println("Getting user profile" + userId);
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(userId);

		if (istarUser == null) {
			// User does not exists
			return Response.status(404).build();
		} else {
			AppPOJOUtility androidPOJOUtility = new AppPOJOUtility();
			StudentProfile studentProfile = androidPOJOUtility.getStudentProfile(istarUser);

			return Response.ok(studentProfile).build();
		}
	}

	@PUT
	@Path("reset/password")
	@Produces(MediaType.APPLICATION_JSON)
	public Response resetPassword(@FormParam("mobile") Long mobile, @FormParam("password") String password) {
		System.out.println("Resetting password");
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUserByMobile(mobile);

		if (istarUser == null) {
			return Response.status(404).build();
		} else {
			istarUser = istarUserServices.updateIstarUser(istarUser.getId(), istarUser.getEmail(), password,
					istarUser.getMobile());
			return Response.status(Response.Status.CREATED).build();
		}
	}

	@GET
	@Path("{userId}/mobile")
	@Produces(MediaType.APPLICATION_JSON)
	public Response verifyMobileNumber(@PathParam("userId") int userId, @QueryParam("mobile") Long mobile) {

		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(userId);

		IstarUser mobileIstarUser = istarUserServices.getIstarUserByMobile(mobile);

		if (istarUser == null && mobileIstarUser != null) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		} else {

			Integer otp = null;
			istarUserServices.updateMobile(istarUser.getId(), mobile);

			try {
				AppServices appServices = new AppServices();
				otp = appServices.sendOTP(mobile.toString());
			} catch (Exception e) {
				e.printStackTrace();
				return Response.status(Response.Status.BAD_GATEWAY).build();
			}
			return Response.ok(otp).build();
		}
	}

	@PUT
	@Path("{userId}/mobile")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateMobileNumber(@PathParam("userId") int userId, @FormParam("mobile") Long mobile) {
		System.out.println("Request to update mobile number->" + mobile);
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(userId);

		if (istarUser == null) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		} else {
			System.out.println("Updating mobile number");
			istarUser = istarUserServices.updateMobile(istarUser.getId(), mobile);
			
			System.out.println("Updated mobile number is :" + istarUser.getMobile());
			
			return Response.status(Response.Status.CREATED).build();
		}
	}

	@POST
	@Path("{userId}/batchCode")
	@Produces(MediaType.APPLICATION_JSON)
	public Response assignBatchCode(@PathParam("istarUserId") int istarUserId,
			@FormParam("batchCode") String batchCode) {

		// check if student batch already exists
		BatchStudentsServices batchStudentServices = new BatchStudentsServices();
		batchStudentServices.createBatchStudents(istarUserId, batchCode);

		return Response.status(Response.Status.CREATED).build();
	}
}