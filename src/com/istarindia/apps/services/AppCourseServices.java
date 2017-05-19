package com.istarindia.apps.services;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.istarindia.android.pojo.ConcreteItemPOJO;
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
import com.viksitpro.core.utilities.DBUTILS;

public class AppCourseServices {
	
	public CoursePOJO getCourseOfUser(int istarUserId, int courseId){
		String mediaUrlPath ="";
		try{
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
				if (inputStream != null) {
					properties.load(inputStream);
					mediaUrlPath =  properties.getProperty("media_url_path");
					System.out.println("media_url_path"+mediaUrlPath);
				}
			} catch (IOException e) {
				e.printStackTrace();
			
		}
		
		CoursePOJO coursePOJO = null;
		StudentPlaylistServices studentPlaylistServices = new StudentPlaylistServices();
		AppUserRankUtility appUserRankUtility = new AppUserRankUtility();
		Course course = getCourse(courseId);
		DBUTILS util = new DBUTILS();
		List<StudentPlaylist> allStudentPlaylist = studentPlaylistServices.getStudentPlaylistOfUserForCourse(istarUserId, courseId);
		if(course!=null && allStudentPlaylist.size() > 0){
			
			coursePOJO = new CoursePOJO();

			coursePOJO.setId(course.getId());
			coursePOJO.setCategory(course.getCategory());
			coursePOJO.setDescription(course.getCourseDescription());
			coursePOJO.setImageURL(mediaUrlPath+course.getImage_url());
			coursePOJO.setName(course.getCourseName());				
			
			coursePOJO.setProgress(getProgressOfUserForCourse(istarUserId, courseId));
			
			String getRankPointsForUser="select * from (select istar_user, user_points, total_points,perc ,cast (row_number() over() as integer) as user_rank from (select istar_user, user_points, total_points, cast ((user_points*100)/total_points as integer) as perc from (select T1.istar_user, sum(T1.points) as user_points, sum(T1.max_points) as total_points from ( WITH summary AS (     SELECT p.istar_user, 					p.skill_objective,	            p.points, 						p.max_points,            ROW_NUMBER() OVER(PARTITION BY p.istar_user, p.skill_objective     ORDER BY p.timestamp DESC) AS rk       FROM  user_gamification p where item_type in ('QUESTION', 'LESSON') and batch_group_id=(select batch_group.id from batch_students, batch_group  where batch_students.batch_group_id = batch_group.id and batch_students.student_id = "+istarUserId+" and batch_group.is_primary ='t' limit 1)  and course_id ="+courseId+"			 ) SELECT s.*   FROM summary s  WHERE s.rk = 1 )T1 group by istar_user having (sum(T1.max_points) >0) )T2 order by perc desc, total_points desc , total_points desc )T3 )T4 where istar_user = "+istarUserId+"";
			List<HashMap<String, Object>> rankPointsData = util.executeQuery(getRankPointsForUser);
			int rank = 0;
			double userPoints = 0;
			double totalPoints = 0;
			if(rankPointsData.size()>0)
			{
				rank = (int)rankPointsData.get(0).get("user_rank");
				userPoints = (double) rankPointsData.get(0).get("user_points");
				totalPoints= (double) rankPointsData.get(0).get("total_points");
			}
			coursePOJO.setUserPoints(userPoints);
			coursePOJO.setRank(rank);
			coursePOJO.setTotalPoints(totalPoints);

		
			
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
					modulePOJO.setImageURL(mediaUrlPath+module.getImage_url());
					modulePOJO.setDescription(module.getModule_description());
					modulePOJO.setOrderId(++moduleOrderId);
					
					if(!cmsessionIds.contains(cmsession.getId())){
						System.out.println("Adding CMSession Skill");
						Set<String> allSkillObjectivesOfModule = new HashSet<String>();
						for (SkillObjective skillObjective : cmsession.getSkillObjectives()) {
							allSkillObjectivesOfModule.add(skillObjective.getName());
						}
						cmsessionIds.add(cmsession.getId());
						modulePOJO.getSkillObjectives().addAll(allSkillObjectivesOfModule);
					}
	
					ConcreteItemPOJO ConcreteItemPOJO = new ConcreteItemPOJO();
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
					
					ConcreteItemPOJO.setId(lesson.getId());
					ConcreteItemPOJO.setType("LESSON_"+lesson.getType());
					ConcreteItemPOJO.setLesson(lessonPOJO);
					ConcreteItemPOJO.setOrderId(studentPlaylist.getId());
					ConcreteItemPOJO.setStatus(studentPlaylist.getStatus());
					ConcreteItemPOJO.setTaskId(studentPlaylist.getTaskId());
					modulePOJO.getLessons().add(ConcreteItemPOJO);
					coursePOJO.getModules().add(modulePOJO);
				}else{
					
					if(!cmsessionIds.contains(cmsession.getId())){
						System.out.println("Adding CMSession Skill");
						Set<String> allSkillObjectivesOfModule = new HashSet<String>();
						for (SkillObjective skillObjective : cmsession.getSkillObjectives()) {
							allSkillObjectivesOfModule.add(skillObjective.getName());
						}
						cmsessionIds.add(cmsession.getId());
						modulePOJO.getSkillObjectives().addAll(allSkillObjectivesOfModule);
					}
					
					ConcreteItemPOJO ConcreteItemPOJO = new ConcreteItemPOJO();
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
					
					ConcreteItemPOJO.setId(lesson.getId());
					ConcreteItemPOJO.setType("LESSON_"+lesson.getType());
					ConcreteItemPOJO.setLesson(lessonPOJO);
					ConcreteItemPOJO.setOrderId(studentPlaylist.getId());
					ConcreteItemPOJO.setStatus(studentPlaylist.getStatus());
					ConcreteItemPOJO.setTaskId(studentPlaylist.getTaskId());
					modulePOJO.getLessons().add(ConcreteItemPOJO);
				}
				modulePOJO.sortLessonsAndAssignStatus();
			}
			coursePOJO.sortModulesAndAssignStatus();
		}		
		return coursePOJO;
	}
	
	/*public List<SkillReportPOJO> getSkillsReportForCourseOfUser(int istarUserId, int courseId){
		
		List<SkillReportPOJO> skillsReport = new ArrayList<SkillReportPOJO>();
		Double benchmark = getBenchmark();
		String benchmarkPointsSQL = "select skill_objective_id,  cast(sum(total) as numeric) as total from( (select skill_objective_id, cast(sum(max_points) as numeric) as total from assessment_benchmark where assessment_benchmark.assessment_id in (select id from assessment where course_id="+courseId+") group by skill_objective_id order by skill_objective_id) union all (select skill_objective.parent_skill as skill_objective_id, count(student_playlist.lesson_id)*"+benchmark+" as total  from student_playlist inner join lesson_skill_objective on student_playlist.lesson_id=lesson_skill_objective.lessonid inner join skill_objective on lesson_skill_objective.learning_objectiveid=skill_objective.id where student_playlist.student_id="+istarUserId+" and lesson_skill_objective.context_id="+courseId+" and student_playlist.course_id="+courseId+" group by skill_objective.parent_skill order by skill_objective.parent_skill) ) as dt group by skill_objective_id order by skill_objective_id"; 
		
		//System.out.println("benchmarkPointsSQL->"+benchmarkPointsSQL);
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery benchmarkQuery = session.createSQLQuery(benchmarkPointsSQL);		
		List<Object[]> benchmarkResult = benchmarkQuery.list();
		
		HashMap<Integer, Double> totalPointsBenchmark = new HashMap<Integer, Double>();
		
		for(Object[] row : benchmarkResult){
			Integer skillObjectId = (Integer) row[0];
			Double totalPoints = ((BigDecimal) row[1]).doubleValue();
			
			totalPointsBenchmark.put(skillObjectId, totalPoints);
		}
		
		String sql = "select skill_objective, module_id, course_id, cast(sum(points) as numeric) as points,cast(sum(coins) as numeric) as coins   from (select skill_objective, points, coins, item_id, item_type, cmsession_id, module_id, course_id, count(batch_group_id) from user_gamification where (istar_user, item_id, item_type, timestamp) in (select istar_user, item_id, item_type, max(timestamp) from user_gamification where istar_user="+istarUserId+" and course_id="+courseId+" group by istar_user, item_id, item_type) group by skill_objective, points, coins, item_id, item_type, cmsession_id, module_id, course_id) as temptable group by skill_objective, module_id, course_id";
		//System.out.println("SQL for user points->"+sql);
		SQLQuery query = session.createSQLQuery(sql);
		List<Object[]> result = query.list();
		AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
		HashMap<Integer, Module> allModules = new HashMap<Integer, Module>();

		for(Object[] row: result){
			Integer cmsessionSkillObjectiveId = (Integer) row[0];
			Integer moduleId = (Integer) row[1];
			//Integer courseId = (Integer) row[2];
			Double points = ((BigDecimal) row[3]).doubleValue();
			//Double coins = ((BigDecimal) row[4]).doubleValue();
			
			SkillObjective cmsessionSkillObjective = appAssessmentServices.getSkillObjective(cmsessionSkillObjectiveId);
			SkillReportPOJO moduleSkillReportPOJO = null;
			SkillReportPOJO cmsessionSkillReportPOJO = null;
			
			if(totalPointsBenchmark.containsKey(cmsessionSkillObjectiveId)){
			if(allModules.containsKey(moduleId)){
				for (SkillReportPOJO tempModuleSkillReport : skillsReport) {
					System.out.println(tempModuleSkillReport.getId() +" == "+ moduleId);
					if (tempModuleSkillReport.getId().equals(moduleId)) {
						moduleSkillReportPOJO = tempModuleSkillReport;
						break;
					}
				}
				
				cmsessionSkillReportPOJO = new SkillReportPOJO();
				cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
				cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
				cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
				
				//System.out.println("benchmark points are----->"+totalPointsBenchmark.get(cmsessionSkillObjectiveId)+"--->"+cmsessionSkillObjectiveId);
				cmsessionSkillReportPOJO.setTotalPoints(totalPointsBenchmark.get(cmsessionSkillObjectiveId));
				cmsessionSkillReportPOJO.setUserPoints(points);
				
				//System.out.println("moduleSkillReportPOJO-->"+moduleSkillReportPOJO.getId());
				//System.out.println("moduleSkillReportPOJO.getSkills()-->"+moduleSkillReportPOJO.getSkills().size());
				
				moduleSkillReportPOJO.getSkills().add(cmsessionSkillReportPOJO);
				cmsessionSkillReportPOJO.calculatePercentage();
				moduleSkillReportPOJO.calculateTotalPoints();
				moduleSkillReportPOJO.calculateUserPoints();
				moduleSkillReportPOJO.calculatePercentage();
				moduleSkillReportPOJO.generateMessage();
			}else{
				Module module = getModule(moduleId);
				if (module != null) {
					allModules.put(moduleId, module);

					moduleSkillReportPOJO = new SkillReportPOJO();
					moduleSkillReportPOJO.setId(module.getId());
					moduleSkillReportPOJO.setName(module.getModuleName());
					moduleSkillReportPOJO.setDescription(module.getModule_description());
					moduleSkillReportPOJO.setImageURL(module.getImage_url());

					List<SkillReportPOJO> cmsessionSkillsReports = new ArrayList<SkillReportPOJO>();

						cmsessionSkillReportPOJO = new SkillReportPOJO();
						cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
						cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
						cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
						cmsessionSkillReportPOJO.setTotalPoints(totalPointsBenchmark.get(cmsessionSkillObjectiveId));
						cmsessionSkillReportPOJO.setUserPoints(points);
			
						cmsessionSkillsReports.add(cmsessionSkillReportPOJO);
						moduleSkillReportPOJO.setSkills(cmsessionSkillsReports);
						
						cmsessionSkillReportPOJO.calculatePercentage();
						moduleSkillReportPOJO.calculateTotalPoints();
						moduleSkillReportPOJO.calculateUserPoints();
						moduleSkillReportPOJO.calculatePercentage();
						moduleSkillReportPOJO.generateMessage();
						skillsReport.add(moduleSkillReportPOJO);
				}
			}
			}else{
				System.out.println("No benchmark available for total points");
			}
		}
		return skillsReport;
	}*/
	
	
	@SuppressWarnings("unchecked")
	public List<SkillReportPOJO> getSkillsReportForCourseOfUser(int istarUserId, int courseId){
		
		List<SkillReportPOJO> shellTree = getShellSkillTreeForCourse(courseId);
		for(SkillReportPOJO dd : shellTree)
		{
			System.out.println("in mod shell tree "+dd.getName()+" - "+dd.getId());
			System.out.println("in mod shell tree "+" "+dd.getUserPoints()+" "+dd.getTotalPoints()+" "+dd.getPercentage());
			for(SkillReportPOJO ll: dd.getSkills())
			{
				System.out.println("in cmsession shell tree "+ll.getName()+" - "+ll.getId());
				System.out.println("in cmsession shell tree "+" "+ll.getUserPoints()+" "+ll.getTotalPoints()+" "+ll.getPercentage());
			}
		}
		DBUTILS utils = new DBUTILS();
		
		
		List<SkillReportPOJO> skillReportForCourse= fillShellTreeWithData(shellTree, istarUserId, courseId);
		
		for(SkillReportPOJO dd : skillReportForCourse)
		{
			System.out.println("in filled mod tree "+dd.getName()+" - "+dd.getId());
			System.out.println("in filled mod tree "+" "+dd.getUserPoints()+" "+dd.getTotalPoints()+" "+dd.getPercentage());
			for(SkillReportPOJO ll: dd.getSkills())
			{
				System.out.println("in filled sms tree "+ll.getName()+" - "+ll.getId());
				System.out.println("in filled sms tree "+" "+ll.getUserPoints()+" "+ll.getTotalPoints()+" "+ll.getPercentage());
			}
		}
		/*String sql = "select skill_objective,cmsession_id,module_id,course_id, sum(points) as user_points, sum(max_points) as total_points from  (select user_gamification.skill_objective, user_gamification.cmsession_id, user_gamification.module_id, user_gamification.course_id, user_gamification.item_id, cast(user_gamification.points as numeric), cast(assessment_benchmark.max_points as numeric), count(student_playlist.lesson_id)  from user_gamification inner join student_playlist on user_gamification.cmsession_id=student_playlist.cmsession_id and user_gamification.module_id=student_playlist.module_id and user_gamification.course_id=student_playlist.course_id inner join assessment_benchmark on user_gamification.item_id=assessment_benchmark.assessment_id and user_gamification.item_type='ASSESSMENT' and user_gamification.skill_objective=assessment_benchmark.skill_objective_id where timestamp in (select max(timestamp) from user_gamification where istar_user="+istarUserId+" and course_id="+courseId+" and item_type='ASSESSMENT' group by item_id) group by user_gamification.skill_objective, user_gamification.cmsession_id, user_gamification.module_id, user_gamification.course_id, cast(user_gamification.points as numeric),cast(assessment_benchmark.max_points as numeric), user_gamification.item_id) as temptable group by skill_objective,cmsession_id,module_id,course_id";
		
		System.out.println("Course Skill Report->"+sql);
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);		
		List<Object[]> result = query.list();
		
		String lessonsStatusSQL = "select cmsession_id,cast(count(case when status='COMPLETED' then 1 end) as integer) as completed_lessons, cast(count(*) as integer) as total_lessons from student_playlist where student_id="+istarUserId+" and course_id="+courseId+" group by cmsession_id";
		
		SQLQuery sessionQuery = session.createSQLQuery(lessonsStatusSQL);		
		List<Object[]> sessionStatusResult = sessionQuery.list();
		
		HashMap<Integer, Module> modulesOfAssessment = new HashMap<Integer, Module>();
		HashMap<Integer, SkillObjective> cmsessionSkillObjectiveOfAssessment= new HashMap<Integer, SkillObjective>();
		
		HashMap<Integer, HashMap<String, Integer>> courseStatus = new HashMap<Integer, HashMap<String, Integer>>();
		Double benchmark = getBenchmark();
		System.out.println("benchmark->" + benchmark);
		AppAssessmentServices appAssessmentServices = new AppAssessmentServices();
		if(result.size()>0 && sessionStatusResult.size()>0){
					
			for(Object[] sessionRow: sessionStatusResult){
				Integer cmsessionId = (Integer) sessionRow[0];
				Integer completedLessons = (Integer) sessionRow[1];
				Integer totalLessons = (Integer) sessionRow[2];
				
				HashMap<String, Integer> sessionStatus = new HashMap<String, Integer>();
				sessionStatus.put("completedLessons", completedLessons);
				sessionStatus.put("totalLessons", totalLessons);
				
				courseStatus.put(cmsessionId, sessionStatus);
			}
			
			
			
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

					if(cmsessionSkillObjectiveOfAssessment.containsKey(cmsessionSkillObjectiveId)){
						for(SkillReportPOJO tempCmsessionSkillReport : moduleSkillReportPOJO.getSkills()){
							if(tempCmsessionSkillReport.getId()==cmsessionSkillObjectiveId){
								cmsessionSkillReportPOJO=tempCmsessionSkillReport;
								break;
							}
						}
					}
					
					if(cmsessionSkillReportPOJO==null){
						//System.out.println("New Skill-->"+ cmsessionSkillObjective.getName()+"-->"+cmsessionSkillObjective.getId());
					cmsessionSkillReportPOJO = new SkillReportPOJO();
					cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
					cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
					cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
					cmsessionSkillObjectiveOfAssessment.put(cmsessionSkillObjectiveId, cmsessionSkillObjective);
					
					if(courseStatus.containsKey(cmsessionId)){
						cmsessionSkillReportPOJO.setTotalPoints(totalPoints + (benchmark*courseStatus.get(cmsessionId).get("totalLessons")));
						cmsessionSkillReportPOJO.setUserPoints(userPoints + (benchmark*courseStatus.get(cmsessionId).get("completedLessons")));						
					}else{
						cmsessionSkillReportPOJO.setTotalPoints(totalPoints);
						cmsessionSkillReportPOJO.setUserPoints(userPoints);
					}
					
					moduleSkillReportPOJO.getSkills().add(cmsessionSkillReportPOJO);
					}else{
						//System.out.println("Old Skill-->"+ cmsessionSkillObjective.getName()+"-->"+cmsessionSkillObjective.getId());
						cmsessionSkillReportPOJO.setTotalPoints(cmsessionSkillReportPOJO.getTotalPoints()+totalPoints);
						cmsessionSkillReportPOJO.setUserPoints(cmsessionSkillReportPOJO.getUserPoints()+userPoints);
					}
					cmsessionSkillReportPOJO.calculatePercentage();
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
						cmsessionSkillObjectiveOfAssessment.put(cmsessionSkillObjectiveId, cmsessionSkillObjective);

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
						
						cmsessionSkillReportPOJO.calculatePercentage();
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
		}else{
			String sqlLessonsFromStudentPlaylist = "with temptable as (select student_playlist.lesson_id, student_playlist.status, student_playlist.cmsession_id,student_playlist.module_id, skill_objective.parent_skill from student_playlist inner join lesson_skill_objective on student_playlist.lesson_id=lesson_skill_objective.lessonid and student_playlist.course_id=lesson_skill_objective.context_id inner join skill_objective on lesson_skill_objective.learning_objectiveid=skill_objective.id where student_id="+istarUserId+" and course_id="+courseId+" group by student_playlist.lesson_id, student_playlist.status, student_playlist.module_id, student_playlist.cmsession_id, skill_objective.parent_skill order by student_playlist.lesson_id,student_playlist.cmsession_id, student_playlist.module_id) select parent_skill as cmsession_skill_objective, module_id, cast(count(lesson_id) as integer) as total_lessons, cast(count(case when status='COMPLETED' then 1 end) as integer) as completed_lessons  from temptable group by parent_skill, module_id";
						
			SQLQuery lessonsFromStudentPlaylistQuery = session.createSQLQuery(sqlLessonsFromStudentPlaylist);		
			List<Object[]> lessonsFromStudentPlaylistResult = lessonsFromStudentPlaylistQuery.list();
			
			if(lessonsFromStudentPlaylistResult.size()>0){

				for(Object[] sessionRow: lessonsFromStudentPlaylistResult){
					Integer cmsessionSkillObjectiveId = (Integer) sessionRow[0];
					Integer moduleId = (Integer) sessionRow[1];
					Integer totalLessons = (Integer) sessionRow[2];
					Integer completedLessons = (Integer) sessionRow[3];
					
					
					SkillObjective cmsessionSkillObjective = appAssessmentServices.getSkillObjective(cmsessionSkillObjectiveId);
					SkillReportPOJO moduleSkillReportPOJO = null;
					SkillReportPOJO cmsessionSkillReportPOJO = null;
					
					if(modulesOfAssessment.containsKey(moduleId)){

						for (SkillReportPOJO tempModuleSkillReport : skillsReport) {
							if (tempModuleSkillReport.getId() == moduleId) {
								moduleSkillReportPOJO = tempModuleSkillReport;
								break;
							}
						}

						if(cmsessionSkillObjectiveOfAssessment.containsKey(cmsessionSkillObjectiveId)){
							for(SkillReportPOJO tempCmsessionSkillReport : moduleSkillReportPOJO.getSkills()){
								if(tempCmsessionSkillReport.getId()==cmsessionSkillObjectiveId){
									cmsessionSkillReportPOJO=tempCmsessionSkillReport;
									break;
								}
							}
						}
						
						if(cmsessionSkillReportPOJO==null){
							//System.out.println("New Skill-->"+ cmsessionSkillObjective.getName()+"-->"+cmsessionSkillObjective.getId());
						cmsessionSkillReportPOJO = new SkillReportPOJO();
						cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
						cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
						cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
						cmsessionSkillObjectiveOfAssessment.put(cmsessionSkillObjectiveId, cmsessionSkillObjective);
						cmsessionSkillReportPOJO.setTotalPoints(benchmark*totalLessons);
						cmsessionSkillReportPOJO.setUserPoints(benchmark*completedLessons);												
						moduleSkillReportPOJO.getSkills().add(cmsessionSkillReportPOJO);
						}else{
							cmsessionSkillReportPOJO.setTotalPoints(cmsessionSkillReportPOJO.getTotalPoints()+(benchmark*totalLessons));
							cmsessionSkillReportPOJO.setUserPoints(cmsessionSkillReportPOJO.getUserPoints()+(benchmark*completedLessons));
						}
						cmsessionSkillReportPOJO.calculatePercentage();
						moduleSkillReportPOJO.calculateTotalPoints();
						moduleSkillReportPOJO.calculateUserPoints();
						moduleSkillReportPOJO.calculatePercentage();
						moduleSkillReportPOJO.generateMessage();
					}else{
						Module module = getModule(moduleId);
						if (module != null) {
							modulesOfAssessment.put(moduleId, module);

							moduleSkillReportPOJO = new SkillReportPOJO();
							moduleSkillReportPOJO.setId(module.getId());
							moduleSkillReportPOJO.setName(module.getModuleName());
							moduleSkillReportPOJO.setDescription(module.getModule_description());
							moduleSkillReportPOJO.setImageURL(module.getImage_url());

							List<SkillReportPOJO> cmsessionSkillsReport = new ArrayList<SkillReportPOJO>();
							cmsessionSkillObjectiveOfAssessment.put(cmsessionSkillObjectiveId, cmsessionSkillObjective);

							cmsessionSkillReportPOJO = new SkillReportPOJO();
							cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
							cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
							cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
							cmsessionSkillReportPOJO.setTotalPoints(benchmark*totalLessons);
							cmsessionSkillReportPOJO.setUserPoints(benchmark*completedLessons);												
							cmsessionSkillReportPOJO.setTotalPoints(cmsessionSkillReportPOJO.getTotalPoints()+(benchmark*totalLessons));
							cmsessionSkillReportPOJO.setUserPoints(cmsessionSkillReportPOJO.getUserPoints()+(benchmark*completedLessons));
							
							cmsessionSkillsReport.add(cmsessionSkillReportPOJO);
							moduleSkillReportPOJO.setSkills(cmsessionSkillsReport);
							
							cmsessionSkillReportPOJO.calculatePercentage();
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
		}	*/	
		return skillReportForCourse;
	}
	
	
	private List<SkillReportPOJO> fillShellTreeWithData(List<SkillReportPOJO> shellTree, int istarUserId, int courseId) {
		
		String getDataForTree="select * from (select T1.id,T1.skill_objective, T1.points,T1.max_points, cmsession_module.module_id from "
				+ "(WITH summary AS ( 	SELECT 		P . ID, 		P .skill_objective, 		P .points, 		P .max_points, 		     ROW_NUMBER () OVER ( 			PARTITION BY P .skill_objective 			ORDER BY 				P . TIMESTAMP DESC 		) AS rk 	FROM 		user_gamification P 	where P.course_id ="+courseId+" and P.item_type='QUESTION' ) SELECT 	s.* FROM 	summary s WHERE 	s.rk = 1) T1 "
						+ "join cmsession_skill_objective on (T1.skill_objective= cmsession_skill_objective.skill_objective_id) "
						+ "join cmsession_module on (cmsession_module.cmsession_id= cmsession_skill_objective.cmsession_id)	 )LT  "
						+ "union "
						+ "select QT.id , QT.skill_objective, QT.points, QT.max_points, QT.module_id from "
						+ "(WITH summary AS ( 	SELECT 		P . ID, 		P .skill_objective, 		P .points, 		P .max_points, 	 		P .module_id, ROW_NUMBER () OVER ( 			PARTITION BY P .skill_objective 			ORDER BY 				P . TIMESTAMP DESC 		) AS rk 	FROM 		user_gamification P 	where P.course_id = "+courseId+" and P.item_type='LESSON' ) SELECT 	s.* FROM 	summary s WHERE 	s.rk = 1) QT   ";
		System.out.println("getDataForTree"+getDataForTree);
		DBUTILS util = new DBUTILS();
		List<HashMap<String, Object>> data = util.executeQuery(getDataForTree);
		for(HashMap<String, Object> row: data)
		{
			int skillId = (int)row.get("skill_objective");
			double userPoints = (double)row.get("points");			
			double maxPoints = (double)row.get("max_points");
			int moduleId = (int)row.get("module_id");
			
			for(SkillReportPOJO mod : shellTree)
			{
				if(mod.getId() == moduleId)
				{
					List<SkillReportPOJO> cmsSkills = mod.getSkills();
					for(SkillReportPOJO cmsSkill: cmsSkills)
					{
						if(cmsSkill.getId()== skillId)
						{
							cmsSkill.setUserPoints(Math.ceil(userPoints));
							cmsSkill.setTotalPoints(Math.ceil(maxPoints));						
							cmsSkill.calculatePercentage();
							//cmsSkills.add(cmsSkill);
							break;
						}
					}
					mod.calculateUserPoints();
					mod.calculateTotalPoints();
					mod.calculatePercentage();
					
					break;
				}
			}
		}
		return shellTree;
	}

	private List<SkillReportPOJO> getShellSkillTreeForCourse(int courseId) {
		String mediaUrlPath ="";
		try{
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
				if (inputStream != null) {
					properties.load(inputStream);
					mediaUrlPath =  properties.getProperty("media_url_path");
					System.out.println("media_url_path"+mediaUrlPath);
				}
			} catch (IOException e) {
				e.printStackTrace();
			
		}
		
		List<SkillReportPOJO> skillsReport = new ArrayList<SkillReportPOJO>();
		DBUTILS utils = new DBUTILS();
		String getEmptyTreeStructure = "select * from (SELECT 	MODULE . ID AS module_id, 	MODULE .module_name, 	COALESCE(module.module_description,' ') as module_description, 	module.image_url, 	skill_objective. ID AS cmsession_skill_id, 	skill_objective. NAME AS cmsession_skill_name FROM 	module_course, 	MODULE, 	cmsession_module, 	cmsession_skill_objective, 	skill_objective WHERE 	module_course.course_id = "+courseId+" AND module_course.module_id = MODULE . ID AND module_course.module_id = cmsession_module.module_id AND cmsession_module.cmsession_id = cmsession_skill_objective.cmsession_id AND cmsession_skill_objective.skill_objective_id = skill_objective. ID ) T1  join (select skill_objective_id, cast (sum(max_points)as float8) as max_points from assessment_benchmark where context_id = "+courseId+" group by skill_objective_id) AB on (AB.skill_objective_id = T1.cmsession_skill_id) ";
		System.out.println("getEmptyTreeStructure>>>"+getEmptyTreeStructure);
		List<HashMap<String, Object>> treeStructure = utils.executeQuery(getEmptyTreeStructure);
		for(HashMap<String, Object> treeRow: treeStructure)
		{
			int moduleId = (int)treeRow.get("module_id");
			String module_name = (String)treeRow.get("module_name");
			String moduleDesc = (String)treeRow.get("module_description");
			String moduleImage =mediaUrlPath+"course_images/"+module_name.charAt(0)+".png";
			if(treeRow.get("module_description")!=null)
			{
				moduleImage = mediaUrlPath+treeRow.get("module_description").toString();
			}
			String skillName = (String)treeRow.get("cmsession_skill_name");
			int skillId = (int)treeRow.get("cmsession_skill_id");
			double maxPoints = (double)treeRow.get("max_points");
			
			//lets create a mod pojo by default
			SkillReportPOJO modPojo = new SkillReportPOJO();
			modPojo.setName(module_name.trim());
			modPojo.setId(moduleId);
			modPojo.setSkills(new ArrayList<>());
			modPojo.setDescription(moduleDesc);
			modPojo.setImageURL(moduleImage);
			
			boolean moduleAlreadyPresentInTree = false;
			//we will check if this module pojo already exist in tree or not.
			//if exist then we will add cmsessions skills only to it
			//if do not exist then we will create one.
			for(SkillReportPOJO mod : skillsReport)
			{
				if(mod.getId()==moduleId)
				{
					modPojo = mod;
					moduleAlreadyPresentInTree= true;
					break;
				}									
			}
			
			
			boolean skillAlreadyPresent = false;
			if(modPojo.getSkills()!=null)
			{
				for(SkillReportPOJO cmsessionSkill : modPojo.getSkills())
				{
					if(cmsessionSkill.getId()== skillId)
					{
						skillAlreadyPresent = true;
						break;
					}
				}
			}
			
			//if session skill is not present in module tree then we will add session skill to module tree.
			if(!skillAlreadyPresent)
			{
				SkillReportPOJO sessionSkill = new SkillReportPOJO();
				sessionSkill.setId(skillId);
				sessionSkill.setName(skillName);
				sessionSkill.setUserPoints((double)0);
				sessionSkill.setTotalPoints(maxPoints);				
				List<SkillReportPOJO> sessionsSkills = modPojo.getSkills();
				sessionsSkills.add(sessionSkill);
				modPojo.setSkills(sessionsSkills);
			}
			modPojo.calculatePercentage();
			modPojo.calculateUserPoints();
			modPojo.calculateTotalPoints();
			
			if(!moduleAlreadyPresentInTree)
			{
				skillsReport.add(modPojo);
			}
			
		}
		 return skillsReport;
	}

	public void insertIntoUserGamificationOnCompletitionOfLessonByUser(int istarUserId, int lessonId, int courseId){
		System.out.println("Starting to update UG");
		String sqlBatch = "select batch_group_id from batch where course_id="+courseId+" and batch_group_id in (select batch_group_id from batch_students where student_id="+istarUserId+")";
		System.out.println(sqlBatch);
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery queryBatch = session.createSQLQuery(sqlBatch);
		
		List<Integer> batchResult = queryBatch.list();
		
		Double benchmark = getBenchmark();
		String dataForUserGamificationSQL = "with temptable as (select student_playlist.student_id,student_playlist.course_id,student_playlist.module_id,student_playlist.cmsession_id, student_playlist.lesson_id, skill_objective.parent_skill from student_playlist inner join lesson_skill_objective on student_playlist.lesson_id= lesson_skill_objective.lessonid inner join skill_objective on lesson_skill_objective.learning_objectiveid=skill_objective.id where course_id="+courseId+" and student_id="+istarUserId+" and student_playlist.lesson_id="+lessonId+" group by student_playlist.student_id,student_playlist.course_id,student_playlist.module_id,student_playlist.cmsession_id, student_playlist.lesson_id, skill_objective.parent_skill) select *, cast("+benchmark+"*1.0/count(parent_skill) over (partition by lesson_id) as numeric) as points from temptable group by student_id, course_id, module_id, cmsession_id, lesson_id, parent_skill";
		System.out.println(dataForUserGamificationSQL);
		SQLQuery queryDataUG = session.createSQLQuery(dataForUserGamificationSQL);
		
		List<Object[]> resultUG = queryDataUG.list();
		
		java.util.Date date = new java.util.Date();
		Timestamp current = new Timestamp(date.getTime());
		
		for(Object[] row: resultUG){
			//Integer studentId = (Integer) row[0];
			//Integer courseId = (Integer) row[1];
			Integer moduleId = (Integer) row[2];
			Integer cmsessionId = (Integer) row[3];
			//Integer lessonId = (Integer) row[4];
			Integer cmsessionSkillObjectiveId = (Integer) row[5];
			Double points = ((BigDecimal) row[6]).doubleValue();
			
			
			for(Integer batchGroupId : batchResult){
			String sql = "INSERT INTO public.user_gamification (id, istar_user, skill_objective, points, coins, created_at, updated_at, item_id, item_type, cmsession_id, module_id, course_id, batch_group_id, org_id, timestamp) VALUES "
					+ "((select max(id)+1 from user_gamification), "+istarUserId+", "+cmsessionSkillObjectiveId+", "+points+", 0, '"+current+"', '"+current+"', "+lessonId+", 'LESSON', "+cmsessionId+", "+moduleId+", "+courseId+","+batchGroupId+", 0, '"+current+"');";
			System.out.println(sql);
			SQLQuery query = session.createSQLQuery(sql);
			query.executeUpdate();
			}
		}
		session.close();		
	}
	
	/*public List<SkillReportPOJO> getSkillReportTreeForCourseOfUser(int istarUserId, int courseId){
		
		List<SkillReportPOJO> skillsReport = new ArrayList<SkillReportPOJO>();
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		String lessonsStatusSQL = "select course_id, module_id, cmsession_id,cast(count(case when status='COMPLETED' then 1 end) as integer) as completed_lessons, cast(count(*) as integer) as total_lessons from student_playlist where student_id="+istarUserId+" and course_id="+courseId+" group by course_id, module_id,cmsession_id";
		
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
		String sql = "select COALESCE(cast(count(case when status='INCOMPLETE' then 1 end) as integer),0) as incomplete, COALESCE(cast(count(case when status='COMPLETED' then 1 end) as integer),0) as complete  from student_playlist where student_id= :istarUserId and course_id= :courseId";
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