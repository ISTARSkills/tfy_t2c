/**
 * 
 */
package com.istarindia.apps.factories;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.istarindia.android.pojo.TaskSummaryPOJO;
import com.istarindia.android.pojo.task.AssessmentTask;
import com.istarindia.android.pojo.task.ClassRoomSessionTask;
import com.istarindia.android.pojo.trainerworkflow.GroupStudentPojo;
import com.istarindia.apps.services.AppAssessmentServices;
import com.istarindia.apps.services.AppCourseServices;
import com.istarindia.apps.services.TrainerWorkflowServices;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.Course;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.utilities.DBUTILS;

/**
 * @author mayank
 *
 */
public class TaskSummaryPojoCreator {

	public TaskSummaryPOJO getAssessmentTask(Task task) {
		
		TaskSummaryPOJO taskSummaryPOJO = null;
		
		AppAssessmentServices appAssessmentServices= new AppAssessmentServices();
		Assessment assessment = appAssessmentServices.getAssessment(task.getItemId());
		AppCourseServices appCourseServices = new AppCourseServices();

		if(assessment!=null && assessment.getAssessmentQuestions().size()>0){
			Course course = appCourseServices.getCourse(assessment.getCourse());
			taskSummaryPOJO = new TaskSummaryPOJO();
			
			taskSummaryPOJO.setId(task.getId());
			taskSummaryPOJO.setItemId(task.getItemId());
			taskSummaryPOJO.setItemType(task.getItemType());
			if(task.getIsActive()){
				taskSummaryPOJO.setStatus("INCOMPLETE");
				taskSummaryPOJO.setDate(task.getStartDate());
			}else{
				taskSummaryPOJO.setStatus("COMPLETED");
				taskSummaryPOJO.setDate(task.getUpdatedAt());
			}
			taskSummaryPOJO.setTitle(assessment.getAssessmenttitle());
			taskSummaryPOJO.setImageURL(null);
			taskSummaryPOJO.setHeader(assessment.getAssessmentType());
			taskSummaryPOJO.setItemPoints(appAssessmentServices.getMaxPointsOfAssessment(assessment.getId()).intValue());
			taskSummaryPOJO.setNumberOfQuestions(assessment.getAssessmentQuestions().size());
			taskSummaryPOJO.setDuration(assessment.getAssessmentdurationminutes());
			if(assessment.getDescription()==null){
				taskSummaryPOJO.setDescription(course.getCourseDescription());
			}else{
				taskSummaryPOJO.setDescription(assessment.getDescription());
			}
		}
		return taskSummaryPOJO;		
		
	}

	public TaskSummaryPOJO getLessonTask(Task task) {
		String mediaUrlPath ="";
		try{
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
				if (inputStream != null) {
					properties.load(inputStream);
					mediaUrlPath =  properties.getProperty("media_url_path");
					System.out.println("media_url_path"+mediaUrlPath);
				}
			} catch (IOException e) {
				e.printStackTrace();
			
		}
		
		TaskSummaryPOJO taskSummaryPOJO = null;
		
		AppCourseServices appCourseServices= new AppCourseServices();
		Lesson lesson = appCourseServices.getLesson(task.getItemId());
		
		if(lesson!=null){
			taskSummaryPOJO = new TaskSummaryPOJO();
			
			taskSummaryPOJO.setId(task.getId());
			taskSummaryPOJO.setItemId(task.getItemId());
			taskSummaryPOJO.setItemType(task.getItemType()+"_"+lesson.getType());
			if(task.getIsActive()){
				taskSummaryPOJO.setStatus("INCOMPLETE");
				taskSummaryPOJO.setDate(task.getStartDate());
			}else{
				taskSummaryPOJO.setStatus("COMPLETED");
				taskSummaryPOJO.setDate(task.getUpdatedAt());
			}
			taskSummaryPOJO.setHeader(lesson.getSubject());
			taskSummaryPOJO.setTitle(lesson.getTitle());
			taskSummaryPOJO.setDescription(lesson.getDescription());
			taskSummaryPOJO.setImageURL(mediaUrlPath+lesson.getImage_url());
			taskSummaryPOJO.setDuration(lesson.getDuration());			
		}
		return taskSummaryPOJO;
	}

	public TaskSummaryPOJO getFeedbackTask(Task task) {
		// TODO Auto-generated method stub
		return null;
	}

	public TaskSummaryPOJO getClassRoomSessionTask(Task task) {
		
		String mediaUrlPath ="";
		try{
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
				if (inputStream != null) {
					properties.load(inputStream);
					mediaUrlPath =  properties.getProperty("media_url_path");
					System.out.println("media_url_path"+mediaUrlPath);
				}
			} catch (IOException e) {
				e.printStackTrace();
			
		}
		
		TaskSummaryPOJO taskSummaryPOJO = null;		
		AppCourseServices appCourseServices= new AppCourseServices();
		//Lesson lesson = appCourseServices.getLesson(task.getItemId());
		DBUTILS util = new DBUTILS();
		String getEventdetails="select T1.*, pincode.lattiude, pincode.longitude,address.addressline1|| ' ' ||address.addressline2 as address from  (select batch_schedule_event.eventhour, batch_schedule_event.eventminute, batch_group.name as group_name, batch_group.id as group_id, classroom_details.id as classroom_id, classroom_details.classroom_identifier  as classroom_name, organization.address_id as address_id, organization.name as org_name, course.course_name as course_name, batch_schedule_event.eventdate, course.image_url   from task, batch_schedule_event, classroom_details, organization, course	, batch_group where task.item_id = batch_schedule_event.id and batch_schedule_event.classroom_id = classroom_details.id and classroom_details.organization_id = organization.id and batch_schedule_event.batch_group_id = batch_group.id and batch_schedule_event.course_id = course.id and task.item_type ='CLASSROOM_SESSION' and task.id = "+task.getId()+" )T1 left join address on (address.id = T1.address_id) join pincode on (address.pincode_id = pincode.id)";		
		List<HashMap<String, Object>> eventData = util.executeQuery(getEventdetails);
		
		if(eventData.size()>0)
		{
			for(HashMap<String, Object> row: eventData)
			{
				taskSummaryPOJO = new TaskSummaryPOJO();
				String header = row.get("org_name").toString().trim();
				String title = row.get("course_name").toString().trim();
				String taskImage = "/course_images/"+title.charAt(0)+".png";				
				if(row.get("image_url")!=null)
				{
					taskImage = row.get("image_url").toString();
				}
				Integer durationHours = (int)row.get("eventhour");
				Integer durationMinutes = (int)row.get("eventminute");
				Integer totalduration = (durationHours*60) + durationMinutes;
				Double longitude = 77.7473;
				Double lattitude = 12.9716;
				if(row.get("longitude")!=null)
				{
					longitude	=	(double)row.get("longitude");
				}
				if(row.get("lattiude")!=null)
				{
					lattitude = (double)row.get("lattiude");
				}
				
				Integer groupId = (int)row.get("group_id");
				Timestamp eventDate = (Timestamp)row.get("eventdate");
				DateFormat writeFormat = new SimpleDateFormat( "HH:mm:ss");
				taskSummaryPOJO.setTime(writeFormat.format(eventDate));			
				String groupName = row.get("group_name").toString();
				String classRoomName = row.get("classroom_name").toString();
				Integer classRoomId = (int)row.get("classroom_id");
				
				
				taskSummaryPOJO.setClassRoomId(classRoomId);
				taskSummaryPOJO.setClassRoomName(classRoomName);
				taskSummaryPOJO.setDurationHours(durationHours);
				taskSummaryPOJO.setDurationMinutes(durationMinutes);
				taskSummaryPOJO.setGroupName(groupName);
				taskSummaryPOJO.setLattitude(lattitude);
				taskSummaryPOJO.setLongitude(longitude);
				taskSummaryPOJO.setEvent_address(row.get("address").toString());
				
				taskSummaryPOJO.setId(task.getId());
				taskSummaryPOJO.setItemId(task.getItemId());
				taskSummaryPOJO.setItemType(task.getItemType());
				if(task.getIsActive()){
					taskSummaryPOJO.setStatus("INCOMPLETE");
					taskSummaryPOJO.setDate(task.getStartDate());
				}else{
					taskSummaryPOJO.setStatus("COMPLETED");
					taskSummaryPOJO.setDate(task.getUpdatedAt());
				}
				taskSummaryPOJO.setHeader(header);
				taskSummaryPOJO.setTitle(title);
				taskSummaryPOJO.setDescription(null);
				taskSummaryPOJO.setImageURL(mediaUrlPath+taskImage);
				taskSummaryPOJO.setDuration(totalduration);
			
			}
		}
		
		return taskSummaryPOJO;
	}

	public TaskSummaryPOJO getClassroomAssessmentTask(Task task) {
		// TODO Auto-generated method stub
		return null;
	}

	public TaskSummaryPOJO getContentTask(Task task) {
		// TODO Auto-generated method stub
		return null;
	}

}
