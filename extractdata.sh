#!/bin/bash

# Tweets downloaded from:
#   https://archive.org/details/archiveteam-twitter-stream-2017-06

# Extract the tar archive.
tar -xvf archiveteam-twitter-stream-2017-06.tar

# Pick an interesting day and hour.
# i.e.  Jun 2 US President Donald Trump announces the US is withdrawing from the
#       Paris Climate Agreement
dayHourOfInterest='02/20'

# This will decompress about 1.67 GB, and should take about X minutes.
tweets=tweets.txt
path=2017/06/$dayHourOfInterest
find $path -type f | while read file; do bzip2 -dkc $file >> $tweets; done

# Remove lines that don't contain a tweet.
sed -i '/{"delete":{/d' $tweets

# Extract text from JSON.
#   Before: {"created_at":"Sat Jun 03 02:30:00 +0000 2017","id":870829894141304832,"id_str":"870829894141304832","text":"Try to look at your weakness and convert it into your strength. That's success.\n\n#MMKPiaWurtzbach","source":"\u003ca href=\"https:\/\/about.twitter.com\/products\/tweetdeck\" rel=\"nofollow\"\u003eTweetDeck\u003c\/a\u003e","truncated":false,"in_reply_to_status_id":null,"in_reply_to_status_id_str":null,"in_reply_to_user_id":null,"in_reply_to_user_id_str":null,"in_reply_to_screen_name":null,"user":{"id":831347491144495104,"id_str":"831347491144495104","name":"ROSALYN ANN TINGSON","screen_name":"RosalynTingson","location":null,"url":null,"description":null,"protected":false,"verified":false,"followers_count":18,"friends_count":46,"listed_count":0,"favourites_count":4,"statuses_count":2554,"created_at":"Tue Feb 14 03:41:02 +0000 2017","utc_offset":null,"time_zone":null,"geo_enabled":false,"lang":"en","contributors_enabled":false,"is_translator":false,"profile_background_color":"F5F8FA","profile_background_image_url":"","profile_background_image_url_https":"","profile_background_tile":false,"profile_link_color":"1DA1F2","profile_sidebar_border_color":"C0DEED","profile_sidebar_fill_color":"DDEEF6","profile_text_color":"333333","profile_use_background_image":true,"profile_image_url":"http:\/\/pbs.twimg.com\/profile_images\/831347719243386881\/NmZQeAz4_normal.jpg","profile_image_url_https":"https:\/\/pbs.twimg.com\/profile_images\/831347719243386881\/NmZQeAz4_normal.jpg","default_profile":true,"default_profile_image":false,"following":null,"follow_request_sent":null,"notifications":null},"geo":null,"coordinates":null,"place":null,"contributors":null,"is_quote_status":false,"retweet_count":0,"favorite_count":0,"entities":{"hashtags":[{"text":"MMKPiaWurtzbach","indices":[81,97]}],"urls":[],"user_mentions":[],"symbols":[]},"favorited":false,"retweeted":false,"filter_level":"low","lang":"en","timestamp_ms":"1496457000657"}
#   After: Try to look at your weakness and convert it into your strength. That's success.\n\n#MMKPiaWurtzbach
sed -i 's/^.*,"text":\(".*\)/\1/g' $tweets
sed -i 's/\(^.*\),"source":.*/\1/g' $tweets
sed -i 's/\(^.*\),"display_text_range":.*/\1/g' $tweets

# Delete tweets with Unicode
sed -i '/\\u/d' $tweets
