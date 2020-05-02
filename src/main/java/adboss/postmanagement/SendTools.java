package adboss.postmanagement;

import java.util.Iterator;

public class SendTools {
	
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
	
	public PostsList identifyFBPosts(PostsList postsList) {
		PostsList newList = new PostsList();
		Iterator<Post> iter = postsList.iterator();
		while (iter.hasNext()) {
			Post post = iter.next();
			if (post.getPlatform().equals("Facebook")) {
				newList.add(post);
			}
		}
		return newList;
	}

	public static void main(String[] args) {
		
	}

}
