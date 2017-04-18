package com.istarindia.apps.services;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.StudentPlaylist;
import com.viksitpro.core.dao.entities.StudentPlaylistDAO;

public class StudentPlaylistServices {
	
	public StudentPlaylist updateStatus(StudentPlaylist studentPlaylist, String status){
		
		studentPlaylist.setStatus(status);		
		studentPlaylist = updateStudentPlaylistToDAO(studentPlaylist);
		
		return studentPlaylist;
	}
	
	@SuppressWarnings("unchecked")
	public List<StudentPlaylist> getStudentPlaylistOfUser(int istarUserId){

		String hql = "from StudentPlaylist studentPlaylist where istarUser.id= :istarUser";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		Query query = session.createQuery(hql);
		query.setParameter("istarUser",istarUserId);
		
		List<StudentPlaylist> allStudentPlaylist = query.list();

		return allStudentPlaylist;		
	}
	
	@SuppressWarnings("unchecked")
	public List<Integer> getStudentPlaylistOfCourseforUser(int istarUserId){

		String sql = "select distinct course_id from student_playlist where student_id= :istarUser";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("istarUser",istarUserId);
		
		List<Integer> allCourses = query.list();
		
		return allCourses;		
	}
	
	public StudentPlaylist getStudentPlaylist(Integer studentPlaylistId){
		StudentPlaylistDAO studentPlaylistDAO = new StudentPlaylistDAO();
		StudentPlaylist studentPlaylist;
		try{
		studentPlaylist = studentPlaylistDAO.findById(studentPlaylistId);
		}catch(IllegalArgumentException e){
			studentPlaylist = null;
		}
		return studentPlaylist;
	}

	public StudentPlaylist saveStudentPlaylistToDAO(StudentPlaylist studentPlaylist) {

		StudentPlaylistDAO studentPlaylistDAO = new StudentPlaylistDAO();

		Session studentPlaylistSession = studentPlaylistDAO.getSession();
		Transaction studentPlaylistTransaction = null;
		try {
			studentPlaylistTransaction = studentPlaylistSession.beginTransaction();
			studentPlaylistSession.save(studentPlaylist);
			studentPlaylistTransaction.commit();
		} catch (HibernateException e) {
			e.printStackTrace();
			if (studentPlaylistTransaction != null)
				studentPlaylistTransaction.rollback();
		} finally {
			studentPlaylistSession.close();
		}
		return studentPlaylist;
	}

	public StudentPlaylist updateStudentPlaylistToDAO(StudentPlaylist studentPlaylist) {

		StudentPlaylistDAO studentPlaylistDAO = new StudentPlaylistDAO();

		Session studentPlaylistSession = studentPlaylistDAO.getSession();
		Transaction studentPlaylistTransaction = null;
		try {
			studentPlaylistTransaction = studentPlaylistSession.beginTransaction();
			studentPlaylistSession.update(studentPlaylist);
			studentPlaylistTransaction.commit();
		} catch (HibernateException e) {
			e.printStackTrace();
			if (studentPlaylistTransaction != null)
				studentPlaylistTransaction.rollback();
		} finally {
			studentPlaylistSession.close();
		}
		return studentPlaylist;
	}
}


