package com.istarindia.apps.services;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.istarindia.android.pojo.AssessmentReportPOJO;
import com.istarindia.android.pojo.AssessmentResponsePOJO;
import com.istarindia.android.pojo.QuestionResponsePOJO;
import com.istarindia.android.pojo.SkillReportPOJO;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.AssessmentDAO;
import com.viksitpro.core.dao.entities.AssessmentOption;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.StudentAssessment;
import com.viksitpro.core.utilities.DBUTILS;

public class AppAssessmentServices {

	
	public AssessmentReportPOJO getAssessmentReportNew(int istarUserId, int assessmentId)
	{
		StudentAssessmentServices studentAssessmentServices = new StudentAssessmentServices();
		AssessmentReportPOJO assessmentReportPOJO = null;
		Assessment assessment = new AssessmentDAO().findById(assessmentId);
		if(assessment!=null)
		{
			assessmentReportPOJO= new AssessmentReportPOJO();
			if(assessment.getRetryAble()!=null && assessment.getRetryAble())
			{
				assessmentReportPOJO.setRetryable(true);
			}
			else
			{
				assessmentReportPOJO.setRetryable(false);
			}
			
			int  answeredCorrectly = 0;
			int totalQuestions = 0;
			List<StudentAssessment> allStudentAssessments = studentAssessmentServices.getStudentAssessmentForUser(istarUserId, assessmentId);
			List<QuestionResponsePOJO> allQuestionsResponse = new ArrayList<QuestionResponsePOJO>();
			AssessmentResponsePOJO assessmentResponsePOJO = null;
			if (allStudentAssessments.size() > 0) {
				assessmentResponsePOJO = new AssessmentResponsePOJO();
				for (StudentAssessment studentAssessment : allStudentAssessments) {
					totalQuestions++;
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
					if(studentAssessment.getCorrect())
					{
						answeredCorrectly++;
					}
					questionResponsePOJO.setOptions(markedOptions);
					questionResponsePOJO.setDuration(studentAssessment.getTimeTaken());
					allQuestionsResponse.add(questionResponsePOJO);
				}
				assessmentResponsePOJO = new AssessmentResponsePOJO();
				assessmentResponsePOJO.setId(assessmentId);
				assessmentResponsePOJO.setResponse(allQuestionsResponse);
			}
			if(assessmentResponsePOJO!=null)
			{
				assessmentReportPOJO.setAssessmentResponse(assessmentResponsePOJO);
			}
			double	accuracy = 0;
			if(totalQuestions!=0){
				accuracy = (double) (Math.round((((double) answeredCorrectly)/totalQuestions)*100.0*100.0)/100.0);
				
			}
			assessmentReportPOJO.setAccuracy(accuracy);
			assessmentReportPOJO.setTotalNumberOfCorrectlyAnsweredQuestions(answeredCorrectly);
			assessmentReportPOJO.setTotalNumberOfQuestions(totalQuestions);
			String message="";
			String messageDescription="";
			if(accuracy > 50){
				 message = "You passed the assessment!" ;
				 messageDescription = "Make sure you check out the full report to find out where you went wrong.";
				}else{
				 message = 	"Sorry! You failed the assessment.";
				 messageDescription = "You need atleast " + 50 + "% to pass. Brush up on the concepts and try again.";
			}	
			assessmentReportPOJO.setMessage(message);
			assessmentReportPOJO.setMessageDescription(messageDescription);
			assessmentReportPOJO.setId(assessmentId);
			assessmentReportPOJO.setName(assessment.getAssessmenttitle());
			
			DecimalFormat formnat = new DecimalFormat("##.##");
			HashMap<Integer, ArrayList<Integer>>sessionSkillInModuleSkill = new HashMap<>(); 
			HashMap<Integer, SkillReportPOJO> sessionSkillsTotal = new HashMap<>();
			HashMap<Integer, SkillReportPOJO> moduleSkillsTotal = new HashMap<>();
			//calculating skill tree for assessment
			String getPointsForSkills = ""
			+ "select module_id,"
			+ " module_name, "
			+ " session_id, "
			+ "title, "
			+ "cast (COALESCE(sum(uppa.total_points),0) as float8) as total_points, "
			+ "cast (COALESCE(sum(uppa.user_points),0) as float8) as user_points "
			+ "from "
			+ "( "
				+ "select distinct module.id as module_id , module.module_name, cmsession.id as session_id, cmsession.title "
				+ "from cmsession_module, assessment_benchmark, module, cmsession "
				+ "where assessment_benchmark.item_id = "+assessmentId+" "
				+ "and assessment_benchmark.item_type ='ASSESSMENT' "
				+ "and assessment_benchmark.skill_id = cmsession_module.cmsession_id "
				+ "and cmsession_module.cmsession_id= cmsession.id "
				+ "and cmsession_module.module_id = module.id "
			+ ")T1 left join user_points_per_assessment uppa on (T1.session_id = uppa.skill_id and uppa.user_id = "+istarUserId+") "
			+ "group by module_id,module_name,session_id,title";
			DBUTILS util = new DBUTILS();
			List<HashMap<String, Object>> skillData = util.executeQuery(getPointsForSkills);
			for(HashMap<String, Object> row: skillData)
			{
				int sessionSkillId = (int)row.get("session_id");
				int moduleSkillId = (int)row.get("module_id");
				String moduleSkillName = (String)row.get("module_name");
				String sessionSkillName = (String)row.get("title");
				double totalPointsInSessionSkill = (double)row.get("total_points");
				double userPointsInSessionSkill = (double)row.get("user_points");
				
				double perc=0;
				if(totalPointsInSessionSkill!=0)
				{
					perc = (userPointsInSessionSkill*100)/totalPointsInSessionSkill;
				}					
				
				if(!sessionSkillsTotal.containsKey(sessionSkillId))
				{
					SkillReportPOJO sessionSkill = new SkillReportPOJO();
					sessionSkill.setId(sessionSkillId);
					sessionSkill.setName(sessionSkillName);
					sessionSkill.setUserPoints(Double.parseDouble(formnat.format(userPointsInSessionSkill)));
					sessionSkill.setTotalPoints(Double.parseDouble(formnat.format(totalPointsInSessionSkill)));
					sessionSkill.setPercentage(Double.parseDouble(formnat.format(perc)));
					sessionSkillsTotal.put(sessionSkillId, sessionSkill);
				}
				
				if(!moduleSkillsTotal.containsKey(moduleSkillId))
				{
					SkillReportPOJO moduleSkill = new SkillReportPOJO();
					moduleSkill.setId(moduleSkillId);
					moduleSkill.setName(moduleSkillName);
					moduleSkillsTotal.put(moduleSkillId, moduleSkill);
				}
				
				if(sessionSkillInModuleSkill.containsKey(moduleSkillId))
				{
					ArrayList<Integer> sesionsSkill = sessionSkillInModuleSkill.get(moduleSkillId);
					sesionsSkill.add(sessionSkillId);
					sessionSkillInModuleSkill.put(moduleSkillId, sesionsSkill);
					
				}
				else
				{
					ArrayList<Integer> sesionsSkill = new ArrayList<>();
					sesionsSkill.add(sessionSkillId);
					sessionSkillInModuleSkill.put(moduleSkillId, sesionsSkill);
				}
				
			}
			
			double userScore = 0d;
			double totalScore =0d;
			ArrayList<SkillReportPOJO>finalSkillTree = new ArrayList<>();
			for(int moduleSkillId : sessionSkillInModuleSkill.keySet())
			{
				if(moduleSkillsTotal.get(moduleSkillId)!=null)
				{
					SkillReportPOJO modSkill = moduleSkillsTotal.get(moduleSkillId);
					double totalPointsInModuleSkill = 0d;
					double userPointsInModuleSkill = 0d;
					double percInModuleskill = 0d;
					ArrayList<SkillReportPOJO> sessionSkills = new ArrayList<>();
					for(int sessionSkillId : sessionSkillInModuleSkill.get(moduleSkillId))
					{
						if(sessionSkillsTotal.get(sessionSkillId)!=null)
						{
							SkillReportPOJO s = sessionSkillsTotal.get(sessionSkillId);
							sessionSkills.add(s);
							totalPointsInModuleSkill+=s.getTotalPoints();
							userPointsInModuleSkill+=s.getUserPoints();							
						}						
					}
					modSkill.setUserPoints(userPointsInModuleSkill);
					modSkill.setTotalPoints(totalPointsInModuleSkill);
					if(totalPointsInModuleSkill!=0)
					{
						percInModuleskill = (userPointsInModuleSkill*100)/totalPointsInModuleSkill;
					}
					userScore+=userPointsInModuleSkill;
					totalScore+=totalPointsInModuleSkill;
					modSkill.setPercentage(Double.parseDouble(formnat.format(percInModuleskill)));	
					modSkill.setSkills(sessionSkills);
					finalSkillTree.add(modSkill);
				}					
			}
			assessmentReportPOJO.setSkillsReport(finalSkillTree);
			assessmentReportPOJO.setUserScore(userScore);
			assessmentReportPOJO.setTotalScore(totalScore);
						
			double batchAverage = 0d;
			int userInBatch =0;
			int usetAttended = 0;
			if(assessment.getLesson()!=null)
			{
				String findBatchAvgUserAttempted=""
						+ "select cast(count(student_id) as integer) as total_in_batch, "
						+ "cast (COALESCE(avg(user_points),0) as float8) as batch_avg , "
						+ "cast (count(*) filter(where user_points is not null) as integer) as total_attended "
						+ "from "
						+ "( "
						+ "select batch_students.student_id, "
						+ "sum(user_points) as user_points  "
						+ "from batch_students left join user_points_per_assessment on (user_points_per_assessment.user_id = batch_students.student_id and user_points_per_assessment.assessment_id ="+assessmentId+") "
						+ "where batch_group_id in "
							+ "("
							+ "select batch_students.batch_group_id "
							+ "from batch_students,batch  "
							+ "where batch.batch_group_id = batch_students.batch_group_id "
							+ "and batch.course_id in "
								+ "("
								+ "select DISTINCT course_id "
								+ "from lesson_cmsession, cmsession_module, module_course "
								+ "where lesson_cmsession.lesson_id = "+assessment.getLesson().getId()+" "
								+ "and lesson_cmsession.cmsession_id = cmsession_module.cmsession_id "
								+ "and cmsession_module.module_id = module_course.module_id) "
							+ "and batch_students.student_id = "+istarUserId+" "
							+ ") "
						+ "group by batch_students.student_id )T1";
				System.err.println(findBatchAvgUserAttempted); 
				List<HashMap<String, Object>> dd = util.executeQuery(findBatchAvgUserAttempted);
				if(dd.size()>0)
				{
					if(dd.get(0).get("total_in_batch")!=null)
					{
						userInBatch = (int)dd.get(0).get("total_in_batch");
					}
					if(dd.get(0).get("batch_avg")!=null)
					{
						batchAverage = (double)dd.get(0).get("batch_avg");
					}
					if(dd.get(0).get("total_attended")!=null)
					{
						usetAttended = (int)dd.get(0).get("total_attended");
					}
				}
			}
			
			assessmentReportPOJO.setTotalNumberOfUsersInBatch(userInBatch);
			assessmentReportPOJO.setBatchAverage(batchAverage);
			assessmentReportPOJO.setUsersAttemptedCount(usetAttended);
			
		}
		return assessmentReportPOJO;
	}
	
	@SuppressWarnings("unchecked")
	public List<Integer> getAttemptedAssessmentsOfUser(int istarUserId){
		
		String sql = "select distinct item_id from report where istar_user="+istarUserId+" and item_type='ASSESSMENT'  and timestamp is not null";		
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
		DBUTILS util = new DBUTILS();
		List<AssessmentReportPOJO> allReports = new ArrayList<AssessmentReportPOJO>();
		String getDistinctAssessment ="select distinct assessment_id from report where user_id="+istarUserId;
		List<HashMap<String, Object>> dd = util.executeQuery(getDistinctAssessment);
		if(dd.size()>0){
			for(HashMap<String, Object> assessmentIdRow: dd){
				int assessmentId = (int)assessmentIdRow.get("assessment_id");
				AssessmentReportPOJO assessmentReportPOJO = getAssessmentReportNew(istarUserId, assessmentId);
				if(assessmentReportPOJO!=null){
					allReports.add(assessmentReportPOJO);
				}
			}
		}
		return allReports;		
	}


	public Double getMaxPointsOfAssessment(Integer assessmentId) {
		
		double totalPoints = 0d;		
		String getTotalPoints =""
		+ "select CAST "
		+ "( "
			+ "custom_eval "
			+ "( "
				+ "CAST "
				+ "( "
					+ "TRIM "
					+ "( "
					+ "REPLACE "
					+ "( "
					+ "REPLACE "
					+ "( "
					+ "REPLACE "
					+ "( COALESCE (string_agg(max_points,'+'), '0'), "
					+ "':per_lesson_points', (select property_value from constant_properties where property_name='per_lesson_points') ), "
					+ "':per_assessment_points', (select property_value from constant_properties where property_name='per_assessment_points') ), "
					+ "':per_question_points', (select property_value from constant_properties where property_name='per_question_points') "
					+ ") "
					+ ") "
					+ "AS TEXT "
				+ ") "
			+ ") AS float8 "
		+ ") as tot_points   "
		+ "from assessment_benchmark "
		+ "where (item_id = "+assessmentId+" and item_type='ASSESSMENT') "
		+ "or (item_id in (select questionid from assessment_question where assessmentid ="+assessmentId+") and item_type ='QUESTION')";
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

}
