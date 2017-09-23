package com.istarindia.apps.services;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import com.istarindia.android.pojo.ConcreteItemPOJO;
import com.istarindia.android.pojo.CoursePOJO;
import com.istarindia.android.pojo.LessonPOJO;
import com.istarindia.android.pojo.ModulePOJO;
import com.istarindia.android.pojo.SessionPOJO;
import com.istarindia.android.pojo.SkillReportPOJO;
import com.istarindia.android.pojo.StudentRankPOJO;
import com.istarindia.android.utility.AppUserRankUtility;
import com.viksitpro.core.dao.entities.Assessment;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.Cmsession;
import com.viksitpro.core.dao.entities.CmsessionDAO;
import com.viksitpro.core.dao.entities.Context;
import com.viksitpro.core.dao.entities.Course;
import com.viksitpro.core.dao.entities.CourseDAO;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.LessonDAO;
import com.viksitpro.core.dao.entities.Module;
import com.viksitpro.core.dao.entities.ModuleDAO;
import com.viksitpro.core.dao.entities.SkillObjective;
import com.viksitpro.core.dao.entities.StudentPlaylist;
import com.viksitpro.core.skill.pojo.CourseLevelSkill;
import com.viksitpro.core.skill.pojo.ModuleLevelSkill;
import com.viksitpro.core.skill.pojo.SessionLevelSkill;
import com.viksitpro.core.skill.services.CoreSkillService;
import com.viksitpro.core.utilities.AppProperies;
import com.viksitpro.core.utilities.DBUTILS;

public class AppCourseServices {

	String mediaUrlPath = AppProperies.getProperty("media_url_path");
	public CoursePOJO getCoursePojoForUser(int istarUserId, int courseId)
	{
		
		CoursePOJO courseObject = null;
		DBUTILS util = new DBUTILS();
		
		Course course = new CourseDAO().findById(courseId);
		StudentPlaylistServices studentPlaylistServices = new StudentPlaylistServices();
		List<StudentPlaylist> allStudentPlaylist = studentPlaylistServices.getStudentPlaylistOfUserForCourse(istarUserId, courseId);
		if (course != null && allStudentPlaylist.size() > 0) {
			courseObject = new CoursePOJO();
			courseObject.setId(courseId);
			courseObject.setName(course.getCourseName());
			courseObject.setDescription(course.getCourseDescription());
			
			String category ="N/A";
			if(course.getContexts()!=null)
			{
				category = String.join(", ", course.getContexts().stream() .map(Context::getTitle).collect(Collectors.toList()));
			}
			courseObject.setCategory(category);
			courseObject.setImageURL(mediaUrlPath + course.getImage_url());
											
			String getRankPointsForUser = ""
			+ "select * from "
			+ "( "
				+ "select DISTINCT "
				+ "batch_students.student_id as student_id, "
				+ "COALESCE(coins,0) as coins, "
				+ "COALESCE(user_points,0) as user_points, "
				+ "COALESCE(total_points,0) as total_points, "
				+ "CAST ( RANK () OVER (  ORDER BY COALESCE(percentage, 0) DESC ) AS INTEGER ) as user_rank "
				+ "from  batch_students left join leaderboard on (leaderboard.user_id = batch_students.student_id) "
				+ "where batch_students.batch_group_id in "
				+ "("
					+ "select batch_group_id from batch_students where student_id = "+istarUserId+""
				+ ") "
				+ "and course_id ="+courseId+" order by user_rank "
			+ ")TT "
			+ "where student_id = "+istarUserId;
			List<HashMap<String, Object>> rankPointsData = util.executeQuery(getRankPointsForUser);
			int rank = 0;
			double userPoints = 0;
			double totalPoints = 0;
			if (rankPointsData.size() > 0) {
				rank = (int) rankPointsData.get(0).get("user_rank");
				userPoints = (double) rankPointsData.get(0).get("user_points");
				totalPoints = (double) rankPointsData.get(0).get("total_points");
			}
			String pattern = "##.##";
			DecimalFormat decimalFormat = new DecimalFormat(pattern);
			courseObject.setUserPoints(Double.parseDouble(decimalFormat.format(userPoints)));
			courseObject.setRank(rank);
			courseObject.setTotalPoints(Double.parseDouble(decimalFormat.format(totalPoints)));
			
			
			HashMap<Integer, Float> moduleProgress= new HashMap<>();
			HashMap<Integer, Float> sessionProgress = new HashMap<>();
			HashMap<Integer, Float> lessonProgress = new HashMap<>();
			HashMap<Integer, Integer> lessonLastSlideId = new HashMap<>();
			double courseProgress = 0;
			int modulesCompleted = 0;
			String findLessonProgressAndSlide =""
					+ "select DISTINCT lesson_id as item_id, last_slide_id, progress, 'LESSON' as type "
					+ "from user_lesson_progress "
					+ "where user_id = "+istarUserId+" and course_id= "+courseId+" "
			+ "union "
					+ "select DISTINCT session_id as item_id, 0, avg(progress) as progress, 'SESSION' as type "
					+ "from user_lesson_progress "
					+ "where user_id = "+istarUserId+" and course_id= "+courseId+" "
					+ "group by session_id "
			+ "union "
					+ "select DISTINCT module_id as item_id,0,  avg(progress) as progress, 'MODULE' as type "
					+ "from user_lesson_progress "
					+ "where user_id = "+istarUserId+"  and course_id= "+courseId+" "
					+ "group by module_id "
			+ "union "
					+ "select DISTINCT course_id as item_id,0,  avg(progress) as progress, 'COURSE' as type "
					+ "from user_lesson_progress "
					+ "where user_id = "+istarUserId+" and course_id= "+courseId+" "
					+ "group by course_id"; 
			List<HashMap<String, Object>> lessonProgressData = util.executeQuery(findLessonProgressAndSlide);
			if(lessonProgressData!=null)
			{
				for(HashMap<String, Object> row: lessonProgressData)
				{
					int itemId = (int)row.get("item_id");
					String  type  = row.get("type").toString();
					if(type.equalsIgnoreCase("LESSON"))
					{
						Integer lastSlideId = null;
						if(row.get("last_slide_id")!=null)
						{
							lastSlideId = (int)row.get("last_slide_id");
						}
						lessonLastSlideId.put(itemId, lastSlideId);
						float prog =0;
						if(row.get("progress")!=null)
						{	
							prog= (float)row.get("progress");
						}
						lessonProgress.put(itemId, prog);

					}
					else if(type.equalsIgnoreCase("SESSION"))
					{
						float prog =0;
						if(row.get("progress")!=null)
						{	
							prog= (float)row.get("progress");
						}
						sessionProgress.put(itemId, prog);
					}else if(type.equalsIgnoreCase("MODULE"))
					{
						float prog =0;
						if(row.get("progress")!=null)
						{	
							prog= (float)row.get("progress");
						}
						if(prog>98)
						{
							modulesCompleted++;
						}
						moduleProgress.put(itemId, prog);
					}
					else if(type.equalsIgnoreCase("COURSE"))
					{
						float prog =0;
						if(row.get("progress")!=null)
						{	
							prog= (float)row.get("progress");
						}
						courseProgress = prog;
					}															
				}
			}
			
			String courseStatus ="INCOMPLETE";
			if(courseProgress>98)
			{
				courseStatus ="COMPLETE";
			}
			courseObject.setStatus(courseStatus);
			
			
			int modOrder = 0;
			HashMap<Integer, ModulePOJO> totalMod = new HashMap<>();
			HashMap<Integer, SessionPOJO> totalSessions = new HashMap<>();
			HashMap<Integer, ConcreteItemPOJO> totalLessons = new HashMap<>();
			HashMap<Integer, ArrayList<Integer>> sessionsInModule = new HashMap<>();
			HashMap<Integer, ArrayList<Integer>> lessonsInSession = new HashMap<>();
			
			for(StudentPlaylist playlist : allStudentPlaylist)
			{
				Module module = playlist.getModule();
				Cmsession cmsession = playlist.getCmsession();
				Lesson lesson = playlist.getLesson();				
				
				if(lesson.getIsDeleted()!=null && !lesson.getIsDeleted() && lesson.getIsPublished())
				{
					ConcreteItemPOJO lessonObject = new ConcreteItemPOJO();
					LessonPOJO actualLesson = new LessonPOJO();
					lessonObject.setId(lesson.getId());
					lessonObject.setType("LESSON_" + lesson.getType());		
					
					actualLesson.setId(lesson.getId());
					actualLesson.setPlaylistId(playlist.getId());
					actualLesson.setType(lesson.getType());
					actualLesson.setTitle(lesson.getTitle());
					actualLesson.setDescription(lesson.getDescription());
					String subject ="NONE";
					if(lesson.getSubject()!=null)
					{
						subject  = lesson.getSubject();
					}
					actualLesson.setSubject(subject);
					actualLesson.setOrderId(playlist.getId());
					actualLesson.setDuration(lesson.getDuration());
					actualLesson.setStatus(playlist.getStatus());
					actualLesson.setLessonUrl(mediaUrlPath+"/lessonXMLs/"+lesson.getId());
					actualLesson.setImageUrl(mediaUrlPath+lesson.getImage_url());
					if(lessonLastSlideId.containsKey(lesson.getId()))
					{
						actualLesson.setCurrentSlideId(lessonLastSlideId.get(lesson.getId()));
					}
						
					lessonObject.setStatus(playlist.getStatus());
					lessonObject.setOrderId(playlist.getId());
					lessonObject.setTaskId(playlist.getTaskId());
					if(lessonProgress.containsKey(lesson.getId()))
					{
						lessonObject.setProgress(lessonProgress.get(lesson.getId()).intValue());
					}
					else {
						lessonObject.setProgress(0);
					}					
					lessonObject.setLesson(actualLesson);								

					if(totalMod.containsKey(module.getId()))
					{
						ModulePOJO m = totalMod.get(module.getId());
						if(sessionsInModule.get(m.getId())!=null && sessionsInModule.get(m.getId()).contains(cmsession.getId()))
						{
							totalLessons.put(lesson.getId(), lessonObject);
							ArrayList<Integer> lessonIds = lessonsInSession.get(cmsession.getId());
							lessonIds.add(lesson.getId());
							lessonsInSession.put(cmsession.getId(), lessonIds);		
						}
						else
						{
							SessionPOJO s = getNewSessionPojo(playlist);
							if(sessionProgress.containsKey(cmsession.getId()) && sessionProgress.get(cmsession.getId())>99)
							{
								s.setProgress(sessionProgress.get(cmsession.getId()).intValue());
							}
							else
							{
								s.setProgress(0);
							}							
							totalLessons.put(lesson.getId(), lessonObject);
							ArrayList<Integer> lessonIds = new ArrayList<>();
							lessonIds.add(lesson.getId());
							lessonsInSession.put(cmsession.getId(), lessonIds);														
							totalSessions.put(cmsession.getId(), s);
							ArrayList<Integer> sessionIds = sessionsInModule.get(m.getId());
							sessionIds.add(cmsession.getId());
							sessionsInModule.put(module.getId(), sessionIds);
						}												
					}
					else
					{
						modOrder++;
						ModulePOJO m = getEmptyModulePojo(playlist,modOrder);
						if(moduleProgress.containsKey(module.getId()) && moduleProgress.get(module.getId())>99)
						{
							m.setStatus("COMPLETED");
						}
						else
						{
							m.setStatus("INCOMPLETE");
						}
						
						SessionPOJO s = getNewSessionPojo(playlist);
						if(sessionProgress.containsKey(cmsession.getId()) && sessionProgress.get(cmsession.getId())>99)
						{
							s.setProgress(sessionProgress.get(cmsession.getId()).intValue());
						}
						else
						{
							s.setProgress(0);
						}
						
						totalLessons.put(lesson.getId(), lessonObject);
						ArrayList<Integer> lessonIds = new ArrayList<>();
						lessonIds.add(lesson.getId());
						lessonsInSession.put(cmsession.getId(), lessonIds);
						
						
						totalSessions.put(cmsession.getId(), s);						
						ArrayList<Integer> sessionIds = new ArrayList<>();
						sessionIds.add(cmsession.getId());
						sessionsInModule.put(module.getId(), sessionIds);
						
						totalMod.put(module.getId(), m);						
						
					}	
				}				
			}
			
			
			
			HashMap<Integer, Double> skillUserPoints = new HashMap<>();
			HashMap<Integer, Double> skillTotalPoints = new HashMap<>();
			HashMap<Integer, Double> skillPercentage = new HashMap<>();
			if(totalSessions.size()>0)
			{
				String skillIds = StringUtils.join(totalSessions.keySet(), ',');
				String getSesssionSkillPoints =""
						+ "select DISTINCT "
						+ "assessment_benchmark.skill_id, "
						+ "cast (COALESCE(sum (user_points),0) as float8) as user_points, "
						+ "cast (COALESCE(sum(total_points),0) as float8) as total_points "
						+ "from assessment_benchmark left join user_points_coins upc on (assessment_benchmark.skill_id=upc.skill_id and upc.user_id = "+istarUserId+") "
						+ "where "
						+ "course_id = "+courseId+" "
						+ "and assessment_benchmark.skill_id in ("+skillIds+") "
						+ "group by assessment_benchmark.skill_id;";
				List<HashMap<String, Object>> sessionSkillData = util.executeQuery(getSesssionSkillPoints);
				for(HashMap<String, Object> row: sessionSkillData)
				{
					int skillId = (int) row.get("skill_id");
					double userPointsPerSkill =(double) row.get("skill_id");
					double totalPointsPerSkill =(double) row.get("skill_id");
					double percentagePerSkill =0;
					if(totalPointsPerSkill!=0)
					{
						percentagePerSkill = (userPointsPerSkill*100)/totalPoints;
					}
					skillUserPoints.put(skillId, Double.parseDouble(decimalFormat.format(userPointsPerSkill)));
					skillTotalPoints.put(skillId, Double.parseDouble(decimalFormat.format(totalPointsPerSkill)));
					skillPercentage.put(skillId, Double.parseDouble(decimalFormat.format(percentagePerSkill)));
				}
			}
			
			
			ArrayList<ModulePOJO> finalModulesInCourse = new ArrayList<>();
			List<SkillReportPOJO> courseSkillReport = new ArrayList<>();
			for(int modId : totalMod.keySet())
			{
				ModulePOJO finalModule = totalMod.get(modId);
				ArrayList<SessionPOJO> finalSessions = new ArrayList<>();
				HashSet<String> sessionSkills = new HashSet<>();
				
				double moduleTotalPoints = 0;
				double moduleUserPoints = 0;
				double modulePercentage = 0;
				List<SkillReportPOJO> sessionSkillsInModSkill = new ArrayList<>();
				for(int sessionId : sessionsInModule.get(modId))
				{
					if(totalSessions.get(sessionId)!=null)
					{   SessionPOJO s = totalSessions.get(sessionId);
						if(lessonsInSession.get(sessionId)!=null &&lessonsInSession.get(sessionId).size()>0)	
						{
							ArrayList<ConcreteItemPOJO> finalLessons= new ArrayList<>();
							for(int lessonId : lessonsInSession.get(sessionId))
							{
								if(totalLessons.get(lessonId)!=null)
								{
									finalLessons.add(totalLessons.get(lessonId));
								}	
							}
							s.setLessons(finalLessons);
							finalSessions.add(s);
							sessionSkills.add(s.getName());
						}
					
						SkillReportPOJO sSkill = new SkillReportPOJO();
						sSkill.setId(s.getId());
						sSkill.setName(s.getName());
						double sessionUserPoints = 0;
						double sessionTotalPoints = 0;
						double sessionPercentage = 0;
						if(skillUserPoints.get(s.getId())!=null)
						{
							sessionUserPoints = skillUserPoints.get(s.getId());
						}
						if(skillTotalPoints.get(s.getId())!=null)
						{
							sessionTotalPoints = skillTotalPoints.get(s.getId());
						}
						if(skillPercentage.get(s.getId())!=null)
						{
							sessionPercentage = skillPercentage.get(s.getId());
						}
						sSkill.setUserPoints(Double.parseDouble(decimalFormat.format(sessionUserPoints)));
						sSkill.setTotalPoints(Double.parseDouble(decimalFormat.format(sessionTotalPoints)));
						sSkill.setPercentage(Double.parseDouble(decimalFormat.format(sessionPercentage)));
						sessionSkillsInModSkill.add(sSkill);
						
					}
					
					if(skillUserPoints.get(sessionId)!=null)
					{
						moduleUserPoints += skillUserPoints.get(sessionId);
					}
					if(skillTotalPoints.get(sessionId)!=null)
					{
						moduleTotalPoints += skillTotalPoints.get(sessionId);
					}
					
				}
				if(finalSessions.size()>0)
				{
					finalModule.setSessions(finalSessions);
					finalModule.setSkillObjectives(sessionSkills);
					finalModulesInCourse.add(finalModule);
					
					
					SkillReportPOJO moduleSkill = new SkillReportPOJO();
					moduleSkill.setId(finalModule.getId());
					moduleSkill.setName(finalModule.getName());
					moduleSkill.setTotalPoints(Double.parseDouble(decimalFormat.format(moduleTotalPoints)));
					moduleSkill.setUserPoints(Double.parseDouble(decimalFormat.format(moduleUserPoints)));
					
					if(moduleTotalPoints!=0)
					{
						modulePercentage= (moduleUserPoints*100)/moduleTotalPoints;
					}
					moduleSkill.setPercentage(Double.parseDouble(decimalFormat.format(modulePercentage)));
					moduleSkill.setSkills(sessionSkillsInModSkill);
					courseSkillReport.add(moduleSkill);
					
				}									
			}
				
			int totalModules = finalModulesInCourse.size();
			String message =""+modulesCompleted+" of "+totalModules+" modules completed";
			courseObject.setModules(finalModulesInCourse);
			courseObject.setSkillObjectives(courseSkillReport);
			courseObject.setMessage(message);
			
		}
		return courseObject;
	}
	private SessionPOJO getNewSessionPojo(StudentPlaylist playlist) {
		SessionPOJO s= new SessionPOJO();
		Cmsession cms = playlist.getCmsession();
		s.setDescription(cms.getDescription());
		s.setId(cms.getId());
		s.setName(cms.getTitle());
		s.setOrderId(playlist.getId());		
		return s;
	}
	private ModulePOJO getEmptyModulePojo(StudentPlaylist playList, int orderID) {
		Module module = playList.getModule();
		ModulePOJO m= new ModulePOJO();
		m.setDescription(module.getModule_description());
		m.setId(module.getId());
		m.setImageURL(mediaUrlPath+ module.getImage_url());
		m.setName(module.getModuleName());
		m.setOrderId(orderID);
		return m;
	}




	public void insertIntoUserGamificationOnCompletitionOfLessonByUser(int istarUserId, int lessonId, int courseId) {
		// System.out.println("Starting to update UG");
		String sqlBatch = "select batch_group_id from batch where course_id=" + courseId
				+ " and batch_group_id in (select batch_group_id from batch_students where student_id=" + istarUserId
				+ ")";
		// System.out.println(sqlBatch);
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery queryBatch = session.createSQLQuery(sqlBatch);

		List<Integer> batchResult = queryBatch.list();

		Double benchmark = getBenchmark();
		String dataForUserGamificationSQL = "with temptable as (select student_playlist.student_id,student_playlist.course_id,student_playlist.module_id,student_playlist.cmsession_id, student_playlist.lesson_id, skill_objective.parent_skill from student_playlist inner join lesson_skill_objective on student_playlist.lesson_id= lesson_skill_objective.lessonid inner join skill_objective on lesson_skill_objective.learning_objectiveid=skill_objective.id where course_id="
				+ courseId + " and student_id=" + istarUserId + " and student_playlist.lesson_id=" + lessonId
				+ " group by student_playlist.student_id,student_playlist.course_id,student_playlist.module_id,student_playlist.cmsession_id, student_playlist.lesson_id, skill_objective.parent_skill) select *, cast("
				+ benchmark
				+ "*1.0/count(parent_skill) over (partition by lesson_id) as numeric) as points from temptable group by student_id, course_id, module_id, cmsession_id, lesson_id, parent_skill";
		// System.out.println(dataForUserGamificationSQL);
		SQLQuery queryDataUG = session.createSQLQuery(dataForUserGamificationSQL);

		List<Object[]> resultUG = queryDataUG.list();

		java.util.Date date = new java.util.Date();
		Timestamp current = new Timestamp(date.getTime());

		for (Object[] row : resultUG) {
			// Integer studentId = (Integer) row[0];
			// Integer courseId = (Integer) row[1];
			Integer moduleId = (Integer) row[2];
			Integer cmsessionId = (Integer) row[3];
			// Integer lessonId = (Integer) row[4];
			Integer cmsessionSkillObjectiveId = (Integer) row[5];
			Double points = ((BigDecimal) row[6]).doubleValue();

			for (Integer batchGroupId : batchResult) {
				String sql = "INSERT INTO public.user_gamification (id, istar_user, skill_objective, points, coins, created_at, updated_at, item_id, item_type, cmsession_id, module_id, course_id, batch_group_id, org_id, timestamp) VALUES "
						+ "((select max(id)+1 from user_gamification), " + istarUserId + ", "
						+ cmsessionSkillObjectiveId + ", " + points + ", 0, '" + current + "', '" + current + "', "
						+ lessonId + ", 'LESSON', " + cmsessionId + ", " + moduleId + ", " + courseId + ","
						+ batchGroupId + ", 0, '" + current + "');";
				// System.out.println(sql);
				SQLQuery query = session.createSQLQuery(sql);
				query.executeUpdate();
			}
		}
		session.close();
	}
	



	public Double getBenchmark() {
		Double benchmark = 1.0;
		try {
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
	public HashMap<String, Object> getPointsAndCoinsOfUserForCmsessionSkillOfCourse(int istarUserId,
			int cmsessionSkillObjectiveId, int courseId) {

		HashMap<String, Object> map = new HashMap<String, Object>();

		/*
		 * String sql =
		 * "select COALESCE(sum(points),0) as points, COALESCE(cast(sum(coins) as integer),0) as coins from user_gamification "
		 * +
		 * "where skill_objective= :cmsessionSkillObjectiveId and istar_user= :istarUserId and item_id in "
		 * + "(select id from assessment where course_id= :courseId)";
		 */

		String sql = "select COALESCE(sum(points),0) as points, COALESCE(cast(sum(coins) as integer),0) as coins from user_gamification "
				+ "where skill_objective= :cmsessionSkillObjectiveId and istar_user= :istarUserId and course_id= :courseId";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId", istarUserId);
		query.setParameter("cmsessionSkillObjectiveId", cmsessionSkillObjectiveId);
		query.setParameter("courseId", courseId);

		List<Object[]> result = query.list();

		if (result.size() > 0) {
			Double points = (Double) result.get(0)[0];
			Integer coins = (Integer) result.get(0)[1];

			map.put("points", points);
			map.put("coins", coins);
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	public HashMap<Integer, HashMap<String, Object>> getPointsAndCoinsOfCmsessionSkillsOfCourseForUser(int istarUserId,
			int courseId) {

		HashMap<Integer, HashMap<String, Object>> skillsMap = new HashMap<Integer, HashMap<String, Object>>();

		/*
		 * String sql =
		 * "select COALESCE(sum(points),0) as points, COALESCE(cast(sum(coins) as integer),0) as coins, skill_objective from user_gamification where istar_user= :istarUserId and item_id in (select id from assessment where course_id= :courseId) group by skill_objective"
		 * ;
		 */
		String sql = "select COALESCE(sum(points),0) as points, COALESCE(cast(sum(coins) as integer),0) as coins, skill_objective from user_gamification "
				+ "where istar_user= :istarUserId and course_id= :courseId group by skill_objective";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId", istarUserId);
		query.setParameter("courseId", courseId);

		List<Object[]> result = query.list();

		for (Object[] obj : result) {
			HashMap<String, Object> map = new HashMap<String, Object>();

			map.put("points", (Double) obj[0]);
			map.put("coins", (Integer) obj[1]);

			skillsMap.put((Integer) obj[2], map);
		}
		return skillsMap;
	}


	@SuppressWarnings("unchecked")
	public Double getMaxPointsOfCmsessionSkill(int cmsessionSkillObjectiveId) {

		Double maxPoints = 0.0;

		String sql = "select COALESCE(cast(sum(question.difficulty_level) as integer),0) as difficulty_level, "
				+ "COALESCE(cast(count(distinct lesson_skill_objective.lessonid) as integer),0) as number_of_lessons from "
				+ "lesson_skill_objective,question_skill_objective,question,skill_objective where "
				+ "lesson_skill_objective.learning_objectiveid=question_skill_objective.learning_objectiveid and "
				+ "question_skill_objective.questionid=question.id and question_skill_objective.learning_objectiveid=skill_objective.id and "
				+ "skill_objective.parent_skill= :cmsessionSkillObjectiveId";

		// System.out.println(sql);
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("cmsessionSkillObjectiveId", cmsessionSkillObjectiveId);

		List<Object[]> result = query.list();

		if (result.size() > 0) {
			Integer difficultyLevelSum = (Integer) result.get(0)[0];
			Integer numberOfLessons = (Integer) result.get(0)[1];

			try {
				Properties properties = new Properties();
				String propertyFileName = "app.properties";
				InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
				if (inputStream != null) {
					properties.load(inputStream);
					String pointsBenchmark = properties.getProperty("pointsBenchmark");
					Integer benchmark = Integer.parseInt(pointsBenchmark);

					maxPoints = (difficultyLevelSum + (numberOfLessons * benchmark)) * 1.0;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return maxPoints;
	}

	public Integer getNumberOfLessonsForCmsessionOfUserForCourse(int cmsessionId, int courseId, int istarUserId) {

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
	public List<Assessment> getAllAssessmentsOfACourse(int courseId) {

		String hql = "from assessment where course_id= :courseId";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		Query query = session.createQuery(hql);
		query.setParameter("courseId", courseId);

		List<Assessment> allAssessments = query.list();

		return allAssessments;
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