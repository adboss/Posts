package adboss.postmanagement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.json.JSONException;
import org.json.JSONObject;

import io.adboss.dataconnection.DB;
import io.adboss.platforms.FBPage;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

public class TWPostHub {
	
	private static final Logger log = Logger.getLogger(TWPostHub.class.getName());
	
	/*****************************************************************************************
	 *  Send PostsList
	 *
	 ******************************************************************************************/
	
	public PostsList sendTWPostsList(String username, PostsList postsList, String idFather) throws ClassNotFoundException, ServletException, IOException, SQLException, TwitterException {
		
		PostsList twPosts = identifyTwPosts(postsList);
		PostsList newPosts = identifyNewPosts(twPosts);
		
		newPosts = sendNewPosts(username, newPosts, idFather);
		twPosts.integratePosts(newPosts);
		Iterator<Post> posts = twPosts.iterator();
		while (posts.hasNext()) {
			Post post = posts.next();
			if (!post.getSons().isEmpty()) {
				post.setSons(sendTWPostsList(username,post.getSons(), post.getId()));
			}
		}
		postsList.integratePosts(twPosts);
		
		return postsList;
	}
	
	
	public PostsList identifyTwPosts(PostsList postsList) {
		PostsList newList = new PostsList();
		Iterator<Post> iter = postsList.iterator();
		while (iter.hasNext()) {
			Post post = iter.next();
			if (post.getPlatform().equals("Twitter")) {
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
	
	public boolean setName(String username, String TWName) throws ClassNotFoundException, SQLException, ServletException, IOException {
		boolean out = false;
		DB db = new DB();
		out = db.setTWUserName(username, TWName);
		return out;
	}
	
	public PostsList sendNewPosts(String username, PostsList posts, String idFather) throws ClassNotFoundException, ServletException, IOException, SQLException, TwitterException {
		
		PostsList newList = new PostsList();
		Twitter twitter = new TwitterFactory().getInstance();
		DB db = new DB();
		db.ConnectDB();
		String ATT = db.getATT(username); 
		String ATTSecret = db.getATTSecret(username); 
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthAccessToken(ATT);
		cb.setOAuthAccessTokenSecret(ATTSecret);
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance(); 
		
		Iterator<Post> iter = posts.iterator();
		while (iter.hasNext()) {
			Post post = iter.next();
			
			Status status;
			if (idFather != null) {
				long idFatherLong = Long.parseLong(idFather);
				status = twitter
						.updateStatus(new StatusUpdate(post.getPost())
						.inReplyToStatusId(idFatherLong)); 
			} else {
				status = twitter
						.updateStatus(post.getPost());
			}
			
			
			long newId = status.getId();
			
			setName(username, status.getUser().getName());
			log.info(status.getUser().getName());
			
			post.setId(Long.toString(newId));
			post.setStatus("old");
			
			newList.add(post);
		}
		
		return newList;
	}
	
	/*****************************************************************************************
	 *  Send Post
	 *
	 ******************************************************************************************/
	
	public Post sendNewPost(Post post, String username) throws ClassNotFoundException, ServletException, IOException, SQLException, TwitterException {
		JSONObject json = anyNewPost(post);
		if (json.length() > 0) {
			post = sendPostTW(username, post);
		} 
		 return post;
	}
	
	
	public JSONObject anyNewPost (Post post) throws JSONException {
		JSONObject result = new JSONObject();
		if (post.getStatus().equals("newAB")) {
			result.put(post.getPost(), post.getId());
		}		
		return result;
	}
	
	public Post sendPostTW(String username, Post post) throws ClassNotFoundException, ServletException, IOException, SQLException, TwitterException {
		
		Twitter twitter = new TwitterFactory().getInstance();
		DB db = new DB();
		db.ConnectDB();
		String ATT = db.getATT(username); 
		String ATTSecret = db.getATTSecret(username); 
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthAccessToken(ATT);
		cb.setOAuthAccessTokenSecret(ATTSecret);
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance(); 
		
		Status status = twitter.updateStatus(post.getPost()); 
		long newId = status.getId();
		post.changeSonID(post.getId(), Long.toString(newId));
		
		return post;
	}
	
	
	
	/*****************************************************************************************
	 *  Send Comments
	 *
	 ******************************************************************************************/
	
	public Post sendNewComments(Post post, String username) throws ClassNotFoundException, ServletException, IOException, SQLException, TwitterException {
		
		JSONObject json = anyNewComments(post.getSons());
		long postId = Long.parseLong(post.getId());
		Iterator<String> keys = json.keys();
		while (keys.hasNext()) {
			String commentName = keys.next();
			String idPost = Long.toString(sendComment(commentName, postId, username));
			post.changeSonID(json.getString("commentName"), idPost);
			
		}
		return post;
	}
	

	public long sendComment(String Comment, long postId, String username) throws ClassNotFoundException, ServletException, IOException, SQLException, TwitterException {
		long result;
		Twitter twitter = new TwitterFactory().getInstance();
		DB db = new DB();
		db.ConnectDB();
		String ATT = db.getATT(username); 
		String ATTSecret = db.getATTSecret(username); 
				
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthAccessToken(ATT);
		cb.setOAuthAccessTokenSecret(ATTSecret);
		TwitterFactory tf = new TwitterFactory(cb.build());
		
		twitter = tf.getInstance(); 
        Status reply = twitter
        		.updateStatus(new StatusUpdate(Comment)
        		.inReplyToStatusId(postId));
         
        result = reply.getId();
		return result;
	    
	}
	
	public JSONObject anyNewComments (Post post) {
		JSONObject result = new JSONObject();
		PostsList sons = post.getSons();
		Iterator<Post> iter = sons.iterator();
		while (iter.hasNext()) {
			Post p = iter.next();
			String status = p.getStatus();
			if (status.equals("newInAB")) {
				result.put(p.getPost(), p.getId());
			}
		}
		
		return result;
	}
	
	public JSONObject anyNewComments (PostsList sonsList) {
		JSONObject result = new JSONObject();
		Iterator<Post> iter = sonsList.iterator();
		while (iter.hasNext()) {
			Post p = iter.next();
			String status = p.getStatus();
			if (status.equals("newInAB")) {
				result.put(p.getPost(), p.getId());
			}
		}
		
		return result;
	}
	
	
	
	public void UpdateStatus() {
		 try {
	         Twitter twitter = new TwitterFactory().getInstance();
	         try {
	             // get request token.
	             // this will throw IllegalStateException if access token is already available
	             RequestToken requestToken = twitter.getOAuthRequestToken();
	             AccessToken accessToken = null;

	             BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	             while (null == accessToken) {
	                 String pin = br.readLine();
	                 try {
	                     if (pin.length() > 0) {
	                         accessToken = twitter.getOAuthAccessToken(requestToken, pin);
	                     } else {
	                         accessToken = twitter.getOAuthAccessToken(requestToken);
	                     }
	                 } catch (TwitterException te) {
	                     if (401 == te.getStatusCode()) {
	                         log.info("Unable to get the access token.");
	                     } else {
	                         te.printStackTrace();
	                     }
	                 }
	             }
	             
	         } catch (IllegalStateException ie) {
	             // access token is already available, or consumer key/secret is not set.
	             if (!twitter.getAuthorization().isEnabled()) {
	                 log.info("OAuth consumer key/secret is not set.");
	                 System.exit(-1);
	             }
	         }
	         Status status = twitter.updateStatus("En la cama");
	         log.info("Successfully updated the status to [" + status.getText() + "].");
	         System.exit(0);
	     } catch (TwitterException te) {
	         te.printStackTrace();
	         log.info("Failed to get timeline: " + te.getMessage());
	         System.exit(-1);
	     } catch (IOException ioe) {
	         ioe.printStackTrace();
	         log.info("Failed to read the system input.");
	         System.exit(-1);
	     }
	 }

	public PostsList getTWPosts(String username) throws ServletException, IOException, ClassNotFoundException, SQLException, TwitterException {
		Twitter twitter = new TwitterFactory().getInstance();
		DB db = new DB();
		db.ConnectDB();
		String ATT = db.getATT(username); 
		String ATTSecret = db.getATTSecret(username); 
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthAccessToken(ATT);
		cb.setOAuthAccessTokenSecret(ATTSecret);
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance(); 
		List<Status> statuses = twitter.getHomeTimeline();
		PostsList postslist = new PostsList();
		
		Iterator<Status> iter = statuses.iterator();
		
		while (iter.hasNext()) {
			Status status = iter.next();
			
			/* log.info("Id: " + status.getId());
			log.info("getInReplyToStatusId: " + status.getInReplyToStatusId());
			log.info("getInReplyToUserId: " + status.getInReplyToUserId());
			*/
			
			Post post = new Post();
			post.setId(String.valueOf(status.getId()));
			
			post.setPost(status.getText());
			post.setName(status.getUser().getName());
			//post.setName(username);
			post.setPlatform("Twitter");
			post.setDateCreation(status.getCreatedAt());
			String fatherId = Long.toString(status.getInReplyToStatusId());
			post.setFatherId(fatherId);
			post.setVisibleWithParent(true);
			
			post.setStatus(status.getUser().getScreenName());
			postslist.add(post);
		}
		
		postslist = postslist.createPostsListTree(postslist);
		
		return postslist;
		
	}

	public static void main(String[] args) {
		

	}

}
