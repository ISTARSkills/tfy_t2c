package com.istarindia.android.rest;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.istarindia.apps.services.AppCourseServices;
import com.istarindia.apps.services.StudentPlaylistServices;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.StudentPlaylist;

@Path("lessons/user/{userId}")
public class RESTLessonService {

	@GET
	@Path("{lessonId}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getLesson(@PathParam("lessonId") int lessonId){
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try{
			String lessonXML = null;
			AppCourseServices appCourseServices = new AppCourseServices();
			Lesson lesson = appCourseServices.getLesson(lessonId);
			
			if(lesson!=null){
				lessonXML = lesson.getLessonXml();
			}

			return Response.ok(lessonXML).build();
		}catch(Exception e){
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}
	
	@PUT
	@Path("{playlistId}/status")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateLessonStatus(@PathParam("playlistId") int playlistId, @FormParam("status") String status) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			StudentPlaylistServices studentPlaylistServices = new StudentPlaylistServices();
			StudentPlaylist studentPlaylist = studentPlaylistServices.getStudentPlaylist(playlistId);

			if (studentPlaylist == null) {
				throw new Exception();
			}

			studentPlaylistServices.updateStatus(studentPlaylist, status);
			String result = gson.toJson("DONE");
			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}
}
