package com.istarindia.android.rest;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import com.istarindia.apps.services.AppEncryptionService;

@Provider
public class RESTResponseFilter implements ContainerResponseFilter{

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

		AppEncryptionService appEncryptionService = new AppEncryptionService();
		String encrypted = appEncryptionService.encrypt((String)responseContext.getEntity());
		responseContext.setEntity(encrypted, responseContext.getEntityAnnotations(), responseContext.getMediaType());
	}	
}
