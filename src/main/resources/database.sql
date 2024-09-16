CREATE SCHEMA main;

create sequence main.common_seq start with 1;
create sequence main.flashcard_id_seq start with 1;

create table main.category
(
    id bigint,
    name varchar(255),
    constraint category_pk primary key(id)
);

create table main.flashcard
(
    id            bigserial,
    word          varchar(255),
    description   text,
    translation   varchar(255),
    transcription varchar(255),
    category_id   bigint,
    constraint flashcard_pk primary key(id),
    constraint category_fk foreign key(category_id) references main.category(id)
);

create table main.flashcard_examples
(
    id           bigint,
    flashcard_id bigint,
    example      text,
    constraint flashcard_examples_pk primary key(id),
    constraint flashcard_examples_to_flashcard_fk foreign key(flashcard_id) references main.flashcard(id)
);

create table main."user"
(
    id                          bigint,
    email                       varchar(255),
    name                        varchar(255),
    notification_interval       integer default 60,
    chat_id                     bigint,
    learn_flashcard_state       boolean default false,
    created                     timestamp(0) default now(),
    cards_per_training          integer default 5,
    watering_session            boolean default false,
    watering_session_reply_time integer default 5,
    constraint user_pk primary key(id),
    constraint email_uk unique(email)
);

create table main.excepted_user_flashcard
(
    user_id      bigint,
    flashcard_id bigint,
    constraint excepted_user_flashcard_pk primary key(user_id, flashcard_id),
    constraint excepted_user_flashcard_to_user_fk foreign key(user_id) references main.user(id),
    constraint excepted_user_flashcard_to_flashcard_fk foreign key(flashcard_id) references main.flashcard(id)
);

create table main.user_category
(
    user_id     bigint not null,
    category_id bigint not null,
    constraint user_category_pk primary key(user_id, category_id),
    constraint user_category_to_user_fk foreign key(user_id) references main.user(id)
        constraint user_category_to_category_fk foreign key(category_id) references main.category(id)
);

create table main.learning_exercise_kind
(
    id      bigint,
    code    varchar(50),
    name    varchar(250),
    "order" integer,
    constraint learning_exercise_kind_pk primary key(id)
);

create table main.user_exercise_settings
(
    id               bigint,
    user_id          bigint,
    exercise_kind_id bigint,
    constraint user_exercise_settings_pk primary key(id),
    constraint user_exercise_settings_to_user_fk foreign key(user_id) references main.user(id)
        constraint user_exercise_settings_to_kind_fk foreign key(exercise_kind_id) references main.learning_exercise_kind(id)
);

create table main.user_flashcard
(
    id                    bigint,
    description           varchar(255),
    transcription         varchar(255),
    translation           varchar(255),
    word                  varchar(255) not null,
    category_id           bigint,
    user_id               bigint,
    push_timestamp        timestamp,
    learned_date          timestamp,
    nearest_training      integer default 0,
    watering_session_date timestamp,
    constraint user_flashcard_pk primary key(id),
    constraint user_flashcard_to_category_fk foreign key(category_id) references main.category(id),
    constraint user_flashcard_to_user_fk foreign key(user_id) references main.user(id)
);

create table main.done_learn_exercise_stat
(
    id                bigint,
    user_flashcard_id bigint,
    exercise_kind_id  bigint,
    is_correct_answer boolean,
    constraint done_learn_exercise_stat_pk primary key(id),
    constraint done_learn_exercise_stat_to_user_flashcard_fk foreign key(user_flashcard_id) references main.user_flashcard(id)
        constraint done_learn_exercise_stat_to_kind_fk foreign key(exercise_kind_id) references main.learning_exercise_kind(id)
);

create table main.flashcard_push_history
(
    id           bigint,
    flashcard_id bigint,
    push_date    timestamp default now(),
    constraint flashcard_push_history_pk primary key(id),
    constraint flashcard_push_history_to_user_flashcard_fk foreign key(flashcard_id) references main.user_flashcard(id)
);

create index user_flashcard_category_idx on user_flashcard(category_id);
create index user_flashcard_user_idx on user_flashcard (user_id);
create index user_flashcard_word_i on user_flashcard (word);
create index user_exercise_settings_idx on user_exercise_settings(user_id);
create index user_exercise_settings_idx1 on user_exercise_settings(exercise_kind_id);
create index flashcard_examples_idx on flashcard_examples(flashcard_id);
create index excepted_user_flashcard_flashcar_i on excepted_user_flashcard(flashcard_id);
create index excepted_user_flashcard_user_i on excepted_user_flashcard(user_id);
create index done_learn_exercise_stat_idx on done_learn_exercise_stat(user_flashcard_id);
create index done_learn_exercise_stat_idx1 on done_learn_exercise_stat(exercise_kind_id);

create view flashcards_push_mono as
select distinct on (u.chat_id) uf.word,
        uf.description,
        uf.transcription,
        u.chat_id  as user_id,
        uf.id  as user_flashcard_id,
        coalesce(max(uf.push_timestamp) over (partition by u.chat_id), (now() - '01:00:00'::interval)) as last_push_timestamp,
        u.notification_interval
        from main."user" u
        join main.user_flashcard uf on u.id = uf.user_id
        where u.chat_id is not null
        order by u.chat_id, uf.push_timestamp nulls first;


create view learned_flashcards_stat as
SELECT uf.id AS user_flashcard_id
FROM main.user_flashcard uf
         JOIN main.flashcard f ON f.word = uf.word
         JOIN main."user" usr ON usr.id = uf.user_id
         LEFT JOIN LATERAL ( SELECT count(*) AS done_qty
                             FROM main.done_learn_exercise_stat s
                             WHERE s.user_flashcard_id = uf.id
                               AND s.is_correct_answer
                               AND (EXISTS (SELECT 1
                                            FROM main.user_exercise_settings ues
                                            WHERE ues.exercise_kind_id = s.exercise_kind_id
                                              AND ues.user_id = usr.id))) d ON true
         LEFT JOIN LATERAL ( SELECT 1 AS with_gaps
                             FROM main.user_exercise_settings ues,
                                  main.learning_exercise_kind lek
                             WHERE ues.user_id = usr.id
                               AND lek.id = ues.exercise_kind_id
                               AND lek.code = 'COMPLETE_THE_GAPS') kind ON true
WHERE uf.learned_date IS NULL
  AND (
          CASE
              WHEN NOT (EXISTS (SELECT 1
                                FROM main.flashcard_examples e
                                WHERE e.flashcard_id = f.id)) AND kind.with_gaps = 1 THEN 1
              ELSE 0
              END + d.done_qty) = ((SELECT count(*) AS count
    FROM main.learning_exercise_kind lek,
    main.user_exercise_settings ues
    WHERE lek.id = ues.exercise_kind_id
    AND ues.user_id = usr.id
));

create view next_exercise_queue as
WITH batch AS (SELECT x.user_flashcard_id,
                      x.flashcard_id,
                      x.word,
                      x.description,
                      x.transcription,
                      x.translation,
                      x.chat_id,
                      x.user_id
               FROM (SELECT a.id AS user_flashcard_id,
                            f.id AS flashcard_id,
                            a.word,
                            a.description,
                            a.transcription,
                            a.translation,
                            u.chat_id,
                            u.id AS user_id,
                            u.cards_per_training,
                            row_number() OVER (PARTITION BY a.user_id ORDER BY a.nearest_training DESC, a.id) AS rn
                     FROM main.user_flashcard a
                              JOIN main.flashcard f ON f.word = a.word
                              JOIN main."user" u ON a.user_id = u.id
                     WHERE a.learned_date IS NULL) x
               WHERE x.rn <= x.cards_per_training)
SELECT DISTINCT
        ON (b.chat_id) b.chat_id,
        b.user_flashcard_id,
        b.word,
        b.description,
        b.transcription,
        b.translation,
        kk.code,
        ex.example
        FROM batch b
        CROSS JOIN LATERAL (SELECT k.id,
        k.code,
        k."order"
        FROM main.learning_exercise_kind k,
        main.user_exercise_settings s
        WHERE k.id = s.exercise_kind_id
        AND s.user_id = b.user_id) kk
        LEFT JOIN LATERAL (SELECT s.id AS answer_order,
        s.is_correct_answer
        FROM main.done_learn_exercise_stat s
        WHERE s.user_flashcard_id = b.user_flashcard_id
        AND s.exercise_kind_id = kk.id
        ORDER BY s.id DESC
        LIMIT 1) st ON true
        LEFT JOIN LATERAL (SELECT e.example
        FROM main.flashcard_examples e
        WHERE e.flashcard_id = b.flashcard_id
        ORDER BY (length (e.example)) DESC
        LIMIT 1) ex ON true
        WHERE NOT COALESCE (st.is_correct_answer, false)
        AND (kk.code = 'COMPLETE_THE_GAPS' AND ex.example IS NOT NULL OR
        kk.code <> 'COMPLETE_THE_GAPS')
        ORDER BY b.chat_id, st.answer_order NULLS FIRST, kk."order", b.user_flashcard_id;

create view random_flashcard as
SELECT f.description,
       f.translation,
       f.word
FROM main.flashcard f TABLESAMPLE bernoulli (0.1)
WHERE NOT (EXISTS (SELECT 1
                   FROM main.user_flashcard uf
                   WHERE uf.word = f.word
                     AND uf.learned_date IS NULL))
    LIMIT 3;

create view swiper_flashcards  as
SELECT u.chat_id,
       uf.id,
       uf.word,
       uf.transcription,
       uf.description,
       uf.translation,
       uf.learned_date,
       sq.push_qty,
       COALESCE(sq.push_qty, 0) * 100 / 7 AS prc,
       uf.nearest_training
FROM main.user_flashcard uf
         JOIN main."user" u ON uf.user_id = u.id
         LEFT JOIN LATERAL ( SELECT max(b.push_date) AS max_push_date,
                                    count(*)         AS push_qty
                             FROM main.flashcard_push_history b
                             WHERE b.flashcard_id = uf.id) sq ON true;

create view unlearned_flashcard(chat_id, description, translation, word, rn) as
SELECT u.chat_id,
       uf.description,
       uf.translation,
       uf.word,
       row_number() OVER (PARTITION BY uf.user_id ORDER BY uf.nearest_training DESC, uf.id) AS rn
FROM main.user_flashcard uf,
     main."user" u
WHERE u.id = uf.user_id
  AND uf.learned_date IS NULL;

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
       case
           when extract(hour from notification_date) between 0 and 8
               then notification_date + interval '10 hours'
        else notification_date
end notification_date,
       current_date last_refresh,
       prc
from t where notification_date::date >= current_date;

