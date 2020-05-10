package adboss.postmanagement;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import com.google.api.services.mybusiness.v4.model.Location;

import io.adboss.dataconnection.DB;
import io.adboss.utils.qreah;
import twitter4j.TwitterException;

public class DBRegisteredPosts extends DB {
	
	private static final Logger log = Logger.getLogger(DBRegisteredPosts.class.getName());
	static String db = "apiadbossDB.DBRegisteredPosts";
	
	public void addPost(String id, String platform, String msg, String username) throws ClassNotFoundException, SQLException, ServletException, IOException {
		qreah q = new qreah();
		msg = msg.replace("'", "");
		String SQL = "INSERT INTO " + db + "(id, platform, msg, username)"
				+ " VALUES ('" + id + "', '" + platform + "', '" + msg + "', '" + username + "')";
		
		SQL = q.cleanString(SQL);
		Execute(SQL);
	}
	
	public void addPost(List<Rows> rows, String username) throws ClassNotFoundException, SQLException, ServletException, IOException {
		qreah q = new qreah();
		String SQL = "INSERT INTO " + db + "(id, platform, msg, username) VALUES";
		Iterator<Rows> iter = rows.iterator();
		while (iter.hasNext()) {
			Rows row = iter.next();
			String id = row.getId();
			String platform = row.getPlatform();
			String msg = row.getMsg();
			msg = q.cleanString(msg).replace("\"", "");
			SQL = SQL + " ('" + id + "', '" + platform + "', \"" + msg + "\", '" + username + "'),";
		}
		SQL = SQL.substring(0, SQL.length()-1);
		SQL = q.cleanString(SQL);
		log.info(SQL);
		Execute(SQL);
	}
	
	public boolean isInside(String id, String platform, String username) throws ClassNotFoundException, SQLException, ServletException, IOException {
		boolean result = false;
		String SQL = "select id, platform FROM " + db 
				+ " where "
				+ "username = '" + username + "'"
				+ " and id = '" + id + "'"
				+ " and platform = '" + platform + "'"
				;
		
		ResultSet rs = ExecuteSELECT(SQL);
		if (rs.next()) {
			result = true;
		}
		
		
		return result;
	}
	
	public void checkAndSend(PostsList posts, String username) throws Exception {
		
		Iterator<Post> iter = posts.iterator();
		DB db = new DB();
		String TWUserName = db.getTWUserName(username);
		String FBUserName = db.getFBUserName(username);
		String GOUserName = db.getGOUserName(username);
		PostsList postsList = new PostsList();
		List<Rows> rows = new ArrayList<Rows>();
		while (iter.hasNext()) {
			Post post = iter.next();
			String id = post.getId();
			String platform = post.getPlatform();
			String msg = post.getPost();
			
			if (post.getName().equals(TWUserName)
					|| post.getName().equals(FBUserName)
					|| post.getName().equals(GOUserName)
					){
				
				if (!isInside(id, platform, username)) {
					log.info("Yes! " + id + " | " + platform);
					addPost(id, platform, msg, username);
					Post FBPost = new Post();
					Post TWPost = new Post();
					Post GMBPost = new Post();
					Rows r = new Rows();
					switch(platform) {
					  case "Facebook":
						TWPost = createTWPost(post);
						GMBPost = createGMBPost(post);
						postsList.add(TWPost);
						postsList.add(GMBPost);
						
						r.setId(id);
						r.setMsg(msg);
						r.setPlatform(platform);
						rows.add(r);
						
					    break;
					  case "Twitter":
						FBPost = createFBPost(post);
						GMBPost = createGMBPost(post);
						postsList.add(FBPost);
						postsList.add(GMBPost);
						
						r.setId(id);
						r.setMsg(msg);
						r.setPlatform(platform);
						rows.add(r);
					    break;
					  case "Google":
						FBPost = createFBPost(post);
						TWPost = createTWPost(post);
						postsList.add(FBPost);
						postsList.add(TWPost);
						
						r.setId(id);
						r.setMsg(msg);
						r.setPlatform(platform);
						rows.add(r);
						break;
					  default:
					   
					}
					
					
					
				}
			}
		}
		
		if (!rows.isEmpty()) {
			addPost (rows, username);
			Platforms pt = new Platforms();
			log.info("posts to send: " + postsList.getString());
			pt.sendPosts(username, postsList);
		}
		
	}
	
	/**
	 * Created to fill the database temporally at the beginning
	 * 
	 */
	
	public void checkAndSend2(PostsList posts, String username) throws Exception {
		
		
		DB db = new DB();
		String TWUserName = db.getTWUserName(username);
		String FBUserName = db.getFBUserName(username);
		String GOUserName = db.getGOUserName(username);
		log.info(TWUserName + " | " + FBUserName  + " | " + GOUserName);
		PostsList postsList = new PostsList();
		//log.info("before: " + posts.getString());
		List<Rows> rows = new ArrayList<Rows>();
		Iterator<Post> iter = posts.iterator();
		while (iter.hasNext()) {
			Post post = iter.next();
			String id = post.getId();
			String platform = post.getPlatform();
			String msg = post.getPost();
			log.info(post.getName() + " | " + platform);
			if (post.getName().equals(TWUserName)
					|| post.getName().equals(FBUserName)
					|| post.getName().equals(GOUserName)
					){
				
				if (!isInside(id, platform, username)) {
					log.info("Yes! " + id + " | " + platform);
					Rows r = new Rows();
					r.setId(id);
					r.setMsg(msg);
					r.setPlatform(platform);
					rows.add(r);
				}
			}
		}
		if (!rows.isEmpty()) {
			addPost (rows, username);
			Platforms pt = new Platforms();
			log.info("posts to send: " + postsList.getString());
			//pt.sendPosts(username, postsList);
		}
		
	}
	
	
	public Post createFBPost(Post post) throws ParseException {
		Post FBPost = new Post();
		qreah q = new qreah();
		FBPost.setAnswerON(false);
		FBPost.setDateCreation(post.getDateCreation());
		FBPost.setFatherId("-1");
		FBPost.setId(q.today());
		FBPost.setPlatform("Facebook");
		FBPost.setPost(post.getPost());
		FBPost.setStatus("newInAB");
		FBPost.setName(post.getName());
		FBPost.setVisibleWithParent(true);
		PostsList sons = new PostsList();
		sons.setPostsList("[]");
		FBPost.setSons(sons);	
		return FBPost;
	}
	
	
	public Post createTWPost(Post post) throws ParseException {
		Post TWPost = new Post();
		qreah q = new qreah();
		TWPost.setAnswerON(false);
		TWPost.setDateCreation(post.getDateCreation());
		TWPost.setFatherId("-1");
		TWPost.setId(q.today());
		TWPost.setPlatform("Twitter");
		TWPost.setPost(post.getPost());
		TWPost.setStatus("newInAB");
		TWPost.setName(post.getName());
		TWPost.setVisibleWithParent(true);
		PostsList sons = new PostsList();
		sons.setPostsList("[]");
		TWPost.setSons(sons);		
		return TWPost;
	}
	
	
	public Post createGMBPost(Post post) throws ParseException {
		Post GMBPost = new Post();
		qreah q = new qreah();
		GMBPost.setAnswerON(false);
		GMBPost.setDateCreation(post.getDateCreation());
		GMBPost.setFatherId("-1");
		GMBPost.setId(q.today());
		GMBPost.setPlatform("Google");
		GMBPost.setPost(post.getPost());
		GMBPost.setStatus("newInAB");
		GMBPost.setName(post.getName());
		GMBPost.setVisibleWithParent(true);
		PostsList sons = new PostsList();
		sons.setPostsList("[]");
		GMBPost.setSons(sons);		
		return GMBPost;
	}
	

	private Iterator<?> iterator() {
		
		return null;
	}
	
	

	public static void main(String[] args) throws ClassNotFoundException, SQLException, ServletException, IOException {
		
		//boolean b = new DBRegisteredPosts().isInside("Atlassian con una base de clientes Fortune 500 es un seguro en estos tiempos de crisis especialmente para las Pymes   http://www.adarga.org", "Facebook", "rafael@adarga.org");
		//log.info("result: " + b);
		
		/*
		String post = "Adarga";
		if (post.equals("Adarga Ventures")
				|| post.equals("Adarga")
				|| post.equals("MarketBoss")
				){
			log.info("OK");
		} else {
			log.info("ko");
		}
		*/
		
		String SQL = "I don't want to eat";
		SQL = SQL.replace("'", "\'");
		log.info(SQL);
				
		
		
	}

}
