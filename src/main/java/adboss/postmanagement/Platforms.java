package adboss.postmanagement;



/**
 * Platforms class manage all the issues that has to do with the different platforms
 * the user is connected to. It includes the own marketBoss platform. It does things 
 * like check if the user and passwords are valid, which platforms the user is connected
 * to, creates de JsonArray (DAO) that communicates with the navigator,...
 */


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.internal.org.json.JSONException;
import io.adboss.dataconnection.DB;
import io.adboss.platforms.FB;
import io.adboss.platforms.FBPage;
import io.adboss.platforms.GMB;
import io.adboss.platforms.TW;
import twitter4j.TwitterException;



public class Platforms {
	private static final Logger log = Logger.getLogger(Platforms.class.getName());
	/**
	 * Checks if the user and the password are correct, and the login is successful
	 * If it is successful it builds the DAO object to pass through the main data in each
	 * platform
	 * 
	 * @param username: from login
	 * @param userpass: from login
	 * @return: DAO object with information of 'platform', 'username', 'password' and 'status'
	 * 	TODO: send the password to the web browser is a security gap. It must be fixed
	 * @throws ClassNotFoundException
	 * @throws ServletException
	 * @throws IOException
	 * @throws SQLException
	 */
	
	public JsonArray loginManagement(String username, String userpass) throws ClassNotFoundException, ServletException, IOException, SQLException {
		JsonArray dao = new JsonArray();
		boolean loginOK = login(username, userpass);
		if (loginOK) {
			dao = getPlatformData(username);
			
		} else {
			dao = setInvalid(username);
		}
		return dao;
		
	}
	
	
	
	/**
	 * Check if the user or passwords match with the information in the database Users
	 * @param username
	 * @param userpass
	 * @return tru is the login is ok, false if username and password doesn't match
	 */
	
	private boolean login(String username, String userpass) {
		boolean loginOK = false;
		DB DBLogin = new DB();
		Connection conn;
		try {
			conn = DBLogin.ConnectDB();
			if (DBLogin.checkUserPass(username, userpass, conn)) {
				 loginOK = true;
				 
				
			} else { 
				loginOK = false;
				
				
			}
		} catch (ClassNotFoundException | SQLException e) {
			
			e.printStackTrace();
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return loginOK;
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

	public boolean itHasTW(String username) throws ClassNotFoundException, SQLException, ServletException, IOException {
		DB dB = new DB();
		boolean itHas =false;
		String TWuserName = dB.getATT(username);
		if ((TWuserName != null) && (!TWuserName.equals(""))) {	
			itHas =true;
		} 
		return itHas;
	}
	
	public boolean itHasGO(String username) throws ClassNotFoundException, SQLException, ServletException, IOException {
		DB dB = new DB();
		boolean itHas = false;
		String GOuserName = dB.getATG(username);
		if ((GOuserName != null) && (!GOuserName.equals(""))) {
			itHas =true;
		} 
		return itHas;
	}
	
	public boolean itHasGOSE(String username) throws ClassNotFoundException, SQLException, ServletException, IOException {
		DB dB = new DB();
		boolean itHas = false;
		String GOuserName = dB.getWebName(username);
		GOuserName = GOuserName.substring(7, GOuserName.length());
		if ((GOuserName != null) && (!GOuserName.equals(""))) {
			itHas =true;
		} 		
		return itHas;
	}

	
	
	
	/**
	 * Sets DAO data set when the user and password doesn't match any combination
	 * in the user database. It has only one row with user and password empty for
	 * Platform 'marketBoss' and status is ko
	 * 
	 * @param username
	 * @return DAO dataset with information about marketBoss (password is empty, it is
	 * not used) and all the platforms the user is registered in marketBoss. By now: only 
	 * Facebook
	 */
	
	private JsonArray setInvalid(String username) {
		
		JsonArray dao = new JsonArray();
		JsonObject marketBoss = new JsonObject();
		marketBoss.addProperty("platform", "marketBoss");
		marketBoss.addProperty("username", "");
		marketBoss.addProperty("userpass", "");
		marketBoss.addProperty("status", "ko");
		
		dao = new JsonArray();
		dao.add(marketBoss); 
		return dao;
	}
	
	
	/**
	 * Sets DAO data set when the user and password match in the user database. 
	 * It has as many rows as platforms the user is registered to including marketBoss
	 * Status is set to 'ok'
	 * 
	 * @param username
	 * @return
	 * @throws ClassNotFoundException
	 * @throws ServletException
	 * @throws IOException
	 * @throws SQLException
	 */

	private JsonArray getPlatformData(String username) throws ClassNotFoundException, ServletException, IOException, SQLException {
		
		DB dB = new DB();
				
		String userpassbossMarket = "";
		String statusbossMarket = "ok";
		
		
		JsonObject marketBoss = new JsonObject();
		marketBoss.addProperty("platform", "marketBoss");
		marketBoss.addProperty("username", username);
		marketBoss.addProperty("userpass", userpassbossMarket);
		marketBoss.addProperty("status", statusbossMarket);
		
		JsonObject facebook = new JsonObject();
		
		if (itHasFB(username)) {
			String ATF = dB.getATF(username);
			String fbPage = dB.getFBPage(username);
			facebook.addProperty("platform", "Facebook");
			facebook.addProperty("AT", ATF);
			facebook.addProperty("fbPage", fbPage);
			facebook.addProperty("status", "ok");
			
		} else {
			
			facebook.addProperty("platform", "Facebook");
			facebook.addProperty("AT", "");
			facebook.addProperty("status", "ko");
		}
		
		JsonArray dao = new JsonArray();
		dao.add(marketBoss); 
		dao.add(facebook);
		log.info(dao.toString());
		return dao;
		
	}


	
	
	public PostsList sendPosts(String username, PostsList postsList) throws Exception {
		
		DB db = new DB();
		String web = db.getWebName(username);
		postsList.addFinalText(web);
		log.info(postsList.getString());
		TWPostHub tw = new TWPostHub();
		PostsList newPostsList = tw.sendTWPostsList(username, postsList, null);
		
		GMBPostHub gmb = new GMBPostHub();
		newPostsList = gmb.sendGOPostsList(username, postsList, null);
		
		FBPostHub fb = new FBPostHub(username);
		newPostsList = fb.sendFBPostsList(username, postsList, null);
			
		PostsList postsListNew = new PostsList();	
		
		log.info(postsListNew.getString());
		
		return postsListNew;
	}
	
	public PostsList getPosts(String username) throws ClassNotFoundException, SQLException, ServletException, IOException, ParseException, Exception {
		FB fb = new FB();
		
		Platforms plat = new Platforms();
		PostsList posts = new PostsList();
		
		if (plat.itHasFB(username)) {
			Facebook facebook = fb.getFacebook(username);
			FBPostHub page = new FBPostHub(username);
			PostsList postsFB = page.getPagePosts(username);
			posts.mergePostsList(postsFB);
			log.info(posts.getString());
		}
		
		if (plat.itHasTW(username)) {
			TWPostHub twHub = new TWPostHub();
			PostsList postsTW = twHub.getTWPosts(username);
			posts.mergePostsList(postsTW);
		}
		
		if (plat.itHasGO(username)) {
			GMBPostHub goHub = new GMBPostHub();
			PostsList postsGO = goHub.getGMBPosts(username);
			posts.mergePostsList(postsGO);
			
		}
		FilterUser filter = new FilterUser();
		posts = filter.applyFilter(posts, username);
		
		posts = posts.sortPosts(posts);
		DBRegisteredPosts regPosts = new DBRegisteredPosts();
		regPosts.checkAndSend2(posts, username);
		
		return posts;
	}
	
	
	
	
	
}
