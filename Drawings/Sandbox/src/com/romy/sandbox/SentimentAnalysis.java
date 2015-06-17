package com.romy.sandbox;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mcm.database.DatabaseManager;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

public class SentimentAnalysis {

	public static void main(String[] args) {
		insertOneByOneFromCSV("/home/romy/Downloads/doc/tweet.csv");
//		insertBatchFromCSV("/home/romy/Downloads/doc/tweet.csv");
	}

	private static void insertOneByOneFromCSV(String filePath){
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		List<Tweet> tweets = new ArrayList<Tweet>();

		try {
			fileReader = new FileReader(filePath);
			bufferedReader = new BufferedReader(fileReader);
			String line = null;

			while((line = bufferedReader.readLine()) != null){
				String[] fields = line.split("\t");
				String tweet_id = fields[0];
				String user_id = fields[1];
				String user_name = fields[2];
				String created = fields[3];
				String text = fields[4];

				Tweet tweet = new Tweet();
				tweet.tweet_id = Long.parseLong(tweet_id.substring(1, tweet_id.length() - 1));
				tweet.user_id = user_id.substring(1, user_id.length() - 1);
				tweet.user_name = user_name.substring(1, user_name.length() - 1);
				tweet.created = Long.parseLong(created.substring(1, created.length() - 1));
				tweet.text = text.substring(1, text.length() - 1);

				tweets.add(tweet);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(bufferedReader != null){
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fileReader != null){
				try {
					fileReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		DatabaseManager dm = DatabaseManager.getInstance();
		Connection conn = dm.openConnection();
		PreparedStatement pst = null;
		try{
			pst = conn.prepareStatement("INSERT INTO samples VALUES (?, ?, ?, ?, ?)");
			for(Tweet tweet : tweets){
				try{
					System.out.println("insert " + tweet.tweet_id);
					pst.setLong(1, tweet.tweet_id);
					pst.setString(2, tweet.user_id);
					pst.setString(3, tweet.user_name);
					pst.setTimestamp(4, new Timestamp(tweet.created));
					pst.setString(5, tweet.text);
					pst.executeUpdate();
				} catch(MySQLIntegrityConstraintViolationException e){
					e.printStackTrace();
				}
			}
		} catch(SQLException e){
			e.printStackTrace();
		} finally{
			dm.closeStatement(pst);
			dm.closeConnection(conn);
		}
	}


	private static void insertBatchFromCSV(String filePath){
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		List<Tweet> tweets = new ArrayList<Tweet>();

		try {
			fileReader = new FileReader(filePath);
			bufferedReader = new BufferedReader(fileReader);
			String line = null;

			while((line = bufferedReader.readLine()) != null){
				String[] fields = line.split("\t");
				String tweet_id = fields[0];
				String user_id = fields[1];
				String user_name = fields[2];
				String created = fields[3];
				String text = fields[4];

				Tweet tweet = new Tweet();
				tweet.tweet_id = Long.parseLong(tweet_id.substring(1, tweet_id.length() - 1));
				tweet.user_id = user_id.substring(1, user_id.length() - 1);
				tweet.user_name = user_name.substring(1, user_name.length() - 1);
				tweet.created = Long.parseLong(created.substring(1, created.length() - 1));
				tweet.text = text.substring(1, text.length() - 1);

				tweets.add(tweet);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(bufferedReader != null){
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fileReader != null){
				try {
					fileReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		DatabaseManager dm = DatabaseManager.getInstance();
		Connection conn = dm.openConnection();
		PreparedStatement pst = null;
		try{
			pst = conn.prepareStatement("INSERT INTO samples VALUES (?, ?, ?, ?, ?)");
			for(Tweet tweet : tweets){
				pst.setLong(1, tweet.tweet_id);
				pst.setString(2, tweet.user_id);
				pst.setString(3, tweet.user_name);
				pst.setTimestamp(4, new Timestamp(tweet.created));
				pst.setString(5, tweet.text);
				pst.addBatch();
			}
			pst.executeBatch();
		} catch(SQLException e){
			e.printStackTrace();
		} finally{
			dm.closeStatement(pst);
			dm.closeConnection(conn);
		}
	}

	private static void analysis(){
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		DatabaseManager dm = DatabaseManager.getInstance();
		Connection conn = dm.openConnection();

		PreparedStatement pst = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			pst = conn.prepareStatement("UPDATE sandboxs SET sentiment = ? WHERE tweet_id = ?");
			st = conn.createStatement();
			rs = st.executeQuery("SELECT tweet_id, text FROM sandboxs");
			int count = 0;
			while(rs.next()){
				long tweet_id = rs.getLong(1);
				String text = rs.getString(2);

				double totalScore = 0;
				double totalSentence = 0;
				Annotation annotation = pipeline.process(text);
				List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
				for (CoreMap sentence : sentences) {
					String sentiment = sentence.get(SentimentCoreAnnotations.ClassName.class);
					totalSentence ++;
					if(sentiment.equals("Very Negative")){
						totalScore += -2;
					}
					else if(sentiment.equals("Negative")){
						totalScore += -1;
					}
					else if(sentiment.equals("Neutral")){
						totalScore += 0;
					}
					else if(sentiment.equals("Positive")){
						totalScore += 1;
					}
					else if(sentiment.equals("Very Positive")){
						totalScore += 2;
					}					
				}
				pst.setLong(1, Math.round(totalScore / totalSentence));
				pst.setLong(2, tweet_id);
				pst.addBatch();
				System.out.println(count ++);
			}
			pst.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			dm.closeResultSet(rs);
			dm.closeStatement(st);
			dm.closeStatement(pst);
			dm.closeConnection(conn);
		}
	}
}
