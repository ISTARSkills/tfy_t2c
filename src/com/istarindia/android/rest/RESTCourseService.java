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
import com.istarindia.android.utility.AppUserRankUtility;
import com.istarindia.apps.services.AppCourseServices;
import com.istarindia.apps.services.StudentPlaylistServices;
import com.viksitpro.core.dao.entities.StudentPlaylist;

@Path("courses/user/{userId}")
public class RESTCourseService {

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
	
	
	@GET
	@Path("{courseId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCourseOfUser(@PathParam("userId") int istarUserId, @PathParam("courseId") int courseId) {
		try {
			AppCourseServices appCourseServices = new AppCourseServices();
			CoursePOJO coursePOJO = appCourseServices.getCourseOfUser(istarUserId, courseId);
			coursePOJO.setSkillObjectives(appCourseServices.getSkillsReportForCourseOfUser(istarUserId, courseId));
						
			Gson gson = new Gson();
			String result = gson.toJson(coursePOJO);

			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllCoursesOfUser(@PathParam("userId") int istarUserId) {

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
			
			Gson gson = new Gson();
			String result = gson.toJson(allCoursePOJO);

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
