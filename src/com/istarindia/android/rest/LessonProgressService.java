package com.istarindia.android.rest;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viksitpro.core.dao.entities.Lesson;
import com.viksitpro.core.dao.entities.LessonDAO;
import com.viksitpro.core.utilities.DBUTILS;

/**
 * Servlet implementation class LessonProgressService
 */
@WebServlet("/LessonProgressService")
public class LessonProgressService extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LessonProgressService() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//			        url: '<%=basePath%>t2c/LessonProgressService?user_id<%=user.getId()%>&lesson_id=<%=lesson_id%>&slide_id='+slideID+'&title='
	//	+title+'&totoal_slides='+document.getElementsByTagName("section").length,

		
		try {
			
			String lessonId = request.getParameter("lesson_id");
			String istarUserId = request.getParameter("user_id");
			String slideId = request.getParameter("slide_id");

			String slideTitle = request.getParameter("title").replaceAll("'", "");

			String totalSlideCount = request.getParameter("total_slides");

			DBUTILS util = new DBUTILS();
			
			Lesson l = new LessonDAO().findById(Integer.parseInt(lessonId));
			int courseId =  l.getCmsessions().iterator().next().getModules().iterator().next().getCourses().iterator().next().getId();
			int moduleId = l.getCmsessions().iterator().next().getModules().iterator().next().getId();
			int cmsessionId = l.getCmsessions().iterator().next().getId();
			
			String insertIntoLog = "INSERT INTO user_session_log (id, cmsession_id, course_id, created_at, lesson_id, lesson_type, module_id,  slide_id, updated_at, url, user_id,total_slide_count)"
					+ " VALUES ((select COALESCE(max(id),0)+1 from user_session_log), "+cmsessionId+","
							+ " "+courseId+", now(), "+lessonId+", '"+l.getType()+"', "+moduleId+", "+slideId+", "
									+ " now(), '"+slideTitle+"', "+istarUserId+","+totalSlideCount+");";
			util.executeUpdate(insertIntoLog);
		} catch (Exception e) {
			
		}	
	response.getWriter().write("200");	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
