package com.istarindia.android.utility;

import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.istarindia.android.pojo.AssessmentPOJO;
import com.istarindia.android.pojo.TaskSummaryPOJO;
import com.istarindia.apps.services.AppAssessmentServices;
import com.istarindia.apps.services.AppCourseServices;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.Task;

public class AppDashboardUtility {

	public TaskSummaryPOJO getTaskSummaryPOJOForAssessment(Task task){
	
		TaskSummaryPOJO taskSummaryPOJO = null;
		
		AppAssessmentServices appAssessmentServices= new AppAssessmentServices();
		Assessment assessment = appAssessmentServices.getAssessment(task.getItemId());
		
		if(assessment!=null && assessment.getAssessmentQuestions().size()>0){
			taskSummaryPOJO = new TaskSummaryPOJO();
			
			taskSummaryPOJO.setId(task.getId());
			taskSummaryPOJO.setItemId(task.getItemId());
			taskSummaryPOJO.setItemType(task.getItemType());
			if(task.getIsActive()){
				taskSummaryPOJO.setStatus("INCOMPLETE");
			}else{
				taskSummaryPOJO.setStatus("COMPLETE");
				taskSummaryPOJO.setDate(task.getUpdatedAt());
			}
			taskSummaryPOJO.setTitle(assessment.getAssessmenttitle());
			taskSummaryPOJO.setImageURL("/root/talentify/assessment.png");
			taskSummaryPOJO.setHeader(assessment.getAssessmentType());
			taskSummaryPOJO.setItemPoints(appAssessmentServices.getMaxPointsOfAssessment(assessment.getId()).intValue());
			taskSummaryPOJO.setNumberOfQuestions(assessment.getAssessmentQuestions().size());
			taskSummaryPOJO.setDuration(assessment.getAssessmentdurationminutes());
			taskSummaryPOJO.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. "
					+ "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, "
					+ "when an unknown printer took a galley of type and scrambled it to make a type specimen book. "
					+ "It has survived not only five centuries, but also the leap into electronic typesetting, "
					+ "remaining essentially unchanged");
		}
		return taskSummaryPOJO;
	}
	
	public TaskSummaryPOJO getTaskSummaryPOJOForLesson(Task task){
		
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
			}else{
				taskSummaryPOJO.setStatus("COMPLETE");
				taskSummaryPOJO.setDate(task.getUpdatedAt());
			}
			taskSummaryPOJO.setHeader(lesson.getSubject());
			taskSummaryPOJO.setTitle(lesson.getTitle());
			taskSummaryPOJO.setDescription(lesson.getDescription());
			taskSummaryPOJO.setImageURL(lesson.getImage_url());
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
