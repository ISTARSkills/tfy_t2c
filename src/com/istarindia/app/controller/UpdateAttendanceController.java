package com.istarindia.app.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.viksitpro.core.dao.entities.BaseHibernateDAO;
import com.viksitpro.core.dao.entities.IstarUser;
import com.viksitpro.core.dao.entities.IstarUserDAO;
import com.viksitpro.core.utilities.DBUTILS;

@WebServlet("/update_attendance")
public class UpdateAttendanceController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public UpdateAttendanceController() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		DBUTILS db = new DBUTILS();

		int taskID = 0;
		int user_id = 0;
		taskID = request.getParameter("task_id") != null ? Integer.parseInt(request.getParameter("task_id")) : 0;
		user_id = request.getParameter("user_id") != null ? Integer.parseInt(request.getParameter("user_id")) : 0;
		
			String sql = "SELECT 	actor_id, 	ID FROM 	batch_schedule_event WHERE 	batch_group_code IN ( 		SELECT 			batch_group_code 		FROM 			batch_schedule_event 		WHERE 			ID IN ( 				SELECT 					item_id 				FROM 					task 				WHERE 					actor = "+user_id+" 				AND item_type = 'CLASSROOM_SESSION_STUDENT' 				AND ID = "+taskID+" 			) 	) AND TYPE = 'BATCH_SCHEDULE_EVENT_TRAINER'";

			System.err.println(sql);

			List<HashMap<String, Object>> data = db.executeQuery(sql);

			if (data.size() > 0) {
				for (HashMap<String, Object> row : data) {

					int taken_by = (int) row.get("actor_id");
					int event_id = (int) row.get("id");
					
					String sql2 = "SELECT cast(count(*) as INTEGER) as ispresent  FROM attendance WHERE taken_by = "+taken_by+" AND event_id = "+event_id+" AND user_id ="+user_id;
					
					List<HashMap<String, Object>> data2 = db.executeQuery(sql2);
					if((int)data2.get(0).get("ispresent") == 0){
						
						String sqqll = "INSERT INTO attendance ( 	id, 	taken_by, 	user_id, 	status, 	created_at, 	updated_at, 	event_id ) VALUES 	( 		(SELECT MAX(id)+1 FROM attendance), 		"+taken_by+", 		"+user_id+", 		'PRESENT', 		now(), 		now(), 		"+event_id+" 	);";
						System.err.println(sqqll);
						db.executeUpdate(sqqll);
						
					}

					
				}

			}
		

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
