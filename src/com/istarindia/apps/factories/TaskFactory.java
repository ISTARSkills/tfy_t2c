/**
 * 
 */
package com.istarindia.apps.factories;

import com.istarindia.android.pojo.TaskSummaryPOJO;
import com.istarindia.android.pojo.task.AssessmentTask;
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
			case TaskItemCategory.CONTENT:
				return creator.getContentTask(task);
			default:
				return null;			
			
		}
	}
}
