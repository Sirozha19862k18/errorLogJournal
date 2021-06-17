import java.util.ArrayList;

public class ErrorReport {

    public long reportDateBegin;
    public long reportDateEnd;
    public ArrayList<String[]> reportBody;

    public long getReportDateBegin() {
        return reportDateBegin;
    }

    public void setReportDateBegin(long unixtime) {

        this.reportDateBegin = unixtime;
    }

    public long getReportDateEnd() {
        return reportDateEnd;
    }

    public void setReportDateEnd(long unixtime) {
        this.reportDateEnd = unixtime;
    }

    public ArrayList<String[]> getReportBody() {
        return reportBody;
    }

    public void setReportBody(ArrayList<String[]> reportBody) {
        this.reportBody = reportBody;
    }

    //Проверка Дата начала < Даты конца
    public boolean checkSelectedDateByErrors() {
        return reportDateBegin < reportDateEnd;
    }

    //Получение ошибок с БД
    public void getErrorsFromDB() {
        SQLQuery sqlQery = new SQLQuery();
        setReportBody(sqlQery.viewErrorBySelectDate(reportDateBegin, reportDateEnd));
    }


}
