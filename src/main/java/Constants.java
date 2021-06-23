
public class Constants {

    final static String[] TABLEHEADER = new String[] {"№","Код", "Описание ошибки", "Дата и время начала" ,"Дата и время конца", "Оператор"};
    final static String DATA_FORMAT = "yyyy-MM-dd hh:mm:ss.SSS Z";
    final static String HUMAN_DATE_FORMAT = "dd.MM.yyyy HH:mm";
    final static String PREFIX_PATH_TO_DB_FILE = "jdbc:sqlite:";
    final static String JSPINNER_VIEW_FORMAT = "dd/MM/yyyy HH:mm";
    final static int HMI_PORT = 21;
    final static String HMI_USER = "uploadhis";
    final static String PATH_TO_DB_IN_HMI = "eventlog/event.db";
    final static String TEMPORARY_DB_FILE_ON_PC = "~tempfile.tmp";
    final static String SERIAL_NUMBER_MOTHERBOARD = "PF20ZN0R";   // Изменить для запуска на новом компьютере

}
