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
import com.viksitpro.core.utilities.DBUTILS;

public class AppAssessmentServices {

	
	@SuppressWarnings("unchecked")
	public AssessmentReportPOJO getAssessmentReport(int istarUserId, int assessmentId) {

		Assessment assessment = getAssessment(assessmentId);
		AssessmentReportPOJO assessmentReportPOJO = null;
		if (assessment != null) {

			List<SkillReportPOJO> shellTree = getShellTreeForAssessment(assessmentId);
			for(SkillReportPOJO dd : shellTree)
			{
				System.out.println("in shell tree "+dd.getName()+" - "+dd.getId());
				System.out.println("inshell tree "+" "+dd.getUserPoints()+" "+dd.getTotalPoints()+" "+dd.getPercentage());
				for(SkillReportPOJO ll: dd.getSkills())
				{
					System.out.println("in shell tree "+ll.getName()+" - "+ll.getId());
					System.out.println("inshell tree "+" "+ll.getUserPoints()+" "+ll.getTotalPoints()+" "+ll.getPercentage());
				}
			}
			
			
			List<SkillReportPOJO> skillReportForAssesssment= fillShellTreeWithAssessmentData(shellTree, istarUserId, assessment);
			
			for(SkillReportPOJO dd : skillReportForAssesssment)
			{
				System.out.println("in shell tree "+dd.getName()+" - "+dd.getId());
				System.out.println("inshell tree "+" "+dd.getUserPoints()+" "+dd.getTotalPoints()+" "+dd.getPercentage());
				for(SkillReportPOJO ll: dd.getSkills())
				{
					System.out.println("in shell tree "+ll.getName()+" - "+ll.getId());
					System.out.println("inshell tree "+" "+ll.getUserPoints()+" "+ll.getTotalPoints()+" "+ll.getPercentage());
				}
			}
			assessmentReportPOJO = new AssessmentReportPOJO();
			/*BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
			Session session = baseHibernateDAO.getSession();
			
			String sql = "select cast(points as numeric) as points, cast(assessment_benchmark.max_points as numeric), cast(coins as integer), skill_objective, cmsession_id, module_id, course_id, count(batch_group_id) from user_gamification inner join assessment_benchmark on user_gamification.item_id=assessment_benchmark.assessment_id and user_gamification.item_type='ASSESSMENT' and user_gamification.skill_objective=assessment_benchmark.skill_objective_id where timestamp in (select max(timestamp) from user_gamification where istar_user="
					+ istarUserId + " and item_id=" + assessmentId +" and item_type='ASSESSMENT') group by cast(points as numeric), coins, skill_objective, module_id, cmsession_id, course_id, assessment_benchmark.max_points order by course_id, cmsession_id, module_id, skill_objective";

			System.out.println("AssessmentReport Query->" + sql);
			SQLQuery query = session.createSQLQuery(sql);
			List<Object[]> result = query.list();

			if (result.size() > 0) {

				HashMap<Integer, Module> modulesOfAssessment = new HashMap<Integer, Module>();
				assessmentReportPOJO = new AssessmentReportPOJO();
				List<SkillReportPOJO> skillsReport = new ArrayList<SkillReportPOJO>();
				AppCourseServices appCourseServices = new AppCourseServices();

				for (Object[] row : result) {
					
					System.out.println(((BigDecimal) row[0]).doubleValue());

					Double userPoints = ((BigDecimal) row[0]).doubleValue();
					Double totalPoints = ((BigDecimal) row[1]).doubleValue();
					//Integer coins = (Integer) row[2]; //not being used, but will need in future
					Integer cmsessionSkillObjectiveId = (Integer) row[3];
					Integer moduleId = (Integer) row[5];

					SkillObjective cmsessionSkillObjective = getSkillObjective(cmsessionSkillObjectiveId);
					SkillReportPOJO moduleSkillReportPOJO = null;
					SkillReportPOJO cmsessionSkillReportPOJO = null;

					System.out.println("moduleId->"+moduleId);
					if (modulesOfAssessment.containsKey(moduleId)) {
						System.out.println("Contains Module-->"+ moduleId);
						for (SkillReportPOJO tempModuleSkillReport : skillsReport) {
							System.out.println("MODULE IS-->" + tempModuleSkillReport.getId());
							if (tempModuleSkillReport.getId().equals(moduleId)) {
								System.out.println("Module found ");
								moduleSkillReportPOJO = tempModuleSkillReport;
								break;
							}
							else{
								System.out.println("Module not found--> "+ moduleId);
							}
						}

						cmsessionSkillReportPOJO = new SkillReportPOJO();
						cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
						cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
						cmsessionSkillReportPOJO.setTotalPoints(totalPoints);
						cmsessionSkillReportPOJO.setUserPoints(userPoints);
						cmsessionSkillReportPOJO.calculatePercentage();
						
						moduleSkillReportPOJO.getSkills().add(cmsessionSkillReportPOJO);
						moduleSkillReportPOJO.calculateTotalPoints();
						moduleSkillReportPOJO.calculateUserPoints();
						moduleSkillReportPOJO.calculatePercentage();
						moduleSkillReportPOJO.generateMessage();
						
					} else {
						Module module = appCourseServices.getModule(moduleId);
						System.out.println("Searching module->"+moduleId);
						if (module != null) {
							System.out.println("Found module in the DB->"+moduleId);
							modulesOfAssessment.put(moduleId, module);

							moduleSkillReportPOJO = new SkillReportPOJO();
							moduleSkillReportPOJO.setId(module.getId());
							moduleSkillReportPOJO.setName(module.getModuleName());
							moduleSkillReportPOJO.setDescription(module.getModule_description());
							moduleSkillReportPOJO.setImageURL(module.getImage_url());

							List<SkillReportPOJO> cmsessionSkillsReport = new ArrayList<SkillReportPOJO>();

							cmsessionSkillReportPOJO = new SkillReportPOJO();
							cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
							cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
							cmsessionSkillReportPOJO.setTotalPoints(totalPoints);
							cmsessionSkillReportPOJO.setUserPoints(userPoints);
							cmsessionSkillReportPOJO.calculatePercentage();

							cmsessionSkillsReport.add(cmsessionSkillReportPOJO);
							moduleSkillReportPOJO.setSkills(cmsessionSkillsReport);
							
							moduleSkillReportPOJO.calculateTotalPoints();
							moduleSkillReportPOJO.calculateUserPoints();
							moduleSkillReportPOJO.calculatePercentage();
							moduleSkillReportPOJO.generateMessage();
							System.out.println("Adding module Skill report with id-->" + moduleSkillReportPOJO.getId());
							skillsReport.add(moduleSkillReportPOJO);
						}else{
							System.out.println("Module is null with id->"+moduleId);
						}
					}
				}
				
*/				
			String getUserAverage ="select cast (avg(user_average) as float8) as batch_average from (select case when T2.total_points !=0 then T2.user_points*100/T2.total_points else 0 end  as user_average from ( select T1.istar_user, sum(T1.points) as user_points, sum(T1.max_points) as total_points from ( WITH summary AS (     SELECT p.istar_user, 					p.skill_objective,	            p.points, 						p.max_points,            ROW_NUMBER() OVER(PARTITION BY p.istar_user, p.skill_objective                                  ORDER BY p.timestamp DESC) AS rk       FROM  user_gamification p where item_type ='QUESTION' and batch_group_id=(select batch_group.id from batch_students, batch_group  where batch_students.batch_group_id = batch_group.id and batch_students.student_id = "+istarUserId+" and batch_group.is_primary ='t' limit 1)  and course_id ="+assessment.getCourse()+" 			and item_id in (select distinct questionid from assessment_question, assessment, question where assessment_question.assessmentid = assessment.id and assessment_question.questionid = question.id and question.context_id = assessment.course_id and assessment.id = "+assessmentId+") ) SELECT s.*   FROM summary s  WHERE s.rk = 1 )T1 group by T1.istar_user )T2 )T3";
			System.out.println("getUserAverage"+getUserAverage);
			DBUTILS util = new DBUTILS();
			List<HashMap<String, Object>> batchAverageData = util.executeQuery(getUserAverage);
			double batchAverage = 0;
			if(batchAverageData.size()>0 && batchAverageData.get(0).get("batch_average")!=null)
			{
				batchAverage = (double)batchAverageData.get(0).get("batch_average");
			}
			
			String countOfStudentInBatchAndAttended = "select cast(count(DISTINCT(student_id)) as integer) as total_stu, cast (count(DISTINCT(istar_user)) as integer) as attend_stu from (select DISTINCT student_id from batch_students where batch_group_id in (select batch_group.id from batch_students, batch_group  where batch_students.batch_group_id = batch_group.id and batch_students.student_id = "+istarUserId+" and batch_group.is_primary ='t' limit 1) )T1 left join user_gamification on (user_gamification.istar_user =  T1.student_id and user_gamification.item_id ="+assessmentId+" and user_gamification.item_type='ASSESSMENT' and user_gamification.course_id="+assessment.getCourse()+")";
			System.out.println("countOfStudentInBatchAndAttended"+countOfStudentInBatchAndAttended);
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
			
			assessmentReportPOJO = new AssessmentReportPOJO();
			
			assessmentReportPOJO.setId(assessment.getId());
			assessmentReportPOJO.setName(assessment.getAssessmenttitle());
			assessmentReportPOJO.setSkillsReport(skillReportForAssesssment); //what if skill size is zero
			assessmentReportPOJO.setBatchAverage(batchAverage);
			assessmentReportPOJO.setUsersAttemptedCount(totalAttended);
			assessmentReportPOJO.setTotalNumberOfUsersInBatch(totalStudentInBatch);
			
			
			
			String questionsSQL ="select cast (count(*) filter (where correct='t') as integer)as correct_answered, cast (count(*) as integer) as total_que from student_assessment where student_id = "+istarUserId+" and assessment_id ="+assessmentId+"";
			System.out.println("questionsSQL->"+questionsSQL);
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
			
		/*	String sqlBatch = "with batch_groups as (select distinct batch_group_id from user_gamification where istar_user="+istarUserId+" and item_id="+assessmentId+" and item_type='ASSESSMENT'), "
						+" all_batch_students as (select distinct student_id from batch_students where batch_group_id in (select * from batch_groups)), "
						+" batch_students_count as (select count(*) from all_batch_students), "
						+" attempted_students as (select istar_user, max(timestamp) from user_gamification where item_id="+assessmentId+" and item_type='ASSESSMENT' and batch_group_id in (select * from batch_groups) group by istar_user) "
				        +" select * from (select *, row_number() over(order by user_points desc) as rank, cast(sum(user_points) over ()/(select * from batch_students_count) as numeric) as batch_average, cast(count(*) over() as integer), cast((select count(*) from all_batch_students) as integer) as total_students from (select DISTINCT istar_user, cast(sum(points) as numeric) as user_points from user_gamification where item_id="+assessmentId+" and item_type='ASSESSMENT' and (istar_user, timestamp) in (select * from attempted_students) group by istar_user, batch_group_id) as temptable) as another where istar_user="+istarUserId;
				
				System.out.println("sqlBatch->"+sqlBatch);				
				SQLQuery queryBatch = session.createSQLQuery(sqlBatch);
				List<Object[]> resultBatch = queryBatch.list();
				
				if(resultBatch.size()>0){
					
					//Integer batchRank =  ((BigInteger) resultBatch.get(0)[2]).intValue(); //not being used, but might need in future
					Double batchAverage = ((BigDecimal) resultBatch.get(0)[3]).doubleValue();
					Integer numberOfStudentsInBatchAttemptedAssessment = (Integer) resultBatch.get(0)[4];
					Integer numberOfStudentsInBatch = (Integer) resultBatch.get(0)[5];
					
					
					SQLQuery queryQuestions = session.createSQLQuery(questionsSQL);
					List<Object[]> resultQuestions = queryQuestions.list();
					
					if(resultQuestions.size()>0){
						Integer correctQuestions = (Integer) resultQuestions.get(0)[0];
						Integer totalQuestions = (Integer) resultQuestions.get(0)[1];
						//Integer duration = (Integer) resultQuestions.get(0)[2]; //not being used, but might need in future
						
						
					}
					
					
				}*/
			}
		
		return assessmentReportPOJO;
	}
	
	private List<SkillReportPOJO> fillShellTreeWithAssessmentData(List<SkillReportPOJO> shellTree, int istarUserId, Assessment assessment) {

		String getDataForTree="SELECT 			T1. ID, 			T1.skill_objective, 			T1.points, 			T1.max_points, 			cmsession_module.module_id 		FROM 			 ( 				WITH summary AS ( 					SELECT 						P . ID, 						P .skill_objective, 						P .points, 						P .max_points, 						ROW_NUMBER () OVER ( 							PARTITION BY P .skill_objective 							ORDER BY 								P . TIMESTAMP DESC 						) AS rk 					FROM 						user_gamification P, assessment_question, question 					WHERE 						P .course_id = "+assessment.getId()+" 					and P.item_id = assessment_question.questionid 					and assessment_question.assessmentid = "+assessment.getId()+" 					and assessment_question.questionid = question.id 					and question.context_id ="+assessment.getCourse()+" 					AND P .item_type = 'QUESTION' 				) SELECT 					s.* 				FROM 					summary s 				WHERE 					s.rk = 1 			) T1 		JOIN cmsession_skill_objective ON ( 			T1.skill_objective = cmsession_skill_objective.skill_objective_id 		) 		JOIN cmsession_module ON ( 			cmsession_module.cmsession_id = cmsession_skill_objective.cmsession_id 		) 			";
		System.out.println("getDataForTree"+getDataForTree);
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
		
		Assessment assessment = new AssessmentDAO().findById(assessmentId);
		List<SkillReportPOJO> shellTree = new ArrayList<>();
		DBUTILS util = new DBUTILS();
		String shelltreeData = "select T1.skill_objective_id, T1.max_points, T1.name as skill_name, module.id as module_id, module.module_name , COALESCE(module.module_description,' ') as module_description, module.image_url from ( select assessment.course_id, skill_objective_id, max_points, skill_objective.name  from assessment_benchmark, assessment, skill_objective where assessment_benchmark.item_id = assessment.id and assessment_benchmark.item_type = 'ASSESSMENT' and assessment.id ="+assessmentId+" and assessment_benchmark.context_id = assessment.course_id and assessment_benchmark.skill_objective_id = skill_objective.id ) T1 join cmsession_skill_objective on (cmsession_skill_objective.skill_objective_id = T1.skill_objective_id) join cmsession_module on (cmsession_skill_objective.cmsession_id = cmsession_module.cmsession_id) join module on (module.id = cmsession_module.module_id) join module_course on (module.id = module_course.module_id) where module_course.course_id = T1.course_id";
		List<HashMap<String, Object>> assessmentData = util.executeQuery(shelltreeData);
		for(HashMap<String, Object> row: assessmentData)
		{
			int skillId = (int)row.get("skill_objective_id");
			double maxPoints = (double)row.get("max_points");
			String skillName = (String)row.get("skill_name");
			int moduleId = (int)row.get("module_id");
			String moduleName = (String)row.get("module_name");
			String moduleDesc = (String)row.get("module_description");
			String moduleImage = mediaUrlPath+"course_images/"+moduleName.trim().charAt(0)+".png";
			if(row.get("image_url")!=null)
			{
				moduleImage = mediaUrlPath+ row.get("image_url").toString();
			}
			
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
				sessionSkill.setUserPoints((double)0);
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
		System.out.println("Getting assessment report");
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
		
		String sql = "select distinct item_id from user_gamification where istar_user="+istarUserId+" and timestamp is not null";
		System.out.println("all Assessments of user from UsrGmfctn---->"+sql);
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
	 * System.out.println("maxPointsOfAllUsers->" + maxPointsOfAllUsers);
	 * System.out.println("maxPointsOfAssessment->" + maxPointsOfAssessment);
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
		System.out.println(sql);

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
		System.out.println(sql);
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
	public Double getMaxPointsForSkillObjectiveOfAssessment(Integer assessmentId, Integer skillObjectiveId) {

		String sql = "from AssessmentBenchmark assessmentBenchmark where assessment.id= :assessmentId and skillObjective.id= :skillObjectiveId";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		Query query = session.createQuery(sql);
		query.setParameter("assessmentId", assessmentId);
		query.setParameter("skillObjectiveId", skillObjectiveId);

		List<AssessmentBenchmark> allAssessmentBenchmark = query.list();

		if (allAssessmentBenchmark.size() > 0) {
			return allAssessmentBenchmark.get(0).getMaxPoints();
		} else {
			return 0.0;
		}
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

		String sql = "select COALESCE(sum(max_points),0) from assessment_benchmark where assessment_id= :assessmentId";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("assessmentId", assessmentId);

		Double totalPoints = (Double) query.list().get(0);

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
