create or replace view main.learned_flashcards_stat
as
select uf.id user_flashcard_id
from main.user_flashcard uf
     join main.flashcard f on f.word = uf.word
     join main.user usr on usr.id = uf.user_id
     left join lateral
     (
       select count(*) done_qty
       from main.done_learn_exercise_stat s
       where s.user_flashcard_id = uf.id and
             s.is_correct_answer and
             exists (select 1
                     from main.user_exercise_settings ues
                     where ues.exercise_kind_id = s.exercise_kind_id and
                           ues.user_id = usr.id)
     ) d on true
     left join lateral(
       select 1 with_gaps
        from main.user_exercise_settings ues,
        	 main.learning_exercise_kind lek
        where ues.user_id = usr.id and
        	  lek.id = ues.exercise_kind_id and
              lek.code = 'COMPLETE_THE_GAPS'
     ) kind on true
where uf.learned_date is null and
      (case
      	when
        	not exists (select 1 from main.flashcard_examples e where e.flashcard_id = f.id) and
            kind.with_gaps = 1
            then 1 else 0
      end) + d.done_qty =
      (
        select count(*)
        from main.learning_exercise_kind lek,
             main.user_exercise_settings ues
        where lek.id = ues.exercise_kind_id and
              ues.user_id = usr.id
      );