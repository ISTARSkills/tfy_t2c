/**
 * 
 */
package com.istarindia.android.rest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
				students = service.studentsInGroup(groupId);
				
				group.setGroupId(groupId);
				group.setGroupName(groupName);
				group.setStudents(students);
				group.setStuCount(students.size());
				
				HashMap<String, Object> jsonMap = new HashMap<String, Object>();
				
				jsonMap.put("group_details", group);
	
				String result = gson.toJson(jsonMap);
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
		Type listType = new TypeToken<GroupStudentPojo>() {}.getType();
		Gson gsonRequest = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		GroupStudentPojo attendanceResponse = (GroupStudentPojo) gsonRequest.fromJson(attendanceResponsesString, listType);
		TrainerWorkflowServices serv = new TrainerWorkflowServices();
		serv.submitAttendance(taskId, istarUserId, attendanceResponse);		
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
			
			TrainerWorkflowServices service = new TrainerWorkflowServices();
			DBUTILS util = new DBUTILS();
			
			Integer prevItemOrderId = null;
			Integer curentItemOrderId = null;
			Integer nextItemOrderId = null;
			Integer currentItemId = null;
			String getCurrentPrevAndNextItemForCourse = "select lesson_id from slide_change_log , batch_schedule_event, task where task.item_id = batch_schedule_event.id and batch_schedule_event.course_id= slide_change_log.course_id and slide_change_log.batch_group_id = batch_schedule_event.batch_group_id and task.id = "+taskId+" order by slide_change_log.id desc  limit 1";
			System.err.println("getCurrentPrevAndNextItemForCourse>>>>>>"+getCurrentPrevAndNextItemForCourse);			
			List<HashMap<String, Object>> itemStats = util.executeQuery(getCurrentPrevAndNextItemForCourse);
			if(itemStats.size()>0)
			{
				currentItemId = (int)itemStats.get(0).get("lesson_id");
			}
			
			String getCourseId ="select lesson.id , lesson.title,  cast (row_number() over()  as integer ) -1 as order_id from module_course, cmsession_module, lesson_cmsession, lesson where module_course.course_id = (select course_id from task,batch_schedule_event where batch_schedule_event.id = task.item_id and item_type ='"+TaskItemCategory.CLASSROOM_SESSION+"' and task.id = "+taskId+") and cmsession_module.module_id = module_course.module_id and lesson_cmsession.cmsession_id = cmsession_module.cmsession_id and lesson_cmsession.lesson_id = lesson.id and lesson.is_published ='t' and lesson.category in ('ILT','BOTH') order by module_course.oid, cmsession_module.oid , lesson_cmsession.oid";
			System.err.println("getCourseId>>>"+getCourseId);
			List<HashMap<String, Object>> courseData = util.executeQuery(getCourseId);
			CourseContent courseContent = new CourseContent();
			
			if(courseData.size()>0)
			{
				ArrayList<CourseItem> courseItems = new ArrayList<>();
				for(HashMap<String, Object> row: courseData)
				{
					CourseItem item = new CourseItem();
					int itemId = (int)row.get("id");
					String itemName = row.get("title").toString().trim();
					int orderId = (int)row.get("order_id");
					item.setItemId(itemId);
					item.setItemName(itemName);
					item.setItemType(TaskItemCategory.LESSON);
					item.setOrderId(orderId);
					if(itemId==currentItemId)
					{
						curentItemOrderId = orderId;
						if(orderId+1<courseData.size())
						{
							nextItemOrderId = orderId+1;
						}
						if(orderId-1 >=0)
						{
							prevItemOrderId = orderId-1;
						}
					}
					courseItems.add(item);
				}
				if(curentItemOrderId==null)
				{
					curentItemOrderId = 0;
				}
				courseContent.setCurrentItemOrderId(curentItemOrderId);
				courseContent.setNextItemOrderId(nextItemOrderId);
				courseContent.setPreviousItemOrderId(prevItemOrderId);				
				courseContent.setItems(courseItems);							
				
				HashMap<String, Object> jsonMap = new HashMap<String, Object>();				
				jsonMap.put("course_content", courseContent);
	
				String result = gson.toJson(jsonMap);
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
	
	
	
}
