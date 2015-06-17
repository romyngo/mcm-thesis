package com.romy.sandbox;

public class Tweet{
	public long tweet_id;
	public String user_id;
	public String user_name;
	public long created;
	public String text;
	public String emotion;
	
	public double[] emoScores = new double[8]; 

	public void addWord(Word word){
		this.emoScores[Word.ANGER] += word.emoScores[Word.ANGER];
		this.emoScores[Word.ANTICIPATION] += word.emoScores[Word.ANTICIPATION];
		this.emoScores[Word.DISGUST] += word.emoScores[Word.DISGUST];
		this.emoScores[Word.FEAR] += word.emoScores[Word.FEAR];
		this.emoScores[Word.JOY] += word.emoScores[Word.JOY];
		this.emoScores[Word.SADNESS] += word.emoScores[Word.SADNESS];
		this.emoScores[Word.SURPRISE] += word.emoScores[Word.SURPRISE];
		this.emoScores[Word.TRUST] += word.emoScores[Word.TRUST];
	}

	public double[] getNormScore(){
		double totalScore = 0;
		double[] normScore = new double[8];
		for(int i = 0; i < Word.EMOLENGTH; i ++){
			totalScore += this.emoScores[i];
		}
		for(int i = 0; i < Word.EMOLENGTH; i ++){
			if(totalScore > 0.0)
				normScore[i] = this.emoScores[i] / totalScore;
			else
				normScore[i] = 0.0;
		}
		return normScore;
	}

	public String getEmotion(){
		if(emoScores[Word.ANGER] > emoScores[Word.FEAR]
			&& emoScores[Word.ANGER] > emoScores[Word.JOY]
			&& emoScores[Word.ANGER] > emoScores[Word.SADNESS])
			return "anger";
		if(emoScores[Word.FEAR] > emoScores[Word.ANGER]
				&& emoScores[Word.FEAR] > emoScores[Word.JOY]
				&& emoScores[Word.FEAR] > emoScores[Word.SADNESS])
				return "fear";
		if(emoScores[Word.JOY] > emoScores[Word.FEAR]
				&& emoScores[Word.JOY] > emoScores[Word.ANGER]
				&& emoScores[Word.JOY] > emoScores[Word.SADNESS])
				return "joy";
		if(emoScores[Word.SADNESS] > emoScores[Word.FEAR]
				&& emoScores[Word.SADNESS] > emoScores[Word.JOY]
				&& emoScores[Word.SADNESS] > emoScores[Word.ANGER])
				return "sadness";
		return "unknown";
	}
}
