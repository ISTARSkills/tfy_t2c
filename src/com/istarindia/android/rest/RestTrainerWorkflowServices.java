/**
 * 
 */
package com.istarindia.android.rest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.istarindia.android.pojo.DailyTaskPOJO;
import com.istarindia.android.pojo.NotificationPOJO;
import com.istarindia.android.pojo.QuestionResponsePOJO;
import com.istarindia.android.pojo.trainerworkflow.ClassFeedbackByTrainer;
import com.istarindia.android.pojo.trainerworkflow.CourseContent;
import com.istarindia.android.pojo.trainerworkflow.CourseItem;
import com.istarindia.android.pojo.trainerworkflow.GroupPojo;
import com.istarindia.android.pojo.trainerworkflow.GroupStudentPojo;
import com.istarindia.apps.services.AppCalendarServices;
import com.istarindia.apps.services.AppNotificationServices;
import com.istarindia.apps.services.TrainerWorkflowServices;
import com.viksitpro.core.dao.entities.IstarNotification;
import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.dao.utils.task.TaskServices;
import com.viksitpro.core.notification.IstarNotificationServices;
import com.viksitpro.core.utilities.DBUTILS;
import com.viksitpro.core.utilities.TaskItemCategory;
import com.viksitpro.core.utilities.TrainerWorkflowStages;

/**
 * @author mayank
 *
 */
@Path("trainerworkflow/")
public class RestTrainerWorkflowServices {

	@GET
	@Path("{taskId}/groupdata")
	@Produces(MediaType.APPLICATION_JSON)
	public Response GroupDataPOJO(@PathParam("taskId") int taskId){
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try{
			
			TrainerWorkflowServices service = new TrainerWorkflowServices();
			DBUTILS util = new DBUTILS();
			String getGroupId ="select batch_group_id, batch_group.name from task,batch_schedule_event, batch_group where batch_group.id = batch_schedule_event.batch_group_id and batch_schedule_event.id = task.item_id and task.id ="+taskId+" and item_type ='"+TaskItemCategory.CLASSROOM_SESSION+"' ";
			System.out.println("getGroupId>>>"+getGroupId);
			List<HashMap<String, Object>> groupData = util.executeQuery(getGroupId);
			GroupPojo group = new GroupPojo();
			if(groupData.size()>0)
			{
				ArrayList<GroupStudentPojo> students = new ArrayList<>();
				int groupId = (int)groupData.get(0).get("batch_group_id");
				String groupName = groupData.get(0).get("name").toString();
				students = service.studentsInGroup(groupId,taskId);
				
				group.setGroupId(groupId);
				group.setGroupName(groupName);
				group.setStudents(students);
				group.setStuCount(students.size());
				
				HashMap<String, Object> jsonMap = new HashMap<String, Object>();
				
	
				String result = gson.toJson(group);
				return Response.ok(result).build();
			}
			else
			{

				HashMap<String, Object> jsonMap = new HashMap<String, Object>();
				String result = gson.toJson(jsonMap);				
				return Response.ok(result).build();
			}											
			
			
		}catch(Exception e){
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}
	
	
	@POST
	@Path("{taskId}/submit_attendance/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response submitAttendance(@PathParam("userId") int istarUserId,
			@PathParam("taskId") int taskId,
			@FormParam("response") String attendanceResponsesString) {
	
		System.out.println("attendanceResponsesString-->" + attendanceResponsesString);
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		Type listType = new TypeToken<GroupPojo>() {}.getType();
		Gson gsonRequest = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		GroupPojo attendanceResponse = (GroupPojo) gsonRequest.fromJson(attendanceResponsesString, listType);
		TrainerWorkflowServices serv = new TrainerWorkflowServices();
		serv.submitAttendance(taskId, istarUserId, attendanceResponse);	
		serv.updateState(taskId, istarUserId,TrainerWorkflowStages.ATTENDANCE);
		
		
		HashMap<String, Object> jsonMap = new HashMap<String, Object>();
		String result = gson.toJson(jsonMap);				
		return Response.ok(result).build();
	}
	
	@POST
	@Path("{taskId}/submit_feedback/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response submitFeedback(@PathParam("userId") int istarUserId,
			@PathParam("taskId") int taskId,
			@FormParam("response") String FeedbackResponsesString) {
	
		System.out.println("attendanceResponsesString-->" + FeedbackResponsesString);
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		Type listType = new TypeToken<ClassFeedbackByTrainer>() {}.getType();
		Gson gsonRequest = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		ClassFeedbackByTrainer feedbackResponse = (ClassFeedbackByTrainer) gsonRequest.fromJson(FeedbackResponsesString, listType);
		TrainerWorkflowServices serv = new TrainerWorkflowServices();
		serv.submitFeedbackByTrainer(taskId, istarUserId, feedbackResponse);
		serv.updateState(taskId, istarUserId,TrainerWorkflowStages.COMPLETED);
		HashMap<String, Object> jsonMap = new HashMap<String, Object>();
		String result = gson.toJson(jsonMap);				
		return Response.ok(result).build();
	}
	
	@GET
	@Path("{taskId}/get_course_contents")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCourseContent(@PathParam("taskId") int taskId){
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try{
			
			
			CourseContent courseContent = new CourseContent();
			TrainerWorkflowServices service = new TrainerWorkflowServices();
			DBUTILS util = new DBUTILS();
			Integer courseId= null;
			String GetCourseId ="select course_id from task,batch_schedule_event where batch_schedule_event.id = task.item_id and item_type ='"+TaskItemCategory.CLASSROOM_SESSION+"' and task.id = "+taskId;
			System.out.println("getCourseId-------------"+GetCourseId);
			List<HashMap<String, Object>> courseIdData = util.executeQuery(GetCourseId);
			if(courseIdData.size()>0)
			{
				courseId = (int)courseIdData.get(0).get("course_id");
				courseContent = service.getCourseContent(courseId, taskId);	
				
				String result = gson.toJson(courseContent);
				return Response.ok(result).build();
			}
			else
			{
				throw new Exception();
			}	
			
			
		}catch(Exception e){
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}
	
	@GET
	@Path("{taskId}/get_course_contents_student")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCourseContentForStudent(@PathParam("taskId") int taskId){
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try{
			CourseContent courseContent = new CourseContent();
			TrainerWorkflowServices service = new TrainerWorkflowServices();
			DBUTILS util = new DBUTILS();
			Integer courseId= null;
			String GetCourseId ="select course_id from task,batch_schedule_event where batch_schedule_event.id = task.item_id and item_type in ('"+TaskItemCategory.CLASSROOM_SESSION_STUDENT+"','"+TaskItemCategory.REMOTE_CLASS_STUDENT+"') and task.id = "+taskId;
			System.out.println("getCourseId-------------"+GetCourseId);
			List<HashMap<String, Object>> courseIdData = util.executeQuery(GetCourseId);
			String getTrainerTaskId = "select id from task where item_type in ('"+TaskItemCategory.CLASSROOM_SESSION+"','"+TaskItemCategory.REMOTE_CLASS_TRAINER+"') and project_id in (select project_id from task where id = "+taskId+")";
			System.out.println("getTrainerTaskId-------------"+getTrainerTaskId);
			List<HashMap<String, Object>> trainerTaskIdData = util.executeQuery(getTrainerTaskId);
			
			if(courseIdData.size()>0)
			{
				courseId = (int)courseIdData.get(0).get("course_id");
				if(trainerTaskIdData.size()>0 && trainerTaskIdData.get(0).get("id")!=null)
				{
					int trainerTaskId = (int)trainerTaskIdData.get(0).get("id");
					courseContent = service.getCourseContent(courseId, trainerTaskId);	
					
					String result = gson.toJson(courseContent);
					return Response.ok(result).build();
				}
				else
				{
					throw new Exception();
				}	
				
			}
			else
			{
				throw new Exception();
			}	
			
			
		}catch(Exception e){
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}
	
	
	@GET
	@Path("{taskId}/{state}/{user_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateTrainerWorkflowState(@PathParam("taskId") int taskId, @PathParam("state") String state,  @PathParam("user_id") int userId){
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		try{
			
			TrainerWorkflowServices service = new TrainerWorkflowServices();
			service.updateState(taskId, userId, state);					
			HashMap<String, Object> jsonMap = new HashMap<String, Object>();
			String result = gson.toJson(jsonMap);				
			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			String result = e.getMessage() != null ? gson.toJson(e.getMessage())
					: gson.toJson("istarViksitProComplexKeyBad Request or Internal Server Error");
			return Response.status(Response.Status.OK).entity(result).build();
		}
	}
	
}
