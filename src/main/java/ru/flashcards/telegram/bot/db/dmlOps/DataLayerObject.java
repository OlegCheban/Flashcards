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
