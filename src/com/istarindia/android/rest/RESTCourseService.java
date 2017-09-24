package com.istarindia.android.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.istarindia.android.pojo.CoursePOJO;
import com.istarindia.android.pojo.CourseRankPOJO;
import com.istarindia.android.utility.AppUserRankUtility;
import com.istarindia.apps.services.AppCourseServices;
import com.istarindia.apps.services.StudentPlaylistServices;

@Path("courses/user/{userId}")
public class RESTCourseService {
	@GET
	@Path("{courseId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCourseOfUser(@PathParam("userId") int istarUserId, @PathParam("courseId") int courseId) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			AppCourseServices appCourseServices = new AppCourseServices();
			CoursePOJO coursePOJO = appCourseServices.getCoursePojoForUser(istarUserId, courseId);
			if(coursePOJO==null){
				throw new Exception();
			}
			String result = gson.toJson(coursePOJO);

			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllCoursesOfUser(@PathParam("userId") int istarUserId) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			List<CoursePOJO> allCoursePOJO = new ArrayList<CoursePOJO>();
			StudentPlaylistServices studentPlaylistServices= new StudentPlaylistServices();
			List<Integer> allCourseId = studentPlaylistServices.getCoursesforUser(istarUserId);
			AppCourseServices appCourseServices = new AppCourseServices();
			for(Integer courseId : allCourseId){
				CoursePOJO coursePOJO = appCourseServices.getCoursePojoForUser(istarUserId, courseId);
				coursePOJO.setImageURL(coursePOJO.getImageURL().replaceAll("/video//video/", "/video/"));
				
				allCoursePOJO.add(coursePOJO);
			}

			String result = gson.toJson(allCoursePOJO);

			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}
	
	@GET
	@Path("leaderboard")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLeaderboardOfAllCoursesOfUser(@PathParam("userId") int userId){
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try{			

			AppUserRankUtility appUserRankUtility = new AppUserRankUtility();
			List<CourseRankPOJO> allCourseRanks = appUserRankUtility.getCourseRankPOJOForCoursesOfUsersBatch(userId);

			String result = gson.toJson(allCourseRanks);
			
			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}		
	}
	
	@GET
	@Path("{courseId}/leaderboard")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLeaderboardOfCoursesOfUser(@PathParam("userId") int userId, @PathParam("courseId") int courseId){
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try{			
			AppUserRankUtility appUserRankUtility = new AppUserRankUtility();
			CourseRankPOJO courseRankPOJO = appUserRankUtility.getLeaderboardForCourseOfUser(userId, courseId);
			
			if(courseRankPOJO==null){
				throw new Exception();
			}
			
			String result = gson.toJson(courseRankPOJO);
			
			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}		
	}
	
}