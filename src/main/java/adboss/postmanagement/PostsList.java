package adboss.postmanagement;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import org.json.JSONArray;


public class PostsList extends ArrayList<Post> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3260040604900794004L;
	private static final Logger log = Logger.getLogger(PostsList.class.getName());
	JSONArray jsonArray = new JSONArray();
	
	public PostsList mergePostsList(PostsList p2) throws ParseException {
		
		Iterator<Post> iter = p2.iterator();
		while (iter.hasNext()) {
			Post post = iter.next();
			//jsonArray.put(post);
			this.add(post);
		}
		
		return this;

	}
	
	
	
	public PostsList sortPosts(PostsList postList) {
				
		Collections.sort(postList, new Comparator<Post>() {
			  public int compare(Post o1, Post o2) {
			      if (o1.getDateCreation() == null || o2.getDateCreation() == null)
			        return 0;
			      return o2.getDateCreation().compareTo(o1.getDateCreation());
			  }
			});
		return postList;
	}
	
		
	
	
	/*
	public JsonArray toJsonArray() { 
		
		JsonArray dao = new JsonArray();
		Iterator<Post> iter = this.iterator();
		String dateString = null;
		qreah q = new qreah();
		
		while (iter.hasNext()) {
			
			Post post = iter.next();
			JsonObject jObj = new JsonObject();
			jObj.addProperty("id", post.getId());
			jObj.addProperty("name", post.getName());
			jObj.addProperty("post", post.getPost());
			jObj.addProperty("platform", post.getPlatform());
			
			// Date in format "18-06-2018"
			dateString = q.day(post.getDateCreation()) + "-" + q.month(post.getDateCreation()) + "-" + q.year(post.getDateCreation());
			jObj.addProperty("dateCreation", dateString.toString());
			jObj.addProperty("fatherId", post.getFatherId());
			jObj.addProperty("answerON", post.getAnswerON());
			jObj.addProperty("visibleWithParent", post.getVisibleWithParent());
			jObj.addProperty("status", post.getStatus());
			
			if (post.getSons() != null) {
				
				jObj.add("sons", post.getSons().toJsonArray());
			} else {
				JSONArray jsonArr = new JSONArray("[]");
				 
				jObj.add("sons", null);
			}
			
			dao.add(jObj); 
			
		}
		return dao;
	}
	*/
	
	
	public void integratePosts(PostsList filteredPosts) {
		
		Iterator<Post> iterFiltered = filteredPosts.iterator();
		Iterator<Post> iterThis = this.iterator();
		
		while (iterFiltered.hasNext()) {
			Post filteredPost = iterFiltered.next();
			while (iterThis.hasNext()) {
				Post thisPost = iterThis.next();
				if (filteredPost.getDateCreation().equals(thisPost.getDateCreation())) {
					if (filteredPost.getFatherId().equals(thisPost.getFatherId())) {
						if (filteredPost.getPost().equals(thisPost.getPost())) {
							thisPost = filteredPost;
						}
					}
				}
			}
		}
		
	}

	
		
	public PostsList createPostsListTree(PostsList list) {
		PostsList finalList = new PostsList();
		Iterator<Post> iterSon = list.iterator();
		while (iterSon.hasNext()) {
			Post post = iterSon.next();	
			if (post.getFatherId().equals("") || post.getFatherId().equals("-1")) {
				PostsList sons = searchSons(list, post.getId());
				post.setSons(sons);
				finalList.add(post);
			} 		
		}
		return finalList;
	}
	
	public PostsList searchSons(PostsList list, String idFather) {
		PostsList newList = new PostsList();
		Iterator<Post> iter = list.iterator();
		while (iter.hasNext()) {
			Post post = iter.next();
			if (idFather.equals(post.getFatherId())) {
				PostsList sons = searchSons(list,post.getId());
				post.setSons(sons);
				newList.add(post);
			}
		}
		return newList;
	}
	
	public PostsList identifyPlatformPosts(PostsList postsList, String Platform) {
		PostsList newList = new PostsList();
		Iterator<Post> iter = postsList.iterator();
		while (iter.hasNext()) {
			Post post = iter.next();
			if (post.getPlatform().equals(Platform)) {
				newList.add(post);
			}
		}
		return newList;
	}
	
	public PostsList identifyNewPosts(PostsList postsList) {
		PostsList newList = new PostsList();
		Iterator<Post> iter = postsList.iterator();
		while (iter.hasNext()) {
			Post post = iter.next();
			
			if (post.getStatus().equals("newInAB")) {
				
				newList.add(post);
			}
		}
		return newList;
	}
	
	public void setPostsList(String stringArray) throws ParseException {
		//qreah q = new qreah();
		//q.enviarMail("rafa@adboss.io", "setPostsList", stringArray);
		
		if (stringArray.equals("null")) {
			stringArray = "[]";
		}
		jsonArray = new JSONArray(stringArray);
		int len = jsonArray.length();
		for (int i = 0; i < len; i++) {
			Post post = new Post();
			org.json.JSONObject jsonObject = jsonArray.getJSONObject(i);
			post.toPost(jsonObject);
			this.add(post);
		}		
		
	}
	
	public void addFinalText(String finalText) {
		int finalTextLen = finalText.length();
		Iterator<Post> iter = this.iterator();
		while (iter.hasNext()) {
			Post post = (Post) iter.next();
			String msg = post.getPost();
			int lenMsg = msg.length();
			String last = "";
			
			if (lenMsg < finalTextLen) {
				last = msg;
			} else {
				last = msg.substring(lenMsg -finalTextLen, lenMsg);
			}
			
			
			if (!last.equals(finalText)) {
				post.addFinalText(finalText);
				changePost(post);
			}
		}
	}
	
	public void changePost(Post post) {
		String id = post.getId();
		Iterator<?> iter = this.iterator();
		int i = 0;
		while (iter.hasNext()) {
			Post postArray = (Post) iter.next();
			String idArray = postArray.getId();
			if (id.equals(idArray)) {
				jsonArray.put(i, post);
			}
			i++;
		}
	}
	
	
	
	
	public void setPostsList(JSONArray array) throws ParseException {
		
		jsonArray = array;
		int len = jsonArray.length();
		for (int i = 0; i < len; i++) {
			Post post = new Post();
			org.json.JSONObject jsonObject = jsonArray.getJSONObject(i);
			post.toPost(jsonObject);
			this.add(post);
		}		
		
	}
	
		
	public JSONArray toJSONArray() { 
		JSONArray json = new JSONArray();
		Iterator<Post> iter = this.iterator();
		while (iter.hasNext()) {
			Post post = iter.next();
			json.put(post.toJSON());
		}
		return json;
	}
	
	public String getString() {
		return this.toJSONArray().toString();
	}
	
	
	
	public static void main(String[] args) {
		String msg = "Esto es una prueba 2http://www.adarga.org";
		int lenMsg = msg.length();
		String last = msg.substring(0, lenMsg -21);
		log.info(last);
	}
	
}
