package com.istarindia.android.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.istarindia.android.pojo.AssessmentPOJO;
import com.istarindia.android.pojo.LessonPOJO;
import com.istarindia.android.pojo.ModulePOJO;
import com.istarindia.android.pojo.OptionPOJO;
import com.istarindia.android.pojo.QuestionPOJO;
import com.istarindia.android.pojo.StudentProfile;
import com.istarindia.apps.services.AppAssessmentServices;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.AssessmentOption;
import com.viksitpro.core.dao.entities.AssessmentQuestion;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.Module;
import com.viksitpro.core.dao.entities.ProfessionalProfile;
import com.viksitpro.core.dao.entities.Question;
import com.viksitpro.core.dao.entities.UserProfile;
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
		
		String mediaUrlPath = "";
		mediaUrlPath = AppProperies.getProperty("media_url_path");		
		StudentProfile studentProfile = new StudentProfile();
		
		studentProfile.setId(student.getId());
		studentProfile.setMobile(student.getMobile());
		studentProfile.setEmail(student.getEmail());
		studentProfile.setAuthenticationToken(student.getAuthToken());
		studentProfile.setIsVerified(student.getIsVerified());
		studentProfile.setLoginType(student.getLoginType());

		if (student.getUserProfile() != null) {
			UserProfile userProfile = student.getUserProfile();
			studentProfile.setFirstName(userProfile.getFirstName());
			studentProfile.setLastName(userProfile.getLastName());
			studentProfile.setGender(userProfile.getGender());
			studentProfile.setDateOfBirth(userProfile.getDob());

			if (userProfile.getAddress() != null) {
				studentProfile.setLocation(userProfile.getAddress().getPincode().getCity());
			}
			if(userProfile.getImage().contains("http://")){
				studentProfile.setProfileImage(userProfile.getImage());
			} else {
				studentProfile.setProfileImage(mediaUrlPath + userProfile.getImage());
			}
			String userCategory = "COLLEGE_STUDENT";
			if (userProfile.getUserCategory() != null) {
				userCategory = userProfile.getUserCategory();
			}
			studentProfile.setUserCategory(userCategory);

			if (student.getUserRoles() != null && student.getUserRoles().size() > 0) {
				String role = student.getUserRoles().iterator().next().getRole().getRoleName();
				studentProfile.setUserType(role);
			}

		}

		if (student.getProfessionalProfile() != null) {
			ProfessionalProfile proProfile = student.getProfessionalProfile();
			studentProfile.setUnderGraduationDegree(proProfile.getUnderGraduateDegreeName());
			studentProfile.setPostGraduationDegree(proProfile.getPgDegreeName());
			studentProfile.setUnderGraduationCollege(proProfile.getUnderGraduationCollege());
			studentProfile.setPostGraduationCollege(proProfile.getPostGraduationCollege());
			studentProfile.setUnderGraduationSpecializationName(
					proProfile.getUnderGraduationSpecializationName());
			studentProfile.setPostGraduationSpecializationName(
					proProfile.getPostGraduationSpecializationName());
			studentProfile.setResumeURL(proProfile.getResumeUrl());
			studentProfile.setUnderGraduationYear(proProfile.getUnderGraduationYear());
		}

		DBUTILS util = new DBUTILS();
		String getRankPointsForUser = ""
				+ "select * from "
				+ "( "
					+ "select DISTINCT "
					+ "batch_students.student_id as student_id, "
					+ "cast (COALESCE(coins,0) as integer) as coins, "
					+ "cast (COALESCE(user_points,0) as float8) as user_points, "					
					+ "CAST ( RANK () OVER (  ORDER BY COALESCE(percentage, 0) DESC ) AS INTEGER ) as user_rank "
					+ "from  batch_students left join leaderboard on (leaderboard.user_id = batch_students.student_id) "
					+ "where batch_students.batch_group_id in "
					+ "("
						+ "select batch_group_id from batch_students where student_id = "+student.getId()+""
					+ ") "
					+ " order by user_rank "
				+ ")TT "
				+ "where student_id = "+student.getId();
		List<HashMap<String, Object>> rankPointsData = util.executeQuery(getRankPointsForUser);
		int rank = 0;
		int coins = 0;
		double userPoints = 0;		
		if (rankPointsData.size() > 0) {
			rank = (int) rankPointsData.get(0).get("user_rank");
			coins = (int) rankPointsData.get(0).get("coins");
			userPoints = (double) rankPointsData.get(0).get("user_points");
			
		}
		studentProfile.setCoins(coins);
		studentProfile.setExperiencePoints((int) userPoints);
		studentProfile.setBatchRank(rank);
		return studentProfile;
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
			
				questions.add(getQuestionPOJO(assessmentQuestion));
			
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
