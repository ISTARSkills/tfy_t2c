package com.istarindia.apps.services;

import java.util.HashMap;
import java.util.List;

import com.viksitpro.core.dao.entities.BatchGroup;
import com.viksitpro.core.dao.entities.BatchGroupDAO;
import com.viksitpro.core.utilities.DBUTILS;

import org.hibernate.HibernateException;
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

	public void createOrUpdateOrganization(String batchCode, int urseId) {
		if (batchCode != null && !batchCode.equalsIgnoreCase("")) {
			DBUTILS db = new DBUTILS();
			String findGroupIdForCode = "select id,college_id from batch_group where batch_code='" + batchCode + "'";
			List<HashMap<String, Object>> grpData = db.executeQuery(findGroupIdForCode);
			for (HashMap<String, Object> row : grpData) {
				int orgId = (int) row.get("college_id");
				String checkIfExistInOrgMapping = "select cast(count(*) as integer) as cnt,id from user_org_mapping where user_id = "
						+ urseId + " and organization_id=" + orgId + " GROUP BY user_org_mapping.id";
				List<HashMap<String, Object>> orgMappins = db.executeQuery(checkIfExistInOrgMapping);
				if (orgMappins.size() == 0 || (int) orgMappins.get(0).get("cnt") == 0) {
					String insertIntoUserOrg = "INSERT INTO user_org_mapping (user_id, organization_id, id) VALUES ("
							+ urseId + ", " + orgId + ", ((select COALESCE(max(id),0)+1 from user_org_mapping)));";
					db.executeUpdate(insertIntoUserOrg);
				} else if (orgMappins.size() > 0 && orgMappins.get(0).get("id") != null) {
					String updateIntoUserOrg = "UPDATE user_org_mapping SET user_id='" + urseId + "', organization_id='"
							+ orgId + "' WHERE (id='" + orgMappins.get(0).get("id") + "');";
					db.executeUpdate(updateIntoUserOrg);
				}
			}
		}

	}
}
