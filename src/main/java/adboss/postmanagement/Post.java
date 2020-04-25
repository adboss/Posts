package adboss.postmanagement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import com.google.gson.JsonObject;


import java.util.Date;
import java.util.Iterator;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject; 

public class Post {
	
	private static final Logger log = Logger.getLogger(Post.class.getName());
	private String id = null;
	private String post = null;
	private String name = null;
	private String platform = null;
	private Date dateCreation = null;
	private String fatherId = null;
	private boolean answerON = false;
	private boolean visibleWithParent = false;
	private PostsList sons = null;
	private String status = null;

	public String getId() {
		return this.id;	
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getPost() {
		return this.post;	
	}
	public void setPost(String post) {
		this.post = post;
	}
	
	public String getName() {
		return this.name;	
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPlatform() {
		return this.platform;	
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	
	public Date getDateCreation() {
		return this.dateCreation;	
	}
	public void setDateCreation(Date date) {
		this.dateCreation = date;
	}
	
	public String getFatherId() {
		return this.fatherId;	
	}
	public void setFatherId(String fatherId) {
		this.fatherId = fatherId;
	}
	
	public boolean getAnswerON() {
		return this.answerON;	
	}
	public void setAnswerON(boolean answerON) {
		this.answerON = answerON;
	}
	
	public boolean getVisibleWithParent() {
		return this.visibleWithParent;	
	}
	public void setVisibleWithParent(boolean visibleWithParent) {
		this.visibleWithParent = visibleWithParent;
	}
	
	public PostsList getSons() {
		return this.sons;	
	}
	public void setSons(PostsList sons) {
		this.sons = sons;
	}
	
	public String getStatus() {
		return this.status;	
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public boolean isNew() {
		boolean result = false;
		if (this.getStatus().equals("newInAB")) {
			result = true;
		}
		return result;
		
	}
	
	/**
	 * Changes the Id when the post is created at the platform
	 * 
	 * @param oldId The Id generated internally and temporally until the platform
	 * create a new Id
	 * @param newId The Id from the platform
	 */
	
	public void changeSonID(String oldId, String newId) {
		Iterator<Post> iter = this.getSons().iterator();
		while (iter.hasNext()) {
			Post s = iter.next();
			if (s.getId().equals(oldId)) {
				s.setId(newId);
			}
			
		}
	}
	
		
	public void toPost(JSONObject jsonObj) throws ParseException {
		
		this.setId(jsonObj.getString("id"));
		this.setPost(jsonObj.getString("post"));
		this.setName(jsonObj.getString("name"));
		this.setPlatform(jsonObj.getString("platform"));
		String dateString = jsonObj.getString("dateCreation");
		//Date date = new SimpleDateFormat("dd/MM/yyyy").parse(dateString); 
		Date date = new Date();
		this.setDateCreation(date);
		if(jsonObj.isNull("fatherId")) {
			this.setFatherId("");
		} else {
			this.setFatherId(jsonObj.getString("fatherId"));
		}
		
		this.setAnswerON(jsonObj.getBoolean("answerON"));
		this.setVisibleWithParent(jsonObj.getBoolean("visibleWithParent"));
		if(jsonObj.isNull("status")) {
			this.setStatus("");
		} else {
			this.setStatus(jsonObj.getString("status"));
		}
		
		PostsList sons = new PostsList();
		
		
		if (jsonObj.optJSONArray("sons") != null) {
			
	
			int len = jsonObj.optJSONArray("sons").length();
			for (int i = 0; i < len; i++) {
				sons.setPostsList(jsonObj.optJSONArray("sons").toString());
				this.setSons(sons);
				
			}	
			if (len == 0) {
				sons.setPostsList("[]");
				this.setSons(sons);
			}
			
		} else {
			
			sons.setPostsList("[]");
			this.setSons(sons);
		}
	
	}
	
	public String getString() {
		String result = "";
		JSONObject json = new JSONObject();
		json.put("id", this.getId());
		json.put("post", this.getPost());
		json.put("name", this.getName());
		json.put("platform", this.getPlatform());
		json.put("dateCreation", this.getDateCreation());
		json.put("fatherId", this.getFatherId());
		json.put("answerON", this.getAnswerON());
		json.put("visibleWithParent", this.getVisibleWithParent());
		json.put("sons", this.getSons().toJSONArray().toString());
		json.put("status", this.getStatus());
		
		result = json.toString();
	
		return result;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("id", this.getId());
		json.put("post", this.getPost());
		json.put("name", this.getName());
		json.put("platform", this.getPlatform());
		json.put("dateCreation", this.getDateCreation());
		json.put("fatherId", this.getFatherId());
		json.put("answerON", this.getAnswerON());
		json.put("visibleWithParent", this.getVisibleWithParent());
		json.put("sons", this.getSons());
		json.put("status", this.getStatus());
		return json;
	}
	
	public void addFinalText(String finalText) {
		post = post + "   " + finalText;
	}
	
}
