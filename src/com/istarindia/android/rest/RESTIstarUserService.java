package com.istarindia.android.rest;

import java.util.HashSet;
import java.util.List;

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
import com.viksitpro.core.dao.entities.UserProfile;
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

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			String authenticationToken = AppUtility.getRandomString(20);
			IstarUser istarUser = null;
			IstarUser istarUserByMobile = null;

			istarUser = istarUserServices.getIstarUserByEmail(email);
			istarUserByMobile = istarUserServices.getIstarUserByMobile(mobile);

			if (istarUser != null) {
				// User with Email already registered
				throw new Exception("istarViksitProComplexKeyA user already registered with this email.");
			} else if (istarUserByMobile != null) {
				// User with Mobile already registered
				throw new Exception("istarViksitProComplexKeyA user already registered with this mobile");
			}

			istarUser = istarUserServices.createIstarUser(email, password, mobile, authenticationToken);

			RoleServices roleServices = new RoleServices();
			Role role = roleServices.getRoleByName("STUDENT");

			UserRoleServices userRoleServices = new UserRoleServices();
			UserRole userRole = userRoleServices.createUserRole(istarUser, role, 1);

			HashSet<UserRole> allUserRole = new HashSet<UserRole>();
			allUserRole.add(userRole);

			istarUser.setUserRoles(allUserRole);

			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
			StudentProfile studentProfile = appPOJOUtility.getStudentProfile(istarUser);

			String result = gson.toJson(studentProfile);

			AppServices appServices = new AppServices();
			appServices.logEntryToLoginTable(istarUser, "LOGIN");
			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@GET
	@Path("{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserProfile(@PathParam("userId") int userId) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);

			if (istarUser == null) {
				throw new Exception("istarViksitProComplexKeyUser does not exists");
			}
			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
			StudentProfile studentProfile = appPOJOUtility.getStudentProfile(istarUser);

			String result = gson.toJson(studentProfile);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@POST
	@Path("{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateUserProfile(@PathParam("userId") int userId, @FormParam("profile") String profile) {

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);

			Gson requestGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
			//System.out.println("profule json---"+profile);
			StudentProfile requestStudentProfile = requestGson.fromJson(profile, StudentProfile.class);

			if (istarUser == null) {
				throw new Exception();
			}
			AppServices appServices = new AppServices();
			appServices.updateStudentProfile(requestStudentProfile);

			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
			StudentProfile studentProfile = appPOJOUtility.getStudentProfile(istarUser);

			String result = gson.toJson(studentProfile);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@POST
	@Path("{userId}/upload")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateUserProfileImage(@PathParam("userId") int userId,
			@FormParam("profileImage") String profileImage) {

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);

			if (istarUser == null) {
				throw new Exception();
			} else if (profileImage == null) {
				throw new Exception("istarViksitProComplexKeyInvalid Image");
			}
			AppUtility appUtility = new AppUtility();
			String fileName = appUtility.imageUpload(profileImage, "jpg", "PROFILE_IMAGE", istarUser.getId()+"");			
			UserProfile userProfile = istarUserServices.updateProfileImage(istarUser, fileName);
			istarUserServices.testProfile(userId,fileName);
			
			istarUser = istarUserServices.getIstarUser(userId);
			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
			StudentProfile studentProfile = appPOJOUtility.getStudentProfile(istarUser);

			String result = gson.toJson(studentProfile);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@PUT
	@Path("password/reset")
	@Produces(MediaType.APPLICATION_JSON)
	public Response resetPassword(@FormParam("userId") int userId, @FormParam("password") String password) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);

			if (istarUser == null) {
				return Response.status(404).build();
			}
			istarUser = istarUserServices.updateIstarUser(istarUser.getId(), istarUser.getEmail(), password,
					istarUser.getMobile());
			String result = gson.toJson("DONE");
			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@GET
	@Path("password/forgot")
	@Produces(MediaType.APPLICATION_JSON)
	public Response forgotPassword(@QueryParam("mobile") Long mobile) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUserByMobile(mobile);

			if (istarUser == null) {
				return Response.status(404).build();
			} else {
				AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
				StudentProfile studentProfile = appPOJOUtility.getStudentProfile(istarUser);

				String result = gson.toJson(studentProfile);

				return Response.ok(result).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@GET
	@Path("{userId}/mobile")
	@Produces(MediaType.APPLICATION_JSON)
	public Response verifyMobileNumber(@PathParam("userId") int userId, @QueryParam("mobile") Long mobile) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);

			IstarUser mobileIstarUser = istarUserServices.getIstarUserByMobile(mobile);

			if (istarUser == null || mobileIstarUser == null) {
				throw new Exception("istarViksitProComplexKeyOops! No account registered for this mobile number.");
			}
			Integer otp = null;

			try {
				AppServices appServices = new AppServices();
				otp = appServices.sendOTP(mobile.toString());
			} catch (Exception e) {
				throw new Exception("istarViksitProComplexKeyInternal Server Error");
			}
			return Response.ok(otp).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@PUT
	@Path("{userId}/mobile")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateAndVerifyMobileNumber(@PathParam("userId") int userId, @QueryParam("mobile") String mobile) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(userId);

			Long mobileNumber = Long.parseLong(mobile);

			IstarUser mobileIstarUser = istarUserServices.getIstarUserByMobile(mobileNumber);

			if (istarUser == null) {
				throw new Exception();
			} else if (mobileIstarUser != null && mobileIstarUser.getId() != istarUser.getId()) {
				throw new Exception(
						"istarViksitProComplexKeyA user already exits with this mobile. Please try with another mobile number or raise a ticket.");
			}
			Integer otp = null;
			istarUserServices.updateMobile(istarUser.getId(), mobileNumber);

			try {
				AppServices appServices = new AppServices();
				otp = appServices.sendOTP(mobile.toString());
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception();
			}
			return Response.ok(gson.toJson(otp)).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@PUT
	@Path("{userId}/verify/{isVerified}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response isVerified(@PathParam("userId") int userId, @PathParam("isVerified") String isVerified) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			boolean verifiedUser = Boolean.parseBoolean(isVerified);
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.updateIsVerified(userId, verifiedUser);

			if (istarUser == null) {
				throw new Exception();
			}
			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
			StudentProfile studentProfile = appPOJOUtility.getStudentProfile(istarUser);

			String result = gson.toJson(studentProfile);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@POST
	@Path("{userId}/batch")
	@Produces(MediaType.APPLICATION_JSON)
	public Response assignBatchCode(@PathParam("userId") int istarUserId, @FormParam("batchCode") String batchCode) {

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {

			AppBatchGroupServices batchGroupServices = new AppBatchGroupServices();
			BatchGroup batchGroup = batchGroupServices.getBatchGroupByBatchCode(batchCode);

			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(istarUserId);

			if (batchGroup == null) {
				throw new Exception(
						"istarViksitProComplexKeyInvalid batch code. Please contact your college administrator.");
			}
			AppBatchStudentsServices batchStudentServices = new AppBatchStudentsServices();
			batchStudentServices.createBatchStudents(istarUser, batchGroup, "STUDENT");

			AppComplexObjectServices appComplexObjectServices = new AppComplexObjectServices();
			ComplexObject complexObject = appComplexObjectServices.getComplexObjectForUser(istarUserId);

			if (complexObject == null) {
				throw new Exception("istarViksitProComplexKeyInvalid user");
			}
			String result = gson.toJson(complexObject);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@GET
	@Path("{userId}/skills")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSkillsMap(@PathParam("userId") int istarUserId) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			AppServices appServices = new AppServices();
			List<SkillReportPOJO> allSkills = appServices.getSkillsMapOfUser(istarUserId);

			String result = gson.toJson(allSkills);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

	@GET
	@Path("{userId}/complex")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getComplexObject(@PathParam("userId") int istarUserId) {
		
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			AppComplexObjectServices appComplexObjectServices = new AppComplexObjectServices();
			ComplexObject complexObject = appComplexObjectServices.getComplexObjectForUser(istarUserId);

			if (complexObject == null) {
				throw new Exception();
			}
			String result = gson.toJson(complexObject);

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}

}