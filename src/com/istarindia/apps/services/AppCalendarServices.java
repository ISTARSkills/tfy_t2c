package com.istarindia.apps.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.istarindia.android.pojo.DailyTaskPOJO;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.utilities.TaskItemCategory;

public class AppCalendarServices {

	
	@SuppressWarnings("unchecked")
	public List<DailyTaskPOJO> getDailyTask(int istarUserId, int day, int month, int year){
		
		List<DailyTaskPOJO> allTasks = new ArrayList<DailyTaskPOJO>();
	
		String sql = "select * from (select id, case when task.is_active then 'INCOMPLETE' else 'COMPLETED' END as status, case when task.is_active then null else task.updated_at END as completed_at, name, task.start_date,task.end_date, extract(day from task.start_date) as day, extract(month from task.start_date) as month, extract(year from task.start_date) as year, item_id, item_type from task where actor= :istarUserId) as all_tasks where year= :year and month= :month and day= :day";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId", istarUserId);
		query.setParameter("year", year);
		query.setParameter("month", month);
		query.setParameter("day", day);
		
		List<Object[]> result = query.list();

		for(Object[] dailyTask : result){
			Integer id = (Integer) dailyTask[0];
			String status = (String) dailyTask[1];
			Timestamp completedAt = (Timestamp) dailyTask[2];
			String name = (String) dailyTask[3];
			Timestamp startDate = (Timestamp) dailyTask[4];
			Timestamp endDate = (Timestamp) dailyTask[5];
			Integer itemId = (Integer) dailyTask[9];
			String itemType = (String) dailyTask[10];
			
			DailyTaskPOJO dailyTaskPOJO = new DailyTaskPOJO();
			
			dailyTaskPOJO.setId(id);
			dailyTaskPOJO.setName(name);
			dailyTaskPOJO.setStatus(status);
			dailyTaskPOJO.setStartDate(startDate);
			dailyTaskPOJO.setEndDate(endDate);
			dailyTaskPOJO.setItemId(itemId);
			dailyTaskPOJO.setItemType(itemType);
						
			if(status.equals("COMPLETED")){
				dailyTaskPOJO.setCompletedAt(completedAt);
			}
			
			if(itemType.equals(TaskItemCategory.LESSON)){
				AppCourseServices appCourseServices = new AppCourseServices();
				Lesson lesson = appCourseServices.getLesson(itemId);
				dailyTaskPOJO.setItemType(itemType+"_"+lesson.getType());
			}else{
				dailyTaskPOJO.setItemType(itemType);
			}
			
			
			allTasks.add(dailyTaskPOJO);
		}
		return allTasks;
	}
	
	@SuppressWarnings("unchecked")
	public List<DailyTaskPOJO> getMonthlyTask(int istarUserId, int month, int year){
		
		List<DailyTaskPOJO> allTasks = new ArrayList<DailyTaskPOJO>();
	
		String sql = "select * from (select id, case when task.is_active then 'INCOMPLETE' else 'COMPLETED' END as status, case when task.is_active then null else task.updated_at END as completed_at, name, task.start_date,task.end_date, extract(day from task.start_date) as day, extract(month from task.start_date) as month, extract(year from task.start_date) as year, item_id, item_type from task where actor= :istarUserId) as all_tasks where year= :year and month= :month";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId", istarUserId);
		query.setParameter("year", year);
		query.setParameter("month", month);
		
		List<Object[]> result = query.list();
		
		for(Object[] dailyTask : result){
			Integer id = (Integer) dailyTask[0];
			String status = (String) dailyTask[1];
			Timestamp completedAt = (Timestamp) dailyTask[2];
			String name = (String) dailyTask[3];
			Timestamp startDate = (Timestamp) dailyTask[4];
			Timestamp endDate = (Timestamp) dailyTask[5];
			Integer itemId = (Integer) dailyTask[9];
			String itemType = (String) dailyTask[10];
			
			DailyTaskPOJO dailyTaskPOJO = new DailyTaskPOJO();
			
			dailyTaskPOJO.setId(id);
			dailyTaskPOJO.setName(name);
			dailyTaskPOJO.setStatus(status);
			dailyTaskPOJO.setStartDate(startDate);
			dailyTaskPOJO.setEndDate(endDate);
			dailyTaskPOJO.setItemId(itemId);
			
			if(itemType.equals(TaskItemCategory.LESSON)){
				AppCourseServices appCourseServices = new AppCourseServices();
				Lesson lesson = appCourseServices.getLesson(itemId);
				dailyTaskPOJO.setItemType(itemType+"_"+lesson.getType());
			}else{
				dailyTaskPOJO.setItemType(itemType);
			}
			
			if(status.equals("COMPLETED")){
				dailyTaskPOJO.setCompletedAt(completedAt);
			}
			allTasks.add(dailyTaskPOJO);
		}
		return allTasks;
	}
	
	@SuppressWarnings("unchecked")
	public List<DailyTaskPOJO> getYearlyTask(int istarUserId, int year){
		
		List<DailyTaskPOJO> allTasks = new ArrayList<DailyTaskPOJO>();
	
		String sql = "select * from (select id, case when task.is_active then 'INCOMPLETE' else 'COMPLETED' END as status, case when task.is_active then null else task.updated_at END as completed_at, name, task.start_date,task.end_date, extract(day from task.start_date) as day, extract(month from task.start_date) as month, extract(year from task.start_date) as year, item_id, item_type from task where actor= :istarUserId) as all_tasks where year= :year";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId", istarUserId);
		query.setParameter("year", year);
		
		List<Object[]> result = query.list();
		
		for(Object[] dailyTask : result){
			Integer id = (Integer) dailyTask[0];
			String status = (String) dailyTask[1];
			Timestamp completedAt = (Timestamp) dailyTask[2];
			String name = (String) dailyTask[3];
			Timestamp startDate = (Timestamp) dailyTask[4];
			Timestamp endDate = (Timestamp) dailyTask[5];
			Integer itemId = (Integer) dailyTask[9];
			String itemType = (String) dailyTask[10];
			
			DailyTaskPOJO dailyTaskPOJO = new DailyTaskPOJO();
			
			dailyTaskPOJO.setId(id);
			dailyTaskPOJO.setName(name);
			dailyTaskPOJO.setStatus(status);
			dailyTaskPOJO.setStartDate(startDate);
			dailyTaskPOJO.setEndDate(endDate);
			dailyTaskPOJO.setItemId(itemId);
			
			if(itemType.equals(TaskItemCategory.LESSON)){
				AppCourseServices appCourseServices = new AppCourseServices();
				Lesson lesson = appCourseServices.getLesson(itemId);
				dailyTaskPOJO.setItemType(itemType+"_"+lesson.getType());
			}else{
				dailyTaskPOJO.setItemType(itemType);
			}
			
			if(status.equals("COMPLETED")){
				dailyTaskPOJO.setCompletedAt(completedAt);
			}
			allTasks.add(dailyTaskPOJO);
		}
		return allTasks;
	}
	
	@SuppressWarnings("unchecked")
	public List<DailyTaskPOJO> getAllTask(int istarUserId){
		
		List<DailyTaskPOJO> allTasks = new ArrayList<DailyTaskPOJO>();
	
		String sql = "select * from (select id, case when task.is_active then 'INCOMPLETE' else 'COMPLETED' END as status, case when task.is_active then null else task.updated_at END as completed_at, name, task.start_date,task.end_date, extract(day from task.start_date) as day, extract(month from task.start_date) as month, extract(year from task.start_date) as year, item_id, item_type from task where actor= :istarUserId) as all_tasks";

		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUserId", istarUserId);
		
		List<Object[]> result = query.list();
		
		for(Object[] dailyTask : result){
			Integer id = (Integer) dailyTask[0];
			String status = (String) dailyTask[1];
			Timestamp completedAt = (Timestamp) dailyTask[2];
			String name = (String) dailyTask[3];
			Timestamp startDate = (Timestamp) dailyTask[4];
			Timestamp endDate = (Timestamp) dailyTask[5];
			Integer itemId = (Integer) dailyTask[9];
			String itemType = (String) dailyTask[10];
			
			DailyTaskPOJO dailyTaskPOJO = new DailyTaskPOJO();
			
			dailyTaskPOJO.setId(id);
			dailyTaskPOJO.setName(name);
			dailyTaskPOJO.setStatus(status);
			dailyTaskPOJO.setStartDate(startDate);
			dailyTaskPOJO.setEndDate(endDate);
			dailyTaskPOJO.setItemId(itemId);
			
			if(itemType.equals(TaskItemCategory.LESSON)){
				AppCourseServices appCourseServices = new AppCourseServices();
				Lesson lesson = appCourseServices.getLesson(itemId);
				dailyTaskPOJO.setItemType(itemType+"_"+lesson.getType());
			}else{
				dailyTaskPOJO.setItemType(itemType);
			}
			
			if(status.equals("COMPLETED")){
				dailyTaskPOJO.setCompletedAt(completedAt);
			}
			allTasks.add(dailyTaskPOJO);
		}
		return allTasks;
	}
	
	public DailyTaskPOJO getDailyTaskPOJO(Task task) {

		DailyTaskPOJO dailyTaskPOJO = null;
		if (task != null) {
			dailyTaskPOJO = new DailyTaskPOJO();
			dailyTaskPOJO.setId(task.getId());
			dailyTaskPOJO.setName(task.getName());
			dailyTaskPOJO.setStatus(task.getState());
			dailyTaskPOJO.setStartDate(task.getStartDate());
			dailyTaskPOJO.setEndDate(task.getEndDate());
			dailyTaskPOJO.setItemId(task.getItemId());

			if (task.getItemType().equals(TaskItemCategory.LESSON)) {
				AppCourseServices appCourseServices = new AppCourseServices();
				Lesson lesson = appCourseServices.getLesson(task.getItemId());
				dailyTaskPOJO.setItemType(task.getItemType() + "_" + lesson.getType());
			} else {
				dailyTaskPOJO.setItemType(task.getItemType());
			}

			if (task.getState().equals("COMPLETED")) {
				dailyTaskPOJO.setCompletedAt(task.getUpdatedAt());
			}
		}
		return dailyTaskPOJO;
	}
}
