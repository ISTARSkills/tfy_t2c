package com.istarindia.android.utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.istarindia.android.pojo.AssessmentPOJO;
import com.istarindia.android.pojo.CoursePOJO;
import com.istarindia.android.pojo.LessonPOJO;
import com.istarindia.android.pojo.ModulePOJO;
import com.istarindia.android.pojo.OptionPOJO;
import com.istarindia.android.pojo.QuestionPOJO;
import com.istarindia.android.pojo.StudentProfile;
import com.istarindia.android.pojo.StudentRankPOJO;
import com.istarindia.apps.services.AppAssessmentServices;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.AssessmentOption;
import com.viksitpro.core.dao.entities.AssessmentQuestion;
import com.viksitpro.core.dao.entities.Cmsession;
import com.viksitpro.core.dao.entities.Course;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.Module;
import com.viksitpro.core.dao.entities.Question;
import com.viksitpro.core.dao.entities.StudentPlaylist;
import com.viksitpro.core.pojo.recruiter.IstarUserPOJO;
import com.viksitpro.core.utilities.AppProperies;
import com.viksitpro.core.utilities.DBUTILS;

public class AppPOJOUtility {

	public IstarUserPOJO getIstarUserPOJO(IstarUser istarUser) {

		IstarUserPOJO istarUserPOJO = new IstarUserPOJO();

		istarUserPOJO.setIstarUserId(istarUser.getId());
		istarUserPOJO.setAuthenticationToken(istarUser.getAuthToken());
		istarUserPOJO.setEmail(istarUser.getEmail());
		istarUserPOJO.setIsVerified(istarUser.getIsVerified());

		if (istarUser.getUserRoles().iterator().hasNext()) {
			istarUserPOJO.setRole(istarUser.getUserRoles().iterator().next().getRole().getRoleName());
		}

		if (istarUser.getUserProfile() != null) {
			istarUserPOJO.setName(istarUser.getUserProfile().getFirstName());
		}

		return istarUserPOJO;
	}

	public StudentProfile getStudentProfile(IstarUser student) {
		if(AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
			//System.out.println("POJO service");
		}
		String mediaUrlPath = "";
		int per_assessment_points = 5, per_lesson_points = 5, per_question_points = 1, per_assessment_coins = 5,
				per_lesson_coins = 5, per_question_coins = 1;
		try {
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);
				mediaUrlPath = properties.getProperty("media_url_path");
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

		StudentProfile studentProfile = new StudentProfile();

		studentProfile.setId(student.getId());
		// studentProfile.setPassword(student.getPassword());
		studentProfile.setMobile(student.getMobile());
		studentProfile.setEmail(student.getEmail());
		studentProfile.setAuthenticationToken(student.getAuthToken());
		studentProfile.setIsVerified(student.getIsVerified());
		studentProfile.setLoginType(student.getLoginType());

		if (student.getUserProfile() != null) {
			if(AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
				//System.out.println("FIRST NAME POJO is->" + student.getUserProfile().getFirstName());

	}			studentProfile.setFirstName(student.getUserProfile().getFirstName());
			studentProfile.setLastName(student.getUserProfile().getLastName());
			studentProfile.setGender(student.getUserProfile().getGender());
			studentProfile.setDateOfBirth(student.getUserProfile().getDob());

			if (student.getUserProfile().getAddress() != null) {
				studentProfile.setLocation(student.getUserProfile().getAddress().getPincode().getCity());
			}
			if(student.getUserProfile().getImage().contains("http://")){
				studentProfile.setProfileImage(student.getUserProfile().getImage());
			} else {
				studentProfile.setProfileImage(mediaUrlPath + student.getUserProfile().getImage());
			}
			String userCategory = "COLLEGE_STUDENT";
			if (student.getUserProfile().getUserCategory() != null) {
				userCategory = student.getUserProfile().getUserCategory();
			}
			studentProfile.setUserCategory(userCategory);

			if (student.getUserRoles() != null && student.getUserRoles().size() > 0) {
				String role = student.getUserRoles().iterator().next().getRole().getRoleName();
				studentProfile.setUserType(role);
			}

		}

		if (student.getProfessionalProfile() != null) {
			studentProfile.setUnderGraduationDegree(student.getProfessionalProfile().getUnderGraduateDegreeName());
			studentProfile.setPostGraduationDegree(student.getProfessionalProfile().getPgDegreeName());
			studentProfile.setUnderGraduationCollege(student.getProfessionalProfile().getUnderGraduationCollege());
			studentProfile.setPostGraduationCollege(student.getProfessionalProfile().getPostGraduationCollege());
			studentProfile.setUnderGraduationSpecializationName(
					student.getProfessionalProfile().getUnderGraduationSpecializationName());
			studentProfile.setPostGraduationSpecializationName(
					student.getProfessionalProfile().getPostGraduationSpecializationName());
			studentProfile.setResumeURL(student.getProfessionalProfile().getResumeUrl());
			studentProfile.setUnderGraduationYear(student.getProfessionalProfile().getUnderGraduationYear());
		}

		DBUTILS util = new DBUTILS();
		String getRankPointsForUser = "SELECT * FROM ( SELECT istar_user, user_points, total_points, CAST (coins AS INTEGER) AS coins, perc, CAST ( RANK () OVER (ORDER BY user_points DESC) AS INTEGER ) AS user_rank FROM ( SELECT istar_user, user_points, total_points, coins, (case when total_points!= 0 then CAST ( (user_points * 100) / total_points AS INTEGER ) else 0 end )AS perc FROM ( SELECT T1.istar_user, SUM (T1.points) AS user_points, SUM (T1.max_points) AS total_points, SUM (T1.coins) AS coins FROM ( WITH summary AS ( SELECT P .istar_user, P .skill_objective, custom_eval ( CAST ( REPLACE ( REPLACE ( REPLACE ( COALESCE (P .points, '0'), ':per_lesson_points', '"
				+ per_lesson_points + "' ), ':per_assessment_points', '" + per_assessment_points
				+ "' ), ':per_question_points', '" + per_question_points
				+ "' ) AS TEXT ) ) AS points, custom_eval ( CAST ( REPLACE ( REPLACE ( REPLACE ( COALESCE (P .coins, '0'), ':per_lesson_coins', '"
				+ per_lesson_coins + "' ), ':per_assessment_coins', '" + per_assessment_coins
				+ "' ), ':per_question_coins', '" + per_assessment_coins
				+ "' ) AS TEXT ) ) AS coins, custom_eval ( CAST ( REPLACE ( REPLACE ( REPLACE ( COALESCE (P .max_points, '0'), ':per_lesson_points', '"
				+ per_lesson_points + "' ), ':per_assessment_points', '" + per_assessment_points
				+ "' ), ':per_question_points', '" + per_question_points
				+ "' ) AS TEXT ) ) AS max_points, P .item_id, ROW_NUMBER () OVER ( PARTITION BY P .istar_user, P .skill_objective, P .item_id ORDER BY P . TIMESTAMP DESC ) AS rk FROM user_gamification P WHERE item_type IN ('QUESTION', 'LESSON') AND batch_group_id = ( SELECT batch_group. ID FROM batch_students, batch_group WHERE batch_students.batch_group_id = batch_group. ID AND batch_students.student_id = "
				+ student.getId()
				+ " AND batch_group.is_primary = 't' LIMIT 1 ) ) SELECT s.* FROM summary s WHERE s.rk = 1 ) T1 GROUP BY istar_user  ) T2 ORDER BY user_points DESC, perc DESC, total_points DESC ) T3 ) T4 WHERE istar_user = "
				+ student.getId();
		//System.out.println("get getRankPointsForUser" + getRankPointsForUser);
		List<HashMap<String, Object>> rankPointsData = util.executeQuery(getRankPointsForUser);
		int rank = 0;
		int coins = 0;
		double userPoints = 0;
		double totalPoints = 0;
		if (rankPointsData.size() > 0) {
			rank = (int) rankPointsData.get(0).get("user_rank");
			coins = (int) rankPointsData.get(0).get("coins");
			userPoints = (double) rankPointsData.get(0).get("user_points");
			totalPoints = (double) rankPointsData.get(0).get("total_points");
		}
		studentProfile.setCoins(coins);
		studentProfile.setExperiencePoints((int) userPoints);
		studentProfile.setBatchRank(rank);
		return studentProfile;
	}

	public CoursePOJO getCoursePOJO(StudentPlaylist studentPlaylist) {

		CoursePOJO coursePOJO = new CoursePOJO();

		Course course = studentPlaylist.getCourse();

		if (course != null) {
			coursePOJO.setId(course.getId());
			coursePOJO.setName(course.getCourseName());
			coursePOJO.setDescription(course.getCourseDescription());
			coursePOJO.setImageURL(course.getImage_url());
			coursePOJO.setCategory(course.getCategory());

			ArrayList<ModulePOJO> modules = new ArrayList<ModulePOJO>();

			for (Module module : course.getModules()) {
				ModulePOJO modulePOJO = new ModulePOJO();

				modulePOJO.setId(module.getId());
				modulePOJO.setName(module.getModuleName());
				modulePOJO.setDescription(module.getModule_description());
				modulePOJO.setImageURL(module.getImage_url());
				modulePOJO.setOrderId(module.getOrderId());
				modulePOJO.setStatus("");

				ArrayList<LessonPOJO> lessons = new ArrayList<LessonPOJO>();

				for (Cmsession cmsession : module.getCmsessions()) {
					for (Lesson lesson : cmsession.getLessons()) {
						LessonPOJO lessonPOJO = new LessonPOJO();

						lessonPOJO.setId(lesson.getId());
						lessonPOJO.setTitle(lesson.getTitle());
						lessonPOJO.setDescription(lesson.getDescription());
						lessonPOJO.setOrderId(lesson.getOrderId());
						lessonPOJO.setType(lesson.getType());
						lessonPOJO.setDuration(lesson.getDuration());
						lessonPOJO.setSubject(lesson.getSubject());
						lessonPOJO.setStatus(studentPlaylist.getStatus());
						lessonPOJO.setPlaylistId(studentPlaylist.getId());

						lessons.add(lessonPOJO);
					}
				}
				// modulePOJO.setLessons(lessons);
				modules.add(modulePOJO);
			}
			coursePOJO.setModules(modules);
		}
		return coursePOJO;
	}

	public ModulePOJO getModulePOJO(Module module) {

		ModulePOJO modulePOJO = new ModulePOJO();

		modulePOJO.setId(module.getId());
		modulePOJO.setName(module.getModuleName());
		modulePOJO.setDescription(module.getModule_description());
		modulePOJO.setImageURL(module.getImage_url());
		modulePOJO.setOrderId(module.getOrderId());

		return modulePOJO;
	}

	public LessonPOJO getLessonPOJO(Lesson lesson) {

		LessonPOJO lessonPOJO = new LessonPOJO();

		lessonPOJO.setId(lesson.getId());
		lessonPOJO.setTitle(lesson.getTitle());
		lessonPOJO.setDescription(lesson.getDescription());
		lessonPOJO.setOrderId(lesson.getOrderId());
		lessonPOJO.setType(lesson.getType());
		lessonPOJO.setDuration(lesson.getDuration());
		lessonPOJO.setSubject(lesson.getSubject());

		return lessonPOJO;
	}

	public AssessmentPOJO getAssessmentPOJO(Assessment assessment) {

		AssessmentPOJO assessmentPOJO = new AssessmentPOJO();

		assessmentPOJO.setId(assessment.getId());
		assessmentPOJO.setType(assessment.getAssessmentType());
		assessmentPOJO.setName(assessment.getAssessmenttitle());
		assessmentPOJO.setCategory(assessment.getCategory());
		assessmentPOJO.setDescription(assessment.getDescription());
		assessmentPOJO.setDurationInMinutes(assessment.getAssessmentdurationminutes());
		if (assessment.getRetryAble() != null && assessment.getRetryAble()) {
			assessmentPOJO.setRetryable(true);
		} else {
			assessment.setRetryAble(false);
		}
		Double maxPoints = 0.0;
		AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
		maxPoints = appAssessmentServices.getMaxPointsOfAssessment(assessment.getId());
		assessmentPOJO.setPoints((double) Math.round(maxPoints));
		Set<AssessmentQuestion> assessmentQuestions = assessment.getAssessmentQuestions();
		ArrayList<QuestionPOJO> questions = new ArrayList<QuestionPOJO>();

		for (AssessmentQuestion assessmentQuestion : assessmentQuestions) {
			//if (assessmentQuestion.getQuestion().getContext_id() == assessment.getCourse()) {
				questions.add(getQuestionPOJO(assessmentQuestion));
			//}
		}
		assessmentPOJO.setQuestions(questions);

		return assessmentPOJO;
	}

	public QuestionPOJO getQuestionPOJO(AssessmentQuestion assessmentQuestion) {
		QuestionPOJO questionPOJO = new QuestionPOJO();

		Question question = assessmentQuestion.getQuestion();

		int orderId = assessmentQuestion.getOrderId();

		questionPOJO.setId(question.getId());
		questionPOJO.setOrderId(orderId);

		String text = "";
		if (question.getComprehensivePassageText() != null
				&& !question.getComprehensivePassageText().equalsIgnoreCase("")
				&& !question.getComprehensivePassageText().equalsIgnoreCase("null")
				&& !question.getComprehensivePassageText().equalsIgnoreCase("<p>null</p>")) {
			text = question.getComprehensivePassageText() + question.getQuestionText();
		} else {
			text = question.getQuestionText();
		}
		questionPOJO.setText(text);
		questionPOJO.setType(question.getQuestionType());
		questionPOJO.setDifficultyLevel(question.getDifficultyLevel());
		questionPOJO.setExplanation(question.getExplanation());
		questionPOJO.setComprehensivePassageText(question.getComprehensivePassageText());
		questionPOJO.setPoints(question.getPoints());
		questionPOJO.setDurationInSec(question.getDurationInSec());

		Set<AssessmentOption> allAssessmentOption = question.getAssessmentOptions();

		List<OptionPOJO> options = new ArrayList<OptionPOJO>();
		List<Integer> answers = new ArrayList<Integer>();

		for (AssessmentOption assessmentOption : allAssessmentOption) {
			if (assessmentOption.getText() != null && !assessmentOption.getText().trim().isEmpty()) {
				options.add(getOptionPOJO(assessmentOption));
				if (assessmentOption.getMarkingScheme() == 1) {
					answers.add(assessmentOption.getId());
				}
			}
		}
		questionPOJO.setOptions(options);
		questionPOJO.setAnswers(answers);

		return questionPOJO;
	}

	public OptionPOJO getOptionPOJO(AssessmentOption assessmentOption) {

		OptionPOJO optionPOJO = new OptionPOJO();

		optionPOJO.setId(assessmentOption.getId());
		optionPOJO.setText(assessmentOption.getText().replaceAll("\n", "").replaceAll("\r\n", ""));

		return optionPOJO;
	}
}
