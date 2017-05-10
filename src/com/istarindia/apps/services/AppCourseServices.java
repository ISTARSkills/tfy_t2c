package com.istarindia.apps.services;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
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
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.LessonDAO;
import com.viksitpro.core.dao.entities.Module;
import com.viksitpro.core.dao.entities.ModuleDAO;
import com.viksitpro.core.dao.entities.SkillObjective;
import com.viksitpro.core.dao.entities.StudentPlaylist;

public class AppCourseServices {
	
	public CoursePOJO getCourseOfUser(int istarUserId, int courseId){
		CoursePOJO coursePOJO = null;
		StudentPlaylistServices studentPlaylistServices = new StudentPlaylistServices();
		AppUserRankUtility appUserRankUtility = new AppUserRankUtility();
		Course course = getCourse(courseId);

		List<StudentPlaylist> allStudentPlaylist = studentPlaylistServices.getStudentPlaylistOfUserForCourse(istarUserId, courseId);
		if(course!=null && allStudentPlaylist.size() > 0){
			
			coursePOJO = new CoursePOJO();

			coursePOJO.setId(course.getId());
			coursePOJO.setCategory(course.getCategory());
			coursePOJO.setDescription(course.getCourseDescription());
			coursePOJO.setImageURL(course.getImage_url());
			coursePOJO.setName(course.getCourseName());				
			coursePOJO.setTotalPoints(getTotalPointsOfCourseForUser(istarUserId, courseId, allStudentPlaylist.size()));

			coursePOJO.setProgress(getProgressOfUserForCourse(istarUserId, courseId));
			StudentRankPOJO studentRankPOJO = appUserRankUtility.getStudentRankPOJOForCourseOfAUser(istarUserId, coursePOJO.getId());
			if(studentRankPOJO!=null){
				coursePOJO.setUserPoints(studentRankPOJO.getPoints()*1.0);
				coursePOJO.setRank(studentRankPOJO.getBatchRank());
			}
			
			int moduleOrderId = 0;
			
			Set<Integer> cmsessionIds = new HashSet<Integer>();
			
			for(StudentPlaylist studentPlaylist : allStudentPlaylist){
				ModulePOJO modulePOJO = null;
				Module module = studentPlaylist.getModule();
				Cmsession cmsession = studentPlaylist.getCmsession();
				Lesson lesson = studentPlaylist.getLesson();
				
				for(ModulePOJO tempModulePOJO : coursePOJO.getModules()){
					if(tempModulePOJO.getId()== module.getId()){
						modulePOJO = tempModulePOJO;
					}
				}
				
				if(modulePOJO==null){
					modulePOJO = new ModulePOJO();
					modulePOJO.setId(module.getId());
					modulePOJO.setName(module.getModuleName());
					modulePOJO.setImageURL(module.getImage_url());
					modulePOJO.setDescription(module.getModule_description());
					modulePOJO.setOrderId(++moduleOrderId);
					
					if(!cmsessionIds.contains(cmsession.getId())){
						System.out.println("Adding CMSession Skill");
						Set<String> allSkillObjectivesOfModule = new HashSet<String>();
						for (SkillObjective skillObjective : module.getSkillObjectives()) {
							allSkillObjectivesOfModule.add(skillObjective.getName());
						}
						cmsessionIds.add(cmsession.getId());
						modulePOJO.getSkillObjectives().addAll(allSkillObjectivesOfModule);
					}
	
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
					lessonPOJO.setOrderId(studentPlaylist.getId());
					
					cmsessionPOJO.setId(lesson.getId());
					cmsessionPOJO.setType("LESSON_"+lesson.getType());
					cmsessionPOJO.setLesson(lessonPOJO);
					cmsessionPOJO.setOrderId(studentPlaylist.getId());
					cmsessionPOJO.setStatus(studentPlaylist.getStatus());
					
					modulePOJO.getLessons().add(cmsessionPOJO);
					coursePOJO.getModules().add(modulePOJO);
				}else{
					
					if(!cmsessionIds.contains(cmsession.getId())){
						System.out.println("Adding CMSession Skill");
						Set<String> allSkillObjectivesOfModule = new HashSet<String>();
						for (SkillObjective skillObjective : module.getSkillObjectives()) {
							allSkillObjectivesOfModule.add(skillObjective.getName());
						}
						cmsessionIds.add(cmsession.getId());
						modulePOJO.getSkillObjectives().addAll(allSkillObjectivesOfModule);
					}
					
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
					lessonPOJO.setOrderId(studentPlaylist.getId());
					
					cmsessionPOJO.setId(lesson.getId());
					cmsessionPOJO.setType("LESSON_"+lesson.getType());
					cmsessionPOJO.setLesson(lessonPOJO);
					cmsessionPOJO.setOrderId(studentPlaylist.getId());
					cmsessionPOJO.setStatus(studentPlaylist.getStatus());
					
					modulePOJO.getLessons().add(cmsessionPOJO);
				}
				modulePOJO.sortLessonsAndAssignStatus();
			}
		}
		coursePOJO.sortModulesAndAssignStatus();
		return coursePOJO;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<SkillReportPOJO> getSkillsReportForCourseOfUser(int istarUserId, int courseId){
		
		List<SkillReportPOJO> skillsReport = new ArrayList<SkillReportPOJO>();
		
		String sql = "select skill_objective,cmsession_id,module_id,course_id, sum(points) as user_points, sum(max_points) as total_points from  (select user_gamification.skill_objective, user_gamification.cmsession_id, user_gamification.module_id, user_gamification.course_id, user_gamification.item_id, cast(user_gamification.points as numeric), cast(assessment_benchmark.max_points as numeric), count(student_playlist.lesson_id)  from user_gamification inner join student_playlist on user_gamification.cmsession_id=student_playlist.cmsession_id and user_gamification.module_id=student_playlist.module_id and user_gamification.course_id=student_playlist.course_id inner join assessment_benchmark on user_gamification.item_id=assessment_benchmark.assessment_id and user_gamification.item_type='ASSESSMENT' and user_gamification.skill_objective=assessment_benchmark.skill_objective_id where timestamp in (select max(timestamp) from user_gamification where istar_user="+istarUserId+" and course_id="+courseId+" and item_type='ASSESSMENT' group by item_id) group by user_gamification.skill_objective, user_gamification.cmsession_id, user_gamification.module_id, user_gamification.course_id, cast(user_gamification.points as numeric),cast(assessment_benchmark.max_points as numeric), user_gamification.item_id) as temptable group by skill_objective,cmsession_id,module_id,course_id";
		
		System.out.println("Course Skill Report->"+sql);
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);		
		List<Object[]> result = query.list();
		
		String lessonsStatusSQL = "select cmsession_id,cast(count(case when status='COMPLETE' then 1 end) as integer) as completed_lessons, cast(count(*) as integer) as total_lessons from student_playlist where student_id="+istarUserId+" and course_id="+courseId+" group by cmsession_id";
		
		SQLQuery sessionQuery = session.createSQLQuery(lessonsStatusSQL);		
		List<Object[]> sessionStatusResult = sessionQuery.list();
		
		if(result.size()>0 && sessionStatusResult.size()>0){
			HashMap<Integer, Module> modulesOfAssessment = new HashMap<Integer, Module>();
			
			HashMap<Integer, HashMap<String, Integer>> courseStatus = new HashMap<Integer, HashMap<String, Integer>>();
			Double benchmark = getBenchmark();
			
			System.out.println("benchmark->" + benchmark);
			for(Object[] sessionRow: sessionStatusResult){
				Integer cmsessionId = (Integer) sessionRow[0];
				Integer completedLessons = (Integer) sessionRow[1];
				Integer totalLessons = (Integer) sessionRow[2];
				
				HashMap<String, Integer> sessionStatus = new HashMap<String, Integer>();
				sessionStatus.put("completedLessons", completedLessons);
				sessionStatus.put("totalLessons", totalLessons);
				
				courseStatus.put(cmsessionId, sessionStatus);
			}
			
			AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
			
			for(Object[] row : result){
				Integer cmsessionSkillObjectiveId = (Integer) row[0];
				Integer cmsessionId = (Integer) row[1];
				Integer moduleId = (Integer) row[2];				
				Double userPoints = ((BigDecimal) row[4]).doubleValue();
				Double totalPoints = ((BigDecimal) row[5]).doubleValue();

				SkillObjective cmsessionSkillObjective = appAssessmentServices.getSkillObjective(cmsessionSkillObjectiveId);
				SkillReportPOJO moduleSkillReportPOJO = null;
				SkillReportPOJO cmsessionSkillReportPOJO = null;

				if (modulesOfAssessment.containsKey(moduleId)) {
					for (SkillReportPOJO tempModuleSkillReport : skillsReport) {
						if (tempModuleSkillReport.getId() == moduleId) {
							moduleSkillReportPOJO = tempModuleSkillReport;
							break;
						}
					}

					cmsessionSkillReportPOJO = new SkillReportPOJO();
					cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
					cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
					cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
					
					
					if(courseStatus.containsKey(cmsessionId)){
						cmsessionSkillReportPOJO.setTotalPoints(totalPoints + (benchmark*courseStatus.get(cmsessionId).get("totalLessons")));
						cmsessionSkillReportPOJO.setUserPoints(userPoints + (benchmark*courseStatus.get(cmsessionId).get("completedLessons")));						
					}else{
						cmsessionSkillReportPOJO.setTotalPoints(totalPoints);
						cmsessionSkillReportPOJO.setUserPoints(userPoints);
					}
					
					moduleSkillReportPOJO.getSkills().add(cmsessionSkillReportPOJO);
					moduleSkillReportPOJO.calculateTotalPoints();
					moduleSkillReportPOJO.calculateUserPoints();
					moduleSkillReportPOJO.calculatePercentage();
					moduleSkillReportPOJO.generateMessage();
					
				} else {
					Module module = getModule(moduleId);
					if (module != null) {
						modulesOfAssessment.put(moduleId, module);

						moduleSkillReportPOJO = new SkillReportPOJO();
						moduleSkillReportPOJO.setId(module.getId());
						moduleSkillReportPOJO.setName(module.getModuleName());
						moduleSkillReportPOJO.setDescription(module.getModule_description());
						moduleSkillReportPOJO.setImageURL(module.getImage_url());

						List<SkillReportPOJO> cmsessionSkillsReport = new ArrayList<SkillReportPOJO>();

						cmsessionSkillReportPOJO = new SkillReportPOJO();
						cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
						cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
						cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());

						if(courseStatus.containsKey(cmsessionId)){
							cmsessionSkillReportPOJO.setTotalPoints(totalPoints + (benchmark*courseStatus.get(cmsessionId).get("totalLessons")));
							cmsessionSkillReportPOJO.setUserPoints(userPoints + (benchmark*courseStatus.get(cmsessionId).get("completedLessons")));						
						}else{
							cmsessionSkillReportPOJO.setTotalPoints(totalPoints);
							cmsessionSkillReportPOJO.setUserPoints(userPoints);
						}

						cmsessionSkillsReport.add(cmsessionSkillReportPOJO);
						moduleSkillReportPOJO.setSkills(cmsessionSkillsReport);
						
						moduleSkillReportPOJO.calculateTotalPoints();
						moduleSkillReportPOJO.calculateUserPoints();
						moduleSkillReportPOJO.calculatePercentage();
						moduleSkillReportPOJO.generateMessage();
						skillsReport.add(moduleSkillReportPOJO);
					}else{
						System.out.println("Module is null with id->"+moduleId);
					}
				}
			}
		}		
		return skillsReport;
	}
	
	/*public List<SkillReportPOJO> getSkillReportTreeForCourseOfUser(int istarUserId, int courseId){
		
		List<SkillReportPOJO> skillsReport = new ArrayList<SkillReportPOJO>();
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		String lessonsStatusSQL = "select course_id, module_id, cmsession_id,cast(count(case when status='COMPLETE' then 1 end) as integer) as completed_lessons, cast(count(*) as integer) as total_lessons from student_playlist where student_id="+istarUserId+" and course_id="+courseId+" group by course_id, module_id,cmsession_id";
		
		SQLQuery sessionQuery = session.createSQLQuery(lessonsStatusSQL);		
		List<Object[]> sessionStatusResult = sessionQuery.list();
		
		
		if(sessionStatusResult.size()>0){
			HashMap<Integer, Module> modulesOfAssessment = new HashMap<Integer, Module>();
			AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
			for(Object[] sessionRow: sessionStatusResult){
				//Integer courseId = (Integer) sessionRow[0];
				Integer moduleId = (Integer) sessionRow[1];
				Integer cmsessionId = (Integer) sessionRow[2];
				Integer completedLessons = (Integer) sessionRow[3];
				Integer totalLessons = (Integer) sessionRow[4];
				
				SkillObjective cmsessionSkillObjective = appAssessmentServices.getSkillObjective(cmsessionSkillObjectiveId);
				SkillReportPOJO moduleSkillReportPOJO = null;
				SkillReportPOJO cmsessionSkillReportPOJO = null;
				
				if (modulesOfAssessment.containsKey(moduleId)) {
					for (SkillReportPOJO tempModuleSkillReport : skillsReport) {
						if (tempModuleSkillReport.getId() == moduleId) {
							moduleSkillReportPOJO = tempModuleSkillReport;
							break;
						}
					}

					cmsessionSkillReportPOJO = new SkillReportPOJO();
					cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
					cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
					cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
					
					
					if(courseStatus.containsKey(cmsessionId)){
						cmsessionSkillReportPOJO.setTotalPoints(totalPoints + (benchmark*courseStatus.get(cmsessionId).get("totalLessons")));
						cmsessionSkillReportPOJO.setUserPoints(userPoints + (benchmark*courseStatus.get(cmsessionId).get("completedLessons")));						
					}else{
						cmsessionSkillReportPOJO.setTotalPoints(totalPoints);
						cmsessionSkillReportPOJO.setUserPoints(userPoints);
					}
					
					moduleSkillReportPOJO.getSkills().add(cmsessionSkillReportPOJO);
					moduleSkillReportPOJO.calculateTotalPoints();
					moduleSkillReportPOJO.calculateUserPoints();
					moduleSkillReportPOJO.calculatePercentage();
					moduleSkillReportPOJO.generateMessage();
					
				} else {
					Module module = getModule(moduleId);
					if (module != null) {
						modulesOfAssessment.put(moduleId, module);

						moduleSkillReportPOJO = new SkillReportPOJO();
						moduleSkillReportPOJO.setId(module.getId());
						moduleSkillReportPOJO.setName(module.getModuleName());
						moduleSkillReportPOJO.setDescription(module.getModule_description());
						moduleSkillReportPOJO.setImageURL(module.getImage_url());

						List<SkillReportPOJO> cmsessionSkillsReport = new ArrayList<SkillReportPOJO>();

						cmsessionSkillReportPOJO = new SkillReportPOJO();
						cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
						cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
						cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());

						if(courseStatus.containsKey(cmsessionId)){
							cmsessionSkillReportPOJO.setTotalPoints(totalPoints + (benchmark*courseStatus.get(cmsessionId).get("totalLessons")));
							cmsessionSkillReportPOJO.setUserPoints(userPoints + (benchmark*courseStatus.get(cmsessionId).get("completedLessons")));						
						}else{
							cmsessionSkillReportPOJO.setTotalPoints(totalPoints);
							cmsessionSkillReportPOJO.setUserPoints(userPoints);
						}

						cmsessionSkillsReport.add(cmsessionSkillReportPOJO);
						moduleSkillReportPOJO.setSkills(cmsessionSkillsReport);
						
						moduleSkillReportPOJO.calculateTotalPoints();
						moduleSkillReportPOJO.calculateUserPoints();
						moduleSkillReportPOJO.calculatePercentage();
						moduleSkillReportPOJO.generateMessage();
						skillsReport.add(moduleSkillReportPOJO);
					}else{
						System.out.println("Module is null with id->"+moduleId);
					}
				}

			}
		}		
		return null;
	}*/
	
	
/*	public List<SkillReportPOJO> getSkillsReportForCourseOfUser(int istarUserId, int courseId){
		//long previousTime = System.currentTimeMillis();
		//System.err.println("500000000->" + "Time->"+(System.currentTimeMillis()-previousTime));
		List<SkillReportPOJO> allSkillsReport = new ArrayList<SkillReportPOJO>();
		HashMap<Integer, HashMap<String, Object>> skillsMap = getPointsAndCoinsOfCmsessionSkillsOfCourseForUser(istarUserId, courseId);
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
							//System.err.println("5->" + "Time->"+(System.currentTimeMillis()-previousTime));
							cmsessionSkillReportPOJO.setTotalPoints(getMaxPointsOfCmsessionSkill(cmsessionSkillObjective.getId()));
							//System.err.println("6->" + "Time->"+(System.currentTimeMillis()-previousTime));
					          if(skillsMap.containsKey(cmsessionSkillObjective.getId())){
					            cmsessionSkillReportPOJO.setUserPoints((Double) skillsMap.get(cmsessionSkillObjective.getId()).get("points"));
					          }else{
					        	  System.out.println("CMSESSION SKILL NOT PRESENT->" + cmsessionSkillObjective.getId());
					            cmsessionSkillReportPOJO.setUserPoints(0.0);
					          }					          
							allCmsessionSkills.add(cmsessionSkillReportPOJO);
						}
					}		
					moduleSkillReportPOJO.setSkills(allCmsessionSkills);
					moduleSkillReportPOJO.calculateUserPoints();
					moduleSkillReportPOJO.calculateTotalPoints();
					moduleSkillReportPOJO.calculatePercentage();
					moduleSkillReportPOJO.generateMessage();
				}
				allSkillsReport.add(moduleSkillReportPOJO);
			}
		}
		return allSkillsReport;
	}*/
	
	public Double getTotalPointsOfCourseForUser(int istarUserId, int courseId, int numberOfLessons){

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
		
		Double totalPoints = getMaxPointsOfCourseFromAssessment(courseId) + numberOfLessons*benchmark;
		return totalPoints;
	}
	
	
	public Double getBenchmark(){
		Double benchmark= 1.0;
		try{
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
				if (inputStream != null) {
					properties.load(inputStream);
					String pointsBenchmark = properties.getProperty("pointsBenchmark");				
					benchmark = Double.parseDouble(pointsBenchmark);	
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		return benchmark;
	}
	
	
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> getPointsAndCoinsOfUserForCmsessionSkillOfCourse(int istarUserId, int cmsessionSkillObjectiveId, int courseId){
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		
/*		String sql = "select COALESCE(sum(points),0) as points, COALESCE(cast(sum(coins) as integer),0) as coins from user_gamification "
				+ "where skill_objective= :cmsessionSkillObjectiveId and istar_user= :istarUserId and item_id in "
				+ "(select id from assessment where course_id= :courseId)";*/

		String sql = "select COALESCE(sum(points),0) as points, COALESCE(cast(sum(coins) as integer),0) as coins from user_gamification "
				+ "where skill_objective= :cmsessionSkillObjectiveId and istar_user= :istarUserId and course_id= :courseId";
		
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
	
	@SuppressWarnings("unchecked")
	public HashMap<Integer, HashMap<String, Object>> getPointsAndCoinsOfCmsessionSkillsOfCourseForUser(int istarUserId, int courseId){
		
		HashMap<Integer, HashMap<String, Object>> skillsMap = new HashMap<Integer, HashMap<String, Object>>();

/*		String sql = "select COALESCE(sum(points),0) as points, COALESCE(cast(sum(coins) as integer),0) as coins, skill_objective from user_gamification where istar_user= :istarUserId and item_id in (select id from assessment where course_id= :courseId) group by skill_objective";
*/
		String sql = "select COALESCE(sum(points),0) as points, COALESCE(cast(sum(coins) as integer),0) as coins, skill_objective from user_gamification "
				+ "where istar_user= :istarUserId and course_id= :courseId group by skill_objective";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId", istarUserId);
		query.setParameter("courseId", courseId);
		
		List<Object[]> result = query.list();
		
		for(Object[] obj : result){
			HashMap<String, Object> map = new HashMap<String, Object>();

			map.put("points", (Double) obj[0]);
			map.put("coins", (Integer) obj[1]);
			
			skillsMap.put((Integer)obj[2], map);
		}
		return skillsMap;
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
	public List<Cmsession> getCmsessionsOfModule(int moduleId){
		
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