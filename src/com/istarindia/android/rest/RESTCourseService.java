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
			CoursePOJO coursePOJO = appCourseServices.getCourseOfUser(istarUserId, courseId);
			coursePOJO.setSkillObjectives(appCourseServices.getSkillsReportForCourseOfUser(istarUserId, courseId));

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
				CoursePOJO coursePOJO = appCourseServices.getCourseOfUser(istarUserId, courseId);
				coursePOJO.setSkillObjectives(appCourseServices.getSkillsReportForCourseOfUser(istarUserId, courseId));
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
		/*	StudentPlaylistServices studentPlaylistServices = new StudentPlaylistServices();
			List<StudentPlaylist> allStudentPlaylist = studentPlaylistServices.getStudentPlaylistOfUser(userId);
			
			Set<Integer> allCourseId = new HashSet<Integer>();
			
			for(StudentPlaylist StudentPlaylist : allStudentPlaylist){
				allCourseId.add(StudentPlaylist.getCourse().getId());
			}
			*/
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
			
			String result = gson.toJson(courseRankPOJO);
			
			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}		
	}
	

/*	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllCourseOfUser(@PathParam("userId") int istarUserId) {

		try {
			AppCourseServices appCourseServices = new AppCourseServices();
			AppUserRankUtility appUserRankUtility = new AppUserRankUtility();

			List<CoursePOJO> coursesWithoutModuleStatus = appCourseServices.getCoursesOfUser(istarUserId);
			List<CoursePOJO> courses = new ArrayList<CoursePOJO>();
			for(CoursePOJO coursePOJO : coursesWithoutModuleStatus){
				coursePOJO = coursePOJO.sortModulesAndAssignStatus();
				
				coursePOJO.setProgress(appCourseServices.getProgressOfUserForCourse(istarUserId, coursePOJO.getId()));
				coursePOJO.setTotalPoints(appCourseServices.getMaxPointsOfCourse(coursePOJO.getId()));
								
				StudentRankPOJO studentRankPOJO = appUserRankUtility.getStudentRankPOJOForCourseOfAUser(istarUserId, coursePOJO.getId());
				
				if(studentRankPOJO!=null){
					coursePOJO.setUserPoints(studentRankPOJO.getPoints()*1.0);
					coursePOJO.setRank(studentRankPOJO.getBatchRank());
				}
				
				//coursePOJO.setSkillObjectives(appCourseServices.getSkillsReportForCourseOfUser(istarUserId, coursePOJO.getId()));
				
				for(SkillReportPOJO skillReport : coursePOJO.getSkillObjectives()){
					skillReport.calculateUserPoints();
					skillReport.calculateTotalPoints();
					skillReport.calculatePercentage();
				}				
				courses.add(coursePOJO);
			}
			
			Gson gson = new Gson();
			String result = gson.toJson(courses);

			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}*/
	
	
}