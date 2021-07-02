# Project 3 - FBUTwitter

**FBUTwitter** is an android app that allows a user to view their Twitter timeline and post a new tweet. The app utilizes [Twitter REST API](https://dev.twitter.com/rest/public).

Time spent: **18** hours spent in total

## User Stories

The following **required** functionality is completed:

* [X]	User can **sign in to Twitter** using OAuth login
* [X]	User can **view tweets from their home timeline**
  * [X] User is displayed the username, name, and body for each tweet
  * [X] User is displayed the [relative timestamp](https://gist.github.com/nesquena/f786232f5ef72f6e10a7) for each tweet "8m", "7h"
* [X] User can **compose and post a new tweet**
  * [X] User can click a “Compose” floating action button
  * [X] User can then enter a new tweet and post this to Twitter
  * [X] User is taken back to home timeline with **new tweet visible** in timeline
  * [X] Newly created tweet should be manually inserted into the timeline
* [X] User can **see a counter with total number of characters left for tweet** on compose tweet page
* [X] User can **pull down to refresh tweets timeline**
* [X] User can **see embedded image media within a tweet** on list or detail view.

The following **optional** features are implemented:

* [X] User is using **"Twitter branded" colors and styles**
* [X] User can **select "reply" from home timeline to respond to a tweet**
  * [X] User that wrote the original tweet is **automatically "@" replied in compose**
* [X] User can tap a tweet to **open a detailed tweet view**
* [X] User can **take favorite (and unfavorite) or retweet** actions on a tweet
* [X] User can **click a link within a tweet body** on tweet details view. The click will launch the web browser with relevant page opened.
* [X] Replace all icon drawables and other static image assets with [vector drawables](http://guides.codepath.org/android/Drawables#vector-drawables) where appropriate.

The following **additional** features are implemented:

* [X] User can open images in a larger popup window on click, then close it with a floating action button

## Video Walkthrough

Here's a walkthrough of implemented user stories:

<img src='https://github.com/hannahkm/TwitterApp/raw/master/tweeting.gif' title='Video Walkthrough Pt 1' width='200' alt='Video Walkthrough Pt 1' />
<img src='https://github.com/hannahkm/TwitterApp/raw/master/viewingTweets.gif' title='Video Walkthrough Pt 2' width='200' alt='Video Walkthrough Pt 2' />

GIF created with [Kap](https://getkap.co/).

## Notes

* My current version of Twitter does not pull the user to the top after posting a tweet; they have to manually refresh in order to see it posted. Due to some miscommunication on the tutorial, I didn't realize what was wrong until I had implemented the rest of the app entirely :( 
  * TODO: revert the ListView back to a RecyclerView!
* I ran into issues with allowing the user to like and reply to tweets. Clicking on the respective buttons would make the user like/reply completely different tweets instead. This was resolved by using an interface OnClickListener to listen to each of the items.

## Open-source libraries used

- [Android Async HTTP](https://github.com/loopj/android-async-http) - Simple asynchronous HTTP requests with JSON parsing
- [Glide](https://github.com/bumptech/glide) - Image loading and caching library for Android

## License

    Copyright 2021 Hannah Kim

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
