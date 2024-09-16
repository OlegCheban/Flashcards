create or replace view main.random_flashcard
as
select f.description,
       f.translation,
       f.word
from main.flashcard f tablesample bernoulli(1)
where not exists (
                    select 1
                    from main.user_flashcard uf
                    where uf.word = f.word and
                          uf.learned_date is null)
limit 3;