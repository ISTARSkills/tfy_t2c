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

import com.istarindia.android.pojo.CmsessionPOJO;
import com.istarindia.android.pojo.CmsessionSkillObjectivePOJO;
import com.istarindia.android.pojo.CoursePOJO;
import com.istarindia.android.pojo.LessonPOJO;
import com.istarindia.android.pojo.ModulePOJO;
import com.istarindia.android.pojo.SkillReportPOJO;
import com.istarindia.android.pojo.StudentRankPOJO;
import com.istarindia.android.utility.AppUserRankUtility;
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
		
		return null;
	}
	
	
	public CoursePOJO getCourseOfUser(int istarUserId, int courseId){
		CoursePOJO coursePOJO = null;
		StudentPlaylistServices studentPlaylistServices = new StudentPlaylistServices();
		AppUserRankUtility appUserRankUtility = new AppUserRankUtility();
		Course course = getCourse(courseId);
		if(course!=null){
			coursePOJO = new CoursePOJO();
			
			List<Integer> lessonsForUser = studentPlaylistServices.getLessonsOfUserForCourse(istarUserId, courseId);
			
			coursePOJO.setId(course.getId());
			coursePOJO.setCategory(course.getCategory());
			coursePOJO.setDescription(course.getCourseDescription());
			coursePOJO.setImageURL(course.getImage_url());
			coursePOJO.setName(course.getCourseName());				
			coursePOJO.setTotalPoints(getTotalPointsOfCourseForUser(istarUserId, courseId));
			coursePOJO.setProgress(getProgressOfUserForCourse(istarUserId, courseId));
			StudentRankPOJO studentRankPOJO = appUserRankUtility.getStudentRankPOJOForCourseOfAUser(istarUserId, coursePOJO.getId());
			
			if(studentRankPOJO!=null){
				coursePOJO.setUserPoints(studentRankPOJO.getPoints()*1.0);
				coursePOJO.setRank(studentRankPOJO.getBatchRank());
			}
			
			List<ModulePOJO> allModules = new ArrayList<ModulePOJO>();
			for(Module module : course.getModules()){
				ModulePOJO modulePOJO = new ModulePOJO();
				
				modulePOJO.setId(module.getId());
				modulePOJO.setName(module.getModuleName());
				modulePOJO.setImageURL(module.getImage_url());
				modulePOJO.setDescription(module.getModule_description());
				modulePOJO.setOrderId(module.getOrderId());
				
				List<CmsessionPOJO> allLessons = new ArrayList<CmsessionPOJO>();
				for(Cmsession cmsession : getCmsessionsOfCourse(module.getId())){
					
					for(Lesson lesson : getLessonsOfCmsession(cmsession.getId())){						
						StudentPlaylist studentPlaylist = studentPlaylistServices.getStudentPlaylistOfUserForLessonOfCourse(istarUserId, course.getId(), lesson.getId());						
						if(lessonsForUser.contains(lesson.getId()) && studentPlaylist!=null){
							CmsessionPOJO cmsessionPOJO = new CmsessionPOJO();
							LessonPOJO lessonPOJO = new LessonPOJO();
							lessonPOJO.setId(lesson.getId());
							lessonPOJO.setTitle(lesson.getTitle());
							lessonPOJO.setDescription(lesson.getDescription());
							lessonPOJO.setDuration(lesson.getDuration());
							lessonPOJO.setPlaylistId(studentPlaylist.getId());
							lessonPOJO.setStatus(studentPlaylist.getStatus());
							lessonPOJO.setSubject(lesson.getSubject());
							lessonPOJO.setType(lesson.getType());
							lessonPOJO.setOrderId(lesson.getOrderId());
							
							cmsessionPOJO.setId(lesson.getId());
							cmsessionPOJO.setType("LESSON_"+lesson.getType());
							cmsessionPOJO.setItem(lessonPOJO);
							cmsessionPOJO.setOrderId(lesson.getOrderId());
							cmsessionPOJO.setStatus(studentPlaylist.getStatus());
							allLessons.add(cmsessionPOJO);
						}
					}					
					//assessments
				}
				modulePOJO.setLessons(allLessons);	
				modulePOJO.sortLessonsAndAssignStatus();
				List<String> moduleSkillObjectives = new ArrayList<String>();
				
				for(SkillObjective skillObjective : module.getSkillObjectives()){
					moduleSkillObjectives.add(skillObjective.getName());
				}
				modulePOJO.setSkillObjectives(moduleSkillObjectives);
				allModules.add(modulePOJO);
			}
			coursePOJO.setModules(allModules);
		}
		return coursePOJO;
	}
	
	public List<SkillReportPOJO> getSkillsReportForCourseOfUser(int istarUserId, int courseId){
		
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
					
					List<SkillReportPOJO> allCmsessionSkills = new ArrayList<SkillReportPOJO>();
					
					for(Cmsession cmsession : module.getCmsessions()){
						for(SkillObjective cmsessionSkillObjective : cmsession.getSkillObjectives()){
							SkillReportPOJO cmsessionSkillReportPOJO = new SkillReportPOJO();
							
							cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
							cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
							cmsessionSkillReportPOJO.setTotalPoints(getMaxPointsOfCmsessionSkill(cmsessionSkillObjective.getId()));
							
							HashMap<String, Object> map = getPointsAndCoinsOfUserForCmsessionSkillOfCourse(istarUserId, cmsessionSkillObjective.getId(), course.getId());
					          
					          if(map.containsKey("points")){
					            cmsessionSkillReportPOJO.setUserPoints((Double) map.get("points"));
					          }else{
					            cmsessionSkillReportPOJO.setUserPoints(0.0);
					          }					          
							allCmsessionSkills.add(cmsessionSkillReportPOJO);
						}
					}		
					moduleSkillReportPOJO.setSkills(allCmsessionSkills);
					moduleSkillReportPOJO.calculateUserPoints();
					moduleSkillReportPOJO.calculateTotalPoints();
					moduleSkillReportPOJO.calculatePercentage();
				}
				allSkillsReport.add(moduleSkillReportPOJO);
			}
		}
		return allSkillsReport;
	}
	
	public Double getTotalPointsOfCourseForUser(int istarUserId, int courseId){
		
		StudentPlaylistServices studentPlaylistServices = new StudentPlaylistServices();
		List<Integer> lessonsForUser = studentPlaylistServices.getLessonsOfUserForCourse(istarUserId, courseId);
		Integer benchmark= 0;
				try{
					Properties properties = new Properties();
					String propertyFileName = "app.properties";
					InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
						if (inputStream != null) {
							properties.load(inputStream);
							String pointsBenchmark = properties.getProperty("pointsBenchmark");				
							benchmark = Integer.parseInt(pointsBenchmark);	
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
		
		Double totalPoints = getMaxPointsOfCourseFromAssessment(courseId) + lessonsForUser.size()*benchmark;
		return totalPoints;
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> getPointsAndCoinsOfUserForCmsessionSkillOfCourse(int istarUserId, int cmsessionSkillObjectiveId, int courseId){
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		String sql = "select COALESCE(sum(points),0) as points, COALESCE(cast(sum(coins) as integer),0) as coins from user_gamification "
				+ "where skill_objective= :cmsessionSkillObjectiveId and istar_user= :istarUserId and item_id in "
				+ "(select id from assessment where course_id= :courseId)";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId", istarUserId);
		query.setParameter("cmsessionSkillObjectiveId", cmsessionSkillObjectiveId);
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
/*	
	public Double calculateMaxPointsForCmsession(int cmsessionSkillObjectiveId){
		
		Double maxPoints = 0.0;
		
		Integer difficultyLevelSum = getDifficultyLevelSumOfCmsessionSkill(cmsessionSkillObjectiveId);
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
	}*/
	
	
	@SuppressWarnings("unchecked")
	public Double getMaxPointsOfCmsessionSkill(int cmsessionSkillObjectiveId){
		
		Double maxPoints = 0.0;
		
		String sql = "select COALESCE(cast(sum(question.difficulty_level) as integer),0) as difficulty_level, "
				+ "COALESCE(cast(count(distinct lesson_skill_objective.lessonid) as integer),0) as number_of_lessons from "
				+ "lesson_skill_objective,question_skill_objective,question,skill_objective where "
				+ "lesson_skill_objective.learning_objectiveid=question_skill_objective.learning_objectiveid and "
				+ "question_skill_objective.questionid=question.id and question_skill_objective.learning_objectiveid=skill_objective.id and "
				+ "skill_objective.parent_skill= :cmsessionSkillObjectiveId";
		
		System.out.println(sql);
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("cmsessionSkillObjectiveId", cmsessionSkillObjectiveId);

		List<Object[]> result = query.list();
		
		if(result.size()>0){
			Integer difficultyLevelSum = (Integer) result.get(0)[0];		
			Integer numberOfLessons = (Integer) result.get(0)[1];
			
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
		}		
		return maxPoints;
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
	public List<Cmsession> getCmsessionsOfCourse(int moduleId){
		
		List<Cmsession> allCmsessions = new ArrayList<Cmsession>();
		
		String sql = "select cmsession_id from cmsession_module where module_id= :moduleId";
	
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("moduleId", moduleId);
		
		List<Integer> results = query.list();
		
		for(Integer cmsessionId : results){
			allCmsessions.add(getCmsession(cmsessionId));
		}
		System.out.println("allCmsessions" + allCmsessions.size());
		return allCmsessions;
	}
	
	@SuppressWarnings("unchecked")
	public List<Lesson> getLessonsOfCmsession(int cmsessionId){
		
		List<Lesson> allLessons = new ArrayList<Lesson>();
		
		String sql = "select lesson_id from lesson_cmsession where cmsession_id= :cmsessionId";
	
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("cmsessionId", cmsessionId);
		
		List<Integer> results = query.list();
		
		for(Integer lessonId : results){
			allLessons.add(getLesson(lessonId));
		}
		System.out.println("allLessons" + allLessons.size());
		return allLessons;
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

	public Double getMaxPointsOfCourseFromAssessment(Integer courseId){
		
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