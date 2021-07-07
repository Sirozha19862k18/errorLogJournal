
import java.sql.*;
import java.util.ArrayList;

public class SQLQuery {
    ErrorReport errorReport;
    boolean[] errorCategorySelected;

    private Connection connectToDB() {
        Connection conn = null;
        try {
            // db parameters
            String url = Constants.PREFIX_PATH_TO_DB_FILE+errorReport.getErrordatabaseFilePath();
            // create a connection to the database
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public ArrayList<String[]> viewErrorBySelectDate(ErrorReport errorReport) {
        this.errorReport = errorReport;
        ArrayList<String[]> errorResult = new ArrayList<>();
        String errorCategoryStatment = prepareStatmentBySelectedErrorCategory();

        String sql = "SELECT ROW_NUMBER() OVER () AS 'Счетчик',event_log.category as 'Категория', event.event_log_index as 'Код ошибки', event_log.language1 as 'Описание', datetime(event.'trigger_time@timestamp', 'unixepoch', 'localtime')  as 'Время появления ошибки' , datetime(event.'recover_time@timestamp', 'unixepoch', 'localtime')   as 'Время окончания ошибки', \n" +
                "event.WATCH1 as 'Оператор'\n" +
                "FROM event \n" +
                "INNER JOIN event_log ON event.event_log_index=event_log.event_log_index\n" +
                "WHERE event.'recover_time@timestamp' NOT NULL AND event.'trigger_time@timestamp' > " +errorReport.getReportDateBegin() +
                "\n AND event.'recover_time@timestamp'<" + errorReport.getReportDateEnd() + errorCategoryStatment+
                "\n ORDER BY event.'trigger_time@timestamp' ASC\n" +
                "\n";

        Connection conn = this.connectToDB();
        try (Statement stmnt = conn.createStatement();
             ResultSet result = stmnt.executeQuery(sql)) {
            while (result.next()) {
                String[] row = new String[]{result.getString(1), result.getString(2), result.getString(3), result.getString(4),
                        result.getString(5), result.getString(6), result.getString(7)};
                errorResult.add(row);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return errorResult;
    }

    private String prepareStatmentBySelectedErrorCategory() {
        StringBuilder statment= new StringBuilder(" AND (");
        errorCategorySelected = errorReport.getErrorCategorySelectel();
        for (int i = 0; i < errorCategorySelected.length; i++) {
            if (errorCategorySelected[i]) {
                statment.append("event_log.category=").append(i).append(" OR ");
            }
        }
        statment.append(" event_log.category=1000)");
        return statment.toString();
    }

}
