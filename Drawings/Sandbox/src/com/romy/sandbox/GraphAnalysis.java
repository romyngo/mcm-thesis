package com.romy.sandbox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.mcm.database.DatabaseManager;

public class GraphAnalysis {
	public static void main(String[] args) {
		DatabaseManager dm = DatabaseManager.getInstance();

		Connection conn = dm.openConnection();
		Statement st = null;
		ResultSet rs = null;
		ArrayList<Tweet> tweets = new ArrayList<Tweet>();
		try {
			st = conn.createStatement();
			rs = st.executeQuery("SELECT s.tweet_id, s.created, s.text, e.emotion"
					+ " FROM samples s"
					+ " JOIN emotions e ON s.tweet_id = e.tweet_id"
					+ " ORDER BY s.created ASC");
			while(rs.next()){
				Tweet t = new Tweet();
				t.tweet_id = rs.getLong("tweet_id");
				t.created = rs.getTimestamp("created").getTime();
				t.text = rs.getString("text");
				t.emotion = rs.getString("emotion");
				tweets.add(t);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if(rs != null)
				dm.closeResultSet(rs);
			if(st != null)
				dm.closeStatement(st);
			if(conn != null)
				dm.closeConnection(conn);
		}


		final long HISTOGRAM = 1800000;
		long start = tweets.get(0).created;

		int histogram_count = 1;
		int total = 0;
		int total_anger = 0;
		int total_anticipation = 0;
		int total_disgust = 0;
		int total_fear = 0;
		int total_joy = 0;
		int total_sadness = 0;
		int total_surprise = 0;
		int total_trust = 0;
		for(int i = 0; i < tweets.size(); i ++){
			Tweet t = tweets.get(i);
			total ++;
			if("anger".equals(t.emotion))
				total_anger ++;
			if("anticipation".equals(t.emotion))
				total_anticipation ++;
			if("disgust".equals(t.emotion))
				total_disgust ++;
			if("fear".equals(t.emotion))
				total_fear ++;
			if("joy".equals(t.emotion))
				total_joy ++;
			if("sadness".equals(t.emotion))
				total_sadness ++;
			if("surprise".equals(t.emotion))
				total_surprise ++;
			if("trust".equals(t.emotion))
				total_trust ++;

			if(t.created > start + HISTOGRAM || i == tweets.size() - 1){
				System.out.println(histogram_count + "," + total + "," + 
						total_anger + "," + total_anticipation + "," + 
						total_disgust + "," + total_fear + "," + 
						total_joy + "," + total_sadness + "," + 
						total_surprise + "," + total_trust);

				start = t.created;
				total = 0;
				total_anger = 0;
				total_anticipation = 0;
				total_disgust = 0;
				total_fear = 0;
				total_joy = 0;
				total_sadness = 0;
				total_surprise = 0;
				total_trust = 0;
				histogram_count ++;
			}
		}
	}
}
