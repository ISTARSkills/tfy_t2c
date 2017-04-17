package com.istarindia.apps.services;

import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.BatchGroup;
import com.viksitpro.core.dao.entities.BatchStudents;
import com.viksitpro.core.dao.entities.BatchStudentsDAO;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class AppBatchStudentsServices {

	public BatchStudents createBatchStudents(int istarUserId, int batchGroupId){
		
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(istarUserId);
		
		AppBatchGroupServices batchGroupServices = new AppBatchGroupServices();
		BatchGroup batchGroup = batchGroupServices.getBatchGroup(batchGroupId);
				
		BatchStudents batchStudents = new BatchStudents();
		
		batchStudents.setIstarUser(istarUser);
		batchStudents.setBatchGroup(batchGroup);
				
		batchStudents = saveBatchStudentsToDAO(batchStudents);
		
		return batchStudents;
	}
	
	public BatchStudents createBatchStudents(int istarUserId, String batchCode){
		
		IstarUserServices istarUserServices = new IstarUserServices();
		IstarUser istarUser = istarUserServices.getIstarUser(istarUserId);
		
		AppBatchGroupServices batchGroupServices = new AppBatchGroupServices();
		BatchGroup batchGroup = batchGroupServices.getBatchGroupByBatchCode(batchCode);
				
		BatchStudents batchStudents = new BatchStudents();
		
		batchStudents.setIstarUser(istarUser);
		batchStudents.setBatchGroup(batchGroup);
				
		batchStudents = saveBatchStudentsToDAO(batchStudents);
		
		return batchStudents;
	}
	
	public BatchGroup getBatchGroupOfStudent(int istarUserId){
	
		BatchGroup batchGroup = null;
		String hql = "from BatchStudents batchStudents where istarUser.id= :istarUser";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		Query query = session.createQuery(hql);
		query.setParameter("istarUser",istarUserId);
		
		List<BatchStudents> allBatchGroupOfUser = query.list();
		
		if(allBatchGroupOfUser.size() > 0){
			batchGroup = allBatchGroupOfUser.get(0).getBatchGroup();
		}		
		return batchGroup;
	}
	
	public List<IstarUser> getBatchColleaguesOfUsers(Integer istarUserId){
		
		AppBatchStudentsServices appBatchStudentsServices = new AppBatchStudentsServices();
		
		List<IstarUser> allUsersOfABatch = new ArrayList<IstarUser>();		
		List<BatchStudents> allbatchStudents = appBatchStudentsServices.getBatchStudentsOfUser(istarUserId);
				
		if(allbatchStudents.size()>0){
			BatchGroup batchGroup = allbatchStudents.get(0).getBatchGroup();
			
			for(BatchStudents batchStudent : appBatchStudentsServices.getBatchStudentsOfABatchGroup(batchGroup)){				
				allUsersOfABatch.add(batchStudent.getIstarUser());
			}	
		}
		return allUsersOfABatch;		
	}	
	
	@SuppressWarnings("unchecked")
	public List<BatchStudents> getBatchStudentsOfUser(Integer istarUserId){
				
		String hql = "from BatchStudents batchStudents where istarUser.id= :istarUser";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		Query query = session.createQuery(hql);
		query.setParameter("istarUser",istarUserId);
		
		List<BatchStudents> allBatchStudents = query.list();

		return allBatchStudents;
	}
	
	@SuppressWarnings("unchecked")
	public List<BatchStudents> getBatchStudentsOfABatchGroup(BatchGroup batchGroup){
		
		String hql = "from BatchStudents batchStudents where batchGroup.id= :batchGroup";
		
		BaseHibernateDAO baseHibernateDAO = new BaseHibernateDAO();
		Session session = baseHibernateDAO.getSession();
		
		Query query = session.createQuery(hql);
		query.setParameter("batchGroup",batchGroup.getId());
		
		List<BatchStudents> allBatchStudents = query.list();
		
		return allBatchStudents;
	}
	
	public BatchStudents saveBatchStudentsToDAO(BatchStudents batchStudents) {

		BatchStudentsDAO batchStudentsDAO = new BatchStudentsDAO();

		Session batchStudentsSession = batchStudentsDAO.getSession();
		Transaction batchStudentsTransaction = null;
		try {
			batchStudentsTransaction = batchStudentsSession.beginTransaction();
			batchStudentsSession.save(batchStudents);
			batchStudentsTransaction.commit();
		} catch (HibernateException e) {
			e.printStackTrace();
			if (batchStudentsTransaction != null)
				batchStudentsTransaction.rollback();
			e.printStackTrace();
		} finally {
			batchStudentsSession.close();
		}
		return batchStudents;
	}

	public BatchStudents updateBatchStudentsToDAO(BatchStudents batchStudents) {

		BatchStudentsDAO batchStudentsDAO = new BatchStudentsDAO();

		Session batchStudentsSession = batchStudentsDAO.getSession();
		Transaction batchStudentsTransaction = null;
		try {
			batchStudentsTransaction = batchStudentsSession.beginTransaction();
			batchStudentsSession.update(batchStudents);
			batchStudentsTransaction.commit();
		} catch (HibernateException e) {
			e.printStackTrace();
			if (batchStudentsTransaction != null)
				batchStudentsTransaction.rollback();
			e.printStackTrace();
		} finally {
			batchStudentsSession.close();
		}
		return batchStudents;
	}
}
