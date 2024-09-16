package ru.flashcards.telegram.bot.db;

import java.sql.SQLException;

public class DBExceptionHandler {
    public static void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                if (!ignoreSQLException(
                        ((SQLException) e).
                                getSQLState())) {

                    e.printStackTrace(System.out);
                    System.out.println("SQLState: " +
                            ((SQLException)e).getSQLState());

                    System.out.println("Error Code: " +
                            ((SQLException)e).getErrorCode());

                    System.out.println("Message: " + e.getMessage());

                    Throwable t = ex.getCause();
                    while(t != null) {
                        System.out.println("Cause: " + t);
                        t = t.getCause();
                    }
                }
            }
        }
    }
    private static boolean ignoreSQLException(String sqlState) {
        if (sqlState == null) {
            System.out.println("The SQL state is not defined!");
            return false;
        }

        return false;
    }
}
