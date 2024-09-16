drop materialized view if exists main.interval_repetition_queue;

create materialized view main.interval_repetition_queue
as
with
t as (
select u.chat_id user_id, uf.id user_flashcard_id, uf.word, uf.description, uf.transcription,
       (case
            when sq.max_push_date is null then uf.learned_date + interval '1 day'
            when sq.push_qty = 1 then uf.learned_date + interval '2 days'
            when sq.push_qty = 2 then uf.learned_date + interval '3 days'
            when sq.push_qty = 3 then uf.learned_date + interval '7 days'
            when sq.push_qty = 4 then uf.learned_date + interval '14 days'
            when sq.push_qty = 5 then uf.learned_date + interval '30 days'
            when sq.push_qty = 6 then uf.learned_date + interval '90 days'
        end)::date + justify_hours(random() * (interval '24 hours')) notification_date,
        (sq.push_qty + 1) * 100 / 7 as prc
    from main.user u
             join main.user_flashcard uf on u.id = uf.user_id
             left join lateral (select max(b.push_date) max_push_date, count(*) push_qty
                                    from main.flashcard_push_history b
                                    where b.flashcard_id = uf.id) sq on true
    where sq.push_qty <= 6 and u.chat_id is not null)
select user_id,
       user_flashcard_id,
       word,
       description,
       transcription,
       case when extract(hour from notification_date) between 0 and 8
             then notification_date + interval '10 hours'
             else notification_date
       end notification_date,
        current_date last_refresh,
       prc
from t where notification_date::date >= current_date;