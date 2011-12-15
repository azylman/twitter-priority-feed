package com.zylman.twitter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

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
		
	    List<Status> statuses = twitter.getHomeTimeline(new Paging().count(10));
	    for (Status status : statuses) {
	        System.out.println(status.getUser().getName() + ":" +
	                           status.getText());
	    }
	}

}
