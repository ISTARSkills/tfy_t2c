package com.istarindia.android.pojo;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.sun.xml.internal.txw2.annotation.XmlCDATA;

@XmlRootElement(name = "question")
public class QuestionPOJO {
	
	private Integer id;
	private String text;
	private String type;
	private Integer difficultyLevel;
	private Integer durationInSec;
	private String explanation;
	private String comprehensivePassageText;
	private Integer points;
	private List<OptionPOJO> options;
	private List<Integer> correctOptions;
	
	@XmlAttribute(name = "id", required = false)
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	@XmlCDATA
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	@XmlAttribute(name = "type", required = false)
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	@XmlAttribute(name = "difficultyLevel", required = false)
	public Integer getDifficultyLevel() {
		return difficultyLevel;
	}
	public void setDifficultyLevel(Integer difficultyLevel) {
		this.difficultyLevel = difficultyLevel;
	}
	
	@XmlAttribute(name = "durationInSec", required = false)
	public Integer getDurationInSec() {
		return durationInSec;
	}
	public void setDurationInSec(Integer durationInSec) {
		this.durationInSec = durationInSec;
	}
	
	@XmlCDATA
	public String getExplanation() {
		return explanation;
	}
	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}
	
	@XmlCDATA	
	public String getComprehensivePassageText() {
		return comprehensivePassageText;
	}
	public void setComprehensivePassageText(String comprehensivePassageText) {
		this.comprehensivePassageText = comprehensivePassageText;
	}
	
	@XmlAttribute(name = "points", required = false)
	public Integer getPoints() {
		return points;
	}
	public void setPoints(Integer points) {
		this.points = points;
	}
	
	@XmlAttribute(name = "options", required = false)
	public List<OptionPOJO> getOptions() {
		return options;
	}
	public void setOptions(List<OptionPOJO> options) {
		this.options = options;
	}
	
	@XmlAttribute(name = "correctOptionId", required = false)
	public List<Integer> getCorrectOptions() {
		return correctOptions;
	}
	public void setCorrectOptions(List<Integer> correctOptions) {
		this.correctOptions = correctOptions;
	}
}

