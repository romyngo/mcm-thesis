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
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.mcm.database.DatabaseManager;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class LexicalAnalysis {

	/**
	 * Insert the tweets from CSV file one by one into the database. Ignore rows with exception
	 * @param filePath the path to CSV file
	 */
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
			pst = conn.prepareStatement("INSERT INTO samples (tweet_id, user_id, user_name, created, text) VALUES (?, ?, ?, ?, ?)");
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

	/**
	 * Insert the tweets from the CSV file in batch. Stop upon exception
	 * @param filePath the path to the CSV file
	 */
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
			pst = conn.prepareStatement("INSERT INTO samples (tweet_id, user_id, user_name, created, text) VALUES (?, ?, ?, ?, ?)");
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

	/**
	 * Read the lexicon file into memory
	 * @param filePath path to the lexicon file
	 * @return the HashMap contain a word and its association scores with the emotions
	 */
	private static HashMap<String, Word> loadLexiconWeight(String filePath){
		FileReader fileReader = null;
		BufferedReader reader = null;
		HashMap<String, Word> map = new HashMap<String, Word>();
		try {
			fileReader = new FileReader(filePath);
			reader = new BufferedReader(fileReader);
			String line = "";
			while((line = reader.readLine()) != null){
				String[] fields = line.split("\t");
				String category = fields[0];
				String keyword = fields[1];
				double score = Double.valueOf(fields[2]);

				Word word = map.get(keyword);
				if(word == null){
					word = new Word();
				}
				if(category.equals("anger"))
					word.emoScores[Word.ANGER] = score;
				else if(category.equals("anticipation"))
					word.emoScores[Word.ANTICIPATION] = score;
				else if(category.equals("disgust"))
					word.emoScores[Word.DISGUST] = score;
				else if(category.equals("fear"))
					word.emoScores[Word.FEAR] = score;
				else if(category.equals("joy"))
					word.emoScores[Word.JOY] = score;
				else if(category.equals("sadness"))
					word.emoScores[Word.SADNESS] = score;
				else if(category.equals("surprise"))
					word.emoScores[Word.SURPRISE] = score;
				else if(category.equals("trust"))
					word.emoScores[Word.TRUST] = score;
				map.put(keyword, word);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if(fileReader != null)
				try {
					fileReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return map;
	}

	public static void main(String[] args) {
		// insert tweets into database
		insertBatchFromCSV("tweets.csv");
		// read association scoring from lexicon
		HashMap<String, Word> map = loadLexiconWeight("NRC_lexicon.text");

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		DatabaseManager dm = DatabaseManager.getInstance();
		Connection conn = dm.openConnection();
		Statement st = null;
		ResultSet rs = null;

		// get tweets from database
		List<Tweet> tweets = new ArrayList<Tweet>();
		try{
			st = conn.createStatement();
			rs = st.executeQuery("SELECT tweet_id, text FROM samples");
			int count = 0;
			while(rs.next()){
				long tweet_id = rs.getLong(1);
				String text = rs.getString(2);

				Tweet tweet = new Tweet();
				tweet.tweet_id = tweet_id;
				tweet.text = text;

				// Token tweet into words
				Annotation annotation = pipeline.process(text);
				List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
				// Sum up the scores
				for(CoreMap sentence: sentences){
					for(CoreLabel token : sentence.get(TokensAnnotation.class)){
						String pos = token.get(TextAnnotation.class);
						Word word = map.get(pos);
						if(word != null){
							// do sum up
							tweet.addWord(word);
						} else{
							// add epsilon word if not existing in lexicon
							tweet.addWord(new Word());
						}
					}
				}
				tweets.add(tweet);
				System.out.println("process " + tweets.size());
			}
		} catch(SQLException e){
			e.printStackTrace();
		} finally{
			if(rs != null)
				dm.closeResultSet(rs);
			if(st != null)
				dm.closeStatement(st);
			if(conn != null)
				dm.closeConnection(conn);
		}

		// insert tweets into emotion4 tables to save intermediate result
		conn = dm.openConnection();
		PreparedStatement pst = null;
		try {
			pst = conn.prepareStatement("INSERT INTO emotion4 VALUES (?, ?, ?, ?, ?, ?)");
			int count = 0;
			for(Tweet tweet : tweets){
				pst.setLong(1, tweet.tweet_id);
				pst.setString(2, tweet.getEmotion());
				pst.setDouble(3, tweet.emoScores[Word.ANGER]);
				pst.setDouble(4, tweet.emoScores[Word.FEAR]);
				pst.setDouble(5, tweet.emoScores[Word.JOY]);
				pst.setDouble(6, tweet.emoScores[Word.SADNESS]);

				pst.addBatch();
				System.out.println("add to batch " + count++);
			}
			pst.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if(pst != null)
				dm.closeStatement(pst);
			if(conn != null)
				dm.closeConnection(conn);
		}
	
	}
}
