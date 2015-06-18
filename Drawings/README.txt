1. Content

crawler.js
	- the Javascript file to give instruction to CasperJS to crawl Twitter Advanced Search
	- see the instruction below
samples_local.csv
	- the export of table SAMPLES, containing tweets and their labelled emotions
	- the labels of columns is written in the first line
	- each column is separated by a tab and wrapped inside double quotes ""	
analysis.xls
	- the excel file contain the data analysis and graphs
	- see the instruction below
Sandbox
	- the Java application to run the Emotion Analysis using the Bag-of-Words approach
	- see the instruction below
NRC_lexicon.txt
	- The NRC Hashtag Emotion Lexicon, retrieved from http://saifmohammad.com/WebPages/lexicons.html

2. How to run crawler.js
PhantomJS and CasperJS are required to be download and added into the PATH environment
	- PhantomJS: Download from http://phantomjs.org/download.html
	- CasperJS: Download from http://casperjs.org
Run command
	$ casperjs crawler.js

3. How to run the Sandbox Java application
The application requires MySQL database with the configuration specified in DatabaseManager.java

Run the main method under LexicalAnalysis.java

4. How to use analysis.xls
Sheet0 contains the 15 min segments, with the emotion rates of 4 emotions, moving average, z-score and Rule Based Reasoning
Sheet1 contains the emotion extraction result
Sheet2 contains the extraction of Sheet0 focusing on the period of the event
Sheet3 contains diagrams of the analysis
Sheet4 contains the histogram analysis 

5. Extras
These following files are extras, containing intermediate results and other attempts

tweets_local.csv
	- the export of tweets, without the emotion label
	- the original result of crawling
	- the labels of columns is written in the first line
	- each column is separated by a tab and wrapped inside double quotes ""
emotion4.csv
	- the export of tweets (tweet_id) and the summative scores for 4 emotions: anger, fear, happiness, sadness
	- the intermediate result of extracting the dominant emotion from tweets by using the defined 4 emotions
	- the labels of columns is written in the first line
	- each column is separated by a tab and wrapped inside double quotes ""
emotion8.csv
	- the export of tweets (tweet_id) and the summative scores for 8 emotions: anger, anticipation, disgust, fear, joy, sadness, surprise, trust
	- the intermediate result of extracting the dominant emotion from tweets by using Plutchik's theory of 8 emotions
	- the labels of columns is written in the first line
	- each column is separated by a tab and wrapped inside double quotes ""