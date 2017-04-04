package com.istarindia.android.core;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.istarindia.android.utility.AppPOJOUtility;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.utils.user.IstarUserServices;
import com.viksitpro.core.pojo.recruiter.StudentPOJO;

@Path("user")
public class AppIstarUserService {

	@POST
	@Path("create")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response createUser(@FormParam("email") String email, @FormParam("password") String password, @FormParam("mobile") Long mobile){
				
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.createIstarUser(email, password, mobile);
		
		if(istarUser==null){
			//User already exists
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
}