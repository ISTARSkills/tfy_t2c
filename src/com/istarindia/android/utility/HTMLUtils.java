/**
 * 
 */
package com.istarindia.android.utility;

import java.util.ArrayList;
import java.util.List;

import com.istarindia.android.pojo.SkillReportPOJO;
import com.istarindia.apps.services.AppCourseServices;
import com.istarindia.apps.services.StudentPlaylistServices;
import com.viksitpro.core.dao.entities.Course;

/**
 * @author ISTAR-SERVER-PU-1
 *
 */
public class HTMLUtils {

	
	
	public String cleanHtml(String dirtyHtml)
	{
		String str=dirtyHtml;
		/*Document doc = Jsoup.parse(data1);
		data1 = Jsoup.clean(data1, Whitelist.relaxed());*/
		return str;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		testingskills();
	}
	private static void testingskills() {
		// TODO Auto-generated method stub
		List<SkillReportPOJO> allSkills = new ArrayList<SkillReportPOJO>();

		StudentPlaylistServices studentPlaylistServices = new StudentPlaylistServices();
		List<Integer> allCourseId = studentPlaylistServices.getCoursesforUser(5128);

		AppCourseServices appCourseServices = new AppCourseServices();

		for (Integer courseId : allCourseId) {
			Course course = appCourseServices.getCourse(courseId);
			if (course != null) {
				SkillReportPOJO courseSkillPOJO = new SkillReportPOJO();
				courseSkillPOJO.setId(course.getId());
				courseSkillPOJO.setName(course.getCourseName());

				String imageURL = course.getImage_url();
				courseSkillPOJO.setImageURL(imageURL);

				List<SkillReportPOJO> moduleLevelSkillReport = appCourseServices
						.getSkillsReportForCourseOfUser(5128, courseId);

				courseSkillPOJO.setSkills(moduleLevelSkillReport);
				courseSkillPOJO.calculateUserPoints();
				courseSkillPOJO.calculateTotalPoints();
				courseSkillPOJO.calculatePercentage();
				
				allSkills.add(courseSkillPOJO);
			}
		}
	}

}
