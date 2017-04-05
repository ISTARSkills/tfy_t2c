package com.istarindia.android.rest;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.istarindia.android.utility.AppPOJOUtility;
import com.istarindia.apps.services.BatchStudentsServices;
import com.viksitpro.core.dao.entities.BatchStudents;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.utils.user.IstarUserServices;
import com.viksitpro.core.pojo.recruiter.StudentPOJO;

@Path("user")
public class AppIstarUserService {

	@POST
	@Path("create")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createUser(@FormParam("email") String email, @FormParam("password") String password, @FormParam("mobile") Long mobile){
				
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.createIstarUser(email, password, mobile);

		if(istarUser==null){
			//User Email or Mobile already registered
			return Response.status(Response.Status.CONFLICT).build(); 
		}else{			
			return Response.status(Response.Status.CREATED).build();
		}
	}
	
	@GET
	@Path("{userId}/profile")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserProfile(@PathParam("userId") int userId){
		System.out.println("Getting user profile" + userId);
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(userId);
		
		if(istarUser==null){
			//User does not exists
			return Response.status(404).build(); 
		}else{
			AppPOJOUtility androidPOJOUtility = new AppPOJOUtility();
			StudentPOJO studentPOJO = androidPOJOUtility.getStudentPOJO(istarUser);
			//GenericEntity<StudentPOJO> entity = new GenericEntity<StudentPOJO>(studentPOJO){};			
			return Response.ok(studentPOJO).build(); 
		}
	}
	
	@POST
	@Path("reset/password")
	@Produces(MediaType.APPLICATION_JSON)
	public Response resetPassword(@FormParam("mobile") Long mobile, @FormParam("password") String password){
		System.out.println("Resetting password");
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUserByMobile(mobile);
		
		if(istarUser==null){
			return Response.status(404).build(); 
		}else{
			istarUser = istarUserServices.updateIstarUser(istarUser.getId(), istarUser.getEmail(), password, istarUser.getMobile());
			return Response.status(Response.Status.CREATED).build();
		}
	}
	
	@POST
	@Path("{userId}/batchCode")
	@Produces(MediaType.APPLICATION_JSON)
	public Response assignBatchCode(@PathParam("istarUserId") int istarUserId, @FormParam("batchCode") String batchCode){

		//check if student batch already exists
		
		BatchStudentsServices batchStudentServices = new BatchStudentsServices();
		BatchStudents batchStudents = batchStudentServices.createBatchStudents(istarUserId, batchCode);
				
		return Response.status(Response.Status.CREATED).build();
	}
}