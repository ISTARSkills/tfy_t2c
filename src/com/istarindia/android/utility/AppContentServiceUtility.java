package com.istarindia.android.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.AssessmentBenchmark;
import com.viksitpro.core.dao.entities.AssessmentDAO;
import com.viksitpro.core.dao.entities.AssessmentOption;
import com.viksitpro.core.dao.entities.AssessmentOptionDAO;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.Cmsession;
import com.viksitpro.core.dao.entities.CmsessionDAO;
import com.viksitpro.core.dao.entities.Course;
import com.viksitpro.core.dao.entities.CourseDAO;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.LessonDAO;
import com.viksitpro.core.dao.entities.Module;
import com.viksitpro.core.dao.entities.ModuleDAO;
import com.viksitpro.core.dao.entities.Question;
import com.viksitpro.core.dao.entities.QuestionDAO;
import com.viksitpro.core.dao.entities.SkillObjective;
import com.viksitpro.core.dao.entities.SkillObjectiveDAO;

public class AppContentServiceUtility {

	public Course getCourse(int courseId) {
		Course course;
		CourseDAO courseDAO = new CourseDAO();
		try {
			course = courseDAO.findById(courseId);
		} catch (IllegalArgumentException e) {
			course = null;
		}
		return course;
	}

	public Module getModule(int moduleId) {
		Module module;
		ModuleDAO moduleDAO = new ModuleDAO();
		try {
			module = moduleDAO.findById(moduleId);
		} catch (IllegalArgumentException e) {
			module = null;
		}
		return module;
	}

	public Cmsession getCmsession(int cmsessionId) {
		Cmsession cmsession;
		CmsessionDAO cmsessionDAO = new CmsessionDAO();
		try {
			cmsession = cmsessionDAO.findById(cmsessionId);
		} catch (IllegalArgumentException e) {
			cmsession = null;
		}
		return cmsession;
	}

	public Lesson getLesson(int lessonId) {
		Lesson lesson;
		LessonDAO lessonDAO = new LessonDAO();
		try {
			lesson = lessonDAO.findById(lessonId);
		} catch (IllegalArgumentException e) {
			lesson = null;
		}
		return lesson;
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

	public Question getQuestion(int questionId) {
		Question question;
		QuestionDAO questionDAO = new QuestionDAO();
		try {
			question = questionDAO.findById(questionId);
		} catch (IllegalArgumentException e) {
			question = null;
		}
		return question;
	}

	public AssessmentOption getAssessmentOption(int optionId) {
		AssessmentOption assessmentOption;
		AssessmentOptionDAO assessmentOptionDAO = new AssessmentOptionDAO();
		try {
			assessmentOption = assessmentOptionDAO.findById(optionId);
		} catch (IllegalArgumentException e) {
			assessmentOption = null;
		}
		return assessmentOption;
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

	@SuppressWarnings("unchecked")
	public List<AssessmentBenchmark> getAssessmentBenchmarksForAssessment(int assessmentId) {

		String hql = "from AssessmentBenchmark assessmentBenchmark where assessment.id= :assessmentId";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		Query query = session.createQuery(hql);
		query.setParameter("assessmentId", assessmentId);

		List<AssessmentBenchmark> allAssessmentBenchmark = query.list();

		return allAssessmentBenchmark;
	}
	
	@SuppressWarnings("unchecked")
	public List<Assessment> getAssessmentsOfACourse(int courseId){
		
		String hql = "from Assessment assessment where course_id= :courseId";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		Query query = session.createQuery(hql);
		query.setParameter("courseId", courseId);

		List<Assessment> allAssessments = query.list();

		return allAssessments;
	}

	// do not remove comments (sysouts), else be ready to get screwed in the
	// future!
	public HashMap<String, Boolean> getAnsweredOptionsMap(Question question, List<Integer> options) {
		boolean isCorrect = false;

		HashMap<String, Boolean> optionsMap = new HashMap<String, Boolean>();
		if (!options.contains(-1)) {
			// System.out.println("Checking Options with MaRKED Answers");
			ArrayList<AssessmentOption> allOptions = new ArrayList<AssessmentOption>(question.getAssessmentOptions());

			for (int i = 0; i < 5; i++) {
				if (i < allOptions.size() && allOptions.get(i).getMarkingScheme() == 1) {
					if (options.contains(allOptions.get(i).getId())) {
						optionsMap.put("option" + i, true);
						// System.out.println(i + " Answer is Correct and user
						// marked it correct too!");
						isCorrect = true;
					} else {
						optionsMap.put("option" + i, false);
						// System.out.println(i +" Answer is Correct and user
						// did not marked it correct!");
						isCorrect = false;
					}
				} else if (i < allOptions.size()) {
					if (options.contains(allOptions.get(i).getId())) {
						optionsMap.put("option" + i, true);
						// System.out.println(i+" Answer is Not Correct and but
						// user marked it correct!");
						isCorrect = false;
					} else {
						optionsMap.put("option" + i, false);
						// System.out.println(i+" Answer is Not Correct and and
						// user did not marked it also!");
					}
				} else {
					// System.out.println(i+" Less Than 5 Options, so setting it
					// to false");
					optionsMap.put("option" + i, false);
				}
			}
		} else {
			// System.out.println("User did not attempt the question");
			for (int i = 0; i < 5; i++) {
				// System.out.println(i+" Setting All Options to false");
				optionsMap.put("option" + i, false);
			}
		}

		optionsMap.put("isCorrect", isCorrect);
		return optionsMap;
	}
}
