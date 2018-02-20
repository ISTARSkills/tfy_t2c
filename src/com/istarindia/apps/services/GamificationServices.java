/**
 * 
 */
package com.istarindia.apps.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.utilities.DBUTILS;

/**
 * @author mayank
 *
 */
public class GamificationServices {

	public void updatePointsAndCoinsOnLessonComplete(IstarUser istarUser , Lesson lesson)
	{
		DBUTILS util = new DBUTILS();
		String findLessonDetails ="select lesson_cmsession.lesson_id, lesson_cmsession.cmsession_id, module_course.module_id, module_course.course_id from lesson_cmsession, cmsession_module, module_course where lesson_cmsession.cmsession_id = cmsession_module.cmsession_id and cmsession_module.module_id = module_course.module_id and lesson_cmsession.lesson_id = "+lesson.getId()+" limit 1";		
		List<HashMap<String, Object>> lessonData = util.executeQuery(findLessonDetails); 
		int courseId = 0;
		int moduleId = 0;
		int cmsessionId = 0;
		for(HashMap<String, Object> lessonRow: lessonData)
		{
			 courseId = (int)lessonRow.get("course_id");
			 moduleId = (int)lessonRow.get("module_id");
			 cmsessionId = (int)lessonRow.get("cmsession_id");
		}
		
		/*String per_assessment_points="",
				per_lesson_points="",
				per_question_points ="",per_lesson_coins="";
		try{
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
				if (inputStream != null) {
					properties.load(inputStream);
					per_assessment_points =  properties.getProperty("per_assessment_points");
					per_lesson_points =  properties.getProperty("per_lesson_points");
					per_question_points =  properties.getProperty("per_question_points");
					per_lesson_coins = properties.getProperty("per_lesson_coins");
					//ViksitLogger.logMSG(this.getClass().getName(),"per_lesson_coins"+per_lesson_coins);
				}
			} catch (IOException e) {
				e.printStackTrace();			
		}*/
		
		
		String findPrimaryGroupsOfUser = "SELECT distinct	batch_group.id, batch_group.college_id FROM 	batch_students, 	batch_group WHERE 	batch_group. ID = batch_students.batch_group_id AND batch_students.student_id = "+istarUser.getId()+" and batch_group.is_primary='t'";
		//ViksitLogger.logMSG(this.getClass().getName(),"findPrimaryGroupsOfUser>>"+findPrimaryGroupsOfUser);
		List<HashMap<String, Object>> primaryBG = util.executeQuery(findPrimaryGroupsOfUser);
		
		for(HashMap<String, Object>primaryG : primaryBG)
		{
			int groupId = (int)primaryG.get("id");
			int orgId = (int)primaryG.get("college_id");
			String findSkillsInAssesssment = "select skill_objective_id, max_points from assessment_benchmark where item_id = "+lesson.getId()+" and item_type ='LESSON'";
			//ViksitLogger.logMSG(this.getClass().getName(),"findSkillsInLesson>>>>>>"+findSkillsInAssesssment);
			List<HashMap<String, Object>> skillsData = util.executeQuery(findSkillsInAssesssment);
			for(HashMap<String, Object> skills : skillsData)
			{
				int skillObjectiveId = (int)skills.get("skill_objective_id");
				String maxPoints = (String)skills.get("max_points");				
				//double coins = Double.parseDouble(per_lesson_coins);
				String coins = "( :per_lesson_coins )";
				String getPreviousCoins="select * from user_gamification where item_id ='"+lesson.getId()+"' and item_type='LESSON' and "
						+ "istar_user='"+istarUser.getId()+"' and batch_group_id="+groupId+" and skill_objective="+skillObjectiveId+"  order by timestamp desc limit 1";
				//ViksitLogger.logMSG(this.getClass().getName(),"getPreviousCoins"+getPreviousCoins);
				List<HashMap<String, Object>> coinsData = util.executeQuery(getPreviousCoins);
				if(coinsData.size()>0)
				{
					String prevCoins = (String)coinsData.get(0).get("coins");
					coins= coins+" + "+prevCoins;
 				}								
				
				String insertIntoGamification="INSERT INTO user_gamification (id,istar_user, skill_objective, points, coins, created_at, updated_at, item_id, item_type,  course_id,cmsession_id, module_id, batch_group_id, org_id, timestamp, max_points) VALUES "
						+ "((SELECT COALESCE(MAX(ID),0)+1 FROM user_gamification),"+istarUser.getId()+", "+skillObjectiveId+",'"+maxPoints+"' , '"+coins+"', now(), now(), "+lesson.getId()+", 'LESSON', "+courseId+","+cmsessionId+","+moduleId+", "+groupId+", "+orgId+", now(), '"+maxPoints+"');";
				//ViksitLogger.logMSG(this.getClass().getName(),"insertIntoGamification>>>>"+insertIntoGamification);
				util.executeUpdate(insertIntoGamification);
			}			
		}
		
	}
	
	private void updatePointsAndCoinsForAssessment(IstarUser istarUser, Assessment assessment) {
		//here we will update points and coins for IstarUser for a particular assessment.
	/*	String per_assessment_points="",
				per_lesson_points="",
				per_question_points ="",per_assessment_coins="";
		try{
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
				if (inputStream != null) {
					properties.load(inputStream);
					per_assessment_points =  properties.getProperty("per_assessment_points");
					per_lesson_points =  properties.getProperty("per_lesson_points");
					per_question_points =  properties.getProperty("per_question_points");
					per_assessment_coins = properties.getProperty("per_assessment_coins");
					//ViksitLogger.logMSG(this.getClass().getName(),"per_assessment_points"+per_assessment_points);
				}
			} catch (IOException e) {
				e.printStackTrace();			
		}*/
		DBUTILS util = new DBUTILS();
		String findPrimaryGroupsOfUser = "SELECT distinct	batch_group.id, batch_group.college_id FROM 	batch_students, 	batch_group WHERE 	batch_group. ID = batch_students.batch_group_id AND batch_students.student_id = "+istarUser.getId()+" and batch_group.is_primary='t'";
		//ViksitLogger.logMSG(this.getClass().getName(),"findPrimaryGroupsOfUser>>"+findPrimaryGroupsOfUser);
		List<HashMap<String, Object>> primaryBG = util.executeQuery(findPrimaryGroupsOfUser);
		
		for(HashMap<String, Object>primaryG : primaryBG)
		{
			int groupId = (int)primaryG.get("id");
			int orgId = (int)primaryG.get("college_id");
			String findSkillsInAssesssment = "select skill_objective_id, max_points from assessment_benchmark where "
					+ "item_id = "+assessment.getId()+" and item_type ='ASSESSMENT'";
			//ViksitLogger.logMSG(this.getClass().getName(),"findSkillsInAssesssment>>>>>>"+findSkillsInAssesssment);
			List<HashMap<String, Object>> skillsData = util.executeQuery(findSkillsInAssesssment);
			for(HashMap<String, Object> skills : skillsData)
			{
				int skillObjectiveId = (int)skills.get("skill_objective_id");
				String maxPoints = (String)skills.get("max_points");				
				String coins = "( :per_assessment_coins )";
				String getPreviousCoins="select * from user_gamification where item_id ='"+assessment.getId()+"' and item_type='ASSESSMENT' and "
						+ "istar_user='"+istarUser.getId()+"' and batch_group_id="+groupId+" and skill_objective="+skillObjectiveId+"  order by timestamp desc limit 1";
				//ViksitLogger.logMSG(this.getClass().getName(),"getPreviousCoins"+getPreviousCoins);
				List<HashMap<String, Object>> coinsData = util.executeQuery(getPreviousCoins);
				if(coinsData.size()>0)
				{
					String prevCoins = (String)coinsData.get(0).get("coins");
					coins= coins+" + "+prevCoins;
 				}
				
				String insertIntoGamification="INSERT INTO user_gamification (id,istar_user, skill_objective, points, coins, created_at, updated_at, item_id, item_type,  course_id, batch_group_id, org_id, timestamp,max_points) VALUES "
						+ "((SELECT COALESCE(MAX(ID),0)+1 FROM user_gamification),"+istarUser.getId()+", "+skillObjectiveId+",'"+maxPoints+"' , '"+coins+"', now(), now(), "+assessment.getId()+", 'ASSESSMENT', "+assessment.getCourse()+", "+groupId+", "+orgId+", now(),'"+maxPoints+"');";
				//ViksitLogger.logMSG(this.getClass().getName(),"insertIntoGamification>>>>"+insertIntoGamification);
				util.executeUpdate(insertIntoGamification);
			}			
		}
		
	}

	public void updateUserGamificationAfterAssessment(IstarUser istarUser, Assessment assessment) {
		updatePointsAndCoinsForAssessment(istarUser, assessment);
		updatePointsAndCoinsForQuestion(istarUser,assessment);
	}

	private void updatePointsAndCoinsForQuestion(IstarUser istarUser, Assessment assessment) {
		
		
		DBUTILS util = new DBUTILS();
		String findPrimaryGroupsOfUser = "SELECT distinct	batch_group.id, batch_group.college_id FROM 	batch_students, 	batch_group WHERE 	batch_group. ID = batch_students.batch_group_id AND batch_students.student_id = "+istarUser.getId()+" and batch_group.is_primary='t'";
		//ViksitLogger.logMSG(this.getClass().getName(),"findPrimaryGroupsOfUser>>"+findPrimaryGroupsOfUser);
		List<HashMap<String, Object>> primaryBG = util.executeQuery(findPrimaryGroupsOfUser);
		
		for(HashMap<String, Object>primaryG : primaryBG)
		{
			int groupId = (int)primaryG.get("id");
			int orgId = (int)primaryG.get("college_id");
			ArrayList<Integer> questionAnsweredCorrectly = new ArrayList<>();
			String findQueAnsweredCorrectly= "select distinct question_id from student_assessment where assessment_id ="+assessment.getId()+" and student_id="+istarUser.getId()+" and correct='t'";
			//ViksitLogger.logMSG(this.getClass().getName(),"correct que id "+findQueAnsweredCorrectly );
			List<HashMap<String, Object>> correctQueAnsweredData = util.executeQuery(findQueAnsweredCorrectly);
			for(HashMap<String, Object> qro : correctQueAnsweredData)
			{
				//ViksitLogger.logMSG(this.getClass().getName(),"correct que id"+(int)qro.get("question_id"));
				questionAnsweredCorrectly.add((int)qro.get("question_id"));
			}
			
			String findQuestionForAssessment="select distinct questionid from assessment_question, question, assessment where assessment_question.questionid = question.id and assessment_question.assessmentid = assessment.id and assessment.course_id = question.context_id and assessment.id = "+assessment.getId();
			List<HashMap<String, Object>> questionData = util.executeQuery(findQuestionForAssessment);
			for(HashMap<String, Object> qRow: questionData)
			{
				int questionId = (int)qRow.get("questionid");
				
				
				String findSkillsInQuestion = "select skill_objective_id, max_points from assessment_benchmark where item_id = "+questionId+" and item_type ='QUESTION'";
				//ViksitLogger.logMSG(this.getClass().getName(),"findSkillsInAssesssment>>>>>>"+findSkillsInQuestion);
				List<HashMap<String, Object>> skillsData = util.executeQuery(findSkillsInQuestion);
				for(HashMap<String, Object> skills : skillsData)
				{
					int skillObjectiveId = (int)skills.get("skill_objective_id");
					String maxPoints = (String)skills.get("max_points");
					String pointsScored = maxPoints;
					String coins = "( :per_question_coins )";
					
											
					String getPreviousCoins="select * from user_gamification where item_id ='"+questionId+"' and item_type='QUESTION' and istar_user='"+istarUser.getId()+"' and batch_group_id="+groupId+" and skill_objective="+skillObjectiveId+"  order by timestamp desc limit 1";
					//ViksitLogger.logMSG(this.getClass().getName(),"getPreviousCoins"+getPreviousCoins);
					List<HashMap<String, Object>> coinsData = util.executeQuery(getPreviousCoins);
					if(coinsData.size()>0)
					{
						String prevCoins = (String)coinsData.get(0).get("coins");
						coins= coins+" + "+prevCoins;
						pointsScored = (String)coinsData.get(0).get("points");
						if(assessment.getRetryAble()!= null && assessment.getRetryAble())
						{
							if(questionAnsweredCorrectly.contains(questionId))
							{
								//ViksitLogger.logMSG(this.getClass().getName(),"questionAnsweredCorrectly contains "+ questionId);
								pointsScored = maxPoints;
							}
							else
							{
								//ViksitLogger.logMSG(this.getClass().getName(),"questionAnsweredCorrectl do not  contains "+ questionId);
								pointsScored = "0";
							}	
							
						}
						
	 				}
					else
					{
						//user has not answerd thos question previously 
						if(!questionAnsweredCorrectly.contains(questionId))
						{
							pointsScored="0";
							
						}
					}	
					
					
					
					String insertIntoGamification="INSERT INTO user_gamification (id,istar_user, skill_objective, points, coins, created_at, updated_at, item_id, item_type,  course_id, batch_group_id, org_id, timestamp,max_points) VALUES "
							+ "((SELECT COALESCE(MAX(ID),0)+1 FROM user_gamification),"+istarUser.getId()+", "+skillObjectiveId+",'"+pointsScored+"' , '"+coins+"', now(), now(), "+questionId+", 'QUESTION', "+assessment.getCourse()+", "+groupId+", "+orgId+", now(),'"+maxPoints+"');";
					//ViksitLogger.logMSG(this.getClass().getName(),"insertIntoGamification>>>>"+insertIntoGamification);
					util.executeUpdate(insertIntoGamification);
				}
			}						
		}		
	}
}
