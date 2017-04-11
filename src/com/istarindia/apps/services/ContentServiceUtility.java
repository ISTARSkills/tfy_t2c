package com.istarindia.apps.services;

import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.AssessmentDAO;
import com.viksitpro.core.dao.entities.Cmsession;
import com.viksitpro.core.dao.entities.CmsessionDAO;
import com.viksitpro.core.dao.entities.Course;
import com.viksitpro.core.dao.entities.CourseDAO;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.LessonDAO;
import com.viksitpro.core.dao.entities.Module;
import com.viksitpro.core.dao.entities.ModuleDAO;

public class ContentServiceUtility {

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
}
