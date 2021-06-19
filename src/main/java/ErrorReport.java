import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ErrorReport {

    public long reportDateBegin;
    public long reportDateEnd;
    public ArrayList<String[]> reportBody;
    public String errorReportHumanViewDate;
    public String errordatabaseFilePath;

    public String getErrordatabaseFilePath() {
        return errordatabaseFilePath;
    }

    public void setErrordatabaseFilePath(String errordatabaseFilePath) {
        this.errordatabaseFilePath = errordatabaseFilePath;
    }

    public String getErrorReportHumanViewDate() {
        return errorReportHumanViewDate;
    }

    public void setErrorReportHumanViewDate() {
     errorReportHumanViewDate = timeHumanViewConverter(getReportDateBegin())+" - "+ timeHumanViewConverter(getReportDateEnd());
    }

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
        setErrorReportHumanViewDate();
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


    //Конвертер с UnixTime в строку
    private String timeHumanViewConverter(long time){
        Date date = new java.util.Date(time*1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat(Constants.HUMAN_DATE_FORMAT);
        return sdf.format(date);
    }

}
