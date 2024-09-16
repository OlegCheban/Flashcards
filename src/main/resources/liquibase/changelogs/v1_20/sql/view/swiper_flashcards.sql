create or replace view main.swiper_flashcards
as
    select   u.chat_id,
             uf.id,
             uf.word,
             uf.transcription,
             uf.description,
             uf.translation,
             uf.learned_date,
             sq.push_qty,
             coalesce(sq.push_qty,0)*100/7 prc,
             uf.nearest_training
  from main.user_flashcard uf
      join main.user u on uf.user_id = u.id
      left join lateral (select max(b.push_date) max_push_date, count(*) push_qty
                                    from main.flashcard_push_history b
                                    where b.flashcard_id = uf.id) sq on true;