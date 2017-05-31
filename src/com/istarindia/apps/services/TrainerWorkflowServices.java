/**
 * 
 */
package com.istarindia.apps.services;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.istarindia.android.pojo.trainerworkflow.ClassFeedbackByTrainer;
import com.istarindia.android.pojo.trainerworkflow.CourseContent;
import com.istarindia.android.pojo.trainerworkflow.CourseItem;
import com.istarindia.android.pojo.trainerworkflow.FeedbackPojo;
import com.istarindia.android.pojo.trainerworkflow.GroupPojo;
import com.istarindia.android.pojo.trainerworkflow.GroupStudentPojo;
import com.viksitpro.core.utilities.DBUTILS;
import com.viksitpro.core.utilities.TaskItemCategory;

/**
 * @author mayank
 *
 */
public class TrainerWorkflowServices {
	
	
	public ArrayList<GroupStudentPojo> studentsInGroup(int groupId)
	{
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
		
		ArrayList<GroupStudentPojo> students = new ArrayList<>();
		DBUTILS utils = new DBUTILS();
		String sql="select distinct  user_profile.user_id, user_profile.first_name, user_profile.profile_image  from batch_students, user_profile where batch_students.batch_group_id = "+groupId+" and batch_students.student_id = user_profile.user_id";
		List<HashMap<String, Object>> studentData = utils.executeQuery(sql);
		for(HashMap<String, Object> row : studentData)
		{
			int studentId = (int)row.get("user_id");
			String name = row.get("first_name").toString();
			String profileImage = "/users/"+name.charAt(0)+".png";
			if(row.get("profile_image")!=null)
			{
				profileImage = row.get("profile_image").toString();
			}
			GroupStudentPojo stu = new GroupStudentPojo();
			stu.setImageUrl(mediaUrlPath+profileImage);
			stu.setStudentId(studentId);
			stu.setStudentName(name);
			students.add(stu);
		}
		
		return students;
	}

	public void submitAttendance(int taskId, int istarUserId, GroupPojo attendanceResponse) {
		
		DBUTILS util = new DBUTILS();
		
		String deleteOldEntry = "delete from attendance where event_id = (select item_id from task where id = "+taskId+")";
		util.executeUpdate(deleteOldEntry);
		for(GroupStudentPojo stu :attendanceResponse.getStudents())
		{
			String status ="ABSENT";
			if(stu.getStatus()!=null && stu.getStatus() )
			{
				status="PRESENT";
			}
			else
			{
				status="ABSENT";
			}	
			String insertIntoAttendance ="INSERT INTO attendance (id, taken_by, user_id, status, created_at, updated_at, event_id) "
					+ "VALUES ((select COALESCE(max(id),0)+1 from attendance), '"+istarUserId+"', '"+stu.getStudentId()+"', '"+status+"', 'now()', 'now()', (select item_id from task where id="+taskId+"));";
			util.executeUpdate(insertIntoAttendance);
		}
		
	}

	public CourseContent getCourseContent(Integer courseId, int taskId) {
		String mediaPath = "";
		try {
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);
				mediaPath = properties.getProperty("media_url_path");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		DBUTILS util = new DBUTILS();
		CourseContent courseContent = new CourseContent();
		Integer prevItemOrderId = null;
		Integer curentItemOrderId = null;
		Integer nextItemOrderId = null;
		Integer currentItemId = null;
		Long currentItemSlideId=null;
		String getCurrentPrevAndNextItemForCourse = "select lesson_id , slide_id  as slide_id from slide_change_log , batch_schedule_event, task where task.item_id = batch_schedule_event.id and batch_schedule_event.course_id= slide_change_log.course_id and slide_change_log.batch_group_id = batch_schedule_event.batch_group_id and task.id = "+taskId+" order by slide_change_log.id desc  limit 1";
		System.err.println("getCurrentPrevAndNextItemForCourse>>>>>>"+getCurrentPrevAndNextItemForCourse);			
		List<HashMap<String, Object>> itemStats = util.executeQuery(getCurrentPrevAndNextItemForCourse);
		if(itemStats.size()>0)
		{
			currentItemId = (int)itemStats.get(0).get("lesson_id");
			currentItemSlideId = ((BigInteger)itemStats.get(0).get("slide_id")).longValue();
		}
		
		String getCourseId ="select lesson.id , lesson.title,  cast (row_number() over()  as integer ) -1 as order_id from module_course, cmsession_module, lesson_cmsession, lesson where module_course.course_id = "+courseId+" and cmsession_module.module_id = module_course.module_id and lesson_cmsession.cmsession_id = cmsession_module.cmsession_id and lesson_cmsession.lesson_id = lesson.id and lesson.is_published ='t' and lesson.category in ('ILT','BOTH') and lesson.type!='ASSESSMENT' order by module_course.oid, cmsession_module.oid , lesson_cmsession.oid";
		System.err.println("getCourseId>>>"+getCourseId);
		List<HashMap<String, Object>> courseData = util.executeQuery(getCourseId);
		
		
		if(courseData.size()>0)
		{
			ArrayList<CourseItem> courseItems = new ArrayList<>();
			for(HashMap<String, Object> row: courseData)
			{
				CourseItem item = new CourseItem();
				Integer itemId = (int)row.get("id");
				String itemName = row.get("title").toString().trim();
				int orderId = (int)row.get("order_id");
				item.setItemId(itemId);
				item.setItemName(itemName);
				item.setItemType(TaskItemCategory.LESSON);
				item.setOrderId(orderId);
				item.setItemUrl(mediaPath+"/lessonXMLs/"+itemId+".xml");
				if(currentItemId!=null  && itemId==currentItemId)
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
			courseContent.setCourseId(courseId);
			courseContent.setCurrentItemSlideId(currentItemSlideId);
			courseContent.setContentUrl(mediaPath+"/courseZIPs/"+courseId+".zip");
		
		
	}
		return courseContent;
	}

	public void submitFeedbackByTrainer(int taskId, int istarUserId, ClassFeedbackByTrainer feedbackResponse) {
		
		HashMap<String, String> feedbackData= new HashMap<>(); 
		feedbackData.put("", "0");
		for(FeedbackPojo pojo : feedbackResponse.getFeedbacks())
		{
			feedbackData.put(pojo.getName().toLowerCase(), pojo.getRating());
		}
		DBUTILS util = new DBUTILS();
		String checkIfExist ="delete from trainer_feedback where event_id = (select item_id from task where id = "+taskId+")";
		util.executeUpdate(checkIfExist);
		float avgRating = 5;
		float totalRating = Float.parseFloat(feedbackData.get("noise")) +Float.parseFloat(feedbackData.get("attendance")) +Float.parseFloat(feedbackData.get("sick")) +Float.parseFloat(feedbackData.get("content")) +Float.parseFloat(feedbackData.get("assignment")) +Float.parseFloat(feedbackData.get("internals")) +Float.parseFloat(feedbackData.get("internet")) +Float.parseFloat(feedbackData.get("electricity")) +Float.parseFloat(feedbackData.get("time"));
			avgRating = totalRating/9;
		String insertFeedback="INSERT INTO trainer_feedback (id, user_id, rating, comments, event_id, noise, attendance, sick, content, assignment, internals, internet, electricity, time) "
				+ "VALUES ((select COALESCE(max(id),0)+1 from trainer_feedback), "+istarUserId+","
						+ " "+avgRating+", "
								+ "'"+feedbackData.get("comments")+"',"
										+ " ( select item_id from task where id="+taskId+"),"
												+ " "+Float.parseFloat(feedbackData.get("noise"))+","
														+ " "+Float.parseFloat(feedbackData.get("attendance"))+","
																+ " "+Float.parseFloat(feedbackData.get("sick"))+","
																		+ " "+Float.parseFloat(feedbackData.get("content"))+","
																				+ " "+Float.parseFloat(feedbackData.get("assignment"))+","
																						+ " "+Float.parseFloat(feedbackData.get("internals"))+", "
																								+ ""+Float.parseFloat(feedbackData.get("internet"))+","
																										+ " "+Float.parseFloat(feedbackData.get("electricity"))+", "
																												+ ""+Float.parseFloat(feedbackData.get("time"))+");";
		util.executeUpdate(insertFeedback);
		
		
		
	}
}
