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
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;


public class FBPostHub {
	
	private static final Logger log = Logger.getLogger(FBPostHub.class.getName());
	private FBPage page;
	private String username;
	
	public FBPostHub(String username) throws ClassNotFoundException, ServletException, IOException, SQLException, FacebookException, JSONException {
		DB db = new DB();
		page = new FBPage(username);
		page.getPage();
		db.setFBUserName(username, page.getName());
		this.username = username;
	}
	
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
			 * idPage/feed?fields=id,from,created_time,message,parent_id,attachments,media_type,status_type
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
				
			
				RawAPIResponse resPage = facebook.callGetAPI(idPage + "/feed?fields=id,from,created_time,message,parent_id,status_type,media_type&access_token=" + ATPage);
				JSONObject jsonObjectPage = resPage.asJSONObject();
				log.info(jsonObjectPage.toString());
				
				int numPosts = jsonObjectPage.getJSONArray("data").length();
				for (int i = 0; i < numPosts; i++) {
					
					if (true) {
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
	
	
	
	
	/*
	 *  Send Posts ***********************************************************
	 */
	
	
	public PostsList sendFBPostsList(String username, PostsList postsList, String idFather) throws ClassNotFoundException, ServletException, IOException, SQLException, TwitterException, FacebookException, JSONException {
		
		SendTools tools = new SendTools();
		PostsList fbPosts = tools.identifyFBPosts(postsList);
		PostsList newPosts = tools.identifyNewPosts(fbPosts);
		
		newPosts = sendNewPosts(username, newPosts, idFather);
		fbPosts.integratePosts(newPosts);
		Iterator<Post> posts = fbPosts.iterator();
		while (posts.hasNext()) {
			Post post = posts.next();
			if (!post.getSons().isEmpty()) {
				post.setSons(sendFBPostsList(username,post.getSons(), post.getId()));
			}
		}
		postsList.integratePosts(fbPosts);
		
		return postsList;
	}
	
	
	public PostsList sendNewPosts(String username, PostsList posts, String idFather) throws ClassNotFoundException, ServletException, IOException, SQLException, TwitterException, FacebookException, JSONException {
		
		PostsList newList = new PostsList();
		
		Iterator<Post> iter = posts.iterator();
		while (iter.hasNext()) {
			Post post = iter.next();
			
			Status status;
			if (idFather != null) {
				
				long idFatherLong = Long.parseLong(idFather);
				post = sendFBPageComments(post, idFather);
				 
			} else {
				post = sendFBPagePost(post);
			}
			
			post.setStatus("old");
			DBRegisteredPosts rp = new DBRegisteredPosts();
			rp.addPost(post.getId(), "Facebook", post.getPost(), username);
			newList.add(post);
		}
		
		return newList;
	}
	
/*
 *  End Send Posts ********************************************************
 */
	
	public Post sendFBPagePost(Post post) throws ClassNotFoundException, ServletException, IOException, SQLException, FacebookException, JSONException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("message", post.getPost());
		params.put("access_token", page.getATPage());
		RawAPIResponse res = page.getFacebookObject().callPostAPI(page.getIdPage(username) + "/feed", params);
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
	
	
	public Post sendFBPageComments(Post post, String idFather) throws ClassNotFoundException, ServletException, IOException, SQLException, FacebookException, JSONException {
		
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("message", post.getPost());
		params.put("access_token", page.getATPage());
		RawAPIResponse res = page.getFacebookObject().callPostAPI(idFather + "/comments", params);
		JSONObject jsonObject = res.asJSONObject();
		String id = jsonObject.getString("id");
		post.setId(id);
		
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
	
	/**
	 * Checks if the user has a Facebook account registered in marketBoss
	 * 
	 * @param username
	 * @return: true if the user has a Facebook account; false if the user doesn't have it
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws ServletException
	 * @throws IOException
	 */

	public boolean itHasFB(String username) throws ClassNotFoundException, SQLException, ServletException, IOException {
		DB dB = new DB();
		boolean itHas =false;
		String FBuserName = dB.getATF(username);
		if ((FBuserName != null) && (!FBuserName.equals(""))) {	
			itHas =true;
		} 
		return itHas;
	}
	

	public static void main(String[] args) {
		

	}

}
