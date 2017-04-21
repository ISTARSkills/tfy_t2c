package com.istarindia.apps.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Question;
import com.viksitpro.core.dao.entities.SkillObjective;
import com.viksitpro.core.dao.entities.SkillObjectiveDAO;
import com.viksitpro.core.dao.entities.StudentAssessment;
import com.viksitpro.core.dao.entities.UserGamification;

public class AppAssessmentServices {

	public AssessmentReportPOJO getAssessmentReport(int istarUserId, int assessmentId) {
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

					allSkillsReport.add(moduleSkillReportPOJO);
				}
			}

			StudentAssessmentServices studentAssessmentServices = new StudentAssessmentServices();
			List<StudentAssessment> allStudentAssessment = studentAssessmentServices
					.getStudentAssessmentForUser(istarUserId, assessmentId);

			if(allStudentAssessment.size()>0){
			int totalNumberOfQuestions = allStudentAssessment.size();
			Integer totalNumberOfCorrectlyAnsweredQuestions = getNumberOfCorrectlyAnsweredQuestions(istarUserId,
					assessment.getId());
			Integer numberOfUsersAttemptedTheAssessment = getNumberOfUsersAttemptedTheAssessment(istarUserId,
					assessment.getId());
			assessmentReportPOJO = new AssessmentReportPOJO();
			assessmentReportPOJO.setId(assessment.getId());
			assessmentReportPOJO.setName(assessment.getAssessmenttitle());
			assessmentReportPOJO.setTotalNumberOfQuestions(totalNumberOfQuestions);
			assessmentReportPOJO.setTotalNumberOfCorrectlyAnsweredQuestions(totalNumberOfCorrectlyAnsweredQuestions);
			assessmentReportPOJO.setSkillsReport(allSkillsReport);
			assessmentReportPOJO.setBatchAverage((Double) calculateBatchAverageOfAssessment(assessment, istarUserId,
					numberOfUsersAttemptedTheAssessment).get("batchAverage"));
			assessmentReportPOJO.setTotalNumberOfUsersInBatch((Integer) calculateBatchAverageOfAssessment(assessment,
					istarUserId, numberOfUsersAttemptedTheAssessment).get("totalStudents"));
			assessmentReportPOJO.setUsersAttemptedCount(numberOfUsersAttemptedTheAssessment);
			assessmentReportPOJO.calculateTotalScore();
			assessmentReportPOJO.calculateUserScore();
			assessmentReportPOJO.calculateAccuracy();
			}
		}
		return assessmentReportPOJO;
	}
	
	public AssessmentResponsePOJO getAssessmentResponseOfUser(int assessmentId, int istarUserId){
		
		AssessmentResponsePOJO assessmentResponsePOJO = new AssessmentResponsePOJO();
		StudentAssessmentServices studentAssessmentServices = new StudentAssessmentServices();
		List<StudentAssessment> allStudentAssessments = studentAssessmentServices
				.getStudentAssessmentForUser(istarUserId, assessmentId);
		List<QuestionResponsePOJO> allQuestionsResponse = new ArrayList<QuestionResponsePOJO>();

		if(allStudentAssessments.size()>0){
		for (StudentAssessment studentAssessment : allStudentAssessments) {
			QuestionResponsePOJO questionResponsePOJO = new QuestionResponsePOJO();
			List<Integer> markedOptions = new ArrayList<Integer>();
			questionResponsePOJO.setQuestionId(studentAssessment.getQuestion().getId());

			List<AssessmentOption> allOptionsOfQuestion = new ArrayList<AssessmentOption>(
					studentAssessment.getQuestion().getAssessmentOptions());

			for (int i = 0; i < 5; i++) {
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

	public HashMap<String, Object> calculateBatchAverageOfAssessment(Assessment assessment, Integer istarUserId,
			Integer numberOfUsersAttemptedTheAssessment) {

		Double batchAverage = 0.0;
		Integer totalStudentsInBatch = 0;
		HashMap<String, Object> batchMap = new HashMap<String, Object>();

		if (numberOfUsersAttemptedTheAssessment != null && numberOfUsersAttemptedTheAssessment > 0) {
			AppBatchStudentsServices appBatchStudentsServices = new AppBatchStudentsServices();
			UserGamificationServices userGamificationServices = new UserGamificationServices();
			List<IstarUser> allBatchStudents = appBatchStudentsServices.getBatchColleaguesOfUsers(istarUserId);
			totalStudentsInBatch = allBatchStudents.size();
			Double maxPointsOfAllUsers = 0.0;

			for (IstarUser istarUser : allBatchStudents) {
				maxPointsOfAllUsers = maxPointsOfAllUsers + userGamificationServices
						.getTotalPointsOfUserForItem(istarUser.getId(), assessment.getId(), "ASSESSMENT");
			}

			Double maxPointsOfAssessment = getMaxPointsOfAssessment(assessment.getId());

			System.out.println("maxPointsOfAllUsers->" + maxPointsOfAllUsers);
			System.out.println("maxPointsOfAssessment->" + maxPointsOfAssessment);

			batchAverage = maxPointsOfAllUsers / (maxPointsOfAssessment * numberOfUsersAttemptedTheAssessment);
		}
		batchMap.put("batchAverage", batchAverage);
		batchMap.put("totalStudents", totalStudentsInBatch);
		
		return batchMap;
	}

	public Integer getNumberOfUsersAttemptedTheAssessment(int istarUserId, int assessmentId) {

		String sql = "select COALESCE(cast (count(DISTINCT istar_user)  as integer),0) from user_gamification where istar_user in "
				+ "(select student_id from batch_students where batch_group_id in "
				+ "(select batch_group_id from batch_students where student_id=" + istarUserId
				+ " limit 1)) and item_id=" + assessmentId;

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);

		Integer count = (Integer) query.list().get(0);

		return count;
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
