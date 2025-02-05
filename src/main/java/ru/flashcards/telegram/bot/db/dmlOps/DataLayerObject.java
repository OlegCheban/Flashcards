package ru.flashcards.telegram.bot.db.dmlOps;

import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.db.*;
import ru.flashcards.telegram.bot.db.dmlOps.dto.*;
import ru.flashcards.telegram.bot.exception.SQLRuntimeException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

@Component
public class DataLayerObject {
    DataSource dataSource;

    public DataLayerObject(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void registerUser(Long chatId, String username) {
        final int  randomNotificationInterval = 60;
        try(Connection connection = dataSource.getConnection()) {
            try {
                connection.setAutoCommit(false);

                new Update(dataSource,
                        "insert into main.user (id, name, notification_interval, chat_id) " +
                                "select nextval('main.common_seq'), ?, ?, ? where not exists (select 1 from main.user where chat_id = ?) ") {
                    @Override
                    protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                        preparedStatement.setString(1, username);
                        preparedStatement.setLong(2, randomNotificationInterval);
                        preparedStatement.setLong(3, chatId);
                        preparedStatement.setLong(4, chatId);
                        return preparedStatement;
                    }
                }.run(connection);

                new Update(dataSource,
                        "insert into main.user_exercise_settings (id, user_id, exercise_kind_id)\n" +
                                "with\n" +
                                "usr as\n" +
                                "(\n" +
                                "\tselect u.id\n" +
                                "    from main.user u\n" +
                                "    where u.chat_id = ?\n" +
                                ")\n" +
                                "select nextval('main.common_seq'),\n" +
                                "       usr.id,\n" +
                                "       a.id\n" +
                                "from main.learning_exercise_kind a,\n" +
                                "     usr\n" +
                                "where not exists (\n" +
                                "                   select 1\n" +
                                "                   from main.user_exercise_settings s\n" +
                                "                   where s.exercise_kind_id = a.id and\n" +
                                "                         s.user_id = usr.id\n" +
                                "      )") {
                    @Override
                    protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                        preparedStatement.setLong(1, chatId);
                        return preparedStatement;
                    }
                }.run(connection);

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw new SQLRuntimeException(e);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Текущая порция слов для изучения
     */
    public List<String> getRecentLearned(Long chatId, Long quantity) {
        return new SelectWithParams<String>(dataSource,
                "select word ||' \\[ '||transcription||' ]' word from main.user_flashcard uf, main.user u where uf.user_id = u.id and u.chat_id = ? and uf.learned_date is not null order by uf.learned_date desc limit ?"
        ){
            @Override
            protected String rowMapper(ResultSet rs) throws SQLException {
                return rs.getString("word");
            }

            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, chatId);
                preparedStatement.setLong(2, quantity);
                return preparedStatement;
            }
        }.getCollection();
    }

    public Boolean existsExercise(Long chatId) {
        return new SelectWithParams<Boolean>(dataSource,"select exists(select 1 from main.user_flashcard a, main.user b " +
                "where a.user_id = b.id and a.learned_date is null and b.chat_id = ? limit 1) result"){
            @Override
            protected Boolean rowMapper(ResultSet rs) throws SQLException {
                return rs.getBoolean("result");
            }

            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, chatId);
                return preparedStatement;
            }
        }.getObject();
    }

    public Boolean existsLearnedFlashcards(Long chatId) {
        return new SelectWithParams<Boolean>(dataSource,"select exists(select 1 from main.user_flashcard a, main.user b " +
                "where a.user_id = b.id and b.chat_id = ? and a.learned_date is not null limit 1) result"){
            @Override
            protected Boolean rowMapper(ResultSet rs) throws SQLException {
                return rs.getBoolean("result");
            }

            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, chatId);
                return preparedStatement;
            }
        }.getObject();
    }

    public List<ExerciseKind> getExerciseKindToEnable(Long chatId) {
        return new SelectWithParams<ExerciseKind>(dataSource,
                "select code, name From main.learning_exercise_kind a where " +
                        "not exists (select 1 from main.user_exercise_settings b where a.id = b.exercise_kind_id and b.user_id = (select id from main.user where chat_id = ?)) order by a.order"
        ){
            @Override
            protected ExerciseKind rowMapper(ResultSet rs) throws SQLException {
                return new ExerciseKind(
                        rs.getString("code"),
                        rs.getString("name")
                );
            }

            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, chatId);
                return preparedStatement;
            }
        }.getCollection();
    }

    public List<ExerciseKind> getExerciseKindToDisable(Long chatId) {
        return new SelectWithParams<ExerciseKind>(dataSource,
                "select code, name From main.learning_exercise_kind a where " +
                        "exists (select 1 from main.user_exercise_settings b where a.id = b.exercise_kind_id and b.user_id = (select id from main.user where chat_id = ?)) order by a.order"
        ){
            @Override
            protected ExerciseKind rowMapper(ResultSet rs) throws SQLException {
                return new ExerciseKind(
                        rs.getString("code"),
                        rs.getString("name")
                );
            }

            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, chatId);
                return preparedStatement;
            }
        }.getCollection();
    }

    public int deleteExerciseStat(Long flashcardId) {
        return new Update(dataSource, "delete from main.done_learn_exercise_stat where user_flashcard_id = ?") {
            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, flashcardId);
                return preparedStatement;
            }
        }.run();
    }

    /**
     * Сбросить признак изучения карточки
     */
    public int returnToLearn (Long flashcardId) {
        return new Update(dataSource, "update main.user_flashcard set learned_date = null where id = ?") {
            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, flashcardId);
                return preparedStatement;
            }
        }.run();
    }

    public int removeFlashcard (Long flashcardId) {
        return new Update(dataSource, "delete from main.user_flashcard where id = ?") {
            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, flashcardId);
                return preparedStatement;
            }
        }.run();
    }

    public int disableExcercise(Long chatId, String excerciseCode) {
        return new Update(dataSource, "delete from main.user_exercise_settings where user_id = (select id from main.user where chat_id = ?) and " +
                "exercise_kind_id = (select id from main.learning_exercise_kind where code = ?)") {
            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, chatId);
                preparedStatement.setString(2, excerciseCode);
                return preparedStatement;
            }
        }.run();
    }

    public int enableExcercise(Long chatId, String excerciseCode) {
        return new Update(dataSource, "insert into main.user_exercise_settings (id, user_id, exercise_kind_id) values " +
                "(nextval('main.common_seq'), (select id from main.user where chat_id = ?), (select id from main.learning_exercise_kind where code = ?) )") {
            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, chatId);
                preparedStatement.setString(2, excerciseCode);
                return preparedStatement;
            }
        }.run();
    }

    /**
     * Add flashcard for nearest learning
     */
    public int boostUserFlashcardPriority(Long userFlashcardId) {
        return new Update(dataSource, "update main.user_flashcard set nearest_training = 1 where id = ?"){
            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, userFlashcardId);
                return preparedStatement;
            }
        }.run();
    }

    /**
     * Карточки для заучивания
     */
    public List<SendToLearnFlashcard> getFlashcardsByWordToSuggestLearning(Long chatId, String flashcardWord) {
        return new SelectWithParams<SendToLearnFlashcard>(dataSource,
                "select u.chat_id, fc.flashcard_id, fc.word, fc.description, fc.translation, fc.transcription from main.user u \n" +
                        " join lateral (\n" +
                        "     select f.id flashcard_id, f.word, f.description, f.translation, f.transcription from main.flashcard f\n" +
                        "        where f.word = coalesce(?, f.word) \n" +
                        "              limit 1 \n" +
                        "    ) fc on true\n" +
                        "where u.chat_id = ? "
        ){
            @Override
            protected SendToLearnFlashcard rowMapper(ResultSet rs) throws SQLException {
                return new SendToLearnFlashcard(
                        rs.getLong("chat_id"),
                        rs.getLong("flashcard_id"),
                        rs.getString("description"),
                        rs.getString("transcription"),
                        rs.getString("translation"),
                        rs.getString("word")
                );
            }

            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(1, flashcardWord);
                preparedStatement.setLong(2, chatId);
                return preparedStatement;
            }
        }.getCollection();
    }

    /**
     * Карточки для заучивания
     */
    public List<SendToLearnFlashcard> getFlashcardsByCategoryToSuggestLearning(Long chatId, Long flashcardCategoryId) {
        return new SelectWithParams<SendToLearnFlashcard>(dataSource,
                "select u.chat_id, fc.flashcard_id, fc.word, fc.description, fc.translation, fc.transcription from main.user u \n" +
                        " join lateral (\n" +
                        "     select f.id flashcard_id, f.word, f.description, f.translation, f.transcription from main.flashcard f\n" +
                        "        where not exists(select 1 from main.excepted_user_flashcard euf where euf.flashcard_id = f.id and euf.user_id = u.id) and\n" +
                        "              not exists(select 1 from main.user_flashcard uf where uf.word = f.word and uf.user_id = u.id) and \n" +
                        "              f.category_id = coalesce(?, f.category_id) \n" +
                        "              limit 1 \n" +
                        "    ) fc on true\n" +
                        "where u.chat_id = ? "
        ){
            @Override
            protected SendToLearnFlashcard rowMapper(ResultSet rs) throws SQLException {
                return new SendToLearnFlashcard(
                        rs.getLong("chat_id"),
                        rs.getLong("flashcard_id"),
                        rs.getString("description"),
                        rs.getString("transcription"),
                        rs.getString("translation"),
                        rs.getString("word")
                );
            }

            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, flashcardCategoryId);
                preparedStatement.setLong(2, chatId);
                return preparedStatement;
            }
        }.getCollection();
    }

    /**
     * Список примеров использования
     */
    public List<String> getExamplesByFlashcardId(Long flashcardId) {
        return new SelectWithParams<String>(dataSource,
                "select concat(row_number() over () , '. ', example) as example  From main.flashcard_examples where flashcard_id = ? order by id"
        ){
            @Override
            protected String rowMapper(ResultSet rs) throws SQLException {
                return rs.getString("example");
            }

            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, flashcardId);
                return preparedStatement;
            }
        }.getCollection();
    }

    public int editTranslation (Long flashcardId, String translation) {
        return new Update(dataSource, "update main.user_flashcard uf set translation = ? where uf.id = ? "){
            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(1, translation);
                preparedStatement.setLong(2, flashcardId);
                return preparedStatement;
            }
        }.run();
    }

    /**
     * Найти карточку пользователя по ид
     */
    public UserFlashcard findUserFlashcardById(Long flashcardId) {
        return new SelectWithParams<UserFlashcard>(dataSource,"select id, description, transcription, translation, word from main.user_flashcard where id = ?"){
            @Override
            protected UserFlashcard rowMapper(ResultSet rs) throws SQLException {
                return  new UserFlashcard(
                        rs.getLong("id"),
                        rs.getString("description"),
                        rs.getString("transcription"),
                        rs.getString("translation"),
                        rs.getString("word")
                );
            }

            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, flashcardId);
                return preparedStatement;
            }
        }.getObject();
    }

    public UserFlashcard findUserFlashcardByName(Long chatId, String name) {
        return new SelectWithParams<UserFlashcard>(dataSource,"select a.id, a.description, a.transcription, a.translation, a.word " +
                " from main.user_flashcard a, main.user b where a.user_id = b.id and b.chat_id = ? and a.word = ? "){
            @Override
            protected UserFlashcard rowMapper(ResultSet rs) throws SQLException {
                return  new UserFlashcard(
                        rs.getLong("id"),
                        rs.getString("description"),
                        rs.getString("transcription"),
                        rs.getString("translation"),
                        rs.getString("word")
                );
            }

            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, chatId);
                preparedStatement.setString(2, name);
                return preparedStatement;
            }
        }.getObject();
    }

    /**
     * Найти карточку по ид
     */
    public Flashcard findFlashcardById(Long flashcardId) {
        return new SelectWithParams<Flashcard>(dataSource,"select category_id, description, transcription, translation, word from main.flashcard where id = ?"){
            @Override
            protected Flashcard rowMapper(ResultSet rs) throws SQLException {
                return new Flashcard(
                        rs.getLong("category_id"),
                        rs.getString("description"),
                        rs.getString("transcription"),
                        rs.getString("translation"),
                        rs.getString("word")
                );
            }

            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, flashcardId);
                return preparedStatement;
            }
        }.getObject();
    }

    /**
     * Добавить карточку для изучения
     */
    public int addUserFlashcard(String word, String description, String transcription, String translation, Long categoryId, Long chatId) {
        return new Update(dataSource,
                "insert into main.user_flashcard (id, word, description, transcription, translation, category_id, user_id, push_timestamp)\n" +
                        "select nextval('main.flashcard_id_seq') ,?,?,?,?,?, (select id from main.user where chat_id = ?), now()"
        ){
            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(1, word);
                preparedStatement.setString(2, description);
                preparedStatement.setString(3, transcription);
                preparedStatement.setString(4, translation);
                preparedStatement.setLong(5, categoryId);
                preparedStatement.setLong(6, chatId);
                return preparedStatement;
            }
        }.run();
    }

    /**
     * Исключить карточку
     */
    public int exceptFlashcard (Long chatId, Long flashcardId) {
        return new Update(dataSource,
                "insert into main.excepted_user_flashcard (user_id, flashcard_id) values ((select id from main.user where chat_id = ?),?)"
        ) {
            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, chatId);
                preparedStatement.setLong(2, flashcardId);
                return preparedStatement;
            }
        }.run();
    }

    /**
     * Список примеров использования
     */
    public List<String> getExamplesByUserFlashcardId(Long userFlashcardId) {
        return new SelectWithParams<String>(dataSource,
                "select example From main.flashcard_examples where flashcard_id = (select id from main.flashcard where word = " +
                        "(select word from main.user_flashcard where id = ?))"
        ){
            @Override
            protected String rowMapper(ResultSet rs) throws SQLException {
                return rs.getString("example");
            }

            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, userFlashcardId);
                return preparedStatement;
            }
        }.getCollection();
    }

    /**
     * Установить интервал отправки уведомлений
     */
    public int setNotificationInterval (Integer minQty, Long chatId) {
        return new Update(dataSource, "update main.user set notification_interval = ? where chat_id = ?"){
            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setInt(1, minQty);
                preparedStatement.setLong(2, chatId);
                return preparedStatement;
            }
        }.run();
    }

    public int setTrainingFlashcardsQuantity (Integer qty, Long chatId) {
        return new Update(dataSource, "update main.user set cards_per_training = ? where chat_id = ?"){
            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setInt(1, qty);
                preparedStatement.setLong(2, chatId);
                return preparedStatement;
            }
        }.run();
    }

    /**
     * Удаление истории отправки интервальных уведомлений
     */
    public int deleteSpacedRepetitionHistory(Long flashcardId) {
        return new Update(dataSource, "delete from main.flashcard_push_history where flashcard_id = ?") {
            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, flashcardId);
                return preparedStatement;
            }
        }.run();
    }

    public Long getFirstSwiperFlashcard(Long chatId, String characterCondition, String percentile) {
        return new SelectWithParams<Long>(dataSource, "select min(id) id from main.swiper_flashcards " +
                "where chat_id = ? and " +
                "(length(?) = 0 or lower(word) like lower(?) || '%') and " +
                "(length(?) = 0 or prc::text = ?)"){
            @Override
            protected Long rowMapper(ResultSet rs) throws SQLException {
                return rs.getLong("id");
            }

            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, chatId);
                preparedStatement.setString(2, characterCondition);
                preparedStatement.setString(3, characterCondition);
                preparedStatement.setString(4, percentile);
                preparedStatement.setString(5, percentile);
                return preparedStatement;
            }
        }.getObject();
    }

    /**
     * Карточки для свайпера
     */
    public SwiperFlashcard getSwiperFlashcard(Long chatId, Long currentFlashcardId, String characterCondition, String percentile) {
        return new SelectWithParams<SwiperFlashcard>(dataSource,
                "select * from (" +
                        "                  select lag(id) over (order by id)  prev_id, " +
                        "                         id current_id, " +
                        "                         lead(id) over (order by id) next_id, " +
                        "                         word, " +
                        "                         description, " +
                        "                         translation, " +
                        "                         transcription, " +
                        "                         prc, " +
                        "                         nearest_training " +
                        "                      from main.swiper_flashcards " +
                        "                      where chat_id = ? and " +
                        "                       (length(?)=0 or lower(word) like lower(?) || '%') and " +
                        "                       (length(?)=0 or prc::text = ?) " +
                        "                      order by id " +
                        "              ) x where x.current_id = ? "
        ){
            @Override
            protected SwiperFlashcard rowMapper(ResultSet rs) throws SQLException {
                return new SwiperFlashcard(
                        rs.getLong("prev_id"),
                        rs.getLong("next_id"),
                        rs.getLong("current_id"),
                        rs.getString("word"),
                        rs.getString("description"),
                        rs.getString("translation"),
                        rs.getString("transcription"),
                        rs.getInt("prc"),
                        rs.getInt("nearest_training")
                );
            }

            @Override
            protected PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setLong(1, chatId);
                preparedStatement.setString(2, characterCondition);
                preparedStatement.setString(3, characterCondition);
                preparedStatement.setString(4, percentile);
                preparedStatement.setString(5, percentile);
                preparedStatement.setLong(6, currentFlashcardId);

                return preparedStatement;
            }
        }.getObject();
    }
}
