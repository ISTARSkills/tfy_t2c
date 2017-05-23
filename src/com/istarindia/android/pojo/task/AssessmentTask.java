/**
 * 
 */
package com.istarindia.android.pojo.task;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import com.istarindia.android.pojo.TaskSummaryPOJO;

/**
 * @author mayank
 *
 */
public class AssessmentTask extends TaskSummaryPOJO {
	
	private Integer numberOfQuestions;
	private Integer itemPoints;
	private Integer itemCoins;
	
	
	
	public AssessmentTask() {
		super();
		// TODO Auto-generated constructor stub
	}


	@XmlAttribute(name = "numberOfQuestions", required = false)
	public Integer getNumberOfQuestions() {
		return numberOfQuestions;
	}


	public void setNumberOfQuestions(Integer numberOfQuestions) {
		this.numberOfQuestions = numberOfQuestions;
	}

	@XmlAttribute(name = "itemPoints", required = false)
	public Integer getItemPoints() {
		return itemPoints;
	}


	public void setItemPoints(Integer itemPoints) {
		this.itemPoints = itemPoints;
	}

	@XmlAttribute(name = "itemCoins", required = false)
	public Integer getItemCoins() {
		return itemCoins;
	}


	public void setItemCoins(Integer itemCoins) {
		this.itemCoins = itemCoins;
	}

	
}
