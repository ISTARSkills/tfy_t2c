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

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

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

	public CoursePOJO getCourseOfUser(int istarUserId, int courseId) {
		String mediaUrlPath = "";
		int per_assessment_points = 5, per_lesson_points = 5, per_question_points = 1, per_assessment_coins = 5,
				per_lesson_coins = 5, per_question_coins = 1;
		try {
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);
				mediaUrlPath = properties.getProperty("media_url_path");
				per_assessment_points = Integer.parseInt(properties.getProperty("per_assessment_points"));
				per_lesson_points = Integer.parseInt(properties.getProperty("per_lesson_points"));
				per_question_points = Integer.parseInt(properties.getProperty("per_question_points"));
				per_assessment_coins = Integer.parseInt(properties.getProperty("per_assessment_coins"));
				per_lesson_coins = Integer.parseInt(properties.getProperty("per_lesson_coins"));
				per_question_coins = Integer.parseInt(properties.getProperty("per_question_coins"));
			}
		} catch (IOException e) {
			e.printStackTrace();

		}

		CoursePOJO coursePOJO = null;
		StudentPlaylistServices studentPlaylistServices = new StudentPlaylistServices();
		AppUserRankUtility appUserRankUtility = new AppUserRankUtility();
		Course course = getCourse(courseId);
		DBUTILS util = new DBUTILS();
		List<StudentPlaylist> allStudentPlaylist = studentPlaylistServices
				.getStudentPlaylistOfUserForCourse(istarUserId, courseId);

		if (course != null && allStudentPlaylist.size() > 0) {

			coursePOJO = new CoursePOJO();
			coursePOJO.setId(course.getId());
			coursePOJO.setCategory(course.getCategory());
			coursePOJO.setDescription(course.getCourseDescription());
			coursePOJO.setImageURL(mediaUrlPath + course.getImage_url());
			coursePOJO.setName(course.getCourseName());

			coursePOJO.setProgress(getProgressOfUserForCourse(istarUserId, courseId));

			String getRankPointsForUser = "SELECT * FROM ( SELECT istar_user, user_points, total_points, perc, CAST ( Rank () OVER (order by user_points desc) AS INTEGER ) AS user_rank FROM ( SELECT istar_user, user_points, total_points, CAST ( (user_points * 100) / total_points AS INTEGER ) AS perc FROM ( SELECT T1.istar_user, SUM (T1.user_points) AS user_points, SUM (T1.max_points) AS total_points FROM ( WITH summary AS ( SELECT P .istar_user, P .skill_objective, custom_eval(cast (trim (replace(replace(replace( COALESCE(P.points,'0'),':per_lesson_points','"
					+ per_lesson_points + "'),':per_assessment_points','" + per_assessment_points
					+ "'),':per_question_points','" + per_question_points
					+ "'))  as text)) as user_points, custom_eval(cast (trim (replace(replace(replace( COALESCE(P.max_points,'0'),':per_lesson_points','"
					+ per_lesson_points + "'),':per_assessment_points','" + per_assessment_points
					+ "'),':per_question_points','" + per_question_points
					+ "'))  as text)) as max_points, ROW_NUMBER () OVER ( PARTITION BY P .istar_user, P .skill_objective, P.item_id ORDER BY P . TIMESTAMP DESC ) AS rk FROM user_gamification P WHERE item_type IN ('QUESTION', 'LESSON') AND batch_group_id = ( SELECT batch_group. ID FROM batch_students, batch_group WHERE batch_students.batch_group_id = batch_group. ID AND batch_students.student_id = "
					+ istarUserId + " AND batch_group.is_primary = 't' LIMIT 1 ) AND course_id = " + courseId
					+ " ) SELECT s.* FROM summary s WHERE s.rk = 1 ) T1 GROUP BY istar_user HAVING (SUM(T1.max_points) > 0) ) T2 ORDER BY  user_points DESC, perc DESC, total_points DESC ) T3 ) T4 WHERE istar_user = "
					+ istarUserId + "";
			if (AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
				// System.out.println("getRankPointsForUser>>>>>>>>>>"+getRankPointsForUser);
			}
			List<HashMap<String, Object>> rankPointsData = util.executeQuery(getRankPointsForUser);
			int rank = 0;
			double userPoints = 0;
			double totalPoints = 0;
			if (rankPointsData.size() > 0) {
				rank = (int) rankPointsData.get(0).get("user_rank");
				userPoints = (double) rankPointsData.get(0).get("user_points");
				totalPoints = (double) rankPointsData.get(0).get("total_points");
			}
			if (AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
				// System.out.println("userPoints>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+userPoints);
				// System.out.println("totalPoints>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+totalPoints);
			}
			coursePOJO.setUserPoints(userPoints);
			coursePOJO.setRank(rank);
			coursePOJO.setTotalPoints(totalPoints);
			int moduleOrderId = 0;
			Set<Integer> cmsessionIds = new HashSet<Integer>();
			for (StudentPlaylist studentPlaylist : allStudentPlaylist) {
				ModulePOJO modulePOJO = null;
				Module module = studentPlaylist.getModule();
				Cmsession cmsession = studentPlaylist.getCmsession();
				Lesson lesson = studentPlaylist.getLesson();
				if (lesson.getIsDeleted() != null && !lesson.getIsDeleted()) {
					for (ModulePOJO tempModulePOJO : coursePOJO.getModules()) {
						try {
							if (tempModulePOJO.getId().equals(module.getId())) {
								modulePOJO = tempModulePOJO;
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
						}
					}

					try {
						if (modulePOJO == null) {
							modulePOJO = new ModulePOJO();
							modulePOJO.setId(module.getId());
							modulePOJO.setName(module.getModuleName());
							modulePOJO.setImageURL(mediaUrlPath + module.getImage_url());
							modulePOJO.setDescription(module.getModule_description());
							modulePOJO.setOrderId(++moduleOrderId);
							int cmsId = cmsession.getId();
							if (!cmsessionIds.contains(cmsId)) {
								if (AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
									// System.out.println("Adding CMSession
									// Skill");
								}
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
							lessonPOJO.setLessonUrl(mediaUrlPath + "/lessonXMLs/" + lesson.getId() + ".zip");
							lessonPOJO.setImageUrl(lesson.getImage_url());
							String findLessonProgress = "select lesson_id, total_slide_count, cast (count(DISTINCT slide_id) as integer)+1 as slide_moved   from user_session_log where lesson_id = "
									+ lesson.getId() + " and user_id = " + istarUserId
									+ " group by lesson_id, total_slide_count limit 1";
							List<HashMap<String, Object>> progressData = util.executeQuery(findLessonProgress);
							if (progressData.size() > 0) {
								int slide_moved = (int) progressData.get(0).get("slide_moved");
								int totalSlide = 0;

								try {
									totalSlide = (int) progressData.get(0).get("total_slide_count");
								} catch (Exception e) {

								}

								if (totalSlide != 0) {
									int progress = (slide_moved * 100) / totalSlide;
									ConcreteItemPOJO.setProgress(progress);
								} else {
									ConcreteItemPOJO.setProgress(0);
								}

								String getCurrentSlide = "select slide_id from user_session_log where user_id = "
										+ istarUserId + " and lesson_id = " + lesson.getId()
										+ " order by id desc limit 1";
								List<HashMap<String, Object>> currentslideData = util.executeQuery(getCurrentSlide);
								if (currentslideData.size() > 0) {
									lessonPOJO.setCurrentSlideId((int) currentslideData.get(0).get("slide_id"));
								}

							} else {
								ConcreteItemPOJO.setProgress(0);
							}
							ConcreteItemPOJO.setId(lesson.getId());
							if (!lesson.getType().equalsIgnoreCase("ASSESSMENT")) {
								ConcreteItemPOJO.setType("LESSON_" + lesson.getType());
							} else {
								ConcreteItemPOJO.setType("ASSESSMENT");
							}
							ConcreteItemPOJO.setLesson(lessonPOJO);
							ConcreteItemPOJO.setOrderId(studentPlaylist.getId());
							ConcreteItemPOJO.setStatus(studentPlaylist.getStatus());
							ConcreteItemPOJO.setTaskId(studentPlaylist.getTaskId());
							if (modulePOJO.getSessions() != null && modulePOJO.getSessions().size() != 0) {
								List<SessionPOJO> sessions = modulePOJO.getSessions();
								for (SessionPOJO sessionPOJO : sessions) {
									if (cmsession.getId() == (int) sessionPOJO.getId()) {
										sessionPOJO.getLessons().add(ConcreteItemPOJO);
									} else {
										SessionPOJO pojo = new SessionPOJO();
										pojo.setId(cmsession.getId());
										pojo.setName(cmsession.getTitle());
										pojo.setDescription(cmsession.getDescription() != null
												? cmsession.getDescription() : "Not Available");
										pojo.setImageURL(
												cmsession.getImage_url() != null ? cmsession.getImage_url() : "");
										pojo.setOrderId(studentPlaylist.getId());

										List<ConcreteItemPOJO> concreteItemPOJOs = new ArrayList();
										concreteItemPOJOs.add(ConcreteItemPOJO);
										pojo.setLessons(concreteItemPOJOs);
										int sessionProgress = 0;
										int i = 0;

										if (pojo.getLessons() != null && pojo.getLessons().size() != 0) {
											for (ConcreteItemPOJO itemPOJO : pojo.getLessons()) {
												i++;
												sessionProgress += itemPOJO.getProgress() != null
														? itemPOJO.getProgress() : 0;
											}
										}

										if (i == 0) {
											pojo.setProgress(0);
										} else {
											pojo.setProgress(sessionProgress / i);
										}
										sessions.add(pojo);
									}
								}
								modulePOJO.setSessions(sessions);
							} else {
								SessionPOJO pojo = new SessionPOJO();
								pojo.setId(cmsession.getId());
								pojo.setName(cmsession.getTitle());
								pojo.setDescription(cmsession.getDescription() != null ? cmsession.getDescription()
										: "Not Available");
								pojo.setImageURL(cmsession.getImage_url() != null ? cmsession.getImage_url() : "");
								pojo.setOrderId(studentPlaylist.getId());

								List<ConcreteItemPOJO> concreteItemPOJOs = new ArrayList();
								concreteItemPOJOs.add(ConcreteItemPOJO);
								pojo.setLessons(concreteItemPOJOs);

								int sessionProgress = 0;
								int i = 0;
								if (pojo.getLessons() != null && pojo.getLessons().size() != 0) {
									for (ConcreteItemPOJO itemPOJO : pojo.getLessons()) {
										i++;
										sessionProgress += itemPOJO.getProgress() != null ? itemPOJO.getProgress() : 0;
									}
								}

								if (i == 0) {
									pojo.setProgress(0);
								} else {
									pojo.setProgress(sessionProgress / i);
								}
								modulePOJO.getSessions().add(pojo);
							}

							coursePOJO.getModules().add(modulePOJO);
						} else {

							if (!cmsessionIds.contains(cmsession.getId())) {
								if (AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
									// System.out.println("Adding CMSession
									// Skill");
								}
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
							lessonPOJO.setImageUrl(lesson.getImage_url());
							String findLessonProgress = "select lesson_id, total_slide_count, cast (count(DISTINCT slide_id) as integer)+1 as slide_moved   from user_session_log where lesson_id = "
									+ lesson.getId() + " and user_id = " + istarUserId
									+ " group by lesson_id, total_slide_count limit 1";
							List<HashMap<String, Object>> progressData = util.executeQuery(findLessonProgress);
							if (progressData.size() > 0) {
								int slide_moved = (int) progressData.get(0).get("slide_moved");
								int totalSlide = 0;

								try {
									totalSlide = (int) progressData.get(0).get("total_slide_count");
								} catch (Exception e) {

								}
								if (totalSlide != 0) {
									int progress = (slide_moved * 100) / totalSlide;
									ConcreteItemPOJO.setProgress(progress);
								} else {
									ConcreteItemPOJO.setProgress(0);
								}

								String getCurrentSlide = "select slide_id from user_session_log where user_id = "
										+ istarUserId + " and lesson_id = " + lesson.getId()
										+ " order by id desc limit 1";
								List<HashMap<String, Object>> currentslideData = util.executeQuery(getCurrentSlide);
								if (currentslideData.size() > 0) {
									lessonPOJO.setCurrentSlideId((int) currentslideData.get(0).get("slide_id"));
								}
							} else {
								ConcreteItemPOJO.setProgress(0);
							}

							if (!lesson.getType().equalsIgnoreCase("ASSESSMENT")) {
								ConcreteItemPOJO.setType("LESSON_" + lesson.getType());
							} else {
								ConcreteItemPOJO.setType("ASSESSMENT");
							}
							lessonPOJO.setOrderId(studentPlaylist.getId());
							lessonPOJO.setLessonUrl(mediaUrlPath + "/lessonXMLs/" + lesson.getId() + ".zip");
							ConcreteItemPOJO.setId(lesson.getId());
							ConcreteItemPOJO.setType("LESSON_" + lesson.getType());
							ConcreteItemPOJO.setLesson(lessonPOJO);
							ConcreteItemPOJO.setOrderId(studentPlaylist.getId());
							ConcreteItemPOJO.setStatus(studentPlaylist.getStatus());
							ConcreteItemPOJO.setTaskId(studentPlaylist.getTaskId());
							if (modulePOJO.getSessions() != null && modulePOJO.getSessions().size() != 0) {
								List<SessionPOJO> sessions = modulePOJO.getSessions();
								for (SessionPOJO sessionPOJO : sessions) {
									if (cmsession.getId() == (int) sessionPOJO.getId()) {
										sessionPOJO.getLessons().add(ConcreteItemPOJO);
									} else {

										SessionPOJO pojo = new SessionPOJO();
										pojo.setId(cmsession.getId());
										pojo.setName(cmsession.getTitle());
										pojo.setDescription(cmsession.getDescription() != null
												? cmsession.getDescription() : "Not Available");
										pojo.setImageURL(
												cmsession.getImage_url() != null ? cmsession.getImage_url() : "");
										pojo.setOrderId(studentPlaylist.getId());

										List<ConcreteItemPOJO> concreteItemPOJOs = new ArrayList();
										concreteItemPOJOs.add(ConcreteItemPOJO);
										pojo.setLessons(concreteItemPOJOs);
										int sessionProgress = 0;
										int i = 0;

										if (pojo.getLessons() != null && pojo.getLessons().size() != 0) {
											for (ConcreteItemPOJO itemPOJO : pojo.getLessons()) {
												i++;
												sessionProgress += itemPOJO.getProgress() != null
														? itemPOJO.getProgress() : 0;
											}
										}

										if (i == 0) {
											pojo.setProgress(0);
										} else {
											pojo.setProgress(sessionProgress / i);
										}
										sessions.add(pojo);
									}
								}
								modulePOJO.setSessions(sessions);
							} else {
								SessionPOJO pojo = new SessionPOJO();
								pojo.setId(cmsession.getId());
								pojo.setName(cmsession.getTitle());
								pojo.setDescription(cmsession.getDescription() != null ? cmsession.getDescription()
										: "Not Available");
								pojo.setImageURL(cmsession.getImage_url() != null ? cmsession.getImage_url() : "");
								pojo.setOrderId(studentPlaylist.getId());

								List<ConcreteItemPOJO> concreteItemPOJOs = new ArrayList();
								concreteItemPOJOs.add(ConcreteItemPOJO);
								pojo.setLessons(concreteItemPOJOs);
								int sessionProgress = 0;
								int i = 0;

								if (pojo.getLessons() != null && pojo.getLessons().size() != 0) {
									for (ConcreteItemPOJO itemPOJO : pojo.getLessons()) {
										i++;
										sessionProgress += itemPOJO.getProgress() != null ? itemPOJO.getProgress() : 0;
									}
								}

								if (i == 0) {
									pojo.setProgress(0);
								} else {
									pojo.setProgress(sessionProgress / i);
								}
								modulePOJO.getSessions().add(pojo);
							}
						}

						String finModuleStatus = "select cast( count(*) as integer) as incomple_lesssons from student_playlist where student_id = "
								+ istarUserId + " and status = 'SCHEDULED' and module_id=" + module.getId();
						List<HashMap<String, Object>> incompleteLessons = util.executeQuery(finModuleStatus);
						if (incompleteLessons.size() > 0
								&& (int) incompleteLessons.get(0).get("incomple_lesssons") > 0) {
							modulePOJO.setStatus("INCOMPLETE");
						} else {
							modulePOJO.setStatus("COMPLETED");
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					}

				}

			}

			String findecourseProgress = "SELECT 	cast (count(*) filter (where status ='SCHEDULED') as integer) as incomplete, cast(count(*) filter (where status ='COMPLETED') as integer) as completed FROM 	student_playlist WHERE 	student_id = "
					+ istarUserId + " AND course_id = " + courseId;
			List<HashMap<String, Object>> incompleteLessonsInCourse = util.executeQuery(findecourseProgress);
			if (incompleteLessonsInCourse.size() > 0 && (int) incompleteLessonsInCourse.get(0).get("incomplete") > 0
					&& (int) incompleteLessonsInCourse.get(0).get("completed") > 0) {
				int completed = (int) incompleteLessonsInCourse.get(0).get("completed");
				int incomplete = (int) incompleteLessonsInCourse.get(0).get("incomplete");
				Double percentage = ((double) completed * 100 / (completed + incomplete));
				coursePOJO.setProgress(percentage);
			} else {
				coursePOJO.setProgress(0d);
			}
			coursePOJO.sortModulesAndAssignStatus();
		}
		return coursePOJO;
	}

	@SuppressWarnings("unchecked")
	public List<SkillReportPOJO> getSkillsReportForCourseOfUser(int istarUserId, int courseId) {

		List<SkillReportPOJO> shellTree = getShellSkillTreeForCourse(courseId);
		for (SkillReportPOJO dd : shellTree) {
			if (AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
				// System.err.println("in mod shell tree " + dd.getName() + " -
				// " + dd.getId());
				// System.err.println("in mod shell tree " + " " +
				// dd.getUserPoints() + " " + dd.getTotalPoints() + " "
				// + dd.getPercentage());

			}
			for (SkillReportPOJO ll : dd.getSkills()) {
				if (AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
					// //System.err.println("in cmsession shell tree " +
					// ll.getName() + " - " + ll.getId());
					//// System.err.println("in cmsession shell tree " + " " +
					// ll.getUserPoints() + " " + ll.getTotalPoints()
					// + " " + ll.getPercentage());
				}
			}
		}
		DBUTILS utils = new DBUTILS();

		List<SkillReportPOJO> skillReportForCourse = fillShellTreeWithData(shellTree, istarUserId, courseId);

		for (SkillReportPOJO dd : skillReportForCourse) {
			if (AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
				// System.err.println("in filled mod tree "+dd.getName()+" -
				// "+dd.getId());
				// System.err.println("in filled mod tree "+"
				// "+dd.getUserPoints()+" "+dd.getTotalPoints()+"
				// "+dd.getPercentage());
			}
			for (SkillReportPOJO ll : dd.getSkills()) {
				if (AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
					// System.err.println("in filled sms tree "+ll.getName()+" -
					// "+ll.getId());
					// System.err.println("in filled sms tree "+"
					// "+ll.getUserPoints()+" "+ll.getTotalPoints()+"
					// "+ll.getPercentage());
				}
			}
		}

		return skillReportForCourse;
	}

	private List<SkillReportPOJO> fillShellTreeWithData(List<SkillReportPOJO> shellTree, int istarUserId,
			int courseId) {
		int per_assessment_points = 5, per_lesson_points = 5, per_question_points = 1, per_assessment_coins = 5,
				per_lesson_coins = 5, per_question_coins = 1;
		try {
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);

				per_assessment_points = Integer.parseInt(properties.getProperty("per_assessment_points"));
				per_lesson_points = Integer.parseInt(properties.getProperty("per_lesson_points"));
				per_question_points = Integer.parseInt(properties.getProperty("per_question_points"));
				per_assessment_coins = Integer.parseInt(properties.getProperty("per_assessment_coins"));
				per_lesson_coins = Integer.parseInt(properties.getProperty("per_lesson_coins"));
				per_question_coins = Integer.parseInt(properties.getProperty("per_question_coins"));

			}
		} catch (IOException e) {
			e.printStackTrace();

		}

		String getDataForTree = "SELECT T1. ID, T1.skill_objective, T1.points, T1.max_points, module_skill. ID AS module_id "
				+ "FROM ( WITH summary AS ( SELECT P . ID, P .skill_objective, custom_eval ( CAST ( TRIM ( REPLACE ( REPLACE ( REPLACE ( COALESCE (P .points, '0'), ':per_lesson_points', '"
				+ per_lesson_points + "' ), ':per_assessment_points', '" + per_assessment_points
				+ "' ), ':per_question_points', '" + per_question_points + "' ) ) AS TEXT ) ) AS points,"
				+ " custom_eval ( CAST ( TRIM ( REPLACE ( REPLACE ( REPLACE ( COALESCE (P .max_points, '0'), ':per_lesson_points', '"
				+ per_lesson_points + "' ), ':per_assessment_points', '" + per_assessment_points
				+ "' ), ':per_question_points', '" + per_question_points
				+ "' ) ) AS TEXT ) ) AS max_points, ROW_NUMBER () OVER ( PARTITION BY P .skill_objective, P .item_id ORDER BY P . TIMESTAMP DESC ) AS rk "
				+ "FROM user_gamification P, assessment_question, question " + "WHERE P .course_id = " + courseId
				+ " AND P .istar_user = " + istarUserId
				+ " AND P .item_id = assessment_question.questionid AND assessment_question.assessmentid IN ( SELECT DISTINCT item_id FROM user_gamification WHERE course_id = "
				+ courseId + " AND istar_user = " + istarUserId
				+ " AND item_type = 'ASSESSMENT' ) AND assessment_question.questionid = question. ID AND P .item_type = 'QUESTION' ) "
				+ "SELECT s.* FROM summary s WHERE s.rk = 1 ) T1 JOIN skill_objective cmsession_skill ON ( T1.skill_objective = cmsession_skill. ID ) JOIN module_skill_session_skill_map ON ( module_skill_session_skill_map.session_skill_id = cmsession_skill.id ) join skill_objective module_skill on (module_skill_session_skill_map.module_skill_id = module_skill.id )";
		if (AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
			// System.out.println("getDataForTree in course"+getDataForTree);
		}
		DBUTILS util = new DBUTILS();
		List<HashMap<String, Object>> data = util.executeQuery(getDataForTree);
		for (HashMap<String, Object> row : data) {
			int skillId = (int) row.get("skill_objective");
			double userPoints = (double) row.get("points");
			double maxPoints = (double) row.get("max_points");
			int moduleId = (int) row.get("module_id");

			for (SkillReportPOJO mod : shellTree) {
				if (mod.getId() == moduleId) {
					List<SkillReportPOJO> cmsSkills = mod.getSkills();
					for (SkillReportPOJO cmsSkill : cmsSkills) {
						if (cmsSkill.getId() == skillId) {

							if (cmsSkill.getAccessedFirstTime() == true) {

								cmsSkill.setUserPoints(Math.ceil(userPoints));
								cmsSkill.setTotalPoints(Math.ceil(maxPoints));
								cmsSkill.setAccessedFirstTime(false);
							} else {
								if (AppProperies.getProperty("serverConfig").equalsIgnoreCase("dev")) {
									// System.err.println(cmsSkill.getId()+" is
									// accessed for the second
									// time"+cmsSkill.getAccessedFirstTime());
								}
								double oldUserPoint = cmsSkill.getUserPoints() != null ? cmsSkill.getUserPoints() : 0d;
								double userPoints1 = userPoints + oldUserPoint;
								double oldTotalPoint = cmsSkill.getTotalPoints() != null ? cmsSkill.getTotalPoints()
										: 0d;
								double maxPoints1 = maxPoints + oldTotalPoint;

								cmsSkill.setUserPoints(Math.ceil(userPoints1));
								cmsSkill.setTotalPoints(Math.ceil(maxPoints1));
							}

							cmsSkill.calculatePercentage();
							// cmsSkills.add(cmsSkill);
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
		String mediaUrlPath = "";
		int per_assessment_points = 5, per_lesson_points = 5, per_question_points = 1, per_assessment_coins = 5,
				per_lesson_coins = 5, per_question_coins = 1;
		try {
			Properties properties = new Properties();
			String propertyFileName = "app.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);
			if (inputStream != null) {
				properties.load(inputStream);
				mediaUrlPath = properties.getProperty("media_url_path");
				per_assessment_points = Integer.parseInt(properties.getProperty("per_assessment_points"));
				per_lesson_points = Integer.parseInt(properties.getProperty("per_lesson_points"));
				per_question_points = Integer.parseInt(properties.getProperty("per_question_points"));
				per_assessment_coins = Integer.parseInt(properties.getProperty("per_assessment_coins"));
				per_lesson_coins = Integer.parseInt(properties.getProperty("per_lesson_coins"));
				per_question_coins = Integer.parseInt(properties.getProperty("per_question_coins"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<SkillReportPOJO> skillsReport = new ArrayList<SkillReportPOJO>();
		DBUTILS utils = new DBUTILS();

		CoreSkillService skillService = new CoreSkillService();
		CourseLevelSkill courseLevelSkill = skillService.getShellSkillTreeForCourse(courseId);
		ArrayList<Integer> sessionSkillId = new ArrayList<>();
		if (courseLevelSkill != null && courseLevelSkill.getModuleLevelSkill() != null) {
			for (ModuleLevelSkill modskill : courseLevelSkill.getModuleLevelSkill()) {
				SkillReportPOJO modPojo = new SkillReportPOJO();
				modPojo.setName(modskill.getSkillName().trim());
				modPojo.setId(modskill.getId());
				modPojo.setSkills(new ArrayList<>());
				modPojo.setDescription("");
				modPojo.setImageURL(null);
				List<SkillReportPOJO> sessionsSkills = new ArrayList<>();
				for (SessionLevelSkill sessioLevelSkill : modskill.getSessionLevelSkill()) {
					if (!sessionSkillId.contains(sessioLevelSkill.getId())) {
						sessionSkillId.add(sessioLevelSkill.getId());
					}
					SkillReportPOJO sessionSkill = new SkillReportPOJO();
					sessionSkill.setId(sessioLevelSkill.getId());
					sessionSkill.setName(sessioLevelSkill.getSkillName());
					sessionSkill.setUserPoints((double) 0);
					sessionsSkills.add(sessionSkill);
				}
				modPojo.setSkills(sessionsSkills);
				skillsReport.add(modPojo);
			}
		}

		HashMap<Integer, Double> sessionSkillMaxPoints = new HashMap<>();
		if (sessionSkillId.size() > 0) {
			String getMAxPoints = "SELECT skill_objective_id, SUM ( custom_eval ( CAST ( TRIM ( REPLACE ( REPLACE ( REPLACE ( COALESCE (max_points, '0'), ':per_lesson_points', '"
					+ per_lesson_points + "' ), ':per_assessment_points', '" + per_assessment_points
					+ "' ), ':per_question_points', '" + per_question_points + "' ) ) AS TEXT ) ) ) AS max_points "
					+ "FROM assessment_benchmark where skill_objective_id in (" + StringUtils.join(sessionSkillId, ",")
					+ ") and course_id =" + courseId + " GROUP BY skill_objective_id";
			List<HashMap<String, Object>> treeStructure = utils.executeQuery(getMAxPoints);
			for (HashMap<String, Object> treeRow : treeStructure) {
				int skill_objective_id = (int) treeRow.get("skill_objective_id");
				double maxPoints = (double) treeRow.get("max_points");
				sessionSkillMaxPoints.put(skill_objective_id, maxPoints);
			}
		}
		ArrayList<SkillReportPOJO> updatedTree = new ArrayList<>();
		for (SkillReportPOJO modPojo : skillsReport) {
			SkillReportPOJO updatedMod = modPojo;
			ArrayList<SkillReportPOJO> sessionSkills = new ArrayList<>();
			for (SkillReportPOJO sessionPojo : modPojo.getSkills()) {
				SkillReportPOJO sessionSkill = sessionPojo;
				if (sessionSkillMaxPoints.containsKey(sessionPojo.getId())) {
					sessionSkill.setTotalPoints(sessionSkillMaxPoints.get(sessionPojo.getId()));
				} else {
					sessionSkill.setTotalPoints(0d);
				}

				sessionSkills.add(sessionSkill);
			}
			updatedMod.setSkills(sessionSkills);
			updatedMod.calculatePercentage();
			updatedMod.calculateUserPoints();
			updatedMod.calculateTotalPoints();
			updatedTree.add(updatedMod);
		}
		return updatedTree;
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

	/*
	 * public List<SkillReportPOJO> getSkillReportTreeForCourseOfUser(int
	 * istarUserId, int courseId){
	 * 
	 * List<SkillReportPOJO> skillsReport = new ArrayList<SkillReportPOJO>();
	 * 
	 * BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO(); Session
	 * session = baseHibernateDAO.getSession();
	 * 
	 * String lessonsStatusSQL =
	 * "select course_id, module_id, cmsession_id,cast(count(case when status='COMPLETED' then 1 end) as integer) as completed_lessons, cast(count(*) as integer) as total_lessons from student_playlist where student_id="
	 * +istarUserId+" and course_id="+courseId+
	 * " group by course_id, module_id,cmsession_id";
	 * 
	 * SQLQuery sessionQuery = session.createSQLQuery(lessonsStatusSQL);
	 * List<Object[]> sessionStatusResult = sessionQuery.list();
	 * 
	 * 
	 * if(sessionStatusResult.size()>0){ HashMap<Integer, Module>
	 * modulesOfAssessment = new HashMap<Integer, Module>();
	 * AppAssessmentServices appAssessmentServices = new
	 * AppAssessmentServices(); for(Object[] sessionRow: sessionStatusResult){
	 * //Integer courseId = (Integer) sessionRow[0]; Integer moduleId =
	 * (Integer) sessionRow[1]; Integer cmsessionId = (Integer) sessionRow[2];
	 * Integer completedLessons = (Integer) sessionRow[3]; Integer totalLessons
	 * = (Integer) sessionRow[4];
	 * 
	 * SkillObjective cmsessionSkillObjective =
	 * appAssessmentServices.getSkillObjective(cmsessionSkillObjectiveId);
	 * SkillReportPOJO moduleSkillReportPOJO = null; SkillReportPOJO
	 * cmsessionSkillReportPOJO = null;
	 * 
	 * if (modulesOfAssessment.containsKey(moduleId)) { for (SkillReportPOJO
	 * tempModuleSkillReport : skillsReport) { if (tempModuleSkillReport.getId()
	 * == moduleId) { moduleSkillReportPOJO = tempModuleSkillReport; break; } }
	 * 
	 * cmsessionSkillReportPOJO = new SkillReportPOJO();
	 * cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
	 * cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
	 * cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
	 * 
	 * 
	 * if(courseStatus.containsKey(cmsessionId)){
	 * cmsessionSkillReportPOJO.setTotalPoints(totalPoints +
	 * (benchmark*courseStatus.get(cmsessionId).get("totalLessons")));
	 * cmsessionSkillReportPOJO.setUserPoints(userPoints +
	 * (benchmark*courseStatus.get(cmsessionId).get("completedLessons")));
	 * }else{ cmsessionSkillReportPOJO.setTotalPoints(totalPoints);
	 * cmsessionSkillReportPOJO.setUserPoints(userPoints); }
	 * 
	 * moduleSkillReportPOJO.getSkills().add(cmsessionSkillReportPOJO);
	 * moduleSkillReportPOJO.calculateTotalPoints();
	 * moduleSkillReportPOJO.calculateUserPoints();
	 * moduleSkillReportPOJO.calculatePercentage();
	 * moduleSkillReportPOJO.generateMessage();
	 * 
	 * } else { Module module = getModule(moduleId); if (module != null) {
	 * modulesOfAssessment.put(moduleId, module);
	 * 
	 * moduleSkillReportPOJO = new SkillReportPOJO();
	 * moduleSkillReportPOJO.setId(module.getId());
	 * moduleSkillReportPOJO.setName(module.getModuleName());
	 * moduleSkillReportPOJO.setDescription(module.getModule_description());
	 * moduleSkillReportPOJO.setImageURL(module.getImage_url());
	 * 
	 * List<SkillReportPOJO> cmsessionSkillsReport = new
	 * ArrayList<SkillReportPOJO>();
	 * 
	 * cmsessionSkillReportPOJO = new SkillReportPOJO();
	 * cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
	 * cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
	 * cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
	 * 
	 * if(courseStatus.containsKey(cmsessionId)){
	 * cmsessionSkillReportPOJO.setTotalPoints(totalPoints +
	 * (benchmark*courseStatus.get(cmsessionId).get("totalLessons")));
	 * cmsessionSkillReportPOJO.setUserPoints(userPoints +
	 * (benchmark*courseStatus.get(cmsessionId).get("completedLessons")));
	 * }else{ cmsessionSkillReportPOJO.setTotalPoints(totalPoints);
	 * cmsessionSkillReportPOJO.setUserPoints(userPoints); }
	 * 
	 * cmsessionSkillsReport.add(cmsessionSkillReportPOJO);
	 * moduleSkillReportPOJO.setSkills(cmsessionSkillsReport);
	 * 
	 * moduleSkillReportPOJO.calculateTotalPoints();
	 * moduleSkillReportPOJO.calculateUserPoints();
	 * moduleSkillReportPOJO.calculatePercentage();
	 * moduleSkillReportPOJO.generateMessage();
	 * skillsReport.add(moduleSkillReportPOJO); }else{
	 * //System.out.println("Module is null with id->"+moduleId); } }
	 * 
	 * } } return null; }
	 */

	/*
	 * public List<SkillReportPOJO> getSkillsReportForCourseOfUser(int
	 * istarUserId, int courseId){ //long previousTime =
	 * System.currentTimeMillis(); ////System.err.println("500000000->" +
	 * "Time->"+(System.currentTimeMillis()-previousTime));
	 * List<SkillReportPOJO> allSkillsReport = new ArrayList<SkillReportPOJO>();
	 * HashMap<Integer, HashMap<String, Object>> skillsMap =
	 * getPointsAndCoinsOfCmsessionSkillsOfCourseForUser(istarUserId, courseId);
	 * Course course = getCourse(courseId);
	 * 
	 * for(Module module : course.getModules()){ for(SkillObjective
	 * moduleSkillObjective : module.getSkillObjectives()){ SkillReportPOJO
	 * moduleSkillReportPOJO=null; for(SkillReportPOJO tempModuleSkillReportPOJO
	 * : allSkillsReport){
	 * if(tempModuleSkillReportPOJO.getId()==moduleSkillObjective.getId()){
	 * moduleSkillReportPOJO = tempModuleSkillReportPOJO; } }
	 * 
	 * if(moduleSkillReportPOJO==null){ moduleSkillReportPOJO = new
	 * SkillReportPOJO();
	 * moduleSkillReportPOJO.setId(moduleSkillObjective.getId());
	 * moduleSkillReportPOJO.setName(moduleSkillObjective.getName());
	 * 
	 * List<SkillReportPOJO> allCmsessionSkills = new
	 * ArrayList<SkillReportPOJO>();
	 * 
	 * for(Cmsession cmsession : module.getCmsessions()){ for(SkillObjective
	 * cmsessionSkillObjective : cmsession.getSkillObjectives()){
	 * SkillReportPOJO cmsessionSkillReportPOJO = new SkillReportPOJO();
	 * 
	 * cmsessionSkillReportPOJO.setId(cmsessionSkillObjective.getId());
	 * cmsessionSkillReportPOJO.setName(cmsessionSkillObjective.getName());
	 * ////System.err.println("5->" +
	 * "Time->"+(System.currentTimeMillis()-previousTime));
	 * cmsessionSkillReportPOJO.setTotalPoints(getMaxPointsOfCmsessionSkill(
	 * cmsessionSkillObjective.getId())); ////System.err.println("6->" +
	 * "Time->"+(System.currentTimeMillis()-previousTime));
	 * if(skillsMap.containsKey(cmsessionSkillObjective.getId())){
	 * cmsessionSkillReportPOJO.setUserPoints((Double)
	 * skillsMap.get(cmsessionSkillObjective.getId()).get("points")); }else{
	 * //System.out.println("CMSESSION SKILL NOT PRESENT->" +
	 * cmsessionSkillObjective.getId());
	 * cmsessionSkillReportPOJO.setUserPoints(0.0); }
	 * allCmsessionSkills.add(cmsessionSkillReportPOJO); } }
	 * moduleSkillReportPOJO.setSkills(allCmsessionSkills);
	 * moduleSkillReportPOJO.calculateUserPoints();
	 * moduleSkillReportPOJO.calculateTotalPoints();
	 * moduleSkillReportPOJO.calculatePercentage();
	 * moduleSkillReportPOJO.generateMessage(); }
	 * allSkillsReport.add(moduleSkillReportPOJO); } } return allSkillsReport; }
	 */

	public Double getTotalPointsOfCourseForUser(int istarUserId, int courseId, int numberOfLessons) {

		Integer benchmark = 0;
		try {
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

		Double totalPoints = getMaxPointsOfCourseFromAssessment(courseId) + numberOfLessons * benchmark;
		return totalPoints;
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
	/*
	 * public Double calculateMaxPointsForCmsession(int
	 * cmsessionSkillObjectiveId){
	 * 
	 * Double maxPoints = 0.0;
	 * 
	 * Integer difficultyLevelSum =
	 * getDifficultyLevelSumOfCmsessionSkill(cmsessionSkillObjectiveId); Integer
	 * numberOfLessons =
	 * getNumberOfLessonsForCmsessionOfUserForCourse(cmsessionId, courseId,
	 * istarUserId);
	 * 
	 * //System.out.println("difficultyLevelSum->"+difficultyLevelSum+
	 * " numberOfLessons->" + numberOfLessons);
	 * 
	 * try{ Properties properties = new Properties(); String propertyFileName =
	 * "app.properties"; InputStream inputStream =
	 * getClass().getClassLoader().getResourceAsStream(propertyFileName); if
	 * (inputStream != null) { properties.load(inputStream); String
	 * pointsBenchmark = properties.getProperty("pointsBenchmark"); Integer
	 * benchmark = Integer.parseInt(pointsBenchmark);
	 * 
	 * maxPoints = (difficultyLevelSum + (numberOfLessons* benchmark))*1.0; } }
	 * catch (IOException e) { e.printStackTrace(); } return maxPoints; }
	 */

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

	public Double getProgressOfUserForCourse(int istarUserId, int courseId) {
		String sql = "select COALESCE(cast(count(case when status='INCOMPLETE' then 1 end) as integer),0) as incomplete, COALESCE(cast(count(case when status='COMPLETED' then 1 end) as integer),0) as complete  from student_playlist where student_id= :istarUserId and course_id= :courseId";
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId", istarUserId);
		query.setParameter("courseId", courseId);

		Object[] result = (Object[]) query.list().get(0);

		Integer completed = (Integer) result[1];
		Integer total = (Integer) result[0] + (Integer) result[1];

		Double progress = 0d;
		if (completed != 0) {
			progress = ((completed * 1.0) / total) * 100.0;
		}
		return progress;
	}

	@SuppressWarnings("unchecked")
	public List<Cmsession> getCmsessionsOfModule(int moduleId) {

		List<Cmsession> allCmsessions = new ArrayList<Cmsession>();

		String sql = "select cmsession_id from cmsession_module where module_id= :moduleId";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("moduleId", moduleId);

		List<Integer> results = query.list();

		for (Integer cmsessionId : results) {
			allCmsessions.add(getCmsession(cmsessionId));
		}
		// System.out.println("allCmsessions" + allCmsessions.size());
		return allCmsessions;
	}

	@SuppressWarnings("unchecked")
	public List<Lesson> getLessonsOfCmsession(int cmsessionId) {

		List<Lesson> allLessons = new ArrayList<Lesson>();

		String sql = "select lesson_id from lesson_cmsession where cmsession_id= :cmsessionId";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("cmsessionId", cmsessionId);

		List<Integer> results = query.list();

		for (Integer lessonId : results) {
			allLessons.add(getLesson(lessonId));
		}
		// System.out.println("allLessons" + allLessons.size());
		return allLessons;
	}

	@SuppressWarnings("unchecked")
	public Module getModuleOfLesson(int lessonId) {

		Module module = null;
		String sql = "select module_id from cmsession_module where cmsession_id in( select cmsession_id from lesson_cmsession where lesson_id="
				+ lessonId + " limit 1) limit 1";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();

		SQLQuery query = session.createSQLQuery(sql);

		List<Integer> results = query.list();

		Integer moduleId = null;

		if (results.size() > 0) {
			moduleId = (Integer) query.list().get(0);
		}

		if (moduleId != null) {
			module = getModule(moduleId);
		}

		return module;
	}

	public Double getMaxPointsOfCourseFromAssessment(Integer courseId) {

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