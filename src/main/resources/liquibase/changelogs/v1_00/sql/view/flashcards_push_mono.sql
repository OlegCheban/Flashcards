create or replace view main.flashcards_push_mono
as
select distinct on (u.chat_id) uf.word,
                                       uf.description,
                                       uf.transcription,
                                       u.chat_id user_id,
                                       uf.id as user_flashcard_id,
                                       max(uf.push_timestamp) over (partition by u.chat_id) last_push_timestamp,
                                       u.notification_interval
from main.user u
         join main.user_flashcard uf on u.id = uf.user_id
where u.chat_id is not null
order by u.chat_id, uf.push_timestamp nulls first;