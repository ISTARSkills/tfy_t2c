package com.istarindia.android.rest;

import java.util.HashSet;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.istarindia.android.pojo.StudentProfile;
import com.istarindia.android.utility.AppPOJOUtility;
import com.istarindia.android.utility.AppUtility;
import com.istarindia.apps.services.AppServices;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Role;
import com.viksitpro.core.dao.entities.UserProfile;
import com.viksitpro.core.dao.entities.UserRole;
import com.viksitpro.core.dao.utils.user.IstarUserServices;
import com.viksitpro.core.dao.utils.user.RoleServices;
import com.viksitpro.core.dao.utils.user.UserRoleServices;

@Path("auth")
public class RESTAuthenticationService {

	@POST
	@Path("login")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loginUser(@FormParam("email") String email, @FormParam("password") String password) {

		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUserByEmail(email);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			if (istarUser == null){
				throw new Exception("istarViksitProComplexKeyUsername does not exists");
			}
			
			if(!istarUser.getPassword().equals(password)) {
				throw new Exception("istarViksitProComplexKeyPassword is incorrect");
			}
			AppServices appServices = new AppServices();
			istarUser = appServices.assignToken(istarUser);
			
			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();

			StudentProfile studentProfile = appPOJOUtility.getStudentProfile(istarUser);
			
			String result = gson.toJson(studentProfile);
			appServices.logEntryToLoginTable(istarUser, "LOGIN");			
			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage()!=null?gson.toJson(e.getMessage()):gson.toJson("istarViksitProComplexKeyYou are not authorized.");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@POST
	@Path("login/social")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loginUserWithSocialMedia(@FormParam("email") String email, @FormParam("name") String name,
			@FormParam("profileImage") String profileImage, @FormParam("socialMedia") String socialMedia) {

		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUserByEmail(email);

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

		try {
			if (istarUser == null) {

				istarUser = istarUserServices.createIstarUser(email, AppUtility.getRandomString(10), null, null, socialMedia);
				UserProfile userProfile = istarUserServices.createUserProfile(istarUser.getId(), null, name, null, null,
						null, profileImage, null);

				RoleServices roleServices = new RoleServices();
				Role role = roleServices.getRoleByName("STUDENT");

				UserRoleServices userRoleServices = new UserRoleServices();
				UserRole userRole = userRoleServices.createUserRole(istarUser, role, 1);

				HashSet<UserRole> allUserRole = new HashSet<UserRole>();
				allUserRole.add(userRole);

				istarUser.setUserRoles(allUserRole);
				istarUser.setUserProfile(userProfile);
			}

			AppServices appServices = new AppServices();
			istarUser = appServices.assignToken(istarUser);

			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
			StudentProfile studentProfile = appPOJOUtility.getStudentProfile(istarUser);
			System.out.println("Returing system profile");

			String result = gson.toJson(studentProfile);
			appServices.logEntryToLoginTable(istarUser, "LOGIN_"+socialMedia);
			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage()!=null?gson.toJson(e.getMessage()):gson.toJson("istarViksitProComplexKeyYou are not authorized.");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}
	
	@POST
	@Path("logout/user/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response logoutUser(@PathParam("userId") int userId){
	
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try{
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);
			if(istarUser==null){
				throw new Exception("istarViksitProComplexKeyInvalid User");				
			}
			
			AppServices appServices = new AppServices();
			appServices.logEntryToLoginTable(istarUser, "LOGOUT");
			appServices.invalidateToken(istarUser);
			
			return Response.ok(gson.toJson("LOGOUT Successfull")).build();
		}catch(Exception e){
			e.printStackTrace();
			String result = e.getMessage()!=null?gson.toJson(e.getMessage()):gson.toJson("istarViksitProComplexKeyYou are not authorized.");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}
}
