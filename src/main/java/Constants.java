
public class Constants {

    final static String[] TABLEHEADER = new String[] {"№","Код", "Описание ошибки", "Дата и время начала" ,"Дата и время конца", "Оператор"};
    final static String DATA_FORMAT = "yyyy-MM-dd hh:mm:ss.SSS Z";
    final static String HUMAN_DATE_FORMAT = "dd.MM.yyyy HH:mm";
    final static String PREFIX_PATH_TO_DB_FILE = "jdbc:sqlite:";
    final static String JSPINNER_VIEW_FORMAT = "dd/MM/yyyy HH:mm";
    final static String HMI_ADRESS="192.168.0.3";
    final static int HMI_PORT = 21;
    final static String HMI_USER = "uploadhis";
    final static String HMI_PASS = "111111";
    final static String PATH_TO_DB_IN_HMI = "eventlog/event.db";
    final static String TEMPORARY_DB_FILE_ON_PC = "~tempfile.tmp";
}
