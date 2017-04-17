package com.istarindia.apps.services;

import java.util.List;

import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.BatchGroup;
import com.viksitpro.core.dao.entities.BatchGroupDAO;
import com.viksitpro.core.dao.entities.IstarUser;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class AppBatchGroupServices {

	public BatchGroup getBatchGroup(Integer batchGroupId) {
		BatchGroup batchGroup;
		BatchGroupDAO batchGroupDAO = new BatchGroupDAO();
		try {
			batchGroup = batchGroupDAO.findById(batchGroupId);
		} catch (IllegalArgumentException e) {
			batchGroup = null;
		}
		return batchGroup;
	}
	
	public BatchGroup getBatchGroupByBatchCode(String batchCode) {
		BatchGroup batchGroup = null;
		BatchGroupDAO batchGroupDAO = new BatchGroupDAO();
		List<BatchGroup> allBatchGroup = batchGroupDAO.findByBatchCode(batchCode);

		if (allBatchGroup.size() > 0) {
			batchGroup = allBatchGroup.get(0);
		}
		return batchGroup;
	}

	public BatchGroup saveBatchGroupToDAO(BatchGroup batchGroup) {

		BatchGroupDAO batchGroupDAO = new BatchGroupDAO();

		Session batchGroupSession = batchGroupDAO.getSession();
		Transaction batchGroupTransaction = null;
		try {
			batchGroupTransaction = batchGroupSession.beginTransaction();
			batchGroupSession.save(batchGroup);
			batchGroupTransaction.commit();
		} catch (HibernateException e) {
			e.printStackTrace();
			if (batchGroupTransaction != null)
				batchGroupTransaction.rollback();
			e.printStackTrace();
		} finally {
			batchGroupSession.close();
		}
		return batchGroup;
	}

	public BatchGroup updateBatchGroupToDAO(BatchGroup batchGroup) {

		BatchGroupDAO batchGroupDAO = new BatchGroupDAO();

		Session batchGroupSession = batchGroupDAO.getSession();
		Transaction batchGroupTransaction = null;
		try {
			batchGroupTransaction = batchGroupSession.beginTransaction();
			batchGroupSession.update(batchGroup);
			batchGroupTransaction.commit();
		} catch (HibernateException e) {
			e.printStackTrace();
			if (batchGroupTransaction != null)
				batchGroupTransaction.rollback();
			e.printStackTrace();
		} finally {
			batchGroupSession.close();
		}
		return batchGroup;
	}
}
