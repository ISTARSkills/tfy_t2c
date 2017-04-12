package com.istarindia.android.rest;

import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.istarindia.apps.services.StudentPlaylistServices;
import com.viksitpro.core.dao.entities.StudentPlaylist;

@Path("user/{userId}/lessons")
public class RESTLessonService {

	@PUT
	@Path("{playlistId}/status")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateLessonStatus(@PathParam("playlistId") int playlistId, @FormParam("status") String status) {

		try {
			StudentPlaylistServices studentPlaylistServices = new StudentPlaylistServices();
			StudentPlaylist studentPlaylist = studentPlaylistServices.getStudentPlaylist(playlistId);

			if (studentPlaylist == null) {
				throw new Exception();
			}

			studentPlaylistServices.updateStatus(studentPlaylist, status);
			return Response.status(Response.Status.CREATED).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
