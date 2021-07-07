import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ErrorReport {

    public long reportDateBegin;
    public long reportDateEnd;
    public ArrayList<String[]> reportBody;
    public String errorReportHumanViewDate;
    public String errordatabaseFilePath;
    public String errorReportFileNameVariant;

    public boolean[] getErrorCategorySelectel() {
        return errorCategorySelectel;
    }

    public void setErrorCategorySelectel(boolean[] errorCategorySelectel) {
        this.errorCategorySelectel = errorCategorySelectel;
    }

    public boolean[] errorCategorySelectel;

    public String getErrorReportFileNameVariant() {
        return errorReportFileNameVariant;
    }

    public void setErrorReportFileNameVariant() {
        this.errorReportFileNameVariant = timeHumanViewConverter(getReportDateBegin(), Constants.HUMAN_DATE_FORMAT_FOR_FILE_NAME)+" - "+ timeHumanViewConverter(getReportDateEnd(), Constants.HUMAN_DATE_FORMAT_FOR_FILE_NAME);
    }

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
     errorReportHumanViewDate = timeHumanViewConverter(getReportDateBegin(), Constants.HUMAN_DATE_FORMAT)+" - "+ timeHumanViewConverter(getReportDateEnd(), Constants.HUMAN_DATE_FORMAT);
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
        setErrorReportFileNameVariant();
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
    private String timeHumanViewConverter(long time, String DateFormat){
        Date date = new java.util.Date(time*1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat(DateFormat);
        return sdf.format(date);
    }

}
