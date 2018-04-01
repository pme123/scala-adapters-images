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
Second part of the Bot-workshop - see here to start: [scala-telegrambot4s](https://github.com/pme123/scala-telegrambot4s)
This part expects that you already did te first part!

We will implement a simple conversation, that uses [play-akka-telegrambot4s](https://github.com/pme123/play-akka-telegrambot4s).

This will only focus on the  implementation of the Bot-Conversation (`EmojiConversation). `
Everything else will be already in place.

1. Clone this repo:
   
   `git clone https://github.com/pme123/scala-adapters-images.git`
1. Checkout the starting branch:

   `git checkout create-emoji-conversation`
   
1. Verify project:

   `sbt run -Dpme.bot.token=YOUR_TOKEN`
   
   and open: `http://localhost:9000/images` (it should show emojis or photos **after a minute**)
   
1. The following chapters do the implementation

### Subscribe the conversation
Each Conversation must subscribe itself to the Command-Dispatcher.

As a preparation we need 2 things:

    object EmojiConversation {
      // the command to listen for
      val command = "/addemojis"
      val supportedEmojis: Seq[String] = (0x1F600 to 0x1F642).map(Character.toChars(_).mkString)
    
      // constructor of the Service - which is an Actor
      def props(imagesRepo: ActorRef)(implicit ec: ExecutionContext): Props = Props(EmojiConversation(imagesRepo))
    
    }
    
1. Each Conversation needs a unique command (`/addemojis`)
1. Add a constructor for your Conversation-Actor (`def props(imagesRepo: ActorRef))

Now let's do the subscription:

    // a singleton will inject all needed dependencies and subscribe the service
    @Singleton
    class EmojiConversationSubscription @Inject()(@Named("commandDispatcher") commandDispatcher: ActorRef
                                                  , @Named("`") imagesRepo: ActorRef
                                                  , system: ActorSystem)
                                                 (implicit ec: ExecutionContext) {
    
      import EmojiConversation._
    
      // subscribe the EmojiConversation to the CommandDispatcher
      commandDispatcher ! Subscription(command, SubscrConversation
        , Some(_ => system.actorOf(props(imagesRepo))))
    
    }

This is done by a Singleton that:
* injects needed Services (`imagesRepo`) 
* creates the Conversation (`system.actorOf(props(imagesRepo))`)
* and registers the Conversation to the Dispatcher (`commandDispatcher ! Subscription(..)`)
 
When reloading [localhost:9000](http://localhost:9000/images) you should find:

    2018-03-31 19:41:17,195[INFO] SubscrConversation: Subscription(/addemojis,SubscrConversation,Some(pme123.adapters.images.server.control.EmojiConversationSubscription$$Lambda$6058/36045612@5ac2517e))  

in `server/logs/application.log`

The error below indicates what our next step will be.

### Implement the Conversation
A Conversation is based on `akka.actor.FSM`, check its [documentation](https://doc.akka.io/docs/akka/2.5/fsm.html).   

As the comment suggests, we have 2 States (`FSMState`):
1. `Idle` is the initial state called by the dispatcher
   , if somebody calls the bot with `/addemojis`
1. `AddEmojis` that expects an emoji and adds it to the `EmojiRepo`.

So we first need the `AddEmojis` state:

    case object AddEmojis extends FSMState
  
This is a `case object` that extends from the `FSMState`.

Next we implement the `Idle` state:

      when(Idle) {
        case Event(Command(msg, _), _) =>
          info(s"received Command! $msg") // 1
          bot.sendMessage(msg, "Send an Emoji!") // 2
          goto(AddEmojis) // 3
        case other =>
          notExpectedData(other)
      }
      
As mentioned above this uses the DSL of `akka.actor.FSM`.

You expect an `Event` with a `Command`, here is where to put the action:
1. The message contains everything you get from Telegram 
   (so check it out!)
1. Send a message to Telegram - instruct the user what to do next.
1. Instruct the FSM where to go next.

Ok now lets add the second state:

      when(AddEmojis) {
        case Event(Command(msg, _), _) =>
          val emoji = msg.text.get // 1
          if (EmojiConversation.supportedEmojis.contains(emoji)) { // 2
            bot.sendMessage(msg, s"Thanks, just send another Emoji if you like") // 3
            imagesRepo ! EmojiData(emoji, msg.from.map(_.firstName).getOrElse("Unknown")) // 4
          } else
            bot.sendMessage(msg, s"Sorry I expected a Photo or an Emoji")
          stay() // 5
        case other =>
          notExpectedData(other)
      }

Now we are interested in the Message:
1. Get the text of the message.
1. Check if the emoji is correct.
1. Again instruct the user what to do next.
1. Update the EmojiRepo with the added emoji.
1. Tell FSM that we stay in this state.

Time to verify:
1. Update [localhost:9000](http://localhost:9000/images)
1. Register the commands to your Bot (you used the token for)
   
       addphotos - Add Photos.
       addemojis - Add Emojis.
1. Try it out `/addemojis` and play a bit with it.

### Where to go from here
* Check out a more sophisticated project: [play-akka-telegrambot4s-incidents](https://github.com/pme123/play-akka-telegrambot4s-incidents)

## Heroku
 - Go to this directory.
 - Create a Heroku Project: `heroku create`
 - After `git push` you run `git push heroku master` to publish the changes to Heroku.
 - With `heroku open` you will see the result in the browser.
 - Set the `heroku config:set pme.bot.token=TOKEN`
 
 