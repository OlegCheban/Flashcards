[![Build](https://github.com/OlegCheban/Flashcards/actions/workflows/mvn.yml/badge.svg)](https://github.com/OlegCheban/Flashcards/actions/workflows/mvn.yml)
[![Maintainability](https://api.codeclimate.com/v1/badges/f2d9a17907b5cfa0500a/maintainability)](https://codeclimate.com/github/OlegCheban/Flashcards/maintainability)
[![Hits-of-Code](https://hitsofcode.com/github/olegcheban/flashcards?branch=master&label=Hits-of-Code)](https://hitsofcode.com/github/olegcheban/flashcards/view?branch=master&label=Hits-of-Code)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](https://makeapullrequest.com)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/OlegCheban/Flashcards/blob/master/LICENSE)

# Flashcards Telegram Bot
A Telegram bot that helps you learn vocabulary words quickly and effectively. It uses spaced repetition learning technique and has a lot of flashcards.

Deployed bot (https://t.me/proFlashcardsBot) has around 100k flashcards.


#### Help
```
1. Basic commands for finding flashcards.
'/f' - suggests the most popular flashcards
'/f <value>' - finds a specific flashcard by input value (English word)
Additional flashcard options:
   - The ability to add flashcard to your profile for learning. Use the "add to learn" button;
   - The ability to exclude already known flashcards from learning. Use the "exclude" button.

2. Swiper
Swiper allows you to easily navigate through all of your flashcards.
'/s' - displays all flashcards added by the user to their profile
'/s <value> <prc>' - displays flashcards containing the input value (English word or substring) 
    and/or by percentile of learning
Additional options:
   - The ability to reset learning statistics for flashcards which were already learned (100% progress);
     Use the "reset progress" button;
   - The ability to add the most significant flashcards to the next learning session;
     Use the "boost priority" button;
   - The ability to see examples of usage. Use the "example of usage" button;
   - The ability to remove flashcards from profile. Use the "remove" button.

3. Exercises
Bot has several kinds of exercises which help you learn flashcards
'/l' to start learning
'/e' to enable exercises 
'/d' to disable exercises
'/pr' to start learning prepositions

4. Watering session (repeat your learned flashcards and pick correct translation before time runs out)
'/w' to start the watering session

5. Others commands
'/ni <min>' - changes the random flashcards notifications interval (default 60 min) 
'/fq <min>' -  changes the number of flashcards for training (default 5 flashcards)
'/wt <seconds>' - changes the watering session reply time (default 5 seconds)
'/ed <value>' - finds a flashcard from your profile by input value (English word) and suggests 
    changing the translation
'/h' - displays a list of available commands and options, along with brief descriptions 
    of what each command does

6. Learning statistic
'/rl <quantity>' - to get <quantity> recent learned flashcards 

7. Bot sends notifications:
   - spaced repetition notifications. 
     The bot sends only learned flashcards on the 2nd, 3rd, 7th, 14th, 30th and 90th day. 
     At each stage, you can reset flashcard to learn again;
   - random flashcards notifications.
```
  
   
