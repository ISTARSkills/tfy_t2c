/**
 * 
 */
package com.istarindia.apps.factories;

import com.istarindia.android.pojo.TaskSummaryPOJO;
import com.viksitpro.core.dao.entities.Task;
import com.viksitpro.core.utilities.TaskItemCategory;

/**
 * @author mayank
 *
 */
public class TaskFactory {

	public TaskSummaryPOJO getTaskSummary(Task task)
	{
		TaskSummaryPojoCreator  creator = new TaskSummaryPojoCreator();
		switch(task.getItemType())
		{
			case TaskItemCategory.ASSESSMENT:
				return creator.getAssessmentTask(task);				
			case TaskItemCategory.LESSON:
				return creator.getLessonTask(task);
			case TaskItemCategory.FEEDBACK:
				return creator.getFeedbackTask(task);
			case TaskItemCategory.CLASSROOM_SESSION:
				return creator.getClassRoomSessionTask(task);
			case TaskItemCategory.CLASSROOM_ASSESSMENT:
				return creator.getClassroomAssessmentTask(task);
			case TaskItemCategory.CLASSROOM_SESSION_STUDENT:
				return creator.getClassroomSessionStudent(task);
			case TaskItemCategory.ZOOM_INTERVIEW_INTERVIEWEE:
				return creator.getZoomIntervieweeTask(task);
			case TaskItemCategory.ZOOM_INTERVIEW_INTERVIEWER:
				return creator.getZoomInterviewerTask(task);
			case TaskItemCategory.CUSTOM_TASK:
				return creator.getCustomTask(task);
			case TaskItemCategory.WEBINAR_TRAINER:
				return creator.getTrainerWebinarTask(task);
			case TaskItemCategory.WEBINAR_STUDENT:
				return creator.getStudentWebinarTask(task);	
			case TaskItemCategory.REMOTE_CLASS_TRAINER:
				return creator.getTrainerRemoteTask(task);
			case TaskItemCategory.REMOTE_CLASS_STUDENT:
				return creator.getStudentRemoteTask(task);	
			default:
				return null;			
			
		}
	}
}
