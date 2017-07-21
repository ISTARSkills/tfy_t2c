package com.istarindia.apps.services;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.istarindia.android.pojo.AssessmentReportPOJO;
import com.istarindia.android.pojo.AssessmentResponsePOJO;
import com.istarindia.android.pojo.QuestionResponsePOJO;
import com.istarindia.android.pojo.SkillReportPOJO;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.AssessmentBenchmark;
import com.viksitpro.core.dao.entities.AssessmentDAO;
import com.viksitpro.core.dao.entities.AssessmentOption;
import com.viksitpro.core.dao.entities.AssessmentQuestion;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.Module;
import com.viksitpro.core.dao.entities.Question;
import com.viksitpro.core.dao.entities.SkillObjective;
import com.viksitpro.core.dao.entities.SkillObjectiveDAO;
import com.viksitpro.core.dao.entities.StudentAssessment;
import com.viksitpro.core.utilities.AppProperies;
import com.viksitpro.core.utilities.DBUTILS;

public class AppAssessmentServices {

	
	@SuppressWarnings("unchecked")
	public AssessmentReportPOJO getAssessmentReport(int istarUserId, int assessmentId) {
		
		
		int per_assessment_points=5,
				per_lesson_points=5,
				per_question_points=1,
				per_assessment_coins=5,
				per_lesson_coins=5,
				per_question_coins=1;
		try{
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
				if (inputStream != null) {
					properties.load(inputStream);
					
					per_assessment_points = Integer.parseInt(properties.getProperty("per_assessment_points"));
					per_lesson_points = Integer.parseInt(properties.getProperty("per_lesson_points"));
					per_question_points = Integer.parseInt(properties.getProperty("per_question_points"));
					per_assessment_coins = Integer.parseInt(properties.getProperty("per_assessment_coins"));
					per_lesson_coins = Integer.parseInt(properties.getProperty("per_lesson_coins"));
					per_question_coins = Integer.parseInt(properties.getProperty("per_question_coins"));
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			
		}
		Assessment assessment = getAssessment(assessmentId);
		AssessmentReportPOJO assessmentReportPOJO = null;
		if (assessment != null) {
			assessmentReportPOJO = new AssessmentReportPOJO();
			if(assessment.getRetryAble()!=null && assessment.getRetryAble())
			{
				assessmentReportPOJO.setRetryable(true);
			}
			else
			{
				assessmentReportPOJO.setRetryable(false);
			}	
			
			List<SkillReportPOJO> shellTree = getShellTreeForAssessment(assessmentId);
			for(SkillReportPOJO dd : shellTree)
			{
				if(AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
					//System.out.println("in shell tree "+dd.getName()+" - "+dd.getId());
					//System.out.println("inshell tree "+" "+dd.getUserPoints()+" "+dd.getTotalPoints()+" "+dd.getPercentage());
				}
				for(SkillReportPOJO ll: dd.getSkills())
				{
					if(AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
						//System.out.println("in shell tree "+ll.getName()+" - "+ll.getId());
						//System.out.println("inshell tree "+" "+ll.getUserPoints()+" "+ll.getTotalPoints()+" "+ll.getPercentage());
					}
				}
			}
			
			
			List<SkillReportPOJO> skillReportForAssesssment= fillShellTreeWithAssessmentData(shellTree, istarUserId, assessment);
			
			for(SkillReportPOJO dd : skillReportForAssesssment)
			{
				if (AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
					////System.err.println("skillReportForAssesssment in data tree " + dd.getName() + " - " + dd.getId());
					////System.err.println("skillReportForAssesssment data tree " + " " + dd.getUserPoints() + " "
							//+ dd.getTotalPoints() + " " + dd.getPercentage());
				}
				for (SkillReportPOJO ll : dd.getSkills()) {
					if (AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
					//	System.err
								//.println("skillReportForAssesssment in data tree " + ll.getName() + " - " + ll.getId());
						////System.err.println("skillReportForAssesssment in data tree " + " " + ll.getUserPoints() + " "
								//+ ll.getTotalPoints() + " " + ll.getPercentage());
					}
				}
			}
			
		
			String getUserAverage ="SELECT CAST (AVG(user_average) AS float8) AS batch_average FROM ( SELECT CASE WHEN T2.total_points != 0 THEN T2.user_points * 100 / T2.total_points ELSE 0 END AS user_average FROM ( SELECT T1.istar_user, SUM (T1.points) AS user_points, SUM (T1.max_points) AS total_points FROM ( WITH summary AS ( SELECT P .istar_user, P .skill_objective, custom_eval(cast (trim (replace(replace(replace( COALESCE(P.points,'0'),':per_lesson_points','"+per_lesson_points+"'),':per_assessment_points','"+per_assessment_points+"'),':per_question_points','"+per_question_points+"'))  as text)) as points, custom_eval(cast (trim (replace(replace(replace( COALESCE(P.max_points,'0'),':per_lesson_points','"+per_lesson_points+"'),':per_assessment_points','"+per_assessment_points+"'),':per_question_points','"+per_question_points+"'))  as text)) as max_points, ROW_NUMBER () OVER ( PARTITION BY P .istar_user, P .skill_objective ORDER BY P . TIMESTAMP DESC ) AS rk FROM user_gamification P WHERE item_type = 'QUESTION' AND batch_group_id = ( SELECT batch_group. ID FROM batch_students, batch_group WHERE batch_students.batch_group_id = batch_group. ID AND batch_students.student_id = "+istarUserId+" AND batch_group.is_primary = 't' LIMIT 1 ) AND course_id = "+assessment.getCourse()+" AND item_id IN ( SELECT DISTINCT questionid FROM assessment_question, assessment, question WHERE assessment_question.assessmentid = assessment. ID AND assessment_question.questionid = question. ID AND question.context_id = assessment.course_id AND assessment. ID = "+assessmentId+" ) ) SELECT s.* FROM summary s WHERE s.rk = 1 ) T1 GROUP BY T1.istar_user ) T2 ) T3 ";
			//System.out.println("getUserAverage"+getUserAverage);
			DBUTILS util = new DBUTILS();
			List<HashMap<String, Object>> batchAverageData = util.executeQuery(getUserAverage);
			double batchAverage = 0;
			if(batchAverageData.size()>0 && batchAverageData.get(0).get("batch_average")!=null)
			{
				batchAverage = (double)batchAverageData.get(0).get("batch_average");
			}
			
			String countOfStudentInBatchAndAttended = "select cast(count(DISTINCT(student_id)) as integer) as total_stu, cast (count(DISTINCT(istar_user)) as integer) as attend_stu from (select DISTINCT student_id from batch_students where batch_group_id in (select batch_group.id from batch_students, batch_group  where batch_students.batch_group_id = batch_group.id and batch_students.student_id = "+istarUserId+" and batch_group.is_primary ='t' limit 1) )T1 left join user_gamification on (user_gamification.istar_user =  T1.student_id and user_gamification.item_id ="+assessmentId+" and user_gamification.item_type='ASSESSMENT' and user_gamification.course_id="+assessment.getCourse()+")";
			//System.out.println("countOfStudentInBatchAndAttended"+countOfStudentInBatchAndAttended);
			int totalStudentInBatch = 0;
			int totalAttended =0;
			List<HashMap<String, Object>> totalstu = util.executeQuery(countOfStudentInBatchAndAttended);
			if(totalstu.size()>0)
			{
				if(totalstu.get(0).get("total_stu")!=null)
				{
					totalStudentInBatch = (int)totalstu.get(0).get("total_stu");
				}
				if(totalstu.get(0).get("attend_stu")!=null)
				{
					totalAttended = (int)totalstu.get(0).get("attend_stu");
				}
			}
			
			
			
			assessmentReportPOJO.setId(assessment.getId());
			assessmentReportPOJO.setName(assessment.getAssessmenttitle());
			assessmentReportPOJO.setSkillsReport(skillReportForAssesssment); //what if skill size is zero
			assessmentReportPOJO.setBatchAverage(batchAverage);
			assessmentReportPOJO.setUsersAttemptedCount(totalAttended);
			assessmentReportPOJO.setTotalNumberOfUsersInBatch(totalStudentInBatch);
			
			
			
			String questionsSQL ="select cast (count(*) filter (where correct='t') as integer)as correct_answered, cast (count(*) as integer) as total_que from student_assessment where student_id = "+istarUserId+" and assessment_id ="+assessmentId+"";
			//System.out.println("questionsSQL->"+questionsSQL);
			List<HashMap <String, Object>> queData = util.executeQuery(questionsSQL);
			int totalQueInAssessment = 0;
			int correctattempted =0;
			if(queData.size()>0)
			{
				totalQueInAssessment = (int)queData.get(0).get("total_que");
				correctattempted = (int)queData.get(0).get("correct_answered");
			}
			assessmentReportPOJO.setTotalNumberOfCorrectlyAnsweredQuestions(correctattempted);
			assessmentReportPOJO.setTotalNumberOfQuestions(totalQueInAssessment);
			
			
			assessmentReportPOJO.calculateTotalScore();
			assessmentReportPOJO.calculateUserScore();
			assessmentReportPOJO.calculateAccuracy();
			assessmentReportPOJO.generateMessageAndDescription(50);
			
			AssessmentResponsePOJO assessmentResponse = getAssessmentResponseOfUser(assessmentId, istarUserId);
			if(assessmentResponse!=null){
				assessmentReportPOJO.setAssessmentResponse(assessmentResponse);
			}
		
			}
		
		return assessmentReportPOJO;
	}
	
	private List<SkillReportPOJO> fillShellTreeWithAssessmentData(List<SkillReportPOJO> shellTree, int istarUserId, Assessment assessment) {
		int per_assessment_points=5,
				per_lesson_points=5,
				per_question_points=1,
				per_assessment_coins=5,
				per_lesson_coins=5,
				per_question_coins=1;
		try{
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
				if (inputStream != null) {
					properties.load(inputStream);					
					per_assessment_points = Integer.parseInt(properties.getProperty("per_assessment_points"));
					per_lesson_points = Integer.parseInt(properties.getProperty("per_lesson_points"));
					per_question_points = Integer.parseInt(properties.getProperty("per_question_points"));
					per_assessment_coins = Integer.parseInt(properties.getProperty("per_assessment_coins"));
					per_lesson_coins = Integer.parseInt(properties.getProperty("per_lesson_coins"));
					per_question_coins = Integer.parseInt(properties.getProperty("per_question_coins"));					
				}
			} catch (IOException e) {
				e.printStackTrace();			
		}
		
		
		String getDataForTree="SELECT 	T1. ID, 	T1.skill_objective, 	T1.points, 	T1.max_points, 	module_skill.id as module_id FROM 	( 		WITH summary AS ( 			SELECT 				P . ID, 				P .skill_objective, 				custom_eval ( 					CAST ( 						TRIM ( 							REPLACE ( 								REPLACE ( 									REPLACE ( 										COALESCE (P .points, '0'), 										':per_lesson_points', 										'"+per_lesson_points+"' 									), 									':per_assessment_points', 									'"+per_assessment_points+"' 								), 								':per_question_points', 								'"+per_question_points+"' 							) 						) AS TEXT 					) 				) AS points, 				custom_eval ( 					CAST ( 						TRIM ( 							REPLACE ( 								REPLACE ( 									REPLACE ( 										COALESCE (P .max_points, '0'), 										':per_lesson_points', 										'"+per_lesson_points+"' 									), 									':per_assessment_points', 									'"+per_assessment_points+"' 								), 								':per_question_points', 								'"+per_question_points+"' 							) 						) AS TEXT 					) 				) AS max_points, 				ROW_NUMBER () OVER ( 					PARTITION BY P .skill_objective, 					P .item_id 				ORDER BY 					P . TIMESTAMP DESC 				) AS rk 			FROM 				user_gamification P, 				assessment_question, 				question 			WHERE 				P .course_id = "+assessment.getCourse()+" 			AND P .istar_user = "+istarUserId+" 			AND P .item_id = assessment_question.questionid 			AND assessment_question.assessmentid = "+assessment.getId()+" 			AND assessment_question.questionid = question. ID 			AND question.context_id = "+assessment.getCourse()+" 			AND P .item_type = 'QUESTION' 		) SELECT 			s.* 		FROM 			summary s 		WHERE 			s.rk = 1 	) T1 JOIN skill_objective cmsession_skill ON ( 	T1.skill_objective = cmsession_skill.id ) JOIN skill_objective module_skill ON ( 	module_skill.id = cmsession_skill.parent_skill )";
		//System.out.println("getDataForTree in assessment "+getDataForTree);
		DBUTILS util = new DBUTILS();
		List<HashMap<String, Object>> data = util.executeQuery(getDataForTree);
		for(HashMap<String, Object> row: data)
		{
			int skillId = (int)row.get("skill_objective");
			double userPoints = (double)row.get("points");
			double maxPoints = (double)row.get("max_points");
			int moduleId = (int)row.get("module_id");
			
			for(SkillReportPOJO mod : shellTree)
			{
				if(mod.getId() == moduleId)
				{
					List<SkillReportPOJO> cmsSkills = mod.getSkills();
					for(SkillReportPOJO cmsSkill: cmsSkills)
					{
						if(cmsSkill.getId()== skillId)
						{
							if(AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
							////System.out.println("idsss<<<<<<<<<<<<<<<<<"+cmsSkill.getId());
							}
							
							if(cmsSkill.getId()==188)
							{
								////System.out.println("Math.ceil(userPoints)<<<<<<<<<<<<<<<<<"+Math.ceil(userPoints));
							}
														
							if(cmsSkill.getAccessedFirstTime()==true)
							{
								////System.err.println(cmsSkill.getId()+" is accessed for the first time"+cmsSkill.getAccessedFirstTime());
								
								cmsSkill.setAccessedFirstTime(false);
							}
							else
							{
								double oldUserPoint = cmsSkill.getUserPoints()!=null?cmsSkill.getUserPoints() : 0d;
								userPoints = userPoints+oldUserPoint;
								double oldTotalPoint = cmsSkill.getTotalPoints()!=null?cmsSkill.getTotalPoints() : 0d;
								maxPoints = maxPoints+oldTotalPoint;
							}
							
							cmsSkill.setUserPoints(Math.ceil(userPoints));
							cmsSkill.setTotalPoints(Math.ceil(maxPoints));	
							cmsSkill.calculatePercentage();
							//cmsSkills.add(cmsSkill);
							break;
						}
						
					}
					
					
					mod.calculateUserPoints();
					mod.calculateTotalPoints();
					mod.calculatePercentage();
					
					break;
				}
			}
		}
		return shellTree;
	}

	private List<SkillReportPOJO> getShellTreeForAssessment(int assessmentId ) {
		String mediaUrlPath ="";
		int per_assessment_points=5,
				per_lesson_points=5,
				per_question_points=1,
				per_assessment_coins=5,
				per_lesson_coins=5,
				per_question_coins=1;
		try{
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
				if (inputStream != null) {
					properties.load(inputStream);
					mediaUrlPath =  properties.getProperty("media_url_path");
					per_assessment_points = Integer.parseInt(properties.getProperty("per_assessment_points"));
					per_lesson_points = Integer.parseInt(properties.getProperty("per_lesson_points"));
					per_question_points = Integer.parseInt(properties.getProperty("per_question_points"));
					per_assessment_coins = Integer.parseInt(properties.getProperty("per_assessment_coins"));
					per_lesson_coins = Integer.parseInt(properties.getProperty("per_lesson_coins"));
					per_question_coins = Integer.parseInt(properties.getProperty("per_question_coins"));
				}
			} catch (IOException e) {
				e.printStackTrace();
			
		}
		
		Assessment assessment = new AssessmentDAO().findById(assessmentId);
		List<SkillReportPOJO> shellTree = new ArrayList<>();
		DBUTILS util = new DBUTILS();
		String shelltreeData = "SELECT T1.skill_objective_id, T1.max_points, T1. NAME AS skill_name, module_skill . ID AS module_id, module_skill .name as module_name FROM ( SELECT assessment.course_id, skill_objective_id, custom_eval ( CAST ( TRIM ( REPLACE ( REPLACE ( REPLACE ( COALESCE (max_points, '0'), ':per_lesson_points', '"+per_lesson_points+"' ), ':per_assessment_points', '"+per_assessment_points+"' ), ':per_question_points', '"+per_question_points+"' ) ) AS TEXT ) ) AS max_points, skill_objective. NAME FROM assessment_benchmark, assessment, skill_objective WHERE assessment_benchmark.item_id = assessment. ID AND assessment_benchmark.item_type = 'ASSESSMENT' AND assessment. ID = "+assessmentId+" AND assessment_benchmark.context_id = assessment.course_id AND assessment_benchmark.skill_objective_id = skill_objective. ID ) T1 JOIN skill_objective cmsession_skill ON ( cmsession_skill.id = T1.skill_objective_id ) JOIN skill_objective module_skill ON ( module_skill .id = cmsession_skill.parent_skill ) WHERE module_skill.context  = T1.course_id and  cmsession_skill.context = T1.course_id  ";
		//System.err.println("getShellTreeForAssessment>>>"+shelltreeData);
		List<HashMap<String, Object>> assessmentData = util.executeQuery(shelltreeData);
		for(HashMap<String, Object> row: assessmentData)
		{
			int skillId = (int)row.get("skill_objective_id");
			double maxPoints = (double)row.get("max_points");
			String skillName = (String)row.get("skill_name");
			int moduleId = (int)row.get("module_id");
			String moduleName = (String)row.get("module_name");
			String moduleDesc = null;
			String moduleImage = null;
			
			SkillReportPOJO modPojo = new SkillReportPOJO();
			modPojo.setName(moduleName.trim());
			modPojo.setId(moduleId);
			modPojo.setSkills(new ArrayList<>());
			modPojo.setDescription(moduleDesc);
			modPojo.setImageURL(moduleImage);
			
			boolean moduleAlreadyPresentInTree = false;
			//we will check if this module pojo already exist in tree or not.
			//if exist then we will add cmsessions skills only to it
			//if do not exist then we will create one.
			for(SkillReportPOJO mod : shellTree)
			{
				if(mod.getId()==moduleId)
				{
					modPojo = mod;
					moduleAlreadyPresentInTree= true;
					break;
				}									
			}
			
			
			boolean skillAlreadyPresent = false;
			if(modPojo.getSkills()!=null)
			{
				for(SkillReportPOJO cmsessionSkill : modPojo.getSkills())
				{
					if(cmsessionSkill.getId()== skillId)
					{
						skillAlreadyPresent = true;
						break;
					}
				}
			}
			
			//if session skill is not present in module tree then we will add session skill to module tree.
			if(!skillAlreadyPresent)
			{
				SkillReportPOJO sessionSkill = new SkillReportPOJO();
				sessionSkill.setId(skillId);
				sessionSkill.setName(skillName);
				//sessionSkill.setUserPoints((double)0);
				sessionSkill.setTotalPoints(maxPoints);				
				List<SkillReportPOJO> sessionsSkills = modPojo.getSkills();
				sessionsSkills.add(sessionSkill);
				modPojo.setSkills(sessionsSkills);
			}
			modPojo.calculatePercentage();
			modPojo.calculateUserPoints();
			modPojo.calculateTotalPoints();
			
			if(!moduleAlreadyPresentInTree)
			{
				shellTree.add(modPojo);
			}						
		}
		return shellTree;
	}

/*	public AssessmentReportPOJO getAssessmentReport(int istarUserId, int assessmentId) {
		//System.out.println("Getting assessment report");
		AssessmentReportPOJO assessmentReportPOJO = null;

		Assessment assessment = getAssessment(assessmentId);

		if (assessment != null) {
			UserGamificationServices userGamificationServices = new UserGamificationServices();
			List<UserGamification> allUserGamification = userGamificationServices
					.getUserGamificationsOfUserForItem(istarUserId, assessmentId, "ASSESSMENT");

			List<SkillReportPOJO> allSkillsReport = new ArrayList<SkillReportPOJO>();

			for (UserGamification userGamification : allUserGamification) {

				SkillObjective cmsessionSkillObjective = userGamification.getSkillObjective();
				SkillObjective moduleSkillObjective = getSkillObjective(cmsessionSkillObjective.getParentSkill());

				Double totalPoints = getMaxPointsForSkillObjectiveOfAssessment(assessmentId,
						cmsessionSkillObjective.getId());
				Double userPoints = userGamification.getPoints();

				SkillReportPOJO moduleSkillReportPOJO = null;
				SkillReportPOJO cmsessionSkillReportPOJO = null;

				for (SkillReportPOJO tempModuleSkillReportPOJO : allSkillsReport) {
					if (tempModuleSkillReportPOJO.getId() == moduleSkillObjective.getId()) {
						moduleSkillReportPOJO = tempModuleSkillReportPOJO;
						break;
					}
				}

				if (moduleSkillReportPOJO != null) {

					for (SkillReportPOJO tempCmsessionSkillReportPOJO : moduleSkillReportPOJO.getSkills()) {
						if (tempCmsessionSkillReportPOJO.getId() == cmsessionSkillObjective.getId()) {
							cmsessionSkillReportPOJO = tempCmsessionSkillReportPOJO;
							break;
						}
					}

					if (cmsessionSkillReportPOJO != null) {

						cmsessionSkillReportPOJO
								.setTotalPoints(cmsessionSkillReportPOJO.getTotalPoints() + totalPoints);
						cmsessionSkillReportPOJO.setUserPoints(cmsessionSkillReportPOJO.getUserPoints() + userPoints);
						cmsessionSkillReportPOJO.calculatePercentage();
					} else {
						cmsessionSkillReportPOJO = new SkillReportPOJO();

						cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
						cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
						cmsessionSkillReportPOJO
								.setTotalPoints(cmsessionSkillReportPOJO.getTotalPoints() + totalPoints);
						cmsessionSkillReportPOJO.setUserPoints(cmsessionSkillReportPOJO.getUserPoints() + userPoints);
						cmsessionSkillReportPOJO.calculatePercentage();

						moduleSkillReportPOJO.getSkills().add(cmsessionSkillReportPOJO);
					}
					moduleSkillReportPOJO.calculateTotalPoints();
					moduleSkillReportPOJO.calculateUserPoints();
					moduleSkillReportPOJO.calculatePercentage();
					moduleSkillReportPOJO.generateMessage();
				} else {
					moduleSkillReportPOJO = new SkillReportPOJO();

					moduleSkillReportPOJO.setId(moduleSkillObjective.getId());
					moduleSkillReportPOJO.setName(moduleSkillObjective.getName());
					moduleSkillReportPOJO.setSkills((new ArrayList<SkillReportPOJO>()));

					cmsessionSkillReportPOJO = new SkillReportPOJO();

					cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
					cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
					cmsessionSkillReportPOJO.setTotalPoints(cmsessionSkillReportPOJO.getTotalPoints() + totalPoints);
					cmsessionSkillReportPOJO.setUserPoints(cmsessionSkillReportPOJO.getUserPoints() + userPoints);
					cmsessionSkillReportPOJO.calculatePercentage();

					moduleSkillReportPOJO.getSkills().add(cmsessionSkillReportPOJO);

					moduleSkillReportPOJO.calculateTotalPoints();
					moduleSkillReportPOJO.calculateUserPoints();
					moduleSkillReportPOJO.calculatePercentage();
					moduleSkillReportPOJO.generateMessage();

					allSkillsReport.add(moduleSkillReportPOJO);
				}
			}

			StudentAssessmentServices studentAssessmentServices = new StudentAssessmentServices();
			List<StudentAssessment> allStudentAssessment = studentAssessmentServices
					.getStudentAssessmentForUser(istarUserId, assessmentId);

			if (allStudentAssessment.size() > 0) {
				int totalNumberOfQuestions = allStudentAssessment.size();
				Integer totalNumberOfCorrectlyAnsweredQuestions = getNumberOfCorrectlyAnsweredQuestions(istarUserId,
						assessment.getId());

				assessmentReportPOJO = new AssessmentReportPOJO();
				assessmentReportPOJO.setId(assessment.getId());
				assessmentReportPOJO.setName(assessment.getAssessmenttitle());
				assessmentReportPOJO.setTotalNumberOfQuestions(totalNumberOfQuestions);
				assessmentReportPOJO
						.setTotalNumberOfCorrectlyAnsweredQuestions(totalNumberOfCorrectlyAnsweredQuestions);
				assessmentReportPOJO.setSkillsReport(allSkillsReport);

				HashMap<String, Object> batchAverageMap = calculateBatchAverageOfAssessment(assessment, istarUserId);

				assessmentReportPOJO.setBatchAverage((Double) batchAverageMap.get("batchAverage"));
				assessmentReportPOJO.setTotalNumberOfUsersInBatch((Integer) batchAverageMap.get("totalStudentsInBatch"));
				
				assessmentReportPOJO.setUsersAttemptedCount((Integer) batchAverageMap.get("numberOfStudentsAttemptedAssessment") );
				assessmentReportPOJO.calculateTotalScore();
				assessmentReportPOJO.calculateUserScore();
				assessmentReportPOJO.calculateAccuracy();
			}
		}
		assessmentReportPOJO.generateMessageAndDescription(50); //50 is cut off marks
		return assessmentReportPOJO;
	}*/

	
	@SuppressWarnings("unchecked")
	public List<Integer> getAttemptedAssessmentsOfUser(int istarUserId){
		
		String sql = "select distinct item_id from user_gamification where istar_user="+istarUserId+" and item_type='ASSESSMENT'  and timestamp is not null";
		//System.out.println("all Assessments of user from UsrGmfctn---->"+sql);
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		
		List<Integer> results = query.list();
		
		return results;
	}
	
	public AssessmentResponsePOJO getAssessmentResponseOfUser(int assessmentId, int istarUserId) {

		AssessmentResponsePOJO assessmentResponsePOJO = null;
		StudentAssessmentServices studentAssessmentServices = new StudentAssessmentServices();
		List<StudentAssessment> allStudentAssessments = studentAssessmentServices
				.getStudentAssessmentForUser(istarUserId, assessmentId);
		List<QuestionResponsePOJO> allQuestionsResponse = new ArrayList<QuestionResponsePOJO>();

		if (allStudentAssessments.size() > 0) {
			assessmentResponsePOJO = new AssessmentResponsePOJO();
			for (StudentAssessment studentAssessment : allStudentAssessments) {
				QuestionResponsePOJO questionResponsePOJO = new QuestionResponsePOJO();
				List<Integer> markedOptions = new ArrayList<Integer>();
				questionResponsePOJO.setQuestionId(studentAssessment.getQuestion().getId());

				List<AssessmentOption> allOptionsOfQuestion = new ArrayList<AssessmentOption>(
						studentAssessment.getQuestion().getAssessmentOptions());

				for (int i = 0; i < allOptionsOfQuestion.size(); i++) {
					if (i == 0 && studentAssessment.getOption1()) {
						markedOptions.add(allOptionsOfQuestion.get(i).getId());
					}

					if (i == 1 && studentAssessment.getOption2()) {
						markedOptions.add(allOptionsOfQuestion.get(i).getId());
					}

					if (i == 2 && studentAssessment.getOption3()) {
						markedOptions.add(allOptionsOfQuestion.get(i).getId());
					}

					if (i == 3 && studentAssessment.getOption4()) {
						markedOptions.add(allOptionsOfQuestion.get(i).getId());
					}

					if (i == 4 && studentAssessment.getOption5()) {
						markedOptions.add(allOptionsOfQuestion.get(i).getId());
					}
				}
				questionResponsePOJO.setOptions(markedOptions);
				questionResponsePOJO.setDuration(studentAssessment.getTimeTaken());
				allQuestionsResponse.add(questionResponsePOJO);
			}
			assessmentResponsePOJO = new AssessmentResponsePOJO();
			assessmentResponsePOJO.setId(assessmentId);
			assessmentResponsePOJO.setResponse(allQuestionsResponse);
		}
		return assessmentResponsePOJO;
	}

	public List<AssessmentReportPOJO> getAllAssessmentReportsOfUser(int istarUserId) {
		List<Integer> assessmentIds = getAttemptedAssessmentsOfUser(istarUserId);
		List<AssessmentReportPOJO> allReports = new ArrayList<AssessmentReportPOJO>();
		if(assessmentIds.size()>0){
			for(Integer assessmentId: assessmentIds){
				AssessmentReportPOJO assessmentReportPOJO = getAssessmentReport(istarUserId, assessmentId);
				if(assessmentReportPOJO!=null){
					allReports.add(assessmentReportPOJO);
				}
			}
		}
		return allReports;		
	}
	
	
	
	/*public List<AssessmentReportPOJO> getAllAssessmentReportsOfUser(int istarUserId) {

		List<AssessmentReportPOJO> allReports = new ArrayList<AssessmentReportPOJO>();

		HashMap<Integer, Integer> numberOfCorrectlyAnsweredQuestions = getNumberOfCorrectlyAnsweredQuestionsOfAllAssessments(
				istarUserId);
		HashMap<Integer, Integer> numberOfUsersAttemptedAssessments = getNumberOfUsersAttemptedTheAssessmentOfUser(
				istarUserId);
		HashMap<Integer, HashMap<String, Object>> batchAverageOfAssessments = calculateBatchAverageOfAllAssessments(
				istarUserId);
		HashMap<Integer, HashMap<Integer, Double>> skillsBenchmarkForAssessments = getMaxPointsForSkillObjectiveOfAllAssessment(istarUserId);

		for (Integer assessmentId : numberOfCorrectlyAnsweredQuestions.keySet()) {

			Assessment assessment = getAssessment(assessmentId);
			if (assessment != null) {
				AssessmentReportPOJO assessmentReportPOJO = null;
				UserGamificationServices userGamificationServices = new UserGamificationServices();
				List<UserGamification> allUserGamification = userGamificationServices
						.getUserGamificationsOfUserForItem(istarUserId, assessmentId, "ASSESSMENT");

				List<SkillReportPOJO> allSkillsReport = new ArrayList<SkillReportPOJO>();

				for (UserGamification userGamification : allUserGamification) {

					SkillObjective cmsessionSkillObjective = userGamification.getSkillObjective();
					SkillObjective moduleSkillObjective = getSkillObjective(cmsessionSkillObjective.getParentSkill());

					Double totalPoints = 0.0;
					if(skillsBenchmarkForAssessments.get(assessment.getId()).get(cmsessionSkillObjective.getId())!=null){
						totalPoints = skillsBenchmarkForAssessments.get(assessment.getId()).get(cmsessionSkillObjective.getId());
					}
					Double userPoints = userGamification.getPoints();

					SkillReportPOJO moduleSkillReportPOJO = null;
					SkillReportPOJO cmsessionSkillReportPOJO = null;

					for (SkillReportPOJO tempModuleSkillReportPOJO : allSkillsReport) {
						if (tempModuleSkillReportPOJO.getId() == moduleSkillObjective.getId()) {
							moduleSkillReportPOJO = tempModuleSkillReportPOJO;
							break;
						}
					}

					if (moduleSkillReportPOJO != null) {
						for (SkillReportPOJO tempCmsessionSkillReportPOJO : moduleSkillReportPOJO.getSkills()) {
							if (tempCmsessionSkillReportPOJO.getId() == cmsessionSkillObjective.getId()) {
								cmsessionSkillReportPOJO = tempCmsessionSkillReportPOJO;
								break;
							}
						}

						if (cmsessionSkillReportPOJO != null) {

							cmsessionSkillReportPOJO
									.setTotalPoints(cmsessionSkillReportPOJO.getTotalPoints() + totalPoints);
							cmsessionSkillReportPOJO
									.setUserPoints(cmsessionSkillReportPOJO.getUserPoints() + userPoints);
							cmsessionSkillReportPOJO.calculatePercentage();
						} else {
							cmsessionSkillReportPOJO = new SkillReportPOJO();

							cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
							cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
							cmsessionSkillReportPOJO
									.setTotalPoints(cmsessionSkillReportPOJO.getTotalPoints() + totalPoints);
							cmsessionSkillReportPOJO
									.setUserPoints(cmsessionSkillReportPOJO.getUserPoints() + userPoints);
							cmsessionSkillReportPOJO.calculatePercentage();

							moduleSkillReportPOJO.getSkills().add(cmsessionSkillReportPOJO);
						}
						moduleSkillReportPOJO.calculateTotalPoints();
						moduleSkillReportPOJO.calculateUserPoints();
						moduleSkillReportPOJO.calculatePercentage();
						moduleSkillReportPOJO.generateMessage();
					} else {
						moduleSkillReportPOJO = new SkillReportPOJO();

						moduleSkillReportPOJO.setId(moduleSkillObjective.getId());
						moduleSkillReportPOJO.setName(moduleSkillObjective.getName());
						moduleSkillReportPOJO.setSkills((new ArrayList<SkillReportPOJO>()));

						cmsessionSkillReportPOJO = new SkillReportPOJO();

						cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
						cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
						cmsessionSkillReportPOJO
								.setTotalPoints(cmsessionSkillReportPOJO.getTotalPoints() + totalPoints);
						cmsessionSkillReportPOJO.setUserPoints(cmsessionSkillReportPOJO.getUserPoints() + userPoints);
						cmsessionSkillReportPOJO.calculatePercentage();

						moduleSkillReportPOJO.getSkills().add(cmsessionSkillReportPOJO);

						moduleSkillReportPOJO.calculateTotalPoints();
						moduleSkillReportPOJO.calculateUserPoints();
						moduleSkillReportPOJO.calculatePercentage();
						moduleSkillReportPOJO.generateMessage();

						
						allSkillsReport.add(moduleSkillReportPOJO);
					}
				}

				int totalNumberOfQuestions = assessment.getAssessmentQuestions().size();
				Integer totalNumberOfCorrectlyAnsweredQuestions = numberOfCorrectlyAnsweredQuestions
						.get(assessment.getId());

				assessmentReportPOJO = new AssessmentReportPOJO();
				assessmentReportPOJO.setId(assessment.getId());
				assessmentReportPOJO.setName(assessment.getAssessmenttitle());
				assessmentReportPOJO.setTotalNumberOfQuestions(totalNumberOfQuestions);
				assessmentReportPOJO
						.setTotalNumberOfCorrectlyAnsweredQuestions(totalNumberOfCorrectlyAnsweredQuestions);
				assessmentReportPOJO.setSkillsReport(allSkillsReport);

				HashMap<String, Object> batchAverageMap = batchAverageOfAssessments.get(assessment.getId());
				if(batchAverageMap!=null){
					assessmentReportPOJO.setBatchAverage((Double) batchAverageMap.get("batchAverage"));
					assessmentReportPOJO.setTotalNumberOfUsersInBatch((Integer) batchAverageMap.get("totalStudentsInBatch"));
					assessmentReportPOJO.setUsersAttemptedCount((Integer) batchAverageMap.get("numberOfStudentsAttemptedAssessment"));
				}
				
				assessmentReportPOJO.calculateTotalScore();
				assessmentReportPOJO.calculateUserScore();
				assessmentReportPOJO.calculateAccuracy();
				assessmentReportPOJO.generateMessageAndDescription(50); //50 is cut off marks

				allReports.add(assessmentReportPOJO);
			}
		}
		return allReports;
	}*/

	/*
	 * public HashMap<String, Object>
	 * calculateBatchAverageOfAssessment(Assessment assessment, Integer
	 * istarUserId, Integer numberOfUsersAttemptedTheAssessment) {
	 * 
	 * Double batchAverage = 0.0; Integer totalStudentsInBatch = 0;
	 * HashMap<String, Object> batchMap = new HashMap<String, Object>();
	 * 
	 * if (numberOfUsersAttemptedTheAssessment != null &&
	 * numberOfUsersAttemptedTheAssessment > 0) { AppBatchStudentsServices
	 * appBatchStudentsServices = new AppBatchStudentsServices();
	 * UserGamificationServices userGamificationServices = new
	 * UserGamificationServices(); List<IstarUser> allBatchStudents =
	 * appBatchStudentsServices.getBatchColleaguesOfUsers(istarUserId);
	 * totalStudentsInBatch = allBatchStudents.size(); Double
	 * maxPointsOfAllUsers = 0.0;
	 * 
	 * for (IstarUser istarUser : allBatchStudents) { maxPointsOfAllUsers =
	 * maxPointsOfAllUsers + userGamificationServices
	 * .getTotalPointsOfUserForItem(istarUser.getId(), assessment.getId(),
	 * "ASSESSMENT"); }
	 * 
	 * Double maxPointsOfAssessment =
	 * getMaxPointsOfAssessment(assessment.getId());
	 * 
	 * //System.out.println("maxPointsOfAllUsers->" + maxPointsOfAllUsers);
	 * //System.out.println("maxPointsOfAssessment->" + maxPointsOfAssessment);
	 * 
	 * batchAverage = maxPointsOfAllUsers / (maxPointsOfAssessment *
	 * numberOfUsersAttemptedTheAssessment); } batchMap.put("batchAverage",
	 * batchAverage); batchMap.put("totalStudents", totalStudentsInBatch);
	 * 
	 * return batchMap; }
	 */

/*	@SuppressWarnings("rawtypes")
	public HashMap<String, Object> calculateBatchAverageOfAssessment(Assessment assessment, Integer istarUserId) {

		Double batchAverage = 0.0;
		Integer totalStudentsInBatch = 0;
		HashMap<String, Object> batchMap = new HashMap<String, Object>();

		String sql = "select COALESCE(sum(total_points)/count(batch_assessment.istar_user),0), COALESCE(cast(count(batch_assessment.istar_user) as integer),0) as total_students from (select user_gamification.istar_user, sum(user_gamification.points) as total_points, sum(user_gamification.coins) as total_coins from assessment,user_gamification where user_gamification.item_id=assessment.id and assessment.id= :assessmentId and user_gamification.istar_user in (select student_id from batch_students where batch_group_id in (select batch_group_id from batch_students where batch_students.student_id= :istarUserId)) group by user_gamification.istar_user order by total_points desc) as batch_assessment";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId", istarUserId);
		query.setParameter("assessmentId", assessment.getId());

		List results = query.list();

		if (results.size() > 0) {
			Object[] batchData = (Object[]) results.get(0);

			batchAverage = (Double) batchData[0];
			totalStudentsInBatch = (Integer) batchData[1];

		}
		batchMap.put("batchAverage", batchAverage);
		batchMap.put("totalStudents", totalStudentsInBatch);

		return batchMap;
	}*/
	
	@SuppressWarnings("rawtypes")
	public HashMap<String, Object> calculateBatchAverageOfAssessment(Assessment assessment, Integer istarUserId) {

		Double batchAverage = 0.0;
		Integer totalStudentsInBatch = 0;
		Integer numberOfStudentsAttemptedAssessment = 0;
		HashMap<String, Object> batchMap = new HashMap<String, Object>();

		String sql = "with temp as (select student_id from batch_students where batch_group_id in (select batch_group_id from batch_students where student_id= :istarUserId)) select COALESCE((sum(points_earned)*1.0/count(user_id)),0) as batch_average, total_points, cast (count(user_id) as integer) as attempted_count, cast((select count(*) from temp) as integer) as total_students, assessment_id from report where assessment_id= :assessmentId and user_id in (select * from temp) group by assessment_id, total_points";
		//System.out.println(sql);

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId", istarUserId);
		query.setParameter("assessmentId", assessment.getId());

		List results = query.list();

		if (results.size() > 0) {
			Object[] batchData = (Object[]) results.get(0);

			batchAverage = ((BigDecimal) batchData[0]).doubleValue();
			totalStudentsInBatch = (Integer) batchData[3];
			numberOfStudentsAttemptedAssessment = (Integer) batchData[2];

		}
		
		batchMap.put("batchAverage", batchAverage);
		batchMap.put("numberOfStudentsAttemptedAssessment", numberOfStudentsAttemptedAssessment);
		batchMap.put("totalStudentsInBatch", totalStudentsInBatch);

		return batchMap;
	}

/*	@SuppressWarnings("unchecked")
	public HashMap<Integer, HashMap<String, Object>> calculateBatchAverageOfAllAssessments(Integer istarUserId) {

		HashMap<Integer, HashMap<String, Object>> allAssessmentsMap = new HashMap<Integer, HashMap<String, Object>>();

		String sql = "select COALESCE(sum(total_points)/count(batch_assessment.istar_user),0), COALESCE(cast(count(batch_assessment.istar_user) as integer),0) as total_students, batch_assessment.item_id from (select user_gamification.istar_user, sum(user_gamification.points) as total_points, sum(user_gamification.coins) as total_coins, user_gamification.item_id from assessment,user_gamification where user_gamification.item_id=assessment.id and user_gamification.item_type='ASSESSMENT' and user_gamification.istar_user in (select student_id from batch_students where batch_group_id in (select batch_group_id from batch_students where batch_students.student_id= :istarUserId)) group by user_gamification.istar_user, user_gamification.item_id order by total_points desc) as batch_assessment group by batch_assessment.item_id";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId", istarUserId);

		List<Object[]> results = query.list();

		for (Object[] batchData : results) {
			HashMap<String, Object> batchMap = new HashMap<String, Object>();

			batchMap.put("batchAverage", (Double) batchData[0]);
			batchMap.put("totalStudents", (Integer) batchData[1]);

			allAssessmentsMap.put((Integer) batchData[2], batchMap);
		}
		return allAssessmentsMap;
	}*/

	@SuppressWarnings("unchecked")
	public HashMap<Integer, HashMap<String, Object>> calculateBatchAverageOfAllAssessments11(Integer istarUserId) {

		HashMap<Integer, HashMap<String, Object>> allAssessmentsMap = new HashMap<Integer, HashMap<String, Object>>();

		String sql = "with temp as (select student_id from batch_students where batch_group_id in (select batch_group_id from batch_students where student_id= :istarUserId)) select COALESCE((sum(points_earned)*1.0/count(user_id)),0) as batch_average, total_points, cast (count(user_id) as integer) as attempted_count, cast((select count(*) from temp) as integer) as total_students, assessment_id from report where user_id in (select * from temp) group by assessment_id, total_points";
		//System.out.println(sql);
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId", istarUserId);

		List<Object[]> results = query.list();

		for (Object[] batchData : results) {
			HashMap<String, Object> batchMap = new HashMap<String, Object>();

			batchMap.put("batchAverage", ((BigDecimal) batchData[0]).doubleValue());
			batchMap.put("numberOfStudentsAttemptedAssessment", (Integer) batchData[2]);
			batchMap.put("totalStudentsInBatch", (Integer) batchData[3]);

			allAssessmentsMap.put((Integer) batchData[4], batchMap);
		}
		return allAssessmentsMap;
	}
	
	public Integer getNumberOfUsersAttemptedTheAssessment(int istarUserId, int assessmentId) {

		String sql = "select COALESCE(cast (count(DISTINCT istar_user)  as integer),0) from user_gamification where batch_group_id in (select batch_group_id from batch_students where student_id="+ istarUserId+") and item_type='ASSESSMENT' and item_id=" + assessmentId;

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);

		Integer count = (Integer) query.list().get(0);

		return count;
	}

	@SuppressWarnings("unchecked")
	public HashMap<Integer, Integer> getNumberOfUsersAttemptedTheAssessmentOfUser(int istarUserId) {

		HashMap<Integer, Integer> numberOfUsersForAssessment = new HashMap<Integer, Integer>();

/*		String sql = "select COALESCE(cast (count(DISTINCT istar_user)  as integer),0), item_id from user_gamification where istar_user in "
				+ "(select student_id from batch_students where batch_group_id in "
				+ "(select batch_group_id from batch_students where student_id=" + istarUserId
				+ " )) and item_type='ASSESSMENT' group by item_id";*/

		String sql = "select COALESCE(cast (count(DISTINCT istar_user)  as integer),0) from user_gamification where batch_group_id in (select batch_group_id from batch_students where student_id="+ istarUserId+") and item_type='ASSESSMENT' group by item_id";

		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);

		List<Object[]> result = query.list();

		for (Object[] obj : result) {
			numberOfUsersForAssessment.put((Integer) obj[1], (Integer) obj[0]);
		}
		return numberOfUsersForAssessment;
	}

	public Integer getNumberOfCorrectlyAnsweredQuestions(int istarUserId, int assessmentId) {
		String sql = "select COALESCE(cast(count(*) as integer),0) from student_assessment where student_id="
				+ istarUserId + " and assessment_id=" + assessmentId + " and correct=true";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);

		Integer count = (Integer) query.list().get(0);

		return count;
	}

	@SuppressWarnings("unchecked")
	public HashMap<Integer, Integer> getNumberOfCorrectlyAnsweredQuestionsOfAllAssessments(int istarUserId) {

		HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();

		String sql = "select assessment_id, COALESCE(cast(count(*) as integer),0) from student_assessment where student_id="
				+ istarUserId + " and correct=true group by assessment_id";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);

		List<Object[]> queryResult = query.list();

		for (Object[] obj : queryResult) {

			result.put((Integer) obj[0], (Integer) obj[1]);
		}
		return result;
	}

	public HashMap<Integer, Integer> getSkillsMapOfAssessment(Assessment assessment) {

		HashMap<Integer, Integer> skillsMap = new HashMap<Integer, Integer>();
		HashSet<Integer> allCmsessionSkillObjectivesOfAssessment = new HashSet<Integer>();

		for (AssessmentQuestion assessmentQuestion : assessment.getAssessmentQuestions()) {
			Question question = assessmentQuestion.getQuestion();
			for (SkillObjective skillObjective : question.getSkillObjectives()) {
				allCmsessionSkillObjectivesOfAssessment.add(skillObjective.getParentSkill());
			}
		}

		for (Integer cmsessionSkillObjectiveId : allCmsessionSkillObjectivesOfAssessment) {
			SkillObjective skillObjective = getSkillObjective(cmsessionSkillObjectiveId);
			skillsMap.put(cmsessionSkillObjectiveId, skillObjective.getParentSkill());
		}
		return skillsMap;
	}

	

	@SuppressWarnings("unchecked")
	public HashMap<Integer, Double> getMaxPointsForSkillObjectiveOfAssessment(Integer assessmentId) {

		HashMap<Integer, Double> benchmarks = new HashMap<Integer, Double>();

		String sql = "select COALESCE(sum(max_points),0), skill_objective_id from assessment_benchmark where assessment_id= :assessmentId group by skill_objective_id";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("assessmentId", assessmentId);

		List<Object[]> result = query.list();

		for (Object[] obj : result) {
			benchmarks.put((Integer) obj[1], (Double) obj[0]);
		}
		return benchmarks;
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<Integer, HashMap<Integer, Double>> getMaxPointsForSkillObjectiveOfAllAssessment(int istarUserId) {

		HashMap<Integer, HashMap<Integer, Double>> skillsBenchmark = new HashMap<Integer, HashMap<Integer, Double>>();
		
		String sql = "select COALESCE(sum(max_points),0), skill_objective_id, assessment_id from assessment_benchmark group by assessment_id, skill_objective_id order by assessment_id, skill_objective_id";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);

		List<Object[]> result = query.list();

		for (Object[] obj : result) {			
			if(skillsBenchmark.containsKey((Integer)obj[2])){
				skillsBenchmark.get((Integer)obj[2]).put((Integer) obj[1], (Double) obj[0]);
			}else{
				HashMap<Integer, Double> benchmarks = new HashMap<Integer, Double>();
				benchmarks.put((Integer) obj[1], (Double) obj[0]);
				skillsBenchmark.put((Integer)obj[2], benchmarks); 	
			}		
		}
		return skillsBenchmark;
	}

	public Double getMaxPointsOfAssessment(Integer assessmentId) {
		
		double totalPoints = 0d;
		String per_assessment_points="",
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
					if(AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
					//System.out.println("per_assessment_points"+per_assessment_points);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();			
		}
		String getTotalPoints ="select cast (sum(TFINAL.points_per_item) as float8) as tot_points from (select cast (custom_eval ( CAST ( TRIM ( REPLACE ( REPLACE ( REPLACE ( COALESCE (max_points, '0'), ':per_lesson_points', '"+per_lesson_points+"' ), ':per_assessment_points', '"+per_assessment_points+"' ), ':per_question_points', '"+per_question_points+"' ) ) AS TEXT ) ) as integer) as points_per_item  from ((select max_points, item_id , item_type from assessment_benchmark where item_id in (select distinct questionid from assessment_question where assessmentid = "+assessmentId+" ) and item_type ='QUESTION' ) union  (select max_points , item_id , item_type from assessment_benchmark where item_id ="+assessmentId+" and item_type ='ASSESSMENT' ) )TT ) TFINAL";
		DBUTILS util = new DBUTILS();
		List<HashMap<String, Object>> data = util.executeQuery(getTotalPoints);
		if(data.size()>0 && data.get(0).get("tot_points")!=null)
		{
			totalPoints=(double)data.get(0).get("tot_points");
		}
			
		return totalPoints;
	}

	public Assessment getAssessment(int assessmentId) {
		Assessment assessment;
		AssessmentDAO assessmentDAO = new AssessmentDAO();
		try {
			assessment = assessmentDAO.findById(assessmentId);
		} catch (IllegalArgumentException e) {
			assessment = null;
		}
		return assessment;
	}

	public SkillObjective getSkillObjective(Integer skillObjectiveId) {
		SkillObjectiveDAO skillObjectiveDAO = new SkillObjectiveDAO();
		SkillObjective skillObjective;
		try {
			skillObjective = skillObjectiveDAO.findById(skillObjectiveId);
		} catch (IllegalArgumentException e) {
			skillObjective = null;
		}
		return skillObjective;
	}
	/*
	 * public HashMap<Integer, Set<Integer>> getSkillsMapOfAssessment(int
	 * assessmentId){
	 * 
	 * HashMap<Integer, Set<Integer>> skillsMap = new HashMap<Integer,
	 * Set<Integer>>();
	 * 
	 * Assessment assessment = getAssessment(assessmentId); HashSet<Integer>
	 * allCmsessionSkillObjectivesOfAssessment = new HashSet<Integer>();
	 * 
	 * for(AssessmentQuestion assessmentQuestion :
	 * assessment.getAssessmentQuestions()){ Question question =
	 * assessmentQuestion.getQuestion();
	 * 
	 * for(SkillObjective skillObjective : question.getSkillObjectives()){
	 * allCmsessionSkillObjectivesOfAssessment.add(skillObjective.getParentSkill
	 * ()); } }
	 * 
	 * for(Integer cmsessionSkillObjectiveId :
	 * allCmsessionSkillObjectivesOfAssessment){ SkillObjective skillObjective =
	 * getSkillObjective(cmsessionSkillObjectiveId);
	 * 
	 * Integer moduleSkillObjective = skillObjective.getParentSkill();
	 * 
	 * if(skillsMap.containsKey(moduleSkillObjective)){
	 * skillsMap.get(moduleSkillObjective).add(cmsessionSkillObjectiveId);
	 * }else{ Set<Integer> cmsessionSkillObjectiveSet = new HashSet<Integer>();
	 * cmsessionSkillObjectiveSet.add(cmsessionSkillObjectiveId);
	 * skillsMap.put(moduleSkillObjective, cmsessionSkillObjectiveSet); } }
	 * return skillsMap; }
	 */
}
