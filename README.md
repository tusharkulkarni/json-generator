# json-generator
1.Generate alchemyapi key from : http://www.alchemyapi.com/api/register.html  
2.Edit api_key.txt file and add your key in this file.  
3.Generate microsoft api key from : https://msdn.microsoft.com/en-us/library/mt146806.aspx  
4.Edit src/com/util/Utility.java line nos-38 and 39 to include your clientID and clientSecret.  

Add json objects to which you want to markup with content tags and translated text to file train.json in following format :  
[
{
"id": "676463657069453312",
"username": "",
"created_at": "2015-12-14T01:07:58Z",
"lang": "fr",
"text_fr": "tweet_text",
"text_replace": "",
"favorite_count": "count",
"location": "location",
"tweet_hashtags": ["tweet_hashtags"],
"tweet_urls": ["tweet_urls"]
}
]
  
Run src/com/util/Util.java to markup your json object.  
