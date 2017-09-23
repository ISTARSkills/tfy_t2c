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
					//System.out.println("per_lesson_coins"+per_lesson_coins);
				}
			} catch (IOException e) {
				e.printStackTrace();			
		}*/
		
		
		String findPrimaryGroupsOfUser = "SELECT distinct	batch_group.id, batch_group.college_id FROM 	batch_students, 	batch_group WHERE 	batch_group. ID = batch_students.batch_group_id AND batch_students.student_id = "+istarUser.getId()+" and batch_group.is_primary='t'";
		//System.out.println("findPrimaryGroupsOfUser>>"+findPrimaryGroupsOfUser);
		List<HashMap<String, Object>> primaryBG = util.executeQuery(findPrimaryGroupsOfUser);
		
		for(HashMap<String, Object>primaryG : primaryBG)
		{
			int groupId = (int)primaryG.get("id");
			int orgId = (int)primaryG.get("college_id");
			String findSkillsInAssesssment = "select skill_objective_id, max_points from assessment_benchmark where item_id = "+lesson.getId()+" and item_type ='LESSON' and course_id="+courseId;
			//System.out.println("findSkillsInLesson>>>>>>"+findSkillsInAssesssment);
			List<HashMap<String, Object>> skillsData = util.executeQuery(findSkillsInAssesssment);
			for(HashMap<String, Object> skills : skillsData)
			{
				int skillObjectiveId = (int)skills.get("skill_objective_id");
				String maxPoints = (String)skills.get("max_points");				
				//double coins = Double.parseDouble(per_lesson_coins);
				String coins = "( :per_lesson_coins )";
				String getPreviousCoins="select * from user_gamification where item_id ='"+lesson.getId()+"' and item_type='LESSON' and "
						+ "istar_user='"+istarUser.getId()+"' and batch_group_id="+groupId+" and skill_objective="+skillObjectiveId+"  order by timestamp desc limit 1";
				//System.out.println("getPreviousCoins"+getPreviousCoins);
				List<HashMap<String, Object>> coinsData = util.executeQuery(getPreviousCoins);
				if(coinsData.size()>0)
				{
					String prevCoins = (String)coinsData.get(0).get("coins");
					coins= coins+" + "+prevCoins;
 				}								
				
				String insertIntoGamification="INSERT INTO user_gamification (id,istar_user, skill_objective, points, coins, created_at, updated_at, item_id, item_type,  course_id,cmsession_id, module_id, batch_group_id, org_id, timestamp, max_points) VALUES "
						+ "((SELECT COALESCE(MAX(ID),0)+1 FROM user_gamification),"+istarUser.getId()+", "+skillObjectiveId+",'"+maxPoints+"' , '"+coins+"', now(), now(), "+lesson.getId()+", 'LESSON', "+courseId+","+cmsessionId+","+moduleId+", "+groupId+", "+orgId+", now(), '"+maxPoints+"');";
				//System.out.println("insertIntoGamification>>>>"+insertIntoGamification);
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
					//System.out.println("per_assessment_points"+per_assessment_points);
				}
			} catch (IOException e) {
				e.printStackTrace();			
		}*/
		DBUTILS util = new DBUTILS();
		String findPrimaryGroupsOfUser = "SELECT distinct	batch_group.id, batch_group.college_id FROM 	batch_students, 	batch_group WHERE 	batch_group. ID = batch_students.batch_group_id AND batch_students.student_id = "+istarUser.getId()+" and batch_group.is_primary='t'";
		//System.out.println("findPrimaryGroupsOfUser>>"+findPrimaryGroupsOfUser);
		List<HashMap<String, Object>> primaryBG = util.executeQuery(findPrimaryGroupsOfUser);
		
		for(HashMap<String, Object>primaryG : primaryBG)
		{
			int groupId = (int)primaryG.get("id");
			int orgId = (int)primaryG.get("college_id");
			String findSkillsInAssesssment = "select skill_objective_id, max_points from assessment_benchmark where "
					+ "item_id = "+assessment.getId()+" and item_type ='ASSESSMENT'";
			//System.out.println("findSkillsInAssesssment>>>>>>"+findSkillsInAssesssment);
			List<HashMap<String, Object>> skillsData = util.executeQuery(findSkillsInAssesssment);
			for(HashMap<String, Object> skills : skillsData)
			{
				int skillObjectiveId = (int)skills.get("skill_objective_id");
				String maxPoints = (String)skills.get("max_points");				
				String coins = "( :per_assessment_coins )";
				String getPreviousCoins="select * from user_gamification where item_id ='"+assessment.getId()+"' and item_type='ASSESSMENT' and "
						+ "istar_user='"+istarUser.getId()+"' and batch_group_id="+groupId+" and skill_objective="+skillObjectiveId+"  order by timestamp desc limit 1";
				//System.out.println("getPreviousCoins"+getPreviousCoins);
				List<HashMap<String, Object>> coinsData = util.executeQuery(getPreviousCoins);
				if(coinsData.size()>0)
				{
					String prevCoins = (String)coinsData.get(0).get("coins");
					coins= coins+" + "+prevCoins;
 				}
				
				String insertIntoGamification="INSERT INTO user_gamification (id,istar_user, skill_objective, points, coins, created_at, updated_at, item_id, item_type,  course_id, batch_group_id, org_id, timestamp,max_points) VALUES "
						+ "((SELECT COALESCE(MAX(ID),0)+1 FROM user_gamification),"+istarUser.getId()+", "+skillObjectiveId+",'"+maxPoints+"' , '"+coins+"', now(), now(), "+assessment.getId()+", 'ASSESSMENT', "+assessment.getLesson().getCmsessions().iterator().next().getModules().iterator().next().getCourses().iterator().next().getId()+", "+groupId+", "+orgId+", now(),'"+maxPoints+"');";
				//System.out.println("insertIntoGamification>>>>"+insertIntoGamification);
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
		//System.out.println("findPrimaryGroupsOfUser>>"+findPrimaryGroupsOfUser);
		List<HashMap<String, Object>> primaryBG = util.executeQuery(findPrimaryGroupsOfUser);
		
		for(HashMap<String, Object>primaryG : primaryBG)
		{
			int groupId = (int)primaryG.get("id");
			int orgId = (int)primaryG.get("college_id");
			ArrayList<Integer> questionAnsweredCorrectly = new ArrayList<>();
			String findQueAnsweredCorrectly= "select distinct question_id from student_assessment where assessment_id ="+assessment.getId()+" and student_id="+istarUser.getId()+" and correct='t'";
			//System.out.println("correct que id "+findQueAnsweredCorrectly );
			List<HashMap<String, Object>> correctQueAnsweredData = util.executeQuery(findQueAnsweredCorrectly);
			for(HashMap<String, Object> qro : correctQueAnsweredData)
			{
				//System.out.println("correct que id"+(int)qro.get("question_id"));
				questionAnsweredCorrectly.add((int)qro.get("question_id"));
			}
			
			String findQuestionForAssessment="select distinct questionid from assessment_question, question, assessment where assessment_question.questionid = question.id and assessment_question.assessmentid = assessment.id and assessment.course_id = question.context_id and assessment.id = "+assessment.getId();
			List<HashMap<String, Object>> questionData = util.executeQuery(findQuestionForAssessment);
			for(HashMap<String, Object> qRow: questionData)
			{
				int questionId = (int)qRow.get("questionid");
				
				
				String findSkillsInQuestion = "select skill_objective_id, max_points from assessment_benchmark where item_id = "+questionId+" and item_type ='QUESTION' and course_id ="+assessment.getLesson().getCmsessions().iterator().next().getModules().iterator().next().getCourses().iterator().next().getId()+"";
				//System.out.println("findSkillsInAssesssment>>>>>>"+findSkillsInQuestion);
				List<HashMap<String, Object>> skillsData = util.executeQuery(findSkillsInQuestion);
				for(HashMap<String, Object> skills : skillsData)
				{
					int skillObjectiveId = (int)skills.get("skill_objective_id");
					String maxPoints = (String)skills.get("max_points");
					String pointsScored = maxPoints;
					String coins = "( :per_question_coins )";
					
											
					String getPreviousCoins="select * from user_gamification where item_id ='"+questionId+"' and item_type='QUESTION' and istar_user='"+istarUser.getId()+"' and batch_group_id="+groupId+" and skill_objective="+skillObjectiveId+"  order by timestamp desc limit 1";
					//System.out.println("getPreviousCoins"+getPreviousCoins);
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
								//System.out.println("questionAnsweredCorrectly contains "+ questionId);
								pointsScored = maxPoints;
							}
							else
							{
								//System.out.println("questionAnsweredCorrectl do not  contains "+ questionId);
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
							+ "((SELECT COALESCE(MAX(ID),0)+1 FROM user_gamification),"+istarUser.getId()+", "+skillObjectiveId+",'"+pointsScored+"' , '"+coins+"', now(), now(), "+questionId+", 'QUESTION', "+assessment.getLesson().getCmsessions().iterator().next().getModules().iterator().next().getCourses().iterator().next().getId()+", "+groupId+", "+orgId+", now(),'"+maxPoints+"');";
					//System.out.println("insertIntoGamification>>>>"+insertIntoGamification);
					util.executeUpdate(insertIntoGamification);
				}
			}						
		}		
	}

	public void updateUserPointsCoinsStatsTable(int istarUserId) {
		DBUTILS util = new DBUTILS();
		String sql ="delete from user_points_coins where user_id="+istarUserId;
		util.executeUpdate(sql);
		
		String findSkillData ="select distinct "
				+ "istar_user, "
				+ "skill_objective, "
				+ "custom_eval (CAST ( REPLACE ( REPLACE ( REPLACE ( COALESCE (string_agg(points,'+'), '0'), ':per_lesson_points', ''||(select property_value from constant_properties where property_name='per_lesson_points')||'' ), ':per_assessment_points', ''||(select property_value from constant_properties where property_name='per_assessment_points')||'' ), ':per_question_points', ''||(select property_value from constant_properties where property_name='per_question_points')||'' ) AS TEXT ) ) as points,"
				+ "custom_eval (CAST ( REPLACE ( REPLACE ( REPLACE ( COALESCE (string_agg(coins,'+'), '0'), ':per_lesson_coins', ''||(select property_value from constant_properties where property_name='per_lesson_coins')||'' ), ':per_assessment_coins', ''||(select property_value from constant_properties where property_name='per_assessment_coins')||'' ), ':per_question_coins', ''||(select property_value from constant_properties where property_name='per_question_coins')||'' ) AS TEXT ) ) as coins,  "
				+ "custom_eval (CAST ( REPLACE ( REPLACE ( REPLACE ( COALESCE (string_agg(max_points,'+'), '0'), ':per_lesson_points', ''||(select property_value from constant_properties where property_name='per_lesson_points')||'' ), ':per_assessment_points', ''||(select property_value from constant_properties where property_name='per_assessment_points')||'' ), ':per_question_points', ''||(select property_value from constant_properties where property_name='per_question_points')||'' ) AS TEXT ) )  as max_points "
				+ "from ("
				+ "			WITH summary AS "
				+ "				( "
				+ "					SELECT "
				+ "					P .istar_user, "
				+ "					P .skill_objective,  "
				+ "					CAST ( COALESCE (P .points, '0') AS TEXT )  AS points,  "
				+ "					CAST ( COALESCE (P .coins, '0') AS TEXT )  AS coins,  "
				+ "					CAST ( COALESCE (P .max_points, '0') AS TEXT )  AS max_points, "
				+ "					P .item_id, "
				+ "					ROW_NUMBER () OVER ( PARTITION BY P .istar_user, P .skill_objective, P .item_id ORDER BY P . TIMESTAMP DESC ) AS rk "
				+ "					FROM user_gamification P "
				+ "					WHERE item_type IN ('QUESTION', 'LESSON') and istar_user = "+istarUserId+""
						+ "		) SELECT s.* FROM summary s WHERE s.rk = 1 "
					+ ")T1 group by istar_user,skill_objective";
		
		List<HashMap<String, Object>> skillData = util.executeQuery(findSkillData);
		for(HashMap<String, Object> row: skillData)
		{
			int skillId = (int)row.get("skill_objective");
			double points = (double)row.get("points");
			double coins = (double)row.get("coins");
			double maxPoints = (double)row.get("max_points");
			String insertIntoPointsCoinsTable ="INSERT INTO user_points_coins (user_id, user_points, total_points, coins, skill_id) "
					+ "VALUES ("+istarUserId+", "+points+", "+maxPoints+", "+coins+", "+skillId+");";
			util.executeUpdate(insertIntoPointsCoinsTable);
		}
		
	}

	public void updateLeaderBoard(int istarUser) {
		DBUTILS util = new DBUTILS();
		String sql="select  "
				+ "assessment_benchmark.course_id, "
				+ "user_points_coins.user_id, "
				+ "sum (user_points_coins.coins) as coins, "
				+ "sum (user_points_coins.user_points) as user_points, "
				+ "sum (user_points_coins.total_points) as total_points, "
				+ "(sum (user_points_coins.user_points)*100)/(sum (user_points_coins.total_points)) as perc "
				+ "from user_points_coins, assessment_benchmark "
				+ "where "
				+ "user_points_coins.skill_id = assessment_benchmark.skill_id "
				+ "and  "
				+ "user_points_coins.user_id in "
					+ "("
					+ "		select student_id from batch_students where batch_group_id in "
					+ "			("
					+ "				select batch_group_id from batch_students where student_id = "+istarUser
					+ "			)"
					+ ") "
				+ "group by assessment_benchmark.course_id, user_points_coins.user_id "
				+ "order by assessment_benchmark.course_id, user_points_coins.user_id";
		
		List<HashMap<String, Object>> data = util.executeQuery(sql);
		for(HashMap<String, Object> row: data)
		{
			int courseId = (int)row.get("course_id");
			int userId = (int)row.get("user_id");
			double coins = (double)row.get("coins");
			double user_points = (double)row.get("user_points");
			double total_points = (double)row.get("total_points");
			double perc = (double)row.get("perc");
			
			String upsert=
					"INSERT INTO leaderboard (user_id, course_id, user_points, total_points, coins, percentage)  "
					+"VALUES ("+userId+", "+courseId+", "+user_points+", "+total_points+", "+coins+", "+perc+")  "
					+"ON CONFLICT (user_id,course_id)  "
					+"DO UPDATE SET user_points =  EXCLUDED.user_points,"
					+"total_points =  EXCLUDED.total_points,"
					+"coins =  EXCLUDED.coins,"
					+"percentage =  EXCLUDED.percentage;";
			util.executeUpdate(upsert);
		}
	}

	public void updateUserAssessmentPointsCoinsTable(int istarUserId, int assessmentId) {
		DBUTILS util = new DBUTILS();
		String sql ="delete from user_points_per_assessment where user_id="+istarUserId;
		util.executeUpdate(sql);
		
		String findSkillData ="select distinct "
				+ "istar_user, "
				+ "skill_objective, "
				+ "custom_eval (CAST ( REPLACE ( REPLACE ( REPLACE ( COALESCE (string_agg(points,'+'), '0'), ':per_lesson_points', ''||(select property_value from constant_properties where property_name='per_lesson_points')||'' ), ':per_assessment_points', ''||(select property_value from constant_properties where property_name='per_assessment_points')||'' ), ':per_question_points', ''||(select property_value from constant_properties where property_name='per_question_points')||'' ) AS TEXT ) ) as points,"
				+ "custom_eval (CAST ( REPLACE ( REPLACE ( REPLACE ( COALESCE (string_agg(coins,'+'), '0'), ':per_lesson_coins', ''||(select property_value from constant_properties where property_name='per_lesson_coins')||'' ), ':per_assessment_coins', ''||(select property_value from constant_properties where property_name='per_assessment_coins')||'' ), ':per_question_coins', ''||(select property_value from constant_properties where property_name='per_question_coins')||'' ) AS TEXT ) ) as coins,  "
				+ "custom_eval (CAST ( REPLACE ( REPLACE ( REPLACE ( COALESCE (string_agg(max_points,'+'), '0'), ':per_lesson_points', ''||(select property_value from constant_properties where property_name='per_lesson_points')||'' ), ':per_assessment_points', ''||(select property_value from constant_properties where property_name='per_assessment_points')||'' ), ':per_question_points', ''||(select property_value from constant_properties where property_name='per_question_points')||'' ) AS TEXT ) )  as max_points "
				+ "from ("
				+ "			WITH summary AS "
				+ "				( "
				+ "					SELECT "
				+ "					P .istar_user, "
				+ "					P .skill_objective,  "
				+ "					CAST ( COALESCE (P .points, '0') AS TEXT )  AS points,  "
				+ "					CAST ( COALESCE (P .coins, '0') AS TEXT )  AS coins,  "
				+ "					CAST ( COALESCE (P .max_points, '0') AS TEXT )  AS max_points, "
				+ "					 "
				+ "					ROW_NUMBER () OVER ( PARTITION BY P .istar_user, P .skill_objective, P .item_id, P.item_type ORDER BY P . TIMESTAMP DESC ) AS rk "
				+ "					FROM user_gamification P "
				+ "					WHERE istar_user = "+istarUserId+" "
				+ "					and "
				+ "					( "
				+ "					(item_type = 'QUESTION' and item_id in (select questionid from assessment_question where assessmentid ="+assessmentId+")) "
				+ "					or "
				+ "					(item_type = 'ASSESSMENT' and item_id = "+assessmentId+") "
				+ "					)"
				+ "				) SELECT s.* FROM summary s WHERE s.rk = 1 "
					+ ")T1 group by istar_user,skill_objective";
		
		List<HashMap<String, Object>> skillData = util.executeQuery(findSkillData);
		for(HashMap<String, Object> row: skillData)
		{
			int skillId = (int)row.get("skill_objective");
			double points = (double)row.get("points");
			double coins = (double)row.get("coins");
			double maxPoints = (double)row.get("max_points");
			String insertIntoPointsCoinsTable ="INSERT INTO user_points_per_assessment	 (user_id, user_points, total_points, coins, skill_id, assessment_id) "
					+ "VALUES ("+istarUserId+", "+points+", "+maxPoints+", "+coins+", "+skillId+","+assessmentId+");";
			util.executeUpdate(insertIntoPointsCoinsTable);
		}
		
	}
}
