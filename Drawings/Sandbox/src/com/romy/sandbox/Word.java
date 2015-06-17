package com.romy.sandbox;

public class Word {
	public static final int ANGER = 0;
	public static final int ANTICIPATION = 1;
	public static final int DISGUST = 2;
	public static final int FEAR = 3;
	public static final int JOY = 4;
	public static final int SADNESS = 5;
	public static final int SURPRISE = 6;
	public static final int TRUST = 7;
	public static final int EMOLENGTH = 8;
	public static final double EPSILON = 0.000001;
	
	public double[] emoScores = new double[8];
	
	public Word(){
		for(int i = 0; i < EMOLENGTH; i ++){
			emoScores[i] = EPSILON;
		}
	}
}
