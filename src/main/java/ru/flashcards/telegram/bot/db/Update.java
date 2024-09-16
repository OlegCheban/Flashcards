package ru.flashcards.telegram.bot.db;

import ru.flashcards.telegram.bot.exception.SQLRuntimeException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class Update {
    private String query;
    private DataSource dataSource;

    public Update(DataSource ds, String sql) {
        dataSource = ds;
        query = sql;
    }

    public int run (){
        try(Connection connection = dataSource.getConnection()) {
            return exec(connection);
        } catch (SQLException e) {
            DBExceptionHandler.printSQLException(e);
            throw new SQLRuntimeException(e);
        }
    }

    public int run (Connection connection){
        try {
            return exec(connection);
        } catch (SQLException e) {
            DBExceptionHandler.printSQLException(e);
            throw new SQLRuntimeException(e);
        }
    }

    private int exec (Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement = parameterMapper(preparedStatement);
        return preparedStatement.executeUpdate();
    }

    protected abstract PreparedStatement parameterMapper(PreparedStatement preparedStatement) throws SQLException;
}
