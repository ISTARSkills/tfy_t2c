package com.istarindia.apps.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.istarindia.android.pojo.CmsessionSkillObjectivePOJO;
import com.istarindia.android.pojo.CoursePOJO;
import com.istarindia.android.pojo.LessonPOJO;
import com.istarindia.android.pojo.ModulePOJO;
import com.istarindia.android.pojo.SkillReportPOJO;
import com.viksitpro.core.dao.entities.Assessment;
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
			Cmsession cmsession = null; 
			
			if(lesson.getCmsessions().size() > 0){
				cmsession= lesson.getCmsessions().iterator().next();
			}
			
			Module module = getModuleOfLesson(lesson.getId());

			if (module != null) {
				
				if (studentPlaylist.getStatus().equals("INCOMPLETE")) {
					incompleteModules.add(module.getId());
				}
				
				CoursePOJO coursePOJO = null;
				ModulePOJO modulePOJO = null;
				List<SkillReportPOJO> allSkillsReport = null;
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

					allSkillsReport = new ArrayList<SkillReportPOJO>();
					SkillReportPOJO moduleSkillReportPOJO=null;
					
					modulePOJO = new ModulePOJO();
					modulePOJO.setId(module.getId());
					modulePOJO.setName(module.getModuleName());
					modulePOJO.setOrderId(module.getOrderId());
					modulePOJO.setDescription(module.getModule_description());
					modulePOJO.setImageURL(module.getImage_url());
					
					Set<String> allSkillObjectivesOfModule = new HashSet<String>();
					for (SkillObjective skillObjective : module.getSkillObjectives()) {
						allSkillObjectivesOfModule.add(skillObjective.getName());
					}
					modulePOJO.getSkillObjectives().addAll(allSkillObjectivesOfModule);
					
					for(SkillObjective skillObjective : cmsession.getSkillObjectives()){
						SkillReportPOJO cmsessionSkillReportPOJO = null;
						
						for(SkillReportPOJO tempSkillReportPOJO : coursePOJO.getSkillObjectives()){
							if(tempSkillReportPOJO.getId()==skillObjective.getParentSkill()){
								moduleSkillReportPOJO = tempSkillReportPOJO;
								break;
							}
						}
						
						if(moduleSkillReportPOJO==null){
							SkillObjective moduleSkillObjective = (new AppAssessmentServices()).getSkillObjective(skillObjective.getParentSkill());
							moduleSkillReportPOJO = new SkillReportPOJO();
							moduleSkillReportPOJO.setId(moduleSkillObjective.getId());
							moduleSkillReportPOJO.setName(moduleSkillObjective.getName());
							
							allSkillsReport.add(moduleSkillReportPOJO);
							coursePOJO.setSkillObjectives(allSkillsReport);
							
							List<SkillReportPOJO> allSkillReportsOfCmsession = new ArrayList<SkillReportPOJO>();
							cmsessionSkillReportPOJO = new SkillReportPOJO();
							
							cmsessionSkillReportPOJO.setId(skillObjective.getId());
							cmsessionSkillReportPOJO.setName(skillObjective.getName());
							cmsessionSkillReportPOJO.setDescription(cmsession.getDescription());
							cmsessionSkillReportPOJO.setItemId(cmsession.getId());
							cmsessionSkillReportPOJO.setItemType("CMSESSION_SKILL");
							HashMap<String, Object> map = getPointsAndCoinsOfUserForSkillOfCourse(istarUserId, skillObjective.getId(), course.getId());
							
							if(map.containsKey("points")){
								cmsessionSkillReportPOJO.setUserPoints((Double) map.get("points"));
							}else{
								cmsessionSkillReportPOJO.setUserPoints(0.0);
							}
							
							cmsessionSkillReportPOJO.setTotalPoints(calculateMaxPointsForCmsession(lesson.getId(), cmsession.getId(), course.getId(), istarUserId));
							cmsessionSkillReportPOJO.calculatePercentage();

							allSkillReportsOfCmsession.add(cmsessionSkillReportPOJO);
							moduleSkillReportPOJO.setSkills(allSkillReportsOfCmsession);
							modulePOJO.getSessionSkills().add(cmsessionSkillReportPOJO);
						}else{						
							for(SkillReportPOJO tempCmsessionSkillReportPOJO : moduleSkillReportPOJO.getSkills()){
								if(tempCmsessionSkillReportPOJO.getId()==skillObjective.getId()){
									cmsessionSkillReportPOJO = tempCmsessionSkillReportPOJO;
									break;
								}
							}
							
							if(cmsessionSkillReportPOJO==null){
								cmsessionSkillReportPOJO = new SkillReportPOJO();
								
								cmsessionSkillReportPOJO.setId(skillObjective.getId());
								cmsessionSkillReportPOJO.setName(skillObjective.getName());
								cmsessionSkillReportPOJO.setDescription(cmsession.getDescription());
								cmsessionSkillReportPOJO.setItemId(cmsession.getId());
								cmsessionSkillReportPOJO.setItemType("CMSESSION_SKILL");
								
								HashMap<String, Object> map = getPointsAndCoinsOfUserForSkillOfCourse(istarUserId, skillObjective.getId(), course.getId());
								
								if(map.containsKey("points")){
									cmsessionSkillReportPOJO.setUserPoints((Double) map.get("points"));
								}else{
									cmsessionSkillReportPOJO.setUserPoints(0.0);
								}
								
								cmsessionSkillReportPOJO.setTotalPoints(calculateMaxPointsForCmsession(lesson.getId(), cmsession.getId(), course.getId(), istarUserId));
								cmsessionSkillReportPOJO.calculatePercentage();
								
								moduleSkillReportPOJO.getSkills().add(cmsessionSkillReportPOJO);
								modulePOJO.getSessionSkills().add(cmsessionSkillReportPOJO);
							}
						}
					}
					
					coursePOJO.getModules().add(modulePOJO);
					allCoursePOJO.add(coursePOJO);
				} else {

					allSkillsReport = coursePOJO.getSkillObjectives();
					
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
						
						SkillReportPOJO moduleSkillReportPOJO=null;
						
						Set<String> allSkillObjectivesOfModule = new HashSet<String>();
						for (SkillObjective skillObjective : module.getSkillObjectives()) {
							allSkillObjectivesOfModule.add(skillObjective.getName());
						}

						modulePOJO.getSkillObjectives().addAll(allSkillObjectivesOfModule);
						
						System.out.println("SIZE OF CMSESSION SKILL OBJECTIVES ARE-->"+cmsession.getSkillObjectives().size());
						
						for(SkillObjective skillObjective : cmsession.getSkillObjectives()){
							SkillReportPOJO cmsessionSkillReportPOJO = null;
							
							for(SkillReportPOJO tempSkillReportPOJO : coursePOJO.getSkillObjectives()){
								if(tempSkillReportPOJO.getId()==skillObjective.getParentSkill()){
									moduleSkillReportPOJO = tempSkillReportPOJO;
									break;
								}
							}
							
							if(moduleSkillReportPOJO==null){
								SkillObjective moduleSkillObjective = (new AppAssessmentServices()).getSkillObjective(skillObjective.getParentSkill());
								moduleSkillReportPOJO = new SkillReportPOJO();
								moduleSkillReportPOJO.setId(moduleSkillObjective.getId());
								moduleSkillReportPOJO.setName(moduleSkillObjective.getName());
								
								allSkillsReport.add(moduleSkillReportPOJO);
								coursePOJO.setSkillObjectives(allSkillsReport);
								
								List<SkillReportPOJO> allSkillReportsOfCmsession = new ArrayList<SkillReportPOJO>();
								cmsessionSkillReportPOJO = new SkillReportPOJO();
								
								cmsessionSkillReportPOJO.setId(skillObjective.getId());
								cmsessionSkillReportPOJO.setName(skillObjective.getName());
								cmsessionSkillReportPOJO.setDescription(cmsession.getDescription());
								cmsessionSkillReportPOJO.setItemId(cmsession.getId());
								cmsessionSkillReportPOJO.setItemType("CMSESSION_SKILL");
								
								HashMap<String, Object> map = getPointsAndCoinsOfUserForSkillOfCourse(istarUserId, skillObjective.getId(), course.getId());
								
								if(map.containsKey("points")){
									cmsessionSkillReportPOJO.setUserPoints((Double) map.get("points"));
								}else{
									cmsessionSkillReportPOJO.setUserPoints(0.0);
								}
								
								cmsessionSkillReportPOJO.setTotalPoints(calculateMaxPointsForCmsession(lesson.getId(), cmsession.getId(), course.getId(), istarUserId));
								cmsessionSkillReportPOJO.calculatePercentage();
								
								allSkillReportsOfCmsession.add(cmsessionSkillReportPOJO);
								moduleSkillReportPOJO.setSkills(allSkillReportsOfCmsession);
								modulePOJO.getSessionSkills().add(cmsessionSkillReportPOJO);
							}else{						
								for(SkillReportPOJO tempCmsessionSkillReportPOJO : moduleSkillReportPOJO.getSkills()){
									if(tempCmsessionSkillReportPOJO.getId()==skillObjective.getId()){
										cmsessionSkillReportPOJO = tempCmsessionSkillReportPOJO;
										break;
									}
								}
								
								if(cmsessionSkillReportPOJO==null){
									cmsessionSkillReportPOJO = new SkillReportPOJO();
									
									cmsessionSkillReportPOJO.setId(skillObjective.getId());
									cmsessionSkillReportPOJO.setName(skillObjective.getName());
									cmsessionSkillReportPOJO.setDescription(cmsession.getDescription());
									cmsessionSkillReportPOJO.setItemId(cmsession.getId());
									cmsessionSkillReportPOJO.setItemType("CMSESSION_SKILL");
									
									HashMap<String, Object> map = getPointsAndCoinsOfUserForSkillOfCourse(istarUserId, skillObjective.getId(), course.getId());
									
									if(map.containsKey("points")){
										cmsessionSkillReportPOJO.setUserPoints((Double) map.get("points"));
									}else{
										cmsessionSkillReportPOJO.setUserPoints(0.0);
									}
									
									cmsessionSkillReportPOJO.setTotalPoints(calculateMaxPointsForCmsession(lesson.getId(), cmsession.getId(), course.getId(), istarUserId));
									cmsessionSkillReportPOJO.calculatePercentage();
									
									moduleSkillReportPOJO.getSkills().add(cmsessionSkillReportPOJO);
									modulePOJO.getSessionSkills().add(cmsessionSkillReportPOJO);
								}
							}
						}
						
						coursePOJO.getModules().add(modulePOJO);

					} else {		
						System.out.println("Module Already Added");
					}
				}
				if (incompleteModules.contains(module.getId())) {
					modulePOJO.setStatus("INCOMPLETE");
				} else {
					modulePOJO.setStatus("COMPLETE");
				}
			}
		}
		return allCoursePOJO;
	}
	
	
/*	public List<SkillReportPOJO> getSkillsReportForCourseOfUser(int istarUserId, int courseId){
		
		List<SkillReportPOJO> allSkillsReport = new ArrayList<SkillReportPOJO>();
		
		Course course = getCourse(courseId);
		
		for(Module module : course.getModules()){
			
			for(SkillObjective moduleSkillObjective : module.getSkillObjectives()){
				
				SkillReportPOJO moduleSkillReportPOJO=null;
				
				for(SkillReportPOJO tempModuleSkillReportPOJO :  allSkillsReport){
					if(tempModuleSkillReportPOJO.getId()==moduleSkillObjective.getId()){
						moduleSkillReportPOJO = tempModuleSkillReportPOJO;
					}
				}
								
				if(moduleSkillReportPOJO==null){
					moduleSkillReportPOJO = new SkillReportPOJO();
					moduleSkillReportPOJO.setId(moduleSkillObjective.getId());
					moduleSkillReportPOJO.setName(moduleSkillObjective.getName());
					moduleSkillReportPOJO.setTotalPoints(0.0);
					moduleSkillReportPOJO.setUserPoints(0.0);
					
					List<SkillReportPOJO> allCmsessionSkills = new ArrayList<SkillReportPOJO>();
					
					for(Cmsession cmsession : module.getCmsessions()){
						for(SkillObjective cmsessionSkillObjective : cmsession.getSkillObjectives()){
							SkillReportPOJO cmsessionSkillReportPOJO = new SkillReportPOJO();
							
							cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
							cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
							cmsessionSkillReportPOJO.setTotalPoints(0.0);
							cmsessionSkillReportPOJO.setUserPoints(0.0);
							
							allCmsessionSkills.add(cmsessionSkillReportPOJO);
						}
					}		
					moduleSkillReportPOJO.setSkills(allCmsessionSkills);					
				}
				allSkillsReport.add(moduleSkillReportPOJO);
			}
		}
		return allSkillsReport;
	}*/
	
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> getPointsAndCoinsOfUserForSkillOfCourse(int istarUserId, int skillObjectiveId, int courseId){
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		String sql = "select COALESCE(sum(points),0) as points, COALESCE(cast(sum(coins) as integer),0) as coins from user_gamification "
				+ "where skill_objective= :skillObjectiveId and istar_user= :istarUserId and item_id in "
				+ "(select id from assessment where course_id= :courseId)";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId", istarUserId);
		query.setParameter("skillObjectiveId", skillObjectiveId);
		query.setParameter("courseId", courseId);
		
		List<Object[]> result = query.list();
		
		if(result.size()>0){			
			Double points = (Double) result.get(0)[0];
			Integer coins = (Integer) result.get(0)[1];
			
			map.put("points", points);
			map.put("coins", coins);
		}		
		return map;
	}
	
	public Double calculateMaxPointsForCmsession(int lessonId, int cmsessionId, int courseId, int istarUserId){
		
		Double maxPoints = 0.0;
		
		Integer difficultyLevelSum = getDifficultyLevelSumOfLesson(lessonId);
		Integer numberOfLessons = getNumberOfLessonsForCmsessionOfUserForCourse(cmsessionId, courseId, istarUserId);
		
		System.out.println("difficultyLevelSum->"+difficultyLevelSum+" numberOfLessons->" + numberOfLessons);
		
		try{
		Properties properties = new Properties();
		String propertyFileName = "app.properties";
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);
				String pointsBenchmark = properties.getProperty("pointsBenchmark");				
				Integer benchmark = Integer.parseInt(pointsBenchmark);
				
				maxPoints = (difficultyLevelSum + (numberOfLessons* benchmark))*1.0;		
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return maxPoints;
	}
	
	public Integer getDifficultyLevelSumOfLesson(int lessonId){
		
		String sql = "select COALESCE(cast(sum(question.difficulty_level) as integer),0) as difficulty_level from "
				+ "lesson_skill_objective,question_skill_objective,question,skill_objective "
				+ "where lesson_skill_objective.learning_objectiveid=question_skill_objective.learning_objectiveid and "
				+ "question_skill_objective.questionid=question.id and question_skill_objective.learning_objectiveid=skill_objective.id and "
				+ "lessonid= :lessonId";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("lessonId", lessonId);

		Integer result = (Integer) query.list().get(0);
		
		return result;
	}
	
	public Integer getNumberOfLessonsForCmsessionOfUserForCourse(int cmsessionId, int courseId, int istarUserId){
		
		String sql = "select COALESCE(cast(count(distinct student_playlist.lesson_id)as integer),0) from student_playlist,lesson_cmsession where student_playlist.lesson_id=lesson_cmsession.lesson_id and lesson_cmsession.cmsession_id= :cmsessionId and student_playlist.student_id= :istarUserId and student_playlist.course_id= :courseId";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("cmsessionId", cmsessionId);
		query.setParameter("courseId", courseId);
		query.setParameter("istarUserId", istarUserId);
		
		Integer result = (Integer) query.list().get(0);

		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<Assessment> getAllAssessmentsOfACourse(int courseId){
		
		String hql = "from assessment where course_id= :courseId";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		Query query = session.createQuery(hql);
		query.setParameter("courseId", courseId);
		
		List<Assessment> allAssessments = query.list();
		
		return allAssessments;		
	}
	
	public Double getProgressOfUserForCourse(int istarUserId, int courseId){
		String sql = "select COALESCE(cast(count(case when status='INCOMPLETE' then 1 end) as integer),0) as incomplete, COALESCE(cast(count(case when status='COMPLETE' then 1 end) as integer),0) as complete  from student_playlist where student_id= :istarUserId and course_id= :courseId";
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId", istarUserId);
		query.setParameter("courseId", courseId);
		
		Object[] result = (Object[]) query.list().get(0);
		
		Integer completedQuestions = (Integer) result[1];
		Integer totalQuestions = (Integer) result[0] + (Integer) result[1];
		
		Double progress = ((completedQuestions*1.0)/totalQuestions)*100.0;
		return progress;
	}
	
	@SuppressWarnings("unchecked")
	public Module getModuleOfLesson(int lessonId){
	
		Module module = null;		
		String sql = "select module_id from cmsession_module where cmsession_id in( select cmsession_id from lesson_cmsession where lesson_id="+lessonId+" limit 1) limit 1";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		
		List<Integer> results = query.list();
		
		Integer moduleId = null;
		
		if(results.size()>0){
			moduleId = (Integer) query.list().get(0);
		}
		
		if(moduleId!=null){
			module = getModule(moduleId);
		}
		
		return module;
	}

	public Double getMaxPointsOfCourse(Integer courseId){
		
		String sql = "select COALESCE(sum(max_points),0) from assessment_benchmark where assessment_id in (select distinct assessment.id from assessment where course_id= :courseId)";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("courseId", courseId);
		
		Double maxPoints = (Double) query.list().get(0);
		
		return maxPoints;
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