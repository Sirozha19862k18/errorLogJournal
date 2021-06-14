import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static java.time.ZoneOffset.UTC;

public class ErrorLog extends JFrame {
    private JPanel mainPanel;
    private  JSpinner spinnerDateBegin;
    private  JSpinner spinnerDateEnd;
    private  JButton readData;
    private   JTable errorTable;
    private JButton saveToFile;
    private JLabel labelTextBegin;
    private  JLabel labelTextEnd;
    private JButton synchronizeButton;
    private static DefaultTableModel tableModel;



    public ErrorLog(String title) throws HeadlessException {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        mainPanel.setSize(800, 600);
        this.setBounds(400, 400, 800, 600);
        initUI();


        readData.addActionListener(e -> {
            prepareErrorTableForNewAction();
            checkSelectedDateByErrors(returnTimestamp(spinnerDateBegin), returnTimestamp(spinnerDateEnd));
            errorTable.setModel(tableModel);
        });

        saveToFile.addActionListener(e -> {
            JFrame parentFrame = new JFrame();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Сохранить файл отчета");
            int userSelection = fileChooser.showSaveDialog(parentFrame);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                System.out.println("Сохранить как: " + fileToSave.getAbsolutePath());
            }
        });
    }

    public void setDateInSpinner(){
        setSpinnerModel(spinnerDateBegin);
        setSpinnerModel(spinnerDateEnd);
    }

    public void initUI(){
        initTableModel();
        addComonentToPane();
        setDateInSpinner();
    }

    public void initTableModel(){
        tableModel = new DefaultTableModel(0, 6);
        tableModel.addRow(Constants.TABLEHEADER);
        errorTable.setModel(tableModel);
    }

    public long returnTimestamp(JSpinner spinner) {
        Date date;
        long unixTime=0;
        try {
            date =  new SimpleDateFormat(Constants.DATA_FORMAT).parse(new SimpleDateFormat(Constants.DATA_FORMAT).format(spinner.getValue())+UTC);
            unixTime = date.getTime() / 1000;
        } catch (ParseException parseException) {
            parseException.printStackTrace();
        }
        return unixTime;
    }

    public void setSpinnerModel(JSpinner spinner){
        SpinnerDateModel model = new SpinnerDateModel(new Date() , null, null, Calendar.SHORT_FORMAT);
        spinner.setModel(model);
        Calendar calendar = new GregorianCalendar();
        spinner.setEditor(new JSpinner.DateEditor(spinner,Constants.JSPINNER_VIEW_FORMAT));
        spinner.setValue(calendar.getTime());
    }

    public static void fillErrorTable(ResultSet result) throws SQLException {

        tableModel.addRow(new String[]{result.getString(1), result.getString(2), result.getString(3),
                result.getString(4), result.getString(5), result.getString(6)});
    }

    public void prepareErrorTableForNewAction(){
        tableModel.setRowCount(0);
        initTableModel();
    }

    // Проверка что дата начала отчета меньше чем дата конца отчета
    public void checkSelectedDateByErrors(long startTimeInUnixFormat, long endTimeInUnixFormat){
        if(startTimeInUnixFormat<endTimeInUnixFormat){
            SQLQuery queryToSelectByDate = new SQLQuery();
            queryToSelectByDate.viewErrorBySelectDate(startTimeInUnixFormat, endTimeInUnixFormat);
        }
        else {
            JOptionPane.showMessageDialog(mainPanel,
                    "Дата начала отсчета не может быть больше даты конца отчета",
                    "Внимание!",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public  void  addComonentToPane(){
        GridBagLayout myLayout = new GridBagLayout();
        mainPanel.setLayout(myLayout);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx=1;
        gbc.gridy=1;
        gbc.gridwidth=2;
        gbc.gridheight=2;
        gbc.anchor=GridBagConstraints.NORTH;
        JPanel selectDatePanel = new JPanel();
        selectDatePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Выбор даты"));
        GridBagLayout selectDatePanelLayout = new GridBagLayout();
        mainPanel.add(selectDatePanel, gbc);
        selectDatePanel.setLayout(selectDatePanelLayout);
        GridBagConstraints selectDatePanelGBC = new GridBagConstraints();
        selectDatePanelGBC.insets = new Insets(10,10,20,10);
        selectDatePanelGBC.gridx=0;
        selectDatePanelGBC.gridy=0;
        selectDatePanelGBC.gridwidth=1;
        selectDatePanelGBC.gridheight=1;
        selectDatePanel.add(labelTextBegin, selectDatePanelGBC);
        selectDatePanelGBC.gridx=1;
        selectDatePanelGBC.gridy=0;
        selectDatePanelGBC.gridwidth=1;
        selectDatePanelGBC.gridheight=1;
        selectDatePanel.add(spinnerDateBegin, selectDatePanelGBC);
        selectDatePanelGBC.gridx=0;
        selectDatePanelGBC.gridy=1;
        selectDatePanelGBC.gridwidth=1;
        selectDatePanelGBC.gridheight=1;
        selectDatePanel.add(labelTextEnd, selectDatePanelGBC);
        selectDatePanelGBC.gridx=1;
        selectDatePanelGBC.gridy=1;
        selectDatePanelGBC.gridwidth=1;
        selectDatePanelGBC.gridheight=1;
        selectDatePanel.add(spinnerDateEnd,selectDatePanelGBC);
        //Button Синхронизация
        gbc.gridx=0;
        gbc.gridy=3;
        gbc.gridwidth=1;
        gbc.gridheight=1;
        gbc.insets = new Insets(20,10,20,10);
        gbc.anchor=GridBagConstraints.WEST;
        mainPanel.add(synchronizeButton,gbc);
        //Button Считать данные
        gbc.gridx=1;
        gbc.gridy=3;
        gbc.gridwidth=1;
        gbc.gridheight=1;
        gbc.insets = new Insets(20,10,20,10);
        gbc.anchor=GridBagConstraints.WEST;
        mainPanel.add(readData,gbc);
        //Button Сохранить
        gbc.gridx=2;
        gbc.gridy=3;
        gbc.gridwidth=1;
        gbc.gridheight=1;
        gbc.insets = new Insets(20,10,20,10);
        gbc.anchor=GridBagConstraints.WEST;
        gbc.fill=GridBagConstraints.NONE;
        mainPanel.add(saveToFile, gbc);
        JScrollPane jScrollPane = new JScrollPane(errorTable);
        gbc.gridx=0;
        gbc.gridy=4;
        gbc.gridwidth=4;
        gbc.gridheight=2;
        gbc.weightx=1;
        gbc.weighty=1;
        gbc.fill=GridBagConstraints.BOTH;
        gbc.insets = new Insets(10,10,10,10);
        mainPanel.add(jScrollPane, gbc);
    }
}
