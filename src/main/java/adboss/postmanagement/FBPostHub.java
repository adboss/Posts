package adboss.postmanagement;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.RawAPIResponse;
import facebook4j.ResponseList;
import facebook4j.auth.AccessToken;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;
import io.adboss.dataconnection.DB;
import io.adboss.platforms.FB;
import io.adboss.platforms.FBPage;


public class FBPostHub {
	
	private static final Logger log = Logger.getLogger(FBPostHub.class.getName());
	
	
public PostsList getPagePosts(String username) {
	
	PostsList postslist = new PostsList();
	FB fbDC = new FB(); 
	ArrayList<String> posts = new ArrayList<String>();
	DB db = new DB();
	String idPage;
			
	try {
		Facebook facebook = fbDC.getFacebook(username);
		//posts = this.mbPages(facebook);
		//idPage = posts.get(0);
		
		idPage = db.getIdFBPage(username);
		
		
		if (idPage.equals("")) {
			
		} else { 
					
		// Another version
		/**
		 * With this I have all the posts (send by the owner of the page or someone else:
		 * idPage/feed?fields=id,from,created_time,message,parent_id,type	,status_type
		 * 
		 * With this I have all the comments to the posts:
		 * idPost/comments?fields=id,from,message,created_time,parent,comment_count
		 */
		
		
		String id;
		String msg;
		String name;
		String creationDate;
		String ATPage;
		try {
			FBPage page = new FBPage(username);
			ATPage = page.getATPage();
			
		
			RawAPIResponse resPage = facebook.callGetAPI(idPage + "/feed?fields=id,from,created_time,message,parent_id,type,status_type&access_token=" + ATPage);
			JSONObject jsonObjectPage = resPage.asJSONObject();
			log.info(jsonObjectPage.toString());
			
			int numPosts = jsonObjectPage.getJSONArray("data").length();
			for (int i = 0; i < numPosts; i++) {
				
				if (jsonObjectPage.getJSONArray("data").getJSONObject(i).getString("type").equals("status")) {
					id = jsonObjectPage.getJSONArray("data").getJSONObject(i).getString("id");
					msg = jsonObjectPage.getJSONArray("data").getJSONObject(i).getString("message");
					
					if (jsonObjectPage.getJSONArray("data").getJSONObject(i).isNull("from")) {
						name = "";
					} else {
						name = jsonObjectPage.getJSONArray("data").getJSONObject(i).getJSONObject("from").getString("name");
					}
					
					creationDate = jsonObjectPage.getJSONArray("data").getJSONObject(i).get("created_time").toString();
					
					creationDate.substring(0, 10);
					String day = creationDate.substring(8, 10);
					String month = creationDate.substring(5, 7);
					String year = creationDate.substring(0, 4);
					String hour = creationDate.substring(11, 13);
					String minutes = creationDate.substring(14, 16);
					String seconds = creationDate.substring(17, 19);
					creationDate = year + "-" + month + "-" + day + " " + hour + ":" + minutes + ":" + seconds;
					
					// Get Comment for this Post
					PostsList commentsList = new PostsList();
					RawAPIResponse resPageComments = facebook.callGetAPI(id + "/comments?fields=id,from,message,created_time,parent,comment_count&access_token=" + ATPage);
					JSONObject jsonObjectPageComments = resPageComments.asJSONObject();
					int numComments;
					try {
						numComments = jsonObjectPageComments.getJSONArray("data").length();
					
					for (int j = 0; j < numComments; j++) {
						
						String idC = jsonObjectPageComments.getJSONArray("data").getJSONObject(j).getString("id");
						
						String msgC = jsonObjectPageComments.getJSONArray("data").getJSONObject(j).getString("message");
						//String nameC = jsonObjectPageComments.getJSONArray("data").getJSONObject(j).getJSONObject("from").getString("name");
						String nameC = "";
						if (jsonObjectPageComments.getJSONArray("data").getJSONObject(j).toString().contains("from")) {
							
							nameC = jsonObjectPageComments.getJSONArray("data").getJSONObject(j).getJSONObject("from").getString("name");
						} else {
							
						}
						
						String creationDateC = jsonObjectPageComments.getJSONArray("data").getJSONObject(j).get("created_time").toString();
						
						/*
						creationDateC.substring(0, 10);
						String dayC = creationDateC.substring(8, 10);
						String monthC = creationDateC.substring(5, 7);
						String yearC = creationDateC.substring(0, 4);
						creationDateC = dayC + "-" + monthC + "-" + yearC;
						*/
						
						Post comment = new Post();
						comment.setId(idC);
						comment.setPost(msgC);
						comment.setName(nameC);
						//comment.setName(username);
						comment.setPlatform("Facebook");
						comment.setDateCreation(new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse(creationDateC));
						comment.setFatherId(id);
						comment.setAnswerON(false);
						comment.setVisibleWithParent(true);
						comment.setSons(null);
						comment.setStatus("newInMB");
						commentsList.add(comment);
						

					}
				
				
				} catch (JSONException e) {
					
					e.printStackTrace();
				}
				
				Post post = new Post();
				post.setId(id);
				post.setPost(msg);
				post.setName(name);
				//post.setName(username);
				post.setPlatform("Facebook");
				
				post.setDateCreation(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(creationDate));
				post.setFatherId("");
				post.setAnswerON(false);
				post.setVisibleWithParent(true);
				post.setSons(commentsList);
				post.setStatus("newInMB");
				postslist.add(post);
				
			}
			
			
		}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
		
	} catch (ClassNotFoundException | ServletException | IOException | SQLException | FacebookException | ParseException e) {
		
		e.printStackTrace();
	}
	
	
	
	
	return postslist;
	
}
	
	public Post sendFBPagePost(Facebook facebook, String idPage, Post post) throws ClassNotFoundException, ServletException, IOException, SQLException, FacebookException, JSONException {
		FBPage page = new FBPage();
		String ATPage = page.getATPage();
		Map<String, String> params = new HashMap<String, String>();
		params.put("message", post.getPost());
		params.put("access_token", ATPage);
		RawAPIResponse res = facebook.callPostAPI(idPage + "/feed", params);
		JSONObject jsonObject = res.asJSONObject();
		String id = jsonObject.getString("id");
		post.setId(id);
		page.getPage();      
		return post;
	}
	
	/**
	 * Send comments that are new created in AB inside an existing post- Checks inside all
	 * the sons (comments) if it is new (status = newInAB) and if it is new it is send to
	 * the Facebook page. The result will be the definitive comment id that will be updated 
	 * into the post that will be output of the function
	 * 
	 * @param facebook
	 * @param idPage
	 * @param post
	 * @return Post with all the comments ids updated
	 * @throws ClassNotFoundException
	 * @throws ServletException
	 * @throws IOException
	 * @throws SQLException
	 * @throws FacebookException
	 * @throws JSONException
	 */
	
	
	public Post sendFBPageComments(Facebook facebook, String idPage, Post post) throws ClassNotFoundException, ServletException, IOException, SQLException, FacebookException, JSONException {
		
		FBPage page = new FBPage();
		String ATPage = page.getATPage();
		
		int len = post.getSons().size();
		
		for (int i = 0; i < len; i++) {
			
			if (post.getSons().get(i).getStatus().equals("newInAB")) {
				
				Map<String, String> params = new HashMap<String, String>();
				params.put("message", post.getSons().get(i).getPost());
				
				params.put("access_token", ATPage);
				RawAPIResponse res = facebook.callPostAPI(post.getId() + "/comments", params);
				JSONObject jsonObject = res.asJSONObject();
				String id = jsonObject.getString("id");
				post.getSons().get(i).setId(id);
				
				
			}
			
		}
		
		
		return post;
	}
	
	/*
	 * TODO: It seems that it doesn't work because Facebook changed the policy and
	 * you are not able to post in the main feed from the user
	 */

	public boolean sendPostFB(String username, String msj) throws ClassNotFoundException, ServletException, IOException, SQLException, FacebookException {
		boolean isSent = false;
		DB db = new DB();
		db.ConnectDB();
		String idPage = db.getIdFBPage(username);
		String ATF = db.getATF(username);
		Facebook facebook = new FacebookFactory().getInstance();
		AccessToken accessToken = new AccessToken(ATF);
		facebook.setOAuthAccessToken(accessToken);
		facebook.postStatusMessage(idPage, msj);
		isSent = true;
		return isSent;
	}
	
	public Post sendPagePostFB(String username, Post post) throws JSONException, ClassNotFoundException, ServletException, IOException, SQLException, FacebookException {
		DB db = new DB();
		Facebook facebook = new FB().getFacebook(username);
		FBPage page = new FBPage();
		String idPage = db.getIdFBPage(username);
		page.getPage();
		db.setFBUserName(username, page.getName());
		// It is assumed that if the post is new, there are not comments
		if (post.isNew()) {
			post = sendFBPagePost(facebook, idPage, post);
		} else {
			post = sendFBPageComments(facebook, idPage, post);
		}
		return post;
	}
	
	public PostsList getFBPosts(String username) throws ClassNotFoundException, ServletException, IOException, SQLException, FacebookException {
		PostsList postslist = new PostsList();
		
		FB fbDC = new FB(); 
		Facebook facebook = fbDC.getFacebook(username);
		ResponseList<facebook4j.Post> feed = facebook.getFeed();
		/*
		 * To get Feed from a Facebook Page you have to code this:
		 * 	ResponseList<Post> feeds = facebook.getFeed("187446750783",
            new Reading().limit(25));
		 */
		
		Iterator<facebook4j.Post> iter = feed.iterator();
		while (iter.hasNext()) {
			facebook4j.Post status = iter.next();
			Post post = new Post();
			post.setId(status.getId());
			post.setPost(status.getMessage());
			post.setName(status.getName());
			post.setPlatform("Facebook");
			post.setDateCreation(status.getCreatedTime());
			postslist.add(post);
		}
		
		return postslist;
	}
	

	public static void main(String[] args) {
		

	}

}
