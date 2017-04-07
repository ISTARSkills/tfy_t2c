package com.istarindia.android.rest;

import java.util.ArrayList;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.istarindia.android.pojo.CoursePOJO;
import com.istarindia.android.utility.AppPOJOUtility;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.StudentPlaylist;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

@Path("user")
public class AppContentServices {

	@GET
	@Path("{userId}/courses")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllCourseOfUser(@PathParam("userId") int istarUserId){
		
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(istarUserId);
		
		Set<StudentPlaylist> allStudentPlaylistItems = istarUser.getStudentPlaylists();
		
		ArrayList<CoursePOJO> courses = new ArrayList<CoursePOJO>();
		AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
		
		for(StudentPlaylist studentPlaylist : allStudentPlaylistItems){
			courses.add(appPOJOUtility.getCoursePOJO(studentPlaylist));
		}
		
		Gson gson = new Gson();
		String result = gson.toJson(courses);

		return Response.ok(result).build();
	}	
}
