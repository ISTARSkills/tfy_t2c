package com.istarindia.android.rest;

import java.util.HashSet;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.istarindia.android.pojo.StudentProfile;
import com.istarindia.android.utility.AppPOJOUtility;
import com.istarindia.apps.services.AppEncryptionService;
import com.istarindia.apps.services.AppServices;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Role;
import com.viksitpro.core.dao.entities.UserProfile;
import com.viksitpro.core.dao.entities.UserRole;
import com.viksitpro.core.dao.utils.user.IstarUserServices;
import com.viksitpro.core.dao.utils.user.RoleServices;
import com.viksitpro.core.dao.utils.user.UserRoleServices;

@AppSecured
@Path("auth")
public class RESTAuthenticationService {

	@POST
	@Path("login")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loginUser(@FormParam("email") String email, @FormParam("password") String password) {

		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUserByEmail(email);

		try {
			if (istarUser == null || !istarUser.getPassword().equals(password)) {
				System.out.println("user is null or password does not match");
				throw new Exception();
			}
			AppServices appServices = new AppServices();
			istarUser = appServices.assignToken(istarUser);

			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
			StudentProfile studentProfile = appPOJOUtility.getStudentProfile(istarUser);

			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
			String result = gson.toJson(studentProfile);

			return Response.ok(result, MediaType.APPLICATION_OCTET_STREAM).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}

	@POST
	@Path("login/social")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loginUserWithSocialMedia(@FormParam("email") String email, @FormParam("name") String name,
			@FormParam("profileImage") String profileImage, @FormParam("socialMedia") String socialMedia) {

		System.out.println("Logged In from Social Media--> Name is:" + name);

		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUserByEmail(email);

		try {
			if (istarUser == null) {

				istarUser = istarUserServices.createIstarUser(email, "test123", null, null, socialMedia);
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

			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
			String result = gson.toJson(studentProfile);

			return Response.ok(result, MediaType.APPLICATION_OCTET_STREAM).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}
}
