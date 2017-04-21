package com.istarindia.apps.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.istarindia.android.pojo.DailyTaskPOJO;
import com.viksitpro.core.dao.entities.BaseHibernateDAO;

public class AppCalendarServices {

	
	@SuppressWarnings("unchecked")
	public List<DailyTaskPOJO> getDailyTask(int istarUserId, int day, int month, int year){
		
		List<DailyTaskPOJO> allTasks = new ArrayList<DailyTaskPOJO>();
	
		String sql = "select * from (select id, case when task.is_active then 'INCOMPLETE' else 'COMPLETE' END as status, case when task.is_active then null else task.updated_at END as completed_at, name, task.start_date,task.end_date, extract(day from task.start_date) as day, extract(month from task.start_date) as month, extract(year from task.start_date) as year from task where actor= :istarUserId) as all_tasks where year= :year and month= :month and day= :day";

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
			
			DailyTaskPOJO dailyTaskPOJO = new DailyTaskPOJO();
			
			dailyTaskPOJO.setId(id);
			dailyTaskPOJO.setName(name);
			dailyTaskPOJO.setStatus(status);
			dailyTaskPOJO.setStartDate(startDate);
			dailyTaskPOJO.setEndDate(endDate);
			
			if(status.equals("COMPLETE")){
				dailyTaskPOJO.setCompletedAt(completedAt);
			}
			allTasks.add(dailyTaskPOJO);
		}
		return allTasks;
	}
	
	@SuppressWarnings("unchecked")
	public List<DailyTaskPOJO> getMonthlyTask(int istarUserId, int month, int year){
		
		List<DailyTaskPOJO> allTasks = new ArrayList<DailyTaskPOJO>();
	
		String sql = "select * from (select id, case when task.is_active then 'INCOMPLETE' else 'COMPLETE' END as status, case when task.is_active then null else task.updated_at END as completed_at, name, task.start_date,task.end_date, extract(day from task.start_date) as day, extract(month from task.start_date) as month, extract(year from task.start_date) as year from task where actor= :istarUserId) as all_tasks where year= :year and month= :month";

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
			
			DailyTaskPOJO dailyTaskPOJO = new DailyTaskPOJO();
			
			dailyTaskPOJO.setId(id);
			dailyTaskPOJO.setName(name);
			dailyTaskPOJO.setStatus(status);
			dailyTaskPOJO.setStartDate(startDate);
			dailyTaskPOJO.setEndDate(endDate);
			
			if(status.equals("COMPLETE")){
				dailyTaskPOJO.setCompletedAt(completedAt);
			}
			allTasks.add(dailyTaskPOJO);
		}
		return allTasks;
	}
	
	@SuppressWarnings("unchecked")
	public List<DailyTaskPOJO> getYearlyTask(int istarUserId, int year){
		
		List<DailyTaskPOJO> allTasks = new ArrayList<DailyTaskPOJO>();
	
		String sql = "select * from (select id, case when task.is_active then 'INCOMPLETE' else 'COMPLETE' END as status, case when task.is_active then null else task.updated_at END as completed_at, name, task.start_date,task.end_date, extract(day from task.start_date) as day, extract(month from task.start_date) as month, extract(year from task.start_date) as year from task where actor= :istarUserId) as all_tasks where year= :year";

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
			
			DailyTaskPOJO dailyTaskPOJO = new DailyTaskPOJO();
			
			dailyTaskPOJO.setId(id);
			dailyTaskPOJO.setName(name);
			dailyTaskPOJO.setStatus(status);
			dailyTaskPOJO.setStartDate(startDate);
			dailyTaskPOJO.setEndDate(endDate);
			
			if(status.equals("COMPLETE")){
				dailyTaskPOJO.setCompletedAt(completedAt);
			}
			allTasks.add(dailyTaskPOJO);
		}
		return allTasks;
	}
	
	@SuppressWarnings("unchecked")
	public List<DailyTaskPOJO> getAllTask(int istarUserId){
		
		List<DailyTaskPOJO> allTasks = new ArrayList<DailyTaskPOJO>();
	
		String sql = "select * from (select id, case when task.is_active then 'INCOMPLETE' else 'COMPLETE' END as status, case when task.is_active then null else task.updated_at END as completed_at, name, task.start_date,task.end_date, extract(day from task.start_date) as day, extract(month from task.start_date) as month, extract(year from task.start_date) as year from task where actor= :istarUserId) as all_tasks";

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
			
			DailyTaskPOJO dailyTaskPOJO = new DailyTaskPOJO();
			
			dailyTaskPOJO.setId(id);
			dailyTaskPOJO.setName(name);
			dailyTaskPOJO.setStatus(status);
			dailyTaskPOJO.setStartDate(startDate);
			dailyTaskPOJO.setEndDate(endDate);
			
			if(status.equals("COMPLETE")){
				dailyTaskPOJO.setCompletedAt(completedAt);
			}
			allTasks.add(dailyTaskPOJO);
		}
		return allTasks;
	}
}
