package com.istarindia.apps.services;

import com.viksitpro.core.dao.entities.Presentation;
import com.viksitpro.core.dao.entities.PresentationDAO;

public class PresentationServices {
	
	public Presentation getPresentation(int assessmentId) {
		Presentation presentation;
		PresentationDAO presentationDAO = new PresentationDAO();
		try {
			presentation = presentationDAO.findById(assessmentId);
		} catch (IllegalArgumentException e) {
			presentation = null;
		}
		return presentation;
	}
}
