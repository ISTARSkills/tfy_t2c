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

import com.istarindia.android.pojo.trainerworkflow.GroupStudentPojo;
import com.viksitpro.core.utilities.DBUTILS;

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

}
