drop view main.next_exercise_queue;
create or replace view main.next_exercise_queue
as
with
batch
as
(
select x.user_flashcard_id,
	   x.flashcard_id,
       x.word,
       x.description,
       x.transcription,
       x.translation,
       x.chat_id,
       x.user_id
from (
       select a.id user_flashcard_id,
              f.id flashcard_id,
              a.word,
              a.description,
              a.transcription,
              a.translation,
              u.chat_id,
              u.id user_id,
              u.cards_per_training,
              row_number() over(PARTITION by a.user_id
       order by a.nearest_training desc, a.id) rn
       from main.user_flashcard a
            join main.flashcard f on f.word = a.word
            join main.user u on a.user_id = u.id
       where a.learned_date is null
     ) x
where x.rn <= x.cards_per_training)
select distinct on (b.chat_id)
       b.chat_id,
       b.user_flashcard_id,
       b.word,
       b.description,
       b.transcription,
       b.translation,
       kk.code,
       ex.example
from batch b
     cross join lateral
     (
     	select k.id,
               k.code,
               k.order
        from main.learning_exercise_kind k,
        main.user_exercise_settings s
        where k.id = s.exercise_kind_id and
              s.user_id = b.user_id
	 ) kk
     left join lateral
     (
       select s.id answer_order,
              s.is_correct_answer
       from main.done_learn_exercise_stat s
       where s.user_flashcard_id = b.user_flashcard_id and
             s.exercise_kind_id = kk.id
       order by s.id desc
       limit 1
     ) st on true
     left join lateral
     (
       select
       	e.example
       from main.flashcard_examples e where
       	e.flashcard_id = b.flashcard_id
       order by length(e.example) desc
       limit 1
     ) ex on true

where not coalesce(st.is_correct_answer, false) and
      ((kk.code = 'COMPLETE_THE_GAPS' and ex.example is not null) or
        kk.code <> 'COMPLETE_THE_GAPS')
order by b.chat_id,
         st.answer_order nulls first,
         kk.order,
         b.user_flashcard_id;