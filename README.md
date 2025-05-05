[![Build](https://github.com/OlegCheban/Flashcards/actions/workflows/mvn.yml/badge.svg)](https://github.com/OlegCheban/Flashcards/actions/workflows/mvn.yml)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](https://makeapullrequest.com)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/OlegCheban/Flashcards/blob/master/LICENSE)

# Flashcards Telegram Bot
A Telegram bot that helps you learn vocabulary words quickly and effectively. It uses spaced repetition learning technique and has a lot of flashcards.

Deployed bot (https://t.me/proFlashcardsBot) has around 100k flashcards.

# Основные функции и команды бота

## 1. Поиск новых карточек (около 100 тысяч карточек в базе)

- `/f` - показывает наиболее популярные карточки
- `/f comfy` - ищет карточку для слова "comfy"

### Кнопки в меню карточки:
- **"Добавить для изучения"** - добавляет карточку в ваш профиль для дальнейшего изучения
- **"Узнать больше"** - AI формирует отчет с доп. информацией

  ![image](https://github.com/user-attachments/assets/04ce5a28-1b40-4f94-bccb-c5547b0720b5)

- **"Знаю"** - исключает карточку из предлагаемых ботом
- **"Выйти"** - выходит из режима добавления новых карточек

## 2. Swiper - просмотр добавленных карточек

- `/s` - открывает Swiper (все ваши карточки)
- `/s ca` - показывает карточки, начинающиеся на "ca" (можно задавать любые маски)
- `/s ca 100` - показывает карточки на "ca" с 100% изучения (можно задать любой процент)

  ![image](https://github.com/user-attachments/assets/5a7f55da-5a82-4356-8bbb-55243ac620ce)

### Кнопки меню Swiper:
- **"Учить повторно"** - сбрасывает статистику для изученных на 100% карточек
- **"Повысить приоритет"** - карточка будет показываться в упражнениях первой
- **"Примеры использования"** - показывает примеры предложений с этим словом
- **"Удалить"** - удаляет карточку из вашего профиля

## 3. Упражнения для изучения слов

- `/l` - перейти в режим обучения
- `/d` - показать активные упражнения (можно отключить)
- `/e` - показать неактивные упражнения (можно включить)

Изученные карточки попадают в систему интервальных повторений (см. раздел 5).

## 4. Watering session - быстрый повтор изученного

- `/w` - начать Watering session  
  (нужно быстро выбрать правильный перевод до истечения таймера)

## 5. Тренировка предлогов

- `/pr` - начать упражнения по предлогам

## 6. Уведомления для закрепления материала

- **Интервальные повторения**: бот присылает карточку на 2, 3, 7, 14 и 90 день после изучения
  Кнопка **"Нет"** отправляет карточку на повторное изучение
- **Случайные карточки** из вашего списка для изучения

## 7. Настройки и дополнительные функции

- `/ni 120` - установить интервал уведомлений (в минутах, по умолчанию 60)
- `/fq 10` - установить количество карточек за тренировку (по умолчанию 5)
- `/wt 3` - установить таймер для Watering session (по умолчанию 5 сек)
- `/ed picturesque` - изменить перевод карточки "picturesque"
- `/rl 20` - показать 20 последних выученных карточек
- `/h` - памятка по командам
  

#### Help (English)
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
  
   
