package adboss.postmanagement;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import io.adboss.dataconnection.DB;
import io.adboss.utils.qreah;
import twitter4j.TwitterException;

public class DBRegisteredPosts extends DB {
	
	private static final Logger log = Logger.getLogger(DBRegisteredPosts.class.getName());
	static String db = "apiadbossDB.DBRegisteredPosts";
	
	public void addPost(String id, String platform, String msg, String username) throws ClassNotFoundException, SQLException, ServletException, IOException {
		qreah q = new qreah();
		msg = msg.replace("'", "");
		String SQL = "INSERT INTO " + db 
				+ " VALUES ('" + id + "', '" + platform + "', '" + msg + "', '" + username + "')";
		log.info(SQL);
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
		log.info("size: " + posts.size());
		Iterator<Post> iter = posts.iterator();
		DB db = new DB();
		String TWUserName = db.getTWUserName(username);
		String FBUserName = db.getFBUserName(username);
		String GOUserName = db.getGOUserName(username);
		
		while (iter.hasNext()) {
			Post post = iter.next();
			String id = post.getId();
			String platform = post.getPlatform();
			String msg = post.getPost();
			
			log.info(post.getName() + " | " + username + " | " + msg);
			if (post.getName().equals(TWUserName)
					//|| post.getName().equals(FBUserName)
					|| post.getName().equals(GOUserName)
					){
				if (!isInside(id, platform, username)) {
					addPost(id, platform, msg, username);
					Platforms pt = new Platforms();
					PostsList postsList = new PostsList();
					postsList.add(post);
					pt.sendPosts(username, postsList);
					log.info("send: " + id + " | " + msg + " | " + platform);
				}
			}
		}
	}
	
	public void registerPosts(String username) throws ClassNotFoundException, SQLException, ServletException, IOException {
		DBRegisteredPosts rp = new DBRegisteredPosts();
		Iterator<?> iter = this.iterator();
		while (iter.hasNext()) {
			Post post = (Post) iter.next();
			String id = post.getId();
			String platform = post.getPlatform();
			String msg = post.getPost();
			if ((!rp.isInside(id, platform, username)) && 
					(post.getName().equals("adboss"))) {
				log.info("entra: " +  id + " | " + post.getName());
				rp.addPost(id, platform, msg, username);
			}
			
		}
		
	}

	private Iterator<?> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) throws ClassNotFoundException, SQLException, ServletException, IOException {
		
		//isInside("8989", "Facebook", "rafael@adarga.org");
	}

}
