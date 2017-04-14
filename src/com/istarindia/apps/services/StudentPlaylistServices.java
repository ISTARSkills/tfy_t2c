package com.istarindia.apps.services;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
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
		
		System.out.println("getStudentPlaylistOfUser START");
		
		String hql = "from StudentPlaylist studentPlaylist where istarUser.id= :istarUser";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		Query query = session.createQuery(hql);
		query.setParameter("istarUser",istarUserId);
		
		List<StudentPlaylist> allStudentPlaylist = query.list();
		
		System.out.println("getStudentPlaylistOfUser END");
		
		return allStudentPlaylist;		
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


