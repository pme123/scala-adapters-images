# scala-adapters-images
[![Build Status](https://travis-ci.org/pme123/scala-adapters-images.svg?branch=master)](https://travis-ci.org/pme123/scala-adapters-images)
[![](https://jitpack.io/v/pme123/scala-adapters-images.svg)](https://jitpack.io/#pme123/scala-adapters-images)
[![Heroku](http://heroku-badge.herokuapp.com/?app=quiet-wave-78301)](https://quiet-wave-78301.herokuapp.com)

A project that uses my own small libraries:
* [scala-adapters](https://github.com/pme123/scala-adapters)
* [play-akka-telegrambot4s](https://github.com/pme123/play-akka-telegrambot4s)

It is a simple Job that switches between a page with Photos and a page with Emojis.

The idea is that this could show on a Company monitor:
* The holiday pictures of the employees.
* The mood of the employees.

The following image shows you, how to add your Mood with Telegram:
![image](https://user-images.githubusercontent.com/3437927/38164619-91f8ff72-3507-11e8-862c-a5a612d03234.png)

## Creating a Bot-Conversation
We will implement a simple conversation, that uses [play-akka-telegrambot4s](https://github.com/pme123/play-akka-telegrambot4s).

This will only focus on the  implementation of the Bot-Conversation (). Everything else will be already in place.

1. Clone this repo:
   
   `git clone https://github.com/pme123/scala-adapters-images.git`
2. Checkout the starting branch:

   `git checkout create-emoji-conversation`
   
3. Verify project:

   `sbt run -Dpme.bot.token=YOUR_TOKEN`
   
   and open: `http://localhost:9000/images` (it should show emojis or photos **after a minute**)
   
## Heroku
 - Go to this directory.
 - Create a Heroku Project: `heroku create`
 - After `git push` you run `git push heroku master` to publish the changes to Heroku.
 - With `heroku open` you will see the result in the browser.
 - Set the `heroku config:set pme.bot.token=TOKEN`
 
 