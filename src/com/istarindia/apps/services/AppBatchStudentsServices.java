package com.istarindia.apps.services;

import com.viksitpro.core.dao.entities.BatchGroup;
import com.viksitpro.core.dao.entities.BatchStudents;
import com.viksitpro.core.dao.entities.BatchStudentsDAO;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.utils.user.IstarUserServices;

import org.hibernate.HibernateException;
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
