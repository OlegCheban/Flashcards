package ru.flashcards.telegram.bot.db.dmlOps;

import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.db.*;
import ru.flashcards.telegram.bot.exception.SQLRuntimeException;

import javax.sql.DataSource;
import java.sql.*;

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

}
