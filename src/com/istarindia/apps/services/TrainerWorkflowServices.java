/**
 * 
 */
package com.istarindia.apps.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.istarindia.android.pojo.trainerworkflow.CourseContent;
import com.istarindia.android.pojo.trainerworkflow.CourseItem;
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

	public void submitAttendance(int taskId, int istarUserId, GroupStudentPojo attendanceResponse) {
		
		
		
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
		String getCurrentPrevAndNextItemForCourse = "select lesson_id from slide_change_log , batch_schedule_event, task where task.item_id = batch_schedule_event.id and batch_schedule_event.course_id= slide_change_log.course_id and slide_change_log.batch_group_id = batch_schedule_event.batch_group_id and task.id = "+taskId+" order by slide_change_log.id desc  limit 1";
		System.err.println("getCurrentPrevAndNextItemForCourse>>>>>>"+getCurrentPrevAndNextItemForCourse);			
		List<HashMap<String, Object>> itemStats = util.executeQuery(getCurrentPrevAndNextItemForCourse);
		if(itemStats.size()>0)
		{
			currentItemId = (int)itemStats.get(0).get("lesson_id");
		}
		
		String getCourseId ="select lesson.id , lesson.title,  cast (row_number() over()  as integer ) -1 as order_id from module_course, cmsession_module, lesson_cmsession, lesson where module_course.course_id = "+courseId+" and cmsession_module.module_id = module_course.module_id and lesson_cmsession.cmsession_id = cmsession_module.cmsession_id and lesson_cmsession.lesson_id = lesson.id and lesson.is_published ='t' and lesson.category in ('ILT','BOTH') order by module_course.oid, cmsession_module.oid , lesson_cmsession.oid";
		System.err.println("getCourseId>>>"+getCourseId);
		List<HashMap<String, Object>> courseData = util.executeQuery(getCourseId);
		
		
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
				item.setItemUrl(mediaPath+"/lessonXMLs/"+itemId+".xml");
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
			courseContent.setContentUrl(mediaPath+"/courseZIPs/"+courseId+".zip");
		
		
	}
		return courseContent;
	}
}
