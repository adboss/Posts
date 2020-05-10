package adboss.postmanagement;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.mybusiness.v4.MyBusiness;
import com.google.api.services.mybusiness.v4.MyBusiness.Accounts.Locations.Reviews.UpdateReply;
import com.google.api.services.mybusiness.v4.model.Account;
import com.google.api.services.mybusiness.v4.model.ListAccountsResponse;
import com.google.api.services.mybusiness.v4.model.ListLocalPostsResponse;
import com.google.api.services.mybusiness.v4.model.ListLocationsResponse;
import com.google.api.services.mybusiness.v4.model.ListReviewsResponse;
import com.google.api.services.mybusiness.v4.model.LocalPost;
import com.google.api.services.mybusiness.v4.model.Location;
import com.google.api.services.mybusiness.v4.model.Review;
import com.google.api.services.mybusiness.v4.model.ReviewReply;

import io.adboss.dataconnection.DB;
import io.adboss.platforms.GMB;


public class GMBPostHub {
	
	private static final Logger log = Logger.getLogger(GMBPostHub.class.getName());
	Connection conn;
	private static String accountRafa = "accounts/103268190540206787281";
	//private static String accountJumberStores = "accounts/110386967263955010373";
	private static String accountName = accountRafa;
	protected Writer W = new StringWriter();
	static JsonFactory JSON_FACTORY = new JacksonFactory();
	private static MyBusiness mybusiness;
	
	public GMBPostHub() throws ClassNotFoundException, SQLException, ServletException, IOException {
		GMB gmb = new GMB();
		mybusiness = gmb.getMyBusiness();
	}
	
	public PostsList sendGOPostsList(String username, PostsList postsList, String idFather) throws Exception {
		
		PostsList goPosts = new PostsList();
		goPosts = goPosts.identifyPlatformPosts(postsList, "Google");
		
		PostsList newPosts = new PostsList();
		newPosts = newPosts.identifyNewPosts(goPosts);
		
		newPosts = sendNewPosts(username, newPosts, idFather);
		
		goPosts.integratePosts(newPosts);
		Iterator<Post> posts = goPosts.iterator();
		while (posts.hasNext()) {
			Post post = posts.next();
			if (!post.getSons().isEmpty()) {
				post.setSons(sendGOPostsList(username,post.getSons(), post.getId()));
			}
		}
		postsList.integratePosts(goPosts);
		
		return postsList;
		
	}
	
public PostsList sendNewPosts(String username, PostsList posts, String idFather) throws Exception {
	
		PostsList newList = new PostsList();
		Iterator<Post> iter = posts.iterator();
		
		while (iter.hasNext()) {
			Post post = iter.next();
			post = sendPost(post, username);
			post.setStatus("old");
			newList.add(post);
		}
		
		return newList;
}

	public static Post sendPost(Post post, String username) throws Exception {
		
		DB db = new DB();
		String parent = db.getGMBLocation(username);
		ReviewReply content = new ReviewReply();
		content.setComment(post.getPost());
		String name = post.getFatherId();
		LocalPost publicacion = new LocalPost();
		if (name.equals("-1")) {
			LocalPost content2 = new LocalPost();
			content2.setSummary(post.getPost());
			publicacion = mybusiness.accounts().locations().localPosts().create(parent, content2).execute();
			
		} else {
			
			UpdateReply rev = 
					mybusiness.accounts().locations().reviews().updateReply(name, content);
			post.setId(rev.getName());
			ReviewReply response = rev.execute();
			
			LocalPost content2 = new LocalPost();
			
			content2.setSummary(post.getPost());
			publicacion = mybusiness.accounts().locations().localPosts().create(parent, content2).execute();
			
		}
		
		DBRegisteredPosts rp = new DBRegisteredPosts();
		rp.addPost(publicacion.getName(), "Google", publicacion.getSummary(), username);
		
		
		return post;
	}
	
	/**
	* Returns a list of reviews.
	* @param locationName Name of the location to retrieve reviews for.
	* @return List A list of reviews.
	* @throws Exception
	*/		
	public static List<Review> listReviews(String username) throws Exception {
		List<Review> reviews = new ArrayList<Review>();
		
		//DB db = new DB();
		//String locationName = db.getGMBLocation(username);
		String locationName = "accounts/103268190540206787281/locations/11239012911156423308";
		log.info(locationName);
		if (locationName != null) {
			
			MyBusiness.Accounts.Locations.Reviews.List reviewsList = 
			mybusiness.accounts().locations().reviews().list(locationName);
			reviewsList.setPageSize(50);
			ListReviewsResponse responses = reviewsList.execute();
			reviews = responses.getReviews();		
			Post post = new Post();		
			
			int numReviews = 0;
			
			if (responses.getReviews()!=null) {
				numReviews = responses.getReviews().size();
				
				while (responses.getNextPageToken() != null) {
					reviewsList.setPageToken(responses.getNextPageToken());	
		    	    responses = reviewsList.execute();
		    	    reviews.addAll(responses.getReviews());
		    	} 
			} 
		}
	
			  		 
		return reviews;
		
	}
	
	
	public PostsList getGOReviews(String username) throws Exception {
		PostsList postslist = new PostsList();
		List<Review> reviews = listReviews(username);
		if (reviews != null) {
			Iterator<Review> iter = reviews.iterator();
			while (iter.hasNext()) {
				Review review = iter.next();
				
				Post post = new Post();
				
				post.setId(review.getName());
				if (review.getComment() != null) {
					post.setPost(review.getComment());
				} else {
					post.setPost("");
				}
				
				post.setName(review.getReviewer().getDisplayName());
				//post.setName(username);
				post.setPlatform("Google");
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				post.setDateCreation(format.parse(review.getCreateTime()));
				post.setFatherId(review.getName());
				post.setVisibleWithParent(true);
				Post revReplay = getReviewReply(review);
				PostsList sons = new PostsList();
				if (revReplay != null) {
					sons.add(revReplay);
					
				} else {
					sons.setPostsList("[]");
					
				}
				post.setSons(sons);
				postslist.add(post);
				
			}
		}
		

		
		return postslist;
		
	}
	
	public Post getReviewReply(Review review) throws ParseException {
		Post post = new Post();
		if (review != null) {
			if (review.getReviewReply() != null) {
				if (review.getReviewReply().getComment() != null) {		
					String id = review.getName() + "&&" + review.getReviewReply().getUpdateTime();
					post.setId(id);
					post.setAnswerON(true);
					post.setDateCreation(new SimpleDateFormat("dd-MM-yyyy").parse(review.getReviewReply().getUpdateTime()));
					post.setFatherId(review.getName());
					post.setPlatform("Google");
					post.setName(review.getReviewer().getDisplayName());
					post.setPost(review.getReviewReply().getComment());
					PostsList sons = new PostsList();
					sons.setPostsList("[]");
					post.setSons(sons);
					post.setStatus("old");
					post.setVisibleWithParent(true);
				} else {
					post = null;
				}
			} else {
				post = null;
			}
		} else {
			post = null;
		}
		
			
		return post;
	}
	
	public PostsList getGMBPosts(String username) throws ClassNotFoundException, SQLException, ServletException, IOException, ParseException {
		PostsList posts = new PostsList();
		
		DB db = new DB();
		String parent = db.getGMBLocation(username);
		ListLocalPostsResponse publicacions = mybusiness.accounts().locations().localPosts().list(parent).setPageSize(5).execute();
		
		List<LocalPost> localPosts = publicacions.getLocalPosts();
		Iterator<LocalPost> iter = localPosts.iterator();
		while (iter.hasNext()) {
			LocalPost localPost = iter.next();
			Post post = new Post();
			post.setAnswerON(false);
			
			String sDate = localPost.getCreateTime();
			//String sDate = "2020-05-01T16:54:18.135Z";
			sDate = sDate.substring(0, 10) + " " + sDate.substring(11, 19);
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			post.setDateCreation(format.parse(sDate));
			
			post.setFatherId("-1");
			post.setId(localPost.getName());
			post.setPlatform("Google");
			post.setPost(localPost.getSummary());
			
			Location location = mybusiness.accounts().locations().get(parent).execute();
			post.setStatus(location.getLocationName());
			post.setName(location.getLocationName());
			post.setVisibleWithParent(true);
			PostsList sons = new PostsList();
			sons.setPostsList("[]");
			post.setSons(sons);		
			
			posts.add(post);
		
		}
		log.info(posts.getString());
		return posts;
	}
	
	public static List<Account> listAccounts() throws Exception {
		
		MyBusiness.Accounts.List accountsList = mybusiness.accounts().list();
		ListAccountsResponse response = accountsList.execute();
		List<Account> accounts = response.getAccounts();

	  for (Account account : accounts) {
	    System.out.println(account.toPrettyString());
	  }
	  return accounts;
	}
	
	public static List<Location> listLocations(String accountName) throws Exception {
		
		com.google.api.services.mybusiness.v4.MyBusiness.Accounts.Locations.List locationsList =
				mybusiness.accounts().locations().list(accountName);
		ListLocationsResponse responses = locationsList.execute();  
		List<Location> locations = responses.getLocations();
		//locations = responses.getLocations(); //borrar si no estás probando
			  
		while (responses.getNextPageToken() != null){
			locationsList.setPageToken(responses.getNextPageToken());
			responses = locationsList.execute();
			locations.addAll(responses.getLocations());
		} 
			  
		return locations;
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
	

	public static void main(String[] args) throws Exception {
		
		String sDate = "2020-05-01T16:54:18.135Z";
		sDate = sDate.substring(0, 10) + " " + sDate.substring(11, 19);
		log.info(sDate);
		
		GMB gmb = new GMB();
				
		List<Account> acc = gmb.listAccounts();
		int len2 = acc.size();
		System.out.println(len2);
		for (int i = 0; i < len2; i++) {
			System.out.println("Account: " + acc.get(i).getAccountName());
			System.out.println("LocationId: " + acc.get(i).getAccountNumber());
			System.out.println("LocationId: " + acc.get(i).getName());
			System.out.println("LocationId: " + acc.get(i).getPermissionLevel());
			System.out.println("LocationId: " + acc.get(i).getRole());
		}
		
	
		List<Location> listLoc = listLocations("accounts/103268190540206787281");
		//List<Location> listLoc = gmb.listLocations(accountJumberStores);
		
		int len = listLoc.size();
		System.out.println(len);
		for (int i = 0; i < len; i++) {
			System.out.println("Location: " + listLoc.get(i).getLocationName());
			System.out.println("LocationId: " + listLoc.get(i).getName());
			
		}
		

	}

}
