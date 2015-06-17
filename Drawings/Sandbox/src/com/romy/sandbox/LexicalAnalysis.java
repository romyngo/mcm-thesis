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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.mcm.database.DatabaseManager;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class LexicalAnalysis {	
	private static HashMap<String, Word> loadLexiconWeight(){
		FileReader fileReader = null;
		BufferedReader reader = null;
		HashMap<String, Word> map = new HashMap<String, Word>();
		try {
			fileReader = new FileReader("/home/romy/project/ref/NRC-Hashtag-Emotion-Lexicon.txt");
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
		HashMap<String, Word> map = loadLexiconWeight();

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		DatabaseManager dm = DatabaseManager.getInstance();
		Connection conn = dm.openConnection();
		Statement st = null;
		ResultSet rs = null;

		List<Tweet> tweets = new ArrayList<Tweet>();
		try{
			st = conn.createStatement();
			rs = st.executeQuery("SELECT tweet_id, text FROM samples WHERE tweet_id NOT IN (SELECT tweet_id FROM emotions)");
			int count = 0;
			while(rs.next()){
				long tweet_id = rs.getLong(1);
				String text = rs.getString(2);

				Tweet tweet = new Tweet();
				tweet.tweet_id = tweet_id;
				tweet.text = text;

				Annotation annotation = pipeline.process(text);
				List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

				for(CoreMap sentence: sentences){
					for(CoreLabel token : sentence.get(TokensAnnotation.class)){
						String pos = token.get(TextAnnotation.class);
						Word word = map.get(pos);
						if(word != null){
							tweet.addWord(word);
						} else{
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
		
		
		conn = dm.openConnection();
		PreparedStatement pst = null;
		try {
			pst = conn.prepareStatement("INSERT INTO emotions VALUES (?, ?, ?, ?, ?, ?, ? , ?, ?, ?)");
			int count = 0;
			for(Tweet tweet : tweets){
				pst.setLong(1, tweet.tweet_id);
				pst.setString(2, tweet.getEmotion());
				pst.setDouble(3, tweet.emoScores[Word.ANGER]);
				pst.setDouble(4, tweet.emoScores[Word.ANTICIPATION]);
				pst.setDouble(5, tweet.emoScores[Word.DISGUST]);
				pst.setDouble(6, tweet.emoScores[Word.FEAR]);
				pst.setDouble(7, tweet.emoScores[Word.JOY]);
				pst.setDouble(8, tweet.emoScores[Word.SADNESS]);
				pst.setDouble(9, tweet.emoScores[Word.SURPRISE]);
				pst.setDouble(10, tweet.emoScores[Word.TRUST]);

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
