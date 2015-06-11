var fs = require('fs');
var system = require('system');
var casper = require('casper').create({
	verbose: false,
	logLevel: "debug",
	pageSettings: {
		loadImages:  true,
		loadPlugins: false,
		userAgent: 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1588.0 Safari/537.36'
	},	
});

casper.on('remote.message', function(msg) {
	this.echo('remote message caught: ' + msg);
})

casper.on("page.error", function(msg, trace) {
	this.echo("Error:    " + msg, "ERROR");
	this.echo("file:     " + trace[0].file, "WARNING");
	this.echo("line:     " + trace[0].line, "WARNING");
	this.echo("function: " + trace[0]["function"], "WARNING");
	errors.push(msg);
});

casper.start('https://twitter.com/login', function() {
	this.fill('form.t1-form.js-signin.signin', {
		'session[username_or_email]': 'vnknight88@gmail.com',
		'session[password]': 'bot@2014'
	}, true);
});

// MGM US english
var query = 'lang:en near:"36.102552,-115.169569" within:5mi since:2014-05-03 until:2014-05-04 include:retweets';
// shanghai english and all
// var search_api = 'https://twitter.com/search?f=realtime&q=lang%3Aen%20near%3A%2231.240261%2C121.490577%22%20within%3A15mi%20since%3A2014-12-31%20until%3A2015-01-01%20include%3Aretweets&src=typd';
// var search_api = 'https://twitter.com/search?f=realtime&q=near%3A%22Shanghai%22%20within%3A15mi%20since%3A2014-12-31%20until%3A2015-01-01&src=typd';

var search_api = 'https://twitter.com/search?f=realtime&q=' + encodeURIComponent(query) + '&src=typd';
casper.thenOpen(search_api, function(){
	this.viewport(1024, 768);
	scroll(this);
});


casper.then(function(){
	fs.write('result.html', this.getHTML(), 'w');
	fs.write('result.csv', '', 'w');
	var nodes = this.evaluate(function(){
		var items = document.querySelectorAll('li.stream-item');
		return Array.prototype.map.call(items, function(e) {
			var id = e.getAttribute('data-item-id');
			
			var tag_username = e.querySelector('span.username b');
			var username = tag_username ? tag_username.innerHTML : '';
			
			var tag_timestamp = e.querySelector('small.time a.tweet-timestamp span'); 
			var timestamp = tag_timestamp ? tag_timestamp.getAttribute('data-time-ms') : '';
			
			var tag_text = e.querySelector('div.content p.tweet-text');
			var text = tag_text ? tag_text.innerHTML : '';
			return {'id' : id, 'username' : username, 'timestamp' : timestamp, 'text' : text};
		});		
	});
	this.echo('number of tweets: ' + nodes.length);
	for(var i = 0; i < nodes.length; i ++){
		var node = nodes[i];
		fs.write("result.csv", '"' + node['id'] + '"\t"' + node['username'] + '"\t"' + node['timestamp'] + '"\t"' + node['text'] + '"\r\n', 'a');	
	}		
})

casper.run();

var counter = 0;
function scroll(_casper){
	var num = _casper.evaluate(function(){
		return document.querySelectorAll('li.stream-item').length;
	});
	_casper.echo('scrolling page ' + (++counter) + "=>" + num + " nodes" );
	_casper.waitFor(
		function scrollBottom(){
			this.page.scrollPosition = {top: this.page.scrollPosition["top"] + 500, left: 0};	
			this.wait(100);
			return true;

		}, function then(){
			this.echo('more=' + this.exists('div.has-more-items') + "&fail=" + this.exists('div.has-items-error')) 

			if(this.exists('div.has-items-error')){
				this.echo('clicking try again');
				this.click('a.try-again-after-whale');
				this.then(function() {
					return scroll(_casper);
				});
			} 
			else if(this.exists('div.has-more-items')){
				return scroll(_casper);
			}				
		}, function onTimeout(){
			this.echo('timeout');
		}, 10000);
}