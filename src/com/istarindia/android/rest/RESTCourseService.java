package com.istarindia.android.rest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.istarindia.android.pojo.CoursePOJO;
import com.istarindia.android.pojo.CourseRankPOJO;
import com.istarindia.android.utility.AppPOJOUtility;
import com.istarindia.android.utility.AppUserRankUtility;
import com.istarindia.apps.services.StudentPlaylistServices;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.StudentPlaylist;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

@Path("courses/user/{userId}")
public class RESTCourseService {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllCourseOfUser(@PathParam("userId") int istarUserId) {

		try {
			IstarUserServices istarUserServices = new IstarUserServices();
			IstarUser istarUser = istarUserServices.getIstarUser(istarUserId);

			Set<StudentPlaylist> allStudentPlaylistItems = istarUser.getStudentPlaylists();

			ArrayList<CoursePOJO> courses = new ArrayList<CoursePOJO>();
			AppPOJOUtility appPOJOUtility = new AppPOJOUtility();

			for (StudentPlaylist studentPlaylist : allStudentPlaylistItems) {
				courses.add(appPOJOUtility.getCoursePOJO(studentPlaylist));
			}

			Gson gson = new Gson();
			String result = gson.toJson(courses);

			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GET
	@Path("leaderboard")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLeaderboardOfAllCoursesOfUser(@PathParam("userId") int userId){
		
		try{
			
			StudentPlaylistServices studentPlaylistServices = new StudentPlaylistServices();
			List<StudentPlaylist> allStudentPlaylist = studentPlaylistServices.getStudentPlaylistOfUser(userId);
			
			Set<Integer> allCourseId = new HashSet<Integer>();
			
			for(StudentPlaylist StudentPlaylist : allStudentPlaylist){
				allCourseId.add(StudentPlaylist.getCourse().getId());
			}
			
			AppUserRankUtility appUserRankUtility = new AppUserRankUtility();
			List<CourseRankPOJO> allCourseRanks = appUserRankUtility.getCourseRankPOJOForCoursesOfUsersBatch(userId, allCourseId);
			
			Gson gson = new Gson();
			String result = gson.toJson(allCourseRanks);
			
			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}		
	}
}
