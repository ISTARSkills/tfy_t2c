package com.istarindia.apps.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.istarindia.android.pojo.CoursePOJO;
import com.istarindia.android.pojo.ModulePOJO;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.Cmsession;
import com.viksitpro.core.dao.entities.CmsessionDAO;
import com.viksitpro.core.dao.entities.Course;
import com.viksitpro.core.dao.entities.CourseDAO;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.LessonDAO;
import com.viksitpro.core.dao.entities.Module;
import com.viksitpro.core.dao.entities.ModuleDAO;
import com.viksitpro.core.dao.entities.SkillObjective;
import com.viksitpro.core.dao.entities.StudentPlaylist;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

public class AppCourseServices {

	public List<CoursePOJO> getCoursesOfUser(int istarUserId){
		
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(istarUserId);

		Set<StudentPlaylist> allStudentPlaylistItems = istarUser.getStudentPlaylists();
		ArrayList<CoursePOJO> allCoursePOJO = new ArrayList<CoursePOJO>();
		Set<Integer> incompleteModules = new HashSet<Integer>();

		for (StudentPlaylist studentPlaylist : allStudentPlaylistItems) {

			Course course = studentPlaylist.getCourse();
			Lesson lesson = studentPlaylist.getLesson();
			Module module = getModuleOfLesson(lesson.getId());
			
			if(studentPlaylist.getStatus().equals("INCOMPLETE")){
				incompleteModules.add(module.getId());
			}
			
			System.out.println("Course->" + course.getId() + " lesson->" + lesson.getId() + " module-->" + module);
			if (module != null) {
				CoursePOJO coursePOJO = null;
				ModulePOJO modulePOJO = null;

				for (CoursePOJO tempCoursePOJO : allCoursePOJO) {
					if (tempCoursePOJO.getId() == course.getId()) {
						coursePOJO = tempCoursePOJO;
						break;
					}
				}

				if (coursePOJO == null) {
					coursePOJO = new CoursePOJO();
					coursePOJO.setId(course.getId());
					coursePOJO.setName(course.getCourseName());
					coursePOJO.setCategory(course.getCategory());
					coursePOJO.setDescription(course.getCourseDescription());
					coursePOJO.setImageURL(course.getImage_url());

					modulePOJO = new ModulePOJO();
					modulePOJO.setId(module.getId());
					modulePOJO.setName(module.getModuleName());
					modulePOJO.setOrderId(module.getOrderId());
					modulePOJO.setDescription(module.getModule_description());
					modulePOJO.setImageURL(module.getImage_url());

					Set<String> allSkillObjectivesOfModule = new HashSet<String>();					
					for(SkillObjective skillObjective : module.getSkillObjectives()){
						allSkillObjectivesOfModule.add(skillObjective.getName());
					}

					modulePOJO.getSkillObjectives().addAll(allSkillObjectivesOfModule);
					coursePOJO.getModules().add(modulePOJO);
					allCoursePOJO.add(coursePOJO);
				} else {

					for (ModulePOJO tempModulePOJO : coursePOJO.getModules()) {
						if (tempModulePOJO.getId() == module.getId()) {
							modulePOJO = tempModulePOJO;
						}
					}

					if (modulePOJO == null) {
						modulePOJO = new ModulePOJO();
						modulePOJO.setId(module.getId());
						modulePOJO.setName(module.getModuleName());
						modulePOJO.setOrderId(module.getOrderId());
						modulePOJO.setDescription(module.getModule_description());
						modulePOJO.setImageURL(module.getImage_url());

						Set<String> allSkillObjectivesOfModule = new HashSet<String>();					
						for(SkillObjective skillObjective : module.getSkillObjectives()){
							allSkillObjectivesOfModule.add(skillObjective.getName());
						}

						modulePOJO.getSkillObjectives().addAll(allSkillObjectivesOfModule);
						coursePOJO.getModules().add(modulePOJO);
											
					} else {
						System.out.println("Module Already Added");
					}
				}
				if(incompleteModules.contains(module.getId())){
					modulePOJO.setStatus("INCOMPLETE");
				}else{
					modulePOJO.setStatus("COMPLETE");
				}
			}
		}
		return allCoursePOJO;		
	}
	
	
	public Module getModuleOfLesson(int lessonId){
	
		Module module = null;
		
		String sql = "select module_id from cmsession_module where cmsession_id in( select cmsession_id from lesson_cmsession where lesson_id="+lessonId+" limit 1) limit 1";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		
		Integer moduleId = (Integer) query.list().get(0);
		
		if(moduleId!=null){
			module = getModule(moduleId);
		}
		
		return module;
	}
	
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

	
}
