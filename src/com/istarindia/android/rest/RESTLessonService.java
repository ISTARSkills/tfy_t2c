package com.istarindia.android.rest;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import com.istarindia.android.utility.CreateZIPForItem;
import com.istarindia.apps.services.AppComplexObjectServices;
import com.istarindia.apps.services.AppCourseServices;
import com.istarindia.apps.services.GamificationServices;
import com.istarindia.apps.services.StudentPlaylistServices;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.IstarUserDAO;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.LessonDAO;
import com.viksitpro.core.dao.entities.StudentPlaylist;
import com.viksitpro.core.utilities.DBUTILS;

@Path("lessons/user/{userId}")
public class RESTLessonService {
	@GET
	@Path("{lessonId}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getLesson(@PathParam("lessonId") int lessonId) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {

			
			Lesson lesson = new LessonDAO().findById(lessonId);

			if (lesson == null) {
				throw new Exception();
			}

			CreateZIPForItem createZIPForItem = new CreateZIPForItem();

			String mediaPath = createZIPForItem.getMediaPath();
			String mediaURLPath = createZIPForItem.getMediaURLPath();
			String serverPath = mediaURLPath+"lessonXMLs/" +lessonId+"/"+lessonId+"/"+ lessonId + ".zip";
			String lessonZipFileActualPath = mediaPath + "/lessonXMLs/" + lessonId + ".zip";
			File file = new File(lessonZipFileActualPath);
			Serializer serializer = new Persister();
			Object object = null;
			if(lesson.getType().equalsIgnoreCase("PRESENTATION"))
			{
				//create zip every time
				object = createZIPForItem.generateXMLForLesson(lesson);				
				//System.out.println("oject string in case of lessonXML"+object.toString());
			}
			else if(lesson.getType().equalsIgnoreCase("INTERACTIVE"))
			{
				object = createZIPForItem.generateXMLForLesson(lesson);	
			}
			else if(lesson.getType().equalsIgnoreCase("VIDEO"))
			{
				object = createZIPForItem.generateXMLForLesson(lesson);	
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
	
	
	@GET
	@Path("{lessonId}/{status}")
	@Produces(MediaType.APPLICATION_XML)
	public void updateLessonstatus(@PathParam("userId") int istarUserId,@PathParam("lessonId") int lessonId, @PathParam("status") String status) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			DBUTILS util = new DBUTILS();		
			String updateLessonStatus ="update student_playlist set  status = '"+status+"' where student_id = "+istarUserId+" and lesson_id = "+lessonId;
			util.executeUpdate(updateLessonStatus);
			
				
		} catch (Exception e) {
			e.printStackTrace();
			
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
			//System.out.println("Lesson SP-->"+sql);
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
			////System.err.println(studentPlaylist.getLesson().getId());
			if (studentPlaylist == null) {
				throw new Exception();
			}
			int userId =studentPlaylist.getIstarUser().getId(); 
			studentPlaylistServices.updateStatus(studentPlaylist, "COMPLETED");
			GamificationServices gmService = new GamificationServices();
			gmService.updatePointsAndCoinsOnLessonComplete(studentPlaylist.getIstarUser(), studentPlaylist.getLesson());
			gmService.updateUserPointsCoinsStatsTable(userId);
			gmService.updateLeaderBoard(userId);

			AppComplexObjectServices appComplexObjectServices = new AppComplexObjectServices();
			ComplexObject complexObject = appComplexObjectServices.getComplexObjectForUser(userId);

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
	
	@GET
	@Path("{lesson_id}/update_lesson_status")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateLessonStatusByLessonId(@PathParam("lesson_id") int lessonId,@PathParam("userId") int userId) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try {
			DBUTILS util = new DBUTILS();
			String updateStudentPlayList = "update student_playlist set status='COMPLETED' where lesson_id = "+lessonId+" and student_id="+userId;
			util.executeUpdate(updateStudentPlayList);
			
			String updateStudentPlayList1 = "update  task set is_active='f' where id in(select task_id from student_playlist where student_id = "+userId+" "
					+ " and lesson_id = "+lessonId+")";
			////System.err.println("updateStudentPlayList1--->"+updateStudentPlayList1);
			util.executeUpdate(updateStudentPlayList1);
			
			
			GamificationServices gmService = new GamificationServices();
			IstarUser user = new IstarUserDAO().findById(userId);
			Lesson lesson = new LessonDAO().findById(lessonId);
			gmService.updatePointsAndCoinsOnLessonComplete(user, lesson);
			gmService.updateUserPointsCoinsStatsTable(userId);
			gmService.updateLeaderBoard(userId);
			AppComplexObjectServices appComplexObjectServices = new AppComplexObjectServices();
			ComplexObject complexObject = appComplexObjectServices.getComplexObjectForUser(userId);

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
		@GET
		@Path("{lesson_id}/{task_id}/update_lesson_status22")
		@Produces(MediaType.APPLICATION_JSON)
		public Response updateLessonStatusByLessonId1(@PathParam("lesson_id") int lessonId,@PathParam("userId") int userId ,@PathParam("task_id") int task_id) {
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
			try {
				DBUTILS util = new DBUTILS();
				String updateStudentPlayList = "update student_playlist set status='COMPLETED' where lesson_id = "+lessonId+" and student_id="+userId;
				util.executeUpdate(updateStudentPlayList);
				
				String updateStudentPlayList1 = "update  task set is_active='f', updated_at=now() where id in("+task_id+")";
				////System.err.println("updateStudentPlayList1--->"+updateStudentPlayList1);
				util.executeUpdate(updateStudentPlayList1);
				
				
				GamificationServices gmService = new GamificationServices();
				IstarUser user = new IstarUserDAO().findById(userId);
				Lesson lesson = new LessonDAO().findById(lessonId);
				gmService.updatePointsAndCoinsOnLessonComplete(user, lesson);
				gmService.updateUserPointsCoinsStatsTable(userId);
				gmService.updateLeaderBoard(userId);
				AppComplexObjectServices appComplexObjectServices = new AppComplexObjectServices();
				ComplexObject complexObject = appComplexObjectServices.getComplexObjectForUser(userId);

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
	
	
	@POST
	@Path("add_log/lesson/{lesson_id}/{slide_id}/{slide_title}/{total_slide_count}")
	@Produces(MediaType.APPLICATION_JSON)
	public void addUserSessionLog(@PathParam("userId") int istarUserId,@PathParam("slide_id") int slideId, @PathParam("lesson_id") int lessonId,@PathParam("slide_title") int slideTitle,@PathParam("total_slide_count") int totalSlideCount) {
		try {
			DBUTILS util = new DBUTILS();
			Lesson l = new LessonDAO().findById(lessonId);
			int courseId =  l.getCmsessions().iterator().next().getModules().iterator().next().getCourses().iterator().next().getId();
			int moduleId = l.getCmsessions().iterator().next().getModules().iterator().next().getId();
			int cmsessionId = l.getCmsessions().iterator().next().getId();
			
			String insertIntoLog = "INSERT INTO user_session_log (id, cmsession_id, course_id, created_at, lesson_id, lesson_type, module_id,  slide_id, updated_at, url, user_id,total_slide_count)"
					+ " VALUES ((select COALESCE(max(id),0)+1 from user_session_log), "+cmsessionId+", "+courseId+", now(), "+lessonId+", '"+l.getType()+"', "+moduleId+", "+slideId+", now(), '"+slideTitle+"', "+istarUserId+","+totalSlideCount+");";
			util.executeUpdate(insertIntoLog);
		} catch (Exception e) {
			
		}
	}
}
