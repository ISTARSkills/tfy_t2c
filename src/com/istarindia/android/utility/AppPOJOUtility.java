package com.istarindia.android.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
		System.out.println("POJO service");
		StudentProfile studentProfile = new StudentProfile();

		studentProfile.setId(student.getId());
		//studentProfile.setPassword(student.getPassword());
		studentProfile.setMobile(student.getMobile());
		studentProfile.setEmail(student.getEmail());
		studentProfile.setAuthenticationToken(student.getAuthToken());
		studentProfile.setIsVerified(student.getIsVerified());
		studentProfile.setLoginType(student.getLoginType());

		if (student.getUserProfile() != null) {
			System.out.println("FIRST NAME POJO is->" + student.getUserProfile().getFirstName());
			studentProfile.setFirstName(student.getUserProfile().getFirstName());
			studentProfile.setLastName(student.getUserProfile().getLastName());
			studentProfile.setGender(student.getUserProfile().getGender());
			studentProfile.setDateOfBirth(student.getUserProfile().getDob());

			if (student.getUserProfile().getAddress() != null) {
				studentProfile.setLocation(student.getUserProfile().getAddress().getPincode().getCity());
			}
			studentProfile.setProfileImage(student.getUserProfile().getProfileImage());
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
		
		AppUserRankUtility appUserRankUtility = new AppUserRankUtility();
		StudentRankPOJO studentRank= appUserRankUtility.getStudentRankPOJOOfAUser(student.getId());
		if(studentRank!=null){
			studentProfile.setCoins(studentRank.getCoins());
			studentProfile.setExperiencePoints(studentRank.getPoints());
			studentProfile.setBatchRank(studentRank.getBatchRank());
		}
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
				//modulePOJO.setLessons(lessons);
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

		Double maxPoints = 0.0;
		AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
		maxPoints = appAssessmentServices.getMaxPointsOfAssessment(assessment.getId());
		assessmentPOJO.setPoints((double) Math.round(maxPoints));
		Set<AssessmentQuestion> assessmentQuestions = assessment.getAssessmentQuestions();
		ArrayList<QuestionPOJO> questions = new ArrayList<QuestionPOJO>();

		for (AssessmentQuestion assessmentQuestion : assessmentQuestions) {
			if(assessmentQuestion.getQuestion().getContext_id()==assessment.getCourse()){
				questions.add(getQuestionPOJO(assessmentQuestion));
			}
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
		questionPOJO.setText(question.getQuestionText());
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
			if(assessmentOption.getText()!=null && !assessmentOption.getText().trim().isEmpty()){
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
		optionPOJO.setText(assessmentOption.getText());

		return optionPOJO;
	}
}
