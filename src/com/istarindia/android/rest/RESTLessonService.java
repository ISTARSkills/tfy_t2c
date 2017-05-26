package com.istarindia.android.rest;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.istarindia.android.pojo.ComplexObject;
import com.istarindia.android.pojo.LessonPOJO;
import com.istarindia.android.utility.AppDashboardUtility;
import com.istarindia.android.utility.CreateZIPForItem;
import com.istarindia.apps.services.AppComplexObjectServices;
import com.istarindia.apps.services.AppCourseServices;
import com.istarindia.apps.services.GamificationServices;
import com.istarindia.apps.services.StudentPlaylistServices;
import com.viksitpro.core.cms.interactive.InteractiveContent;
import com.viksitpro.core.cms.lesson.VideoLesson;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.StudentPlaylist;
import com.viksitpro.core.dao.entities.Task;

@Path("lessons/user/{userId}")
public class RESTLessonService {
	@GET
	@Path("{lessonId}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getLesson(@PathParam("lessonId") int lessonId) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {

			AppCourseServices appCourseServices = new AppCourseServices();
			Lesson lesson = appCourseServices.getLesson(lessonId);

			if (lesson == null) {
				throw new Exception();
			}

			CreateZIPForItem createZIPForItem = new CreateZIPForItem();

			String mediaPath = createZIPForItem.getMediaPath();
			String mediaURLPath = createZIPForItem.getMediaURLPath();
			String serverPath = mediaURLPath+"lessons/" + lessonId + ".zip";
			String lessonZipFileActualPath = mediaPath + "/lessons/" + lessonId + ".zip";
			File file = new File(lessonZipFileActualPath);
			Serializer serializer = new Persister();
			Object object = null;
			if (!file.exists()) {
				
				System.out.println("Creating New Zip file");
				object = createZIPForItem.generateXMLForLesson(lessonId);		
			} else {
				String lessonXML = getLessonXML(lesson.getId());
				System.out.println("Zip file exists");
				if (lessonXML != null && !lessonXML.trim().isEmpty()) {
					if (lesson.getType().equals("INTERACTIVE")) {
						InteractiveContent interactiveContent = serializer.read(InteractiveContent.class, lessonXML);
						interactiveContent.setZipFileURL(serverPath);
						object = interactiveContent;
					} else if (lesson.getType().equals("VIDEO")) {
						VideoLesson videoLesson = serializer.read(VideoLesson.class, lessonXML);
						videoLesson.setZipFileURL(serverPath);
						object = videoLesson;
					}
					else if(lesson.getType().equalsIgnoreCase("PRESENTATION"))
					{						
							object = mediaURLPath+"/lessonXMLs/"+lesson.getId()+".zip";
					}
				}
			}

			if (object == null) {
				throw new Exception();
			}
			StringWriter writer = new StringWriter();
			serializer.write(object, writer);
			StringBuffer out = writer.getBuffer();
			String result = out.toString();

			return Response.ok(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}
	
	public String getLessonXML(int lessonId) {

		String lessonXML = null;
		
		String sql = "select cast(lesson_xml as varchar) from lesson where id= :lessonId";
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("lessonId", lessonId);

		List<String> results = query.list();
		
		if(results.size()>0){
			lessonXML = results.get(0);
		}
		return lessonXML;
	}
	
	@GET
	@Path("{lessonId}/pojo")
	@Produces(MediaType.APPLICATION_XML)
	public Response getLessonPOJO(@PathParam("userId") int istarUserId, @PathParam("lessonId") int lessonId){
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try{
			BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
			Session session = baseHibernateDAO.getSession();
			
			String sql = "select id, status from student_playlist where student_id="+istarUserId+" and lesson_id="+lessonId;
			System.out.println("Lesson SP-->"+sql);
			SQLQuery query = session.createSQLQuery(sql);
			List<Object[]> queryResult = query.list();
			
			AppCourseServices appCourseServices = new AppCourseServices();
			Lesson lesson = appCourseServices.getLesson(lessonId);
			if(queryResult.size()<=0 && lesson==null){
				throw new Exception("No entry in student playlist");
			}
				
				Integer studentPlaylistId = (Integer) queryResult.get(0)[0];
				String status = (String) queryResult.get(0)[1];
				
				LessonPOJO lessonPOJO = new LessonPOJO();
				lessonPOJO.setId(lesson.getId());
				lessonPOJO.setTitle(lesson.getTitle());
				lessonPOJO.setDescription(lesson.getDescription());
				lessonPOJO.setDuration(lesson.getDuration());
				lessonPOJO.setPlaylistId(studentPlaylistId);
				lessonPOJO.setStatus(status);
				lessonPOJO.setSubject(lesson.getSubject());
				lessonPOJO.setType(lesson.getType());
				lessonPOJO.setOrderId(studentPlaylistId);
				
				String result = gson.toJson(lessonPOJO);

				return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}
	
	@POST
	@Path("{playlistId}/status")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateLessonStatus(@PathParam("playlistId") int playlistId) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			StudentPlaylistServices studentPlaylistServices = new StudentPlaylistServices();
			StudentPlaylist studentPlaylist = studentPlaylistServices.getStudentPlaylist(playlistId);

			if (studentPlaylist == null) {
				throw new Exception();
			}

			studentPlaylistServices.updateStatus(studentPlaylist, "COMPLETED");
			GamificationServices gmService = new GamificationServices();
			gmService.updatePointsAndCoinsOnLessonComplete(studentPlaylist.getIstarUser(), studentPlaylist.getLesson());
			//AppCourseServices appCourseServices = new AppCourseServices();
			//appCourseServices.insertIntoUserGamificationOnCompletitionOfLessonByUser(studentPlaylist.getIstarUser().getId(), studentPlaylist.getLesson().getId(), studentPlaylist.getCourse().getId());
			
			AppComplexObjectServices appComplexObjectServices = new AppComplexObjectServices();
			ComplexObject complexObject = appComplexObjectServices.getComplexObjectForUser(studentPlaylist.getIstarUser().getId());

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
