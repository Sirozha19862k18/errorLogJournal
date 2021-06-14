
import java.sql.*;

public class SQLQuery {

    private Connection connectToDB() {
        Connection conn=null;
        try {
            // db parameters
            String url = Constants.PATH_TO_DB_FILE;
            // create a connection to the database
            conn = DriverManager.getConnection(url);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }


    public void viewErrorBySelectDate(long startTimeInUnixFormat, long endTimeInUnixFormat)  {

        String sql="SELECT ROW_NUMBER() OVER () AS 'Счетчик', event.event_log_index as 'Код ошибки', event_log.language1 as 'Описание', datetime(event.'trigger_time@timestamp', 'unixepoch', 'localtime')  as 'Время появления ошибки' , datetime(event.'recover_time@timestamp', 'unixepoch', 'localtime')   as 'Время окончания ошибки', \n" +
                "event.WATCH1 as 'Оператор'\n" +
                "FROM event \n" +
                "INNER JOIN event_log ON event.event_log_index=event_log.event_log_index\n" +
                "WHERE event.'recover_time@timestamp' NOT NULL AND event.'trigger_time@timestamp' > "+ startTimeInUnixFormat +
                "\n AND event.'recover_time@timestamp'<"+ endTimeInUnixFormat+
                "\n ORDER BY event.'trigger_time@timestamp' ASC\n" +
                "\n";

       Connection conn = this.connectToDB();
        try ( Statement stmnt = conn.createStatement();
             ResultSet result = stmnt.executeQuery(sql))
        {
            while (result.next()){
              ErrorLog.fillErrorTable(result);
                           }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
          finally {
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

    }
}
