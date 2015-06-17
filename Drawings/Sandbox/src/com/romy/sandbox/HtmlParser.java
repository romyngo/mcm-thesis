package com.romy.sandbox;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlParser {
	public static final String PATH = "/home/romy/Downloads/doc/";
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(PATH + "tweet.csv", false));
		BufferedWriter logger = new BufferedWriter(new FileWriter(PATH + "tweet.log", false));

		HashMap<String, Integer> map = new HashMap<String, Integer>();
		
		File dir = new File(PATH);
		File[] inputs = dir.listFiles();
		for(File input : inputs){
			System.out.println("Reading:" + input.getName());
			Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");
			System.out.println("Read:" + input.getName());
			
			int counter = 0;
			Elements tweets = doc.select("li.stream-item");
			for(Element e : tweets){
				boolean isError = false;
				String tweetId = "";
				String userId = "";
				String userName = "";
				String timestamp = "";
				String content = "";

				Element summaryDiv = e.select("div.original-tweet").first();
				if(summaryDiv != null){
					tweetId = summaryDiv.attr("data-tweet-id");
					userId = summaryDiv.attr("data-user-id");
					userName = summaryDiv.attr("data-screen-name");
				} else{
					isError = true;
				}

				Element timeSpan = e.select("a.tweet-timestamp > span").first();
				if(timeSpan != null){
					timestamp = timeSpan.attr("data-time-ms");
				} else{
					isError = true;
				}

				Element textP = e.select("p.tweet-text").first();
				if(textP != null){
					content = textP.text();
				} else{
					isError = true;
				}

				if(!isError){
					if(!map.containsKey(tweetId)){
						map.put(tweetId, 1);
						System.out.println(++counter + ":" + tweetId);
						String data = "\"" + tweetId + "\"\t\"" + userId + "\"\t\"" + userName + "\"\t\"" + timestamp + "\"\t\"" + content + "\"";
						writer.write(data);
						writer.write(System.lineSeparator());
					} else{
						System.out.println(++counter + ":Duplicated");
					}
				} else{
					System.out.println(++counter + ":Error");
					logger.write(e.html());
					logger.write(System.lineSeparator());
				}
			}
		}
		writer.close();
		logger.close();
	}
}
