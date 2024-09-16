package ru.flashcards.telegram.bot.utils;

public class Help {
    public static String sendBotManualRusConcise(){
        return "*Основные функции и команды:*\n\n" +
                "1. *Поиск новых карточек:*\n" +
                "   - `/f` - покажет популярные карточки.\n" +
                "   - `/f comfy` - найдет карточку для слова \"comfy\".\n" +
                "   - Кнопки в меню карточки:\n" +
                "     - \"Добавить для изучения\" - сохранит в профиль.\n" +
                "     - \"Знаю\" - исключит из предложенных.\n" +
                "     - \"Выйти\" - завершит режим добавления.\n\n" +

                "2. *Swiper (просмотр карточек):*\n" +
                "   - `/s` - откроет Swiper с вашими карточками.\n" +
                "   - `/s ca` - покажет карточки, начинающиеся на \"ca\".\n" +
                "   - `/s ca 100` - покажет карточки \"ca\", выученные на 100%.\n" +
                "   - Кнопки в меню Swiper-а:\n" +
                "     - \"Учить повторно\" - сброс статистики.\n" +
                "     - \"Повысить приоритет\" - делает карточку приоритетной.\n" +
                "     - \"Примеры использования\" - показывает примеры предложений.\n" +
                "     - \"Удалить\" - убирает из профиля.\n\n" +

                "3. *Упражнения:*\n" +
                "   - `/l` - начать упражнения.\n" +
                "   - `/d` - показать активные упражнения и отключить.\n" +
                "   - `/e` - показать отключенные и включить.\n" +
                "   - Изученные в упражнениях карточки попадают в систему интервальных повторений (смотрите раздел 5.Уведомления).\n\n" +

                "4. *Режим Watering session:*\n" +
                "   - `/w` - повторение изученных карточек.\n\n" +

                "5. *Уведомления:*\n" +
                "   - Интервальные повторения:** после упражнений карточка отправляется на повторение через разные интервалы.\n" +
                "   - Случайные карточки:** добавленные вами для изучения.\n\n" +

                "6. *Тренировка предлогов:*\n" +
                "   `/pr` - начать упражнения по предлогам.\n\n" +

                "7. *Настройки и доп. функции:*\n" +
                "   - `/ni 120` - установить периодичность уведомлений для случайных карточек (по умолчанию 60 минут).\n" +
                "   - `/fq 10` - установить количество карточек в упражнениях (по умолчанию 5 карточек).\n" +
                "   - `/wt 3` - установить таймер ответа для режима Watering session (по умолчанию 5 секунд).\n" +
                "   - `/ed picturesque` - отредактировать перевод для карточки \"picturesque\".\n" +
                "   - `/rl 20` - показать последние 20 выученных карточек (в одном сообщении).\n" +
                "   - `/h` - отобразить руководство пользователя\n";

    }
    public static String sendBotManualRus(){
        return "*Основные функции и команды для поиска *\n\n" +
                "1.Поиска новых карточек (бот содержит около 100 тысяч карточек):\n" +
                "Команта '/f' - предлагает наиболее популярные карточки\n" +
                "Команта '/f comfy' - попросить найти карточку для слова comfy\n" +
                "Описаник кнопок в меню карточки:\n" +
                "   - Кнопка \"Добавить для изучения\" добавляет карточку в Ваш профиль для дальшейшего изучения;\n" +
                "   - Кнопка \"Знаю\" исключает карточку из предлагаемых ботом карточек;\n" +
                "   - Кнопка \"Выйти\" выходит из режима добавления новых карточек;\n" +

                "2.Swiper\n" +
                "Через Swiper можно просматривать все Ваши добавленные карточи, а так же процент успешного изучения карточи\n" +
                "Команта '/s' - открыть Swiper (все Ваши карточки)\n" +
                "Команта '/s ca' - в данном случае Swiper отобразит все Ваши карточки начинающиеся на ca (можно задавать любые маски).\n" +
                "Команта '/s ca 100' - в данном случае Swiper отобразит карточки начинающиеся на ca и изученные на 100% (можно задать любой процент).\n" +
                "Описаник кнопок меню Swiper-а:\n" +
                "   - Кнопка \"Учить повторно\" отображается в изученных на 100% карточках и позволяет сбросить статистику.\n" +
                "   - Кнопка \"Повысить приоритет\" повышает приоритет карточки. В упражнения первым делом попадают приоритетные карточки" +
                "   - Кнопка \"Примеры использования\" отображает примеры предложений с употреблением слова из карточки\n" +
                "   - Кнопка \"Удалить\" удаляет карточку из профиля пользователя \n\n" +

                "3.Упражнения\n" +
                "Для изучения слов бот предлагает несколько упражнений, среди которых выбрать правильный перевод, правильное описание слова и др.\n" +
                "Команта '/l' перейти в режим обучения\n" +
                "Команта '/d' отображает список всех активированных упражнения и предлагает отключить \n" +
                "Команта '/e' отображает список всех деактивированных упраждений и предлагает включить\n" +
                "Изученные в упражнениях карточки попадают в систему интервальных повторений (смотрите раздел 5.Уведомления)\n\n" +

                "4.Режим Watering session позволяет повторить изученные карточки. Вам необходимо как можно скорее выбрать правильный передов, до истечении времени таймера\n" +
                "Команта '/w' - перейти в режим Watering session\n\n" +

                "5.Уведомления отправляемые ботом с целью закрепления материала\n" +
                "   - система интервальных повторений. После прохождения упражнений по карточке, бот начнет отправлять Вам данную карточку на второй, третий, седьмой, 14-тый и 90-тый день, после изучения.\n" +
                "     А так же бот будет спрашивать помните ли Вы данное слово. По нажатию на кнопку \"Нет\" карточка отправляется на повторное изучени и прохождение упражнений.\n" +
                "   - случайные карточки добавленные Вами на изучение.\n\n" +

                "6.Настройки и доп. функции\n" +
                "Команта '/ni 120' - установить периодичность отправки (в данном случае 120 минут) уведомлений для случайных карточек (по умолчанию 60 минут) \n" +
                "Команта '/fq 10' - установить количество карточек за одну тренировку (в данном случае 10 карточек) которые будут отобраны для изучения в упражнениях (по умолчанию 5 карточек) \n" +
                "Команта '/wt 3' - установить таймер ответа для режима watering session (по умолчанию 5 секунд) \n" +
                "Команта '/ed picturesque' - отредактировать перевод для карточки picturesque, если не устраивает перевод предоставленный ботом\n" +
                "Команта '/rl 20' - выводит список 20-ти последних выученных карточек (в одном сообщении)\n" +
                "Команта '/h' - отображает данное руководство пользователя \n";

    }
    public static String sendBotManual(){
        return "*Help*\n\n" +
                        "1. Basic commands for finding flashcards\n" +
                        "'/f' - suggests the most popular flashcards\n" +
                        "'/f <value>' - finds a specific flashcard by input value (English word)\n" +
                        "Additional flashcard options:\n" +
                        "   - The ability to add flashcard to your profile for learning. Use the \"add to learn\" button;\n" +
                        "   - The ability to exclude already known flashcards from learning. Use the \"exclude\" button.\n\n" +

                        "2. Swiper\n" +
                        "Swiper allows you to easily navigate through all of your flashcards\n" +
                        "'/s' - displays flashcards added by the user to their profile\n" +
                        "'/s <value> <prc>' - displays flashcards containing the input value (English word or substring) and/or by percentile of learning.\n" +
                        "Additional options:\n" +
                        "   - The ability to reset learning statistics for flashcards which were already learned (100% progress). Use the \"reset progress\" button;\n" +
                        "   - The ability to add the most significant flashcards to the next learning session. Use the \"boost priority\" button;\n" +
                        "   - The ability to see examples of usage. Use the \"example of usage\" button;\n" +
                        "   - The ability to remove flashcards from profile. Use the \"remove\" button.\n\n" +

                        "3. Exercises\n" +
                        "Bot has several kinds of exercises which help you learn flashcards\n" +
                        "'/l' to start learning\n" +
                        "'/e' to enable exercises \n" +
                        "'/d' to disable exercises \n\n" +

                        "4. Watering session (repeat your learned flashcards and pick correct translation before time runs out)\n" +
                        "'/w' to start watering session\n\n" +

                        "5. Others commands\n" +
                        "'/ni' <min> - changes random flashcards notifications interval (default 60 min) \n" +
                        "'/fq' <min> - changes flashcards quantity for training (default 5 flashcards) \n" +
                        "'/wt' <seconds> - changes watering session reply time (default 5 seconds) \n" +
                        "'/ed' <value> - finds a flashcard from your profile by input value (english word) and suggests changing the translation \n" +
                        "'/h' displays a list of available commands and options, along with brief descriptions of what each command does\n\n"+

                        "6. Bot sends notifications:\n" +
                        "   - spaced repetition notifications. Bot sends only learned flashcards on the 2nd, 3rd, 7th, 14th, 30th and 90th day. At each stage, you can reset flashcard to learn again;\n" +
                        "   - random flashcards notifications.\n";
    }
}
