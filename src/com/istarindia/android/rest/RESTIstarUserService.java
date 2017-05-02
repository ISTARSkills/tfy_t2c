package com.istarindia.android.rest;

import java.util.HashSet;
import java.util.List;

import javax.ws.rs.Consumes;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.istarindia.android.pojo.ComplexObject;
import com.istarindia.android.pojo.SkillReportPOJO;
import com.istarindia.android.pojo.StudentProfile;
import com.istarindia.android.utility.AppPOJOUtility;
import com.istarindia.android.utility.AppUtility;
import com.istarindia.apps.services.AppBatchGroupServices;
import com.istarindia.apps.services.AppBatchStudentsServices;
import com.istarindia.apps.services.AppComplexObjectServices;
import com.istarindia.apps.services.AppServices;
import com.viksitpro.core.dao.entities.BatchGroup;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Role;
import com.viksitpro.core.dao.entities.UserRole;
import com.viksitpro.core.dao.utils.user.IstarUserServices;
import com.viksitpro.core.dao.utils.user.RoleServices;
import com.viksitpro.core.dao.utils.user.UserRoleServices;

@Path("user")
public class RESTIstarUserService {

	@POST
	@Path("create")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createUser(@FormParam("email") String email, @FormParam("password") String password,
			@FormParam("mobile") Long mobile) {

		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			String authenticationToken = AppUtility.getRandomString(20);
			IstarUser istarUser = istarUserServices.createIstarUser(email, password, mobile, authenticationToken);

			if (istarUser == null) {
				// User Email or Mobile already registered
				return Response.status(Response.Status.CONFLICT).build();
			} else {
				RoleServices roleServices = new RoleServices();
				Role role = roleServices.getRoleByName("STUDENT");

				UserRoleServices userRoleServices = new UserRoleServices();
				UserRole userRole = userRoleServices.createUserRole(istarUser, role, 1);

				HashSet<UserRole> allUserRole = new HashSet<UserRole>();
				allUserRole.add(userRole);

				istarUser.setUserRoles(allUserRole);

				AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
				StudentProfile studentProfile = appPOJOUtility.getStudentProfile(istarUser);

				Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
				String result = gson.toJson(studentProfile);

				return Response.ok(result).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserProfile(@PathParam("userId") int userId) {

		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);

			if (istarUser == null) {
				// User does not exists
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			} else {
				AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
				StudentProfile studentProfile = appPOJOUtility.getStudentProfile(istarUser);

				Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
				String result = gson.toJson(studentProfile);

				return Response.ok(result).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@Path("{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUserProfile(@PathParam("userId") int userId, StudentProfile studentProfile) {

		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);

			if (istarUser == null) {
				// User does not exists
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			} else {
				
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	@PUT
	@Path("password/reset")
	@Produces(MediaType.APPLICATION_JSON)
	public Response resetPassword(@FormParam("userId") int userId, @FormParam("password") String password) {
		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);

			if (istarUser == null) {
				return Response.status(404).build();
			} else {
				istarUser = istarUserServices.updateIstarUser(istarUser.getId(), istarUser.getEmail(), password,
						istarUser.getMobile());
				return Response.status(Response.Status.CREATED).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("password/forgot")
	@Produces(MediaType.APPLICATION_JSON)
	public Response forgotPassword(@QueryParam("mobile") Long mobile) {
		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUserByMobile(mobile);

			if (istarUser == null) {
				return Response.status(404).build();
			} else {
				AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
				StudentProfile studentProfile = appPOJOUtility.getStudentProfile(istarUser);

				Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
				String result = gson.toJson(studentProfile);

				return Response.ok(result).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("{userId}/mobile")
	@Produces(MediaType.APPLICATION_JSON)
	public Response verifyMobileNumber(@PathParam("userId") int userId, @QueryParam("mobile") Long mobile) {
		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);

			IstarUser mobileIstarUser = istarUserServices.getIstarUserByMobile(mobile);

			if (istarUser == null && mobileIstarUser == null) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			} else {
				Integer otp = null;

				try {
					AppServices appServices = new AppServices();
					otp = appServices.sendOTP(mobile.toString());
				} catch (Exception e) {
					e.printStackTrace();
					return Response.status(Response.Status.BAD_GATEWAY).build();
				}
				return Response.ok(otp).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@Path("{userId}/mobile")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateAndVerifyMobileNumber(@PathParam("userId") int userId, @QueryParam("mobile") String mobile) {
		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);

			Long mobileNumber = Long.parseLong(mobile);
			
			IstarUser mobileIstarUser = istarUserServices.getIstarUserByMobile(mobileNumber);

			if (istarUser == null && mobileIstarUser != null) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			} else {
				Integer otp = null;
				istarUserServices.updateMobile(istarUser.getId(), mobileNumber);

				try {
					AppServices appServices = new AppServices();
					otp = appServices.sendOTP(mobile.toString());
				} catch (Exception e) {
					e.printStackTrace();
					return Response.status(Response.Status.BAD_GATEWAY).build();
				}
				return Response.ok(otp).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@Path("{userId}/verify/{isVerified}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response isVerified(@PathParam("userId") int userId, @PathParam("isVerified") String isVerified) {
		try {
			boolean verifiedUser = Boolean.parseBoolean(isVerified);
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.updateIsVerified(userId, verifiedUser);

			if(istarUser!=null){
				AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
				StudentProfile studentProfile = appPOJOUtility.getStudentProfile(istarUser);

				Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
				String result = gson.toJson(studentProfile);

				return Response.ok(result).build();
			}else{
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Path("{userId}/batch")
	@Produces(MediaType.APPLICATION_JSON)
	public Response assignBatchCode(@PathParam("userId") int istarUserId, @FormParam("batchCode") String batchCode) {

		try {
			
			AppBatchGroupServices batchGroupServices = new AppBatchGroupServices();
			BatchGroup batchGroup = batchGroupServices.getBatchGroupByBatchCode(batchCode);
			
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(istarUserId);
			
			if(batchGroup==null || istarUser==null){
				throw new Exception();
			}
			AppBatchStudentsServices batchStudentServices = new AppBatchStudentsServices();
			batchStudentServices.createBatchStudents(istarUser, batchGroup, "STUDENT");					

			return Response.ok("DONE").build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GET
	@Path("{userId}/skills")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSkillsMap(@PathParam("userId") int istarUserId){
		
		try{
			AppServices appServices= new AppServices();
			List<SkillReportPOJO> allSkills = appServices.getSkillsMapOfUser(istarUserId);
			
			Gson gson = new Gson();
			String result = gson.toJson(allSkills);

			return Response.ok(result).build();
			
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("{userId}/complex")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getComplexObject(@PathParam("userId") int istarUserId){
		try{			
			AppComplexObjectServices appComplexObjectServices = new AppComplexObjectServices();
			ComplexObject complexObject = appComplexObjectServices.getComplexObjectForUser(istarUserId);
						
			if(complexObject!=null){				
				Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
				String result = gson.toJson(complexObject);

				return Response.ok(result).build();
			}else{
				throw new Exception();
			}
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	/*
	 * @GET
	 * 
	 * @Path("mobile/verify")
	 * 
	 * @Produces(MediaType.APPLICATION_JSON) public Response
	 * verifyMobileNumber(@QueryParam("mobile") Long mobile) { Integer otp =
	 * null;
	 * 
	 * IstarUserServices istarUserServices = new IstarUserServices(); IstarUser
	 * istarUser = istarUserServices.getIstarUserByMobile(mobile);
	 * 
	 * try { AppServices appServices = new AppServices(); otp =
	 * appServices.sendOTP(mobile.toString()); } catch (Exception e) {
	 * e.printStackTrace(); return
	 * Response.status(Response.Status.BAD_GATEWAY).build(); }
	 * 
	 * Gson gson = new Gson(); String result = gson.toJson(otp);
	 * 
	 * return Response.ok(result).build(); }
	 */

	/*
	 * @PUT
	 * 
	 * @Path("{userId}/mobile")
	 * 
	 * @Produces(MediaType.APPLICATION_JSON) public Response
	 * updateMobileNumber(@PathParam("userId") int userId, @FormParam("mobile")
	 * Long mobile) { System.out.println("Request to update mobile number->" +
	 * mobile); IstarUserServices istarUserServices = new IstarUserServices();
	 * IstarUser istarUser = istarUserServices.getIstarUser(userId);
	 * 
	 * if (istarUser == null) { return
	 * Response.status(Response.Status.BAD_REQUEST).build(); } else {
	 * System.out.println("Updating mobile number"); istarUser =
	 * istarUserServices.updateMobile(istarUser.getId(), mobile);
	 * 
	 * System.out.println("Updated mobile number is :" + istarUser.getMobile());
	 * 
	 * return Response.status(Response.Status.CREATED).build(); } }
	 */
}