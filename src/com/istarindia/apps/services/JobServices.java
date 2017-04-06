package com.istarindia.apps.services;

import com.viksitpro.core.dao.entities.Job;
import com.viksitpro.core.dao.entities.JobDAO;

public class JobServices {

	public Job getJob(int jobId) {
		Job job;
		JobDAO jobDAO = new JobDAO();
		try {
			job = jobDAO.findById(jobId);
		} catch (IllegalArgumentException e) {
			job = null;
		}
		return job;
	}
}
