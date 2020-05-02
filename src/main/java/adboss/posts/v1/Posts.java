package adboss.posts.v1;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import adboss.postmanagement.DBRegisteredPosts;
import adboss.postmanagement.FBPostHub;
import adboss.postmanagement.FilterUser;
import adboss.postmanagement.GMBPostHub;
import adboss.postmanagement.Platforms;
import adboss.postmanagement.PostsList;
import adboss.postmanagement.TWPostHub;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.internal.org.json.JSONException;
import io.adboss.dataconnection.DB;
import io.adboss.platforms.FB;
import io.adboss.platforms.GMB;
import io.adboss.platforms.TW;
import io.adboss.utils.qreah;
import twitter4j.TwitterException;

/**
 * Servlet implementation class Posts
 */
@WebServlet("v1/posts")
public class Posts extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(Posts.class.getName());

       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Posts() {
        super();
        
    }

	/**
	 * Gets a Posts list with Posts from all the Platforms: Facebook, 
	 * Twitter, GMB
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String username = request.getParameter("username");
		Platforms pt = new Platforms();
		
		
		try {
			
			PostsList posts = pt.getPosts(username);
			
			FilterUser filter = new FilterUser();
			posts = filter.applyFilter(posts, username);
			posts = posts.sortPosts(posts);
			
			DBRegisteredPosts regPosts = new DBRegisteredPosts();
			regPosts.checkAndSend(posts, username);
			
			out.write(posts.getString());
			
						
		} catch (ClassNotFoundException | SQLException | FacebookException | JSONException | TwitterException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	/**
	 * Sends a list of Posts with all the Posts, the older ones with the
	 * Get http method and the new ones. The new ones will have a temporally 
	 * id that will be changed when the platform provides a definitive one
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String username = request.getParameter("username");
		qreah q = new qreah();
		String data = q.postUTF8Message(request);
		log.info(data);
		PostsList postsList = new PostsList();
		
		try {
			postsList.setPostsList(data);
			Platforms pt = new Platforms();
			postsList = pt.sendPosts(username, postsList);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		out.write(postsList.getString());
	}

	/**
	 * When you want to send a Post. Not coded
	 * 
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * When you want to delete a Post. Not coded
	 * 
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
