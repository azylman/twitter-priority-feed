package com.zylman.twitter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

	public static void main(String[] args) throws TwitterException, IOException, TwitterDatabaseException {
		
		BufferedReader props = new BufferedReader(new FileReader("appauth.properties"));
		
		String consumerKey = props.readLine();
		String consumerSecret = props.readLine();
		String accessToken = props.readLine();
		String accessTokenSecret = props.readLine();
		String databaseUser = props.readLine();
		String databasePass = props.readLine();
		
		TwitterDatabase twitterDatabase = new TwitterDatabase("localhost", "twitter", databaseUser, databasePass);
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
			.setOAuthConsumerKey(consumerKey)
			.setOAuthConsumerSecret(consumerSecret)
			.setOAuthAccessToken(accessToken)
			.setOAuthAccessTokenSecret(accessTokenSecret);
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		
		// Get user's posts - content to use in learning (pay special attention to mentions)
		List<Status> userPosts = twitter.getUserTimeline(new Paging().count(10));
		printStatuses(userPosts);
		// Get user's retweets - content to use in learning
		List<Status> userRetweets = twitter.getRetweetedByMe(new Paging().count(10));
		printStatuses(userRetweets);
		// Get user's favorites - content to use in learning
		// Get user's homeline - content to categorize
	    List<Status> homeline = twitter.getHomeTimeline(new Paging().count(10));
	    printStatuses(homeline);
	    
	}
	
	private static void printStatuses(List<Status> statuses) {
		for (Status status : statuses) {
			Status workingStatus = status;
			String originalAuthor = status.getUser().getName();
			String prefix = "From " + originalAuthor;
			if (status.isRetweet()) {
				workingStatus = status.getRetweetedStatus();
				String currentAuthor = workingStatus.getUser().getName();
				prefix = originalAuthor + " retweeted a status by " + currentAuthor;
			}
			Set<String> mentions = getMentions(workingStatus);
	        System.out.println(prefix + " mentioning " + convertStringsToString(mentions) + ": "
	        		+ workingStatus.getText());
	    }
	}

	private static Set<String> getMentions(Status status) {
		Set<String> result = new HashSet<String>();
		result.add(filterMention(status.getInReplyToScreenName()));
		List<String> words = Arrays.asList(status.getText().split(" "));
		for (String word : words) {
			if (word.indexOf('@') == 0) {
				result.add(filterMention(word));
			}
		}
		result.remove(null);
		return result;
	}
	
	/**
	 * Remove any extraneous characters that aren't part of the user name (leading @, trailing :).
	 */
	private static String filterMention(String string) {
		if (string == null) {
			return null;
		}
		return string.replaceAll("[@:]", "");
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
