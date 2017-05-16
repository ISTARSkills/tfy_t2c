package com.istarindia.android.utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.istarindia.android.pojo.AssessmentPOJO;
import com.istarindia.android.pojo.TaskSummaryPOJO;
import com.istarindia.apps.services.AppAssessmentServices;
import com.istarindia.apps.services.AppCourseServices;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.Course;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.Task;

public class AppDashboardUtility {

	public TaskSummaryPOJO getTaskSummaryPOJOForAssessment(Task task){
	
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
				taskSummaryPOJO.setDate(task.getEndDate());
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
	
	public TaskSummaryPOJO getTaskSummaryPOJOForLesson(Task task){
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
				taskSummaryPOJO.setDate(task.getEndDate());
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
	
	public AssessmentPOJO getAssessmentForTask(Task task) {

		AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
		Assessment assessment = appAssessmentServices.getAssessment(task.getItemId());
		AssessmentPOJO assessmentPOJO=null;
		AppPOJOUtility appPOJOUtility = new AppPOJOUtility();
			if(assessment!=null && assessment.getAssessmentQuestions().size() > 0){
				System.out.println("Assessment not null");
				assessmentPOJO = appPOJOUtility.getAssessmentPOJO(assessment);
		}
		return assessmentPOJO;
	}
	
	@SuppressWarnings("unchecked")
	public String getLessonForTask(Task task) {

		String lessonXML = null;
		
		String sql = "select cast(lesson_xml as varchar) from lesson where id= :itemId";
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("itemId", task.getItemId());

		List<String> results = query.list();
		
		if(results.size()>0){
			lessonXML = results.get(0);
		}
		return lessonXML;
	}
	
	public void updateStudentPlaylistStatus(int lessonId, int istarUserId, String status){		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();			
		String sql = "update student_playlist set status='"+status+"' where student_id="+istarUserId+" and lesson_id="+lessonId;
		System.out.println(sql);
		SQLQuery query = session.createSQLQuery(sql);
		query.executeUpdate();
		session.close();
		System.out.println("Updating Student Playlist status");
	}

/*	public Object getDashboardCardForLessonTest(Task task) {

		int itemId = task.getItemId();
		AppCourseServices appCourseServices = new AppCourseServices();
		Lesson lesson = appCourseServices.getLesson(itemId);

		Object dashboardCard = null;
		if (lesson != null) {
			
			if(lesson.getType().equals("VIDEO")){
				System.out.println("LEsson is of type VIDEO");
				String thumbnailURL = lesson.getVideoLesson().getVideo_thumb_url();
				String videoURL = lesson.getVideoLesson().getVideo_url();
				
				dashboardCard = new DashboardCard(task.getId(), lesson.getTitle(), task.getState(), lesson.getDescription(),
						thumbnailURL, videoURL, task.getItemType(), lesson.getId());
			}else if(lesson.getType().equals("INTERACTIVE")){
				dashboardCard = lesson.getLessonXml();
			}else{
				dashboardCard = new DashboardCard(task.getId(), lesson.getTitle(), task.getState(), lesson.getDescription(),
						"/root/talentify/presentation.jpeg", null, task.getItemType(), lesson.getId());
			}
		}
		return dashboardCard;
	}*/
}
