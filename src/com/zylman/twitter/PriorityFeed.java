package com.zylman.twitter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class PriorityFeed {

	public static void main(String[] args) throws TwitterException, IOException {
		
		BufferedReader props = new BufferedReader(new FileReader("appauth.properties"));
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
			.setOAuthConsumerKey(props.readLine())
			.setOAuthConsumerSecret(props.readLine())
			.setOAuthAccessToken(props.readLine())
			.setOAuthAccessTokenSecret(props.readLine());
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		
		// Get user's posts - content to use in learning (pay special attention to mentions)
		List<Status> userPosts = twitter.getUserTimeline(new Paging().count(10));
		printStatuses(userPosts);
		// Get user's retweets - content to use in learning
		// Get user's favorites - content to use in learning
		// Get user's homeline - content to categorize
	    List<Status> homeline = twitter.getHomeTimeline(new Paging().count(10));
	    printStatuses(homeline);
	    
	}
	
	private static void printStatuses(List<Status> statuses) {
		for (Status status : statuses) {
			Set<String> mentions = getMentions(status);
	        System.out.println("From " + status.getUser().getName()
	        		+ " mentioning " + convertStringsToString(mentions) + ": "
	        		+ status.getText());
	    }
	}

	private static Set<String> getMentions(Status status) {
		Set<String> result = new HashSet<String>();
		result.add(status.getInReplyToScreenName());
		List<String> words = Arrays.asList(status.getText().split(" "));
		for (String word : words) {
			if (word.indexOf('@') == 0) {
				result.add(word.substring(1));
			}
		}
		result.remove(null);
		return result;
	}
	
	private static String convertStringsToString(Collection<String> strings) {
		if (strings.size() == 0) {
			return "";
		}
		
		StringBuilder result = new StringBuilder();
		
		for (String string : strings) {
			result.append(string);
			result.append(", ");
		}
		
		return result.substring(0, result.length() - 2);
	}
}
