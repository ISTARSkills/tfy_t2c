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

			if (allStudentAssessment.size() > 0) {
				int totalNumberOfQuestions = allStudentAssessment.size();
				Integer totalNumberOfCorrectlyAnsweredQuestions = getNumberOfCorrectlyAnsweredQuestions(istarUserId,
						assessment.getId());
				Integer numberOfUsersAttemptedTheAssessment = getNumberOfUsersAttemptedTheAssessment(istarUserId,
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
				assessmentReportPOJO.setTotalNumberOfUsersInBatch((Integer) batchAverageMap.get("totalStudents"));
				assessmentReportPOJO.setUsersAttemptedCount(numberOfUsersAttemptedTheAssessment);
				assessmentReportPOJO.calculateTotalScore();
				assessmentReportPOJO.calculateUserScore();
				assessmentReportPOJO.calculateAccuracy();
			}
		}
		return assessmentReportPOJO;
	}

	public AssessmentResponsePOJO getAssessmentResponseOfUser(int assessmentId, int istarUserId) {

		AssessmentResponsePOJO assessmentResponsePOJO = new AssessmentResponsePOJO();
		StudentAssessmentServices studentAssessmentServices = new StudentAssessmentServices();
		List<StudentAssessment> allStudentAssessments = studentAssessmentServices
				.getStudentAssessmentForUser(istarUserId, assessmentId);
		List<QuestionResponsePOJO> allQuestionsResponse = new ArrayList<QuestionResponsePOJO>();

		if (allStudentAssessments.size() > 0) {
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

		List<AssessmentReportPOJO> allReports = new ArrayList<AssessmentReportPOJO>();

		HashMap<Integer, Integer> numberOfCorrectlyAnsweredQuestions = getNumberOfCorrectlyAnsweredQuestionsOfAllAssessments(
				istarUserId);
		HashMap<Integer, Integer> numberOfUsersAttemptedAssessments = getNumberOfUsersAttemptedTheAssessmentOfUser(
				istarUserId);
		HashMap<Integer, HashMap<String, Object>> batchAverageOfAssessments = calculateBatchAverageOfAllAssessments(
				istarUserId);
		HashMap<Integer, HashMap<Integer, Double>> skillsBenchmarkForAssessments = getMaxPointsForSkillObjectiveOfAllAssessment();

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

						allSkillsReport.add(moduleSkillReportPOJO);
					}
				}

				int totalNumberOfQuestions = assessment.getAssessmentQuestions().size();
				Integer totalNumberOfCorrectlyAnsweredQuestions = numberOfCorrectlyAnsweredQuestions
						.get(assessment.getId());
				Integer numberOfUsersAttemptedTheAssessment = numberOfUsersAttemptedAssessments.get(assessment.getId());

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
					assessmentReportPOJO.setTotalNumberOfUsersInBatch((Integer) batchAverageMap.get("totalStudents"));
				}
				assessmentReportPOJO.setUsersAttemptedCount(numberOfUsersAttemptedTheAssessment);
				assessmentReportPOJO.calculateTotalScore();
				assessmentReportPOJO.calculateUserScore();
				assessmentReportPOJO.calculateAccuracy();

				allReports.add(assessmentReportPOJO);
			}
		}
		return allReports;
	}

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

	@SuppressWarnings("rawtypes")
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
	}

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
	public HashMap<Integer, Integer> getNumberOfUsersAttemptedTheAssessmentOfUser(int istarUserId) {

		HashMap<Integer, Integer> numberOfUsersForAssessment = new HashMap<Integer, Integer>();

		String sql = "select COALESCE(cast (count(DISTINCT istar_user)  as integer),0), item_id from user_gamification where istar_user in "
				+ "(select student_id from batch_students where batch_group_id in "
				+ "(select batch_group_id from batch_students where student_id=" + istarUserId
				+ " limit 1)) and item_type='ASSESSMENT' group by item_id";

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
	public HashMap<Integer, HashMap<Integer, Double>> getMaxPointsForSkillObjectiveOfAllAssessment() {

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
