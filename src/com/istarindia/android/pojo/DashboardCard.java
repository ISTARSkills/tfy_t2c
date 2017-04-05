package com.istarindia.android.pojo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "dashboardCard")
public class DashboardCard implements Serializable{

	private static final long serialVersionUID = 1L;
	private Integer id;
    private String header;
    private String title;
    private String description;
    private Integer nosofQuestion;
    private Integer experience;
    private Integer coins;
    private String itemType;
    private Integer itemId;
    private Integer duration;
    private String image_url;
    private Integer personalRank;
    private Integer personalExperience;
    private Integer challengerExperience;
    private String challengerImage_url;
    private String challengerName;
    private Integer challengerRank;


    public DashboardCard() {
    }

    //game type / video type / presentation type
    public DashboardCard(Integer id, String header, String title, String description, String image_url,String itemType, Integer itemId) {
    	this.id = id;
        this.header = header;
        this.title = title;
        this.description = description;
        this.image_url = image_url;
        this.itemType = itemType;
        this.itemId = itemId;
    }


// challenge type
    public DashboardCard(Integer id, String header, String title, String description, String image_url, Integer nosofQuestion, Integer experience, Integer coins,
                         Integer personalRank, Integer personalExperience, Integer challengerExperience, Integer challengerRank, String itemType, Integer itemId){
    	this.id = id;
        this.header = header;
        this.title = title;
        this.description = description;
        this.image_url = image_url;
        this.nosofQuestion = nosofQuestion;
        this.experience = experience;
        this.coins = coins;
        this.personalRank = personalRank;
        this.personalExperience = personalExperience;
        this.challengerExperience = challengerExperience;
        this.challengerRank = challengerRank;
        this.itemType = itemType;
        this.itemId = itemId;
    }

    //assessment type
    public DashboardCard(Integer id, String header, String title, String description, String image_url, Integer nosofQuestion, Integer duration, Integer experience, Integer coins, String itemType, Integer itemId) {
    	this.id = id;
        this.header = header;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.nosofQuestion = nosofQuestion;
        this.experience = experience;
        this.coins = coins;
        this.itemType = itemType;
        this.itemId = itemId;
        this.image_url = "/root/talentify/assessment.png";
    }

    @XmlAttribute(name = "id", required=false)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @XmlAttribute(name = "personalExperience", required=false)
    public Integer getPersonalExperience() {
        return personalExperience;
    }

    public void setPersonalExperience(Integer personalExperience) {
        this.personalExperience = personalExperience;
    }

    @XmlAttribute(name = "header", required=false)
    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    @XmlAttribute(name = "title", required=false)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @XmlAttribute(name = "description", required=false)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlAttribute(name = "nosofQuestion", required=false)
    public Integer getNosofQuestion() {
        return nosofQuestion;
    }

    public void setNosofQuestion(Integer nosofQuestion) {
        this.nosofQuestion = nosofQuestion;
    }

    @XmlAttribute(name = "experience", required=false)
    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    @XmlAttribute(name = "coins", required=false)
    public Integer getCoins() {
        return coins;
    }

    public void setCoins(Integer coins) {
        this.coins = coins;
    }

    @XmlAttribute(name = "image_url", required=false)
    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    @XmlAttribute(name = "duration", required=false)
    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    @XmlAttribute(name = "personalRank", required=false)
    public Integer getPersonalRank() {
        return personalRank;
    }

    public void setPersonalRank(Integer rank) {
        this.personalRank = rank;
    }

    @XmlAttribute(name = "challengerExperience", required=false)
    public Integer getChallengerExperience() {
        return challengerExperience;
    }

    public void setChallengerExperience(Integer challengerExperience) {
        this.challengerExperience = challengerExperience;
    }

    @XmlAttribute(name = "challengerName", required=false)
    public String getChallengerName() {
        return challengerName;
    }

    public void setChallengerName(String challengerName) {
        this.challengerName = challengerName;
    }

    @XmlAttribute(name = "challengerRank", required=false)
    public Integer getChallengerRank() {
        return challengerRank;
    }

    public void setChallengerRank(Integer challengerRank) {
        this.challengerRank = challengerRank;
    }

    @XmlAttribute(name = "challengerImage_url", required=false)
    public String getChallengerImage_url() {
        return challengerImage_url;
    }

    public void setChallengerImage_url(String challengerImage_url) {
        this.challengerImage_url = challengerImage_url;
    }

    @XmlAttribute(name = "itemType", required=false)
	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	 @XmlAttribute(name = "itemId", required=false)
	public Integer getItemId() {
		return itemId;
	}

	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}
    
}
