package adboss.postmanagement;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.json.JSONArray;
import org.json.JSONObject;

import io.adboss.dataconnection.DB;

public class FilterUser {
	private static final Logger log = Logger.getLogger(FilterUser.class.getName());
	private JSONArray filter;	
	
	public boolean getFilter(String username) throws ClassNotFoundException, SQLException, ServletException, IOException {
		DB db = new DB();
		boolean result = false;
		String SQL = "SELECT FilterUser FROM apiadbossDB.Users WHERE User = '" + username + "'";
		ResultSet rs = db.ExecuteSELECT(SQL);
		
		if (rs != null) {
			String filterG = null;
			while (rs.next()) {
				filterG = rs.getString("FilterUser");
			}
			
			if (filterG != null) {
				filter = new JSONArray(filterG);
				result = true;
			} else {
				filter = new JSONArray();
			}
			
			
		} 
		return result;
		
	}
	
	public boolean setFilter(String username) throws SQLException, ClassNotFoundException, ServletException, IOException {
		DB db = new DB();
		String SQL = "UPDATE apiadbossDB.Users SET FilterUser = '" + toString() + "' WHERE User = '" + username + "'";
		
		boolean result = db.Execute(SQL);
		return result;
	}
	
	public boolean addUser(String username, String name, String Platform) throws ClassNotFoundException, SQLException, ServletException, IOException {
		boolean result = getFilter(username);
		
		JSONObject json = new JSONObject();
		json.put("Platform", Platform);
		json.put("User", name);
		filter.put(json);
		boolean result2 = setFilter(username);
		return result2;
	}
	
	public boolean removeUser(String username, String name, String Platform) throws ClassNotFoundException, SQLException, ServletException, IOException {
		boolean result = getFilter(username);
		for (int i=0; i<filter.length();i++) {
			JSONObject json = filter.getJSONObject(i);
			String plat = json.getString("Platform");
			String user = json.getString("User");
			
			if ((plat.equals(Platform))&&(user.equals(name))){
				filter.remove(i);
			}
		}
		boolean result2 = setFilter(username);
		return result2;
	}
	
	public PostsList applyFilter(PostsList posts, String username) throws ClassNotFoundException, SQLException, ServletException, IOException {
		
		PostsList postsNew = new PostsList();
		getFilter(username);
		for (int i=0; i<posts.size();i++) {
			boolean deletePost = false;
			Post post = posts.get(i);
			for (int j=0; j<filter.length(); j++) {
				if (post.getPlatform().equals(filter.getJSONObject(j).get("Platform"))) {
					if (post.getName().equals(filter.getJSONObject(j).get("User"))) {
						deletePost = true;
					}
					
				} 
			}
			if (!deletePost) {
				postsNew.add(post);
			}			
		}
		
		return postsNew;
	}
	
	public List<String> getUsers(String Platform) {
		List<String> users = new ArrayList<String>();
		int len = filter.length();
		for (int i=0; i < len; i++) {
			JSONObject filterElement = filter.getJSONObject(i);
			if (filterElement.get("Platform").equals(Platform)) {
				users.add((String) filterElement.get("User"));
			}
		}
		return users;
	}
	
	public ArrayList<String> getPlatforms() {
		ArrayList<String> plat = new ArrayList<String>();
		
		for (int i=0; i<filter.length();i++) {
			boolean itHasPlatform = false;
			String platformFilter = filter.getJSONObject(i).getString("Platform");
			int len = plat.size();
			for (int j=0; j<len;j++) {
				if (plat.get(j).equals(platformFilter)) {
					itHasPlatform = true;
				} 
			}	
			
			if (!itHasPlatform) {
				plat.add(platformFilter);
			}
		}
		return plat;
	}
	
	public JSONArray toJSON() {
		
		return filter;
	}
	
	public String toString() {
		return toJSON().toString();	
	}

	
	
}
