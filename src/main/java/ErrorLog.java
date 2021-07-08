import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;

import com.formdev.flatlaf.FlatDarkLaf;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import static java.time.ZoneOffset.UTC;

public class ErrorLog extends JFrame {
    ErrorReport errorReport;
    private DefaultTableModel tableModel;
    private JDialog dialog;
    private HMI connection;
    private static final Logger log = Logger.getLogger(ErrorLog.class);


    public ErrorLog() throws UnsupportedLookAndFeelException {
        BasicConfigurator.configure();
        UIManager.setLookAndFeel(new FlatDarkLaf());
        initComponents();
        log.info("Software was started");
        if(checkPossibleToRunProgram()){
        prepareUIComponents();
        errorReport = new ErrorReport();}
        else {
            log.info("Invalid key. Your key is " + new License().getHardwareID());
            showAlert("Лицензионный ключ программы не прошел проверку.\n" +
                    "Возможности программы искуственно ограничены");
        }
    }



    private boolean checkPossibleToRunProgram(){
        License license = new License();
        return license.getLicenseStatus();
    }

    //Изменение размера ячеек таблицы по ширине содержимого
    public void resizeCellInTableByFitContent() {
        TableColumnModel tcm = errorTable.getColumnModel();
        errorTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int column = 0; column < errorTable.getColumnCount(); column++) {
            int width = 15;
            for (int row = 0; row < tcm.getColumnCount(); row++) {
                TableCellRenderer renderer = errorTable.getCellRenderer(row, column);
                Component comp = errorTable.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 10, width);
            }
            if (width > 600)
                width = 600;
            tcm.getColumn(column).setPreferredWidth(width);
        }
    }

    //Установка текущей даты в поля выбра даты и времени
    public void setDateInSpinner() {
        setSpinnerModel(spinnerDateBegin);
        setSpinnerModel(spinnerDateEnd);
    }

  //Установка моделей компонентов
    public void prepareUIComponents() {
        initTableModel();
        setDateInSpinner();
        errorCategorySetAllCheckBox();
    }

   //Модель отображения таблицы ошибок
    public void initTableModel() {
        tableModel = new DefaultTableModel(0, 7);
        tableModel.addRow(Constants.TABLEHEADER);
        errorTable.setModel(tableModel);
    }



    //Перевод времени в Unix Timestamp
    public long returnTimestamp(JSpinner spinner) {
        Date date;
        long unixTime = 0;
        try {
            date = new SimpleDateFormat(Constants.DATA_FORMAT).parse(new SimpleDateFormat(Constants.DATA_FORMAT).format(spinner.getValue()) + UTC);
            unixTime = date.getTime() / 1000;
        } catch (ParseException parseException) {
            log.error(parseException);
            parseException.printStackTrace();
        }
        return unixTime;
    }

    //Установка отображения даты и времени в спиннере
    public void setSpinnerModel(JSpinner spinner) {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, Calendar.SHORT_FORMAT);
        spinner.setModel(model);
        Calendar calendar = new GregorianCalendar();
        spinner.setEditor(new JSpinner.DateEditor(spinner, Constants.JSPINNER_VIEW_FORMAT));
        spinner.setValue(calendar.getTime());
    }

    //Заполнение таблицы значениями
    public void fillErrorTable() {
        for (int i = 0; i < errorReport.getReportBody().size(); i++) {
            tableModel.addRow(errorReport.getReportBody().get(i));
        }
        resizeCellInTableByFitContent();
    }

    //Очистка таблицы перед новым запросом
    public void prepareErrorTableForNewAction() {
        tableModel.setRowCount(0);
        initTableModel();
    }

    // Проверка что дата начала отчета меньше чем дата конца отчета
    public void checkSelectedDateByErrors() {
        if (errorReport.checkSelectedDateByErrors()) {
            getErrorsFromDB();
            fillErrorTable();
        } else {
            log.error("Логическая ошибка оператора. Дата начала отчета больше даты конца отчета. Начало: "+errorReport.getReportDateBegin()+ " Конец: " + errorReport.getReportDateEnd());
            showAlert("Дата начала отсчета не может быть больше даты конца отчета");
        }
    }

    //Создание отчета
    public void createReport() {
        errorReport.setReportDateBegin(returnTimestamp(spinnerDateBegin));
        errorReport.setReportDateEnd(returnTimestamp(spinnerDateEnd));
        errorReport.setErrorCategorySelectel(getStatusCheckBoxOfErrorCategory());
        checkSelectedDateByErrors();
    }

    //Получение ошибок с БД
    public void getErrorsFromDB() {
        SQLQuery sqlQery = new SQLQuery();
        errorReport.setReportBody(sqlQery.viewErrorBySelectDate(errorReport));
    }

    //Окно ошибки
    public static void showError(String exception) {
        log.error("Произошло исключение "+exception);
        JOptionPane.showMessageDialog(new JFrame(),
                "Произошло исключение " + exception,
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void showAlert(String message) {
        log.info("Внимание "+message);
        JOptionPane.showMessageDialog(new JFrame(),
                message,
                "Внимание",
                JOptionPane.WARNING_MESSAGE);
    }

    public static void showOKMessage(String message) {
        JOptionPane.showMessageDialog(new JFrame(),
                message,
                "Успешное завершение операции",
                JOptionPane.INFORMATION_MESSAGE);
    }

    //Проверка пути к БД ошибок на существование
    private boolean checkDatabasePathToExist() {
        boolean result;
        result = errorReport.getErrordatabaseFilePath() != null;
        log.info("Database is avalible ="+result);
        return result;
    }

    private void readDateMenuItemActionPerformed(ActionEvent e) {
        readDataActionPerformed(e);
    }

    private void saveDataMenuItemActionPerformed(ActionEvent e) {
        saveToFileActionPerformed(e);
    }

    private void menuItem3ActionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(new JFrame(),
                "Программа учета аварий и внештатных ситуаций, произошедших на оборудовании. \n" +
                        "Составляет файл отчета в формате PDF на выбранную дату.",
                "О программе",
                JOptionPane.INFORMATION_MESSAGE);
    }


    private void readDataActionPerformed(ActionEvent e) {
        if (!checkDatabasePathToExist()) {
            showAlert("Не выбран файл журнала ошибок");
        } else {
            prepareErrorTableForNewAction();
            createReport();
            errorTable.setModel(tableModel);
        }
    }

    //Генерирование имени файла отчета за выбранный период
    private String setVariantFileNameForReport(){
        String filename;
        filename = "Отчет за период "+errorReport.getErrorReportFileNameVariant();
        return filename;
    }

    private void saveToFileActionPerformed(ActionEvent e) {
        float[] sizePDFTable = sizePDFTableInReport();
        if (checkSummarySizePDFTableInReport(sizePDFTable)) {
            PDFGenerator pdfGenerator = new PDFGenerator();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(setVariantFileNameForReport()));
            fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));
            fileChooser.setDialogTitle("Сохранить файл отчета");
            int userSelection = fileChooser.showSaveDialog(new JFrame());
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                if (fileToSave == null) {
                    return;
                }
                if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                    fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".pdf");
                    pdfGenerator.saveAsPDF(fileToSave, errorTable, errorReport, sizePDFTable);
                    JOptionPane.showMessageDialog(new JFrame(),
                            "Файл '" + fileChooser.getSelectedFile() +
                                    "  сохранен");
                    log.info("Файл отчета сохранен под именем "+ fileChooser.getSelectedFile() );
                } else {
                    log.info("Файл с именем "+ fileChooser.getSelectedFile() + " существует" );
                    JOptionPane.showMessageDialog(new JFrame(),
                            "Файл  '" + fileChooser.getSelectedFile() +
                                    "  не был сохранен \n" +
                                    "Вероятно, файл с таким именем уже существует",
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        else{
            showAlert("Размеры колонок файла отчета, указанных в настройках программы\n" +
                    "в сумме должны составлять 100%");
        }
    }

    private void openFileMenuItemActionPerformed(ActionEvent e) {
        JFileChooser openFileChooser = new JFileChooser();
        openFileChooser.setDialogTitle("Открыть файл отчета");
        openFileChooser.setFileFilter(new FileNameExtensionFilter("Файл журнала ошибок", "db"));
        if (openFileChooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = openFileChooser.getSelectedFile();
            errorReport.setErrordatabaseFilePath(fileToOpen.getPath());
        }
    }

    private void connectToPanelMenuItemActionPerformed(ActionEvent e) {
        readConnectingSettings();
        Thread t1 = new Thread(() -> errorReport.setErrordatabaseFilePath(connection.connectToHMI()));
        t1.start();
        while (t1.isAlive()) {
            showConnectingScreen();
        }
    }

    private boolean checkSummarySizePDFTableInReport(float[] pdfTableSize){
        boolean checked=false;
        float result=0;
        for (float v : pdfTableSize) {
            result = result + v;
        }
        if (result==100f){
            checked=true;
        }
        return checked;
    }

    private float[] sizePDFTableInReport(){
        float[] size = new float[7];
        size[0]= Float.parseFloat(pdfCuntPositionSpinner.getValue().toString());
        size[1]= Float.parseFloat(pdfCategoryPositionSpinner.getValue().toString());
        size[2]= Float.parseFloat(pdfErrorCodeSpinner.getValue().toString());
        size[3]= Float.parseFloat(pdfErrorDescriptionSpinner.getValue().toString());
        size[4]= Float.parseFloat(pdfErrorBeginDateSpinner.getValue().toString());
        size[5]= Float.parseFloat(pdfErrorEndDateSpinner.getValue().toString());
        size[6]= Float.parseFloat(pdfOperatorSpinner.getValue().toString());
        return size;
    }

    //Чтение настроек подключения к панели
    private void readConnectingSettings(){
        connection = new HMI();
        connection.setIp(constructHMIIP());
        connection.setPassword(passwordHMIText.getText());
    }

    //Отображение экрана ожидания при подключении к панели
    private void showConnectingScreen(){
        JOptionPane op = new JOptionPane("Выполняется подключение...", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        dialog = op.createDialog("Панель оператора");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }, 1000);
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    //Чтение IP адреса с настроек программы
    private String constructHMIIP() {
        String ipAdress;
        String first = firstIPOctetText.getText().replaceAll("[^0-9.]", "");
        String second = secondIPOctetText.getText().replaceAll("[^0-9.]", "");
        String three = thirdIPOctetText.getText().replaceAll("[^0-9.]", "");
        String four = fourthIPOctetText.getText().replaceAll("[^0-9.]", "");
        ipAdress = first + "." + second + "." + three + "." + four;
        return ipAdress;
    }


    private void errorCategorySetAllCheckBox(){
        checkBoxCategory0.setSelected(true);
        checkBoxCategory1.setSelected(true);
        checkBoxCategory2.setSelected(true);
        checkBoxCategory3.setSelected(true);
        checkBoxCategory4.setSelected(true);
        checkBoxCategory5.setSelected(true);
        checkBoxCategory6.setSelected(true);
        checkBoxCategory7.setSelected(true);
    }

    private boolean[] getStatusCheckBoxOfErrorCategory(){
        boolean[] errorCategorySelectel = new boolean[8];
        if (checkBoxCategory0.isSelected())
            errorCategorySelectel[0]=true;
        if (checkBoxCategory1.isSelected())
            errorCategorySelectel[1]=true;
        if (checkBoxCategory2.isSelected())
            errorCategorySelectel[2]=true;
        if (checkBoxCategory3.isSelected())
            errorCategorySelectel[3]=true;
        if (checkBoxCategory4.isSelected())
            errorCategorySelectel[4]=true;
        if (checkBoxCategory5.isSelected())
            errorCategorySelectel[5]=true;
        if (checkBoxCategory6.isSelected())
            errorCategorySelectel[6]=true;
        if (checkBoxCategory7.isSelected())
            errorCategorySelectel[7]=true;
        return errorCategorySelectel;
    }


    private void initComponents()  {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        menuBar1 = new JMenuBar();
        menu1 = new JMenu();
        openFileMenuItem = new JMenuItem();
        readDateMenuItem = new JMenuItem();
        saveDataMenuItem = new JMenuItem();
        menu3 = new JMenu();
        connectToPanelMenuItem = new JMenuItem();
        menu2 = new JMenu();
        menuItem3 = new JMenuItem();
        panel1 = new JPanel();
        tabbedPane4 = new JTabbedPane();
        panel2 = new JPanel();
        panel4 = new JPanel();
        label1 = new JLabel();
        label2 = new JLabel();
        spinnerDateBegin = new JSpinner();
        spinnerDateEnd = new JSpinner();
        readData = new JButton();
        saveToFile = new JButton();
        panel6 = new JPanel();
        scrollPane2 = new JScrollPane();
        errorTable = new JTable();
        panel7 = new JPanel();
        iconLabel = new JLabel();
        panel5 = new JPanel();
        checkBoxCategory0 = new JCheckBox();
        checkBoxCategory1 = new JCheckBox();
        checkBoxCategory2 = new JCheckBox();
        checkBoxCategory4 = new JCheckBox();
        checkBoxCategory5 = new JCheckBox();
        checkBoxCategory6 = new JCheckBox();
        checkBoxCategory3 = new JCheckBox();
        checkBoxCategory7 = new JCheckBox();
        panel8 = new JPanel();
        panel9 = new JPanel();
        label3 = new JLabel();
        label4 = new JLabel();
        passwordHMIText = new JTextField();
        firstIPOctetText = new JFormattedTextField();
        secondIPOctetText = new JFormattedTextField();
        thirdIPOctetText = new JFormattedTextField();
        fourthIPOctetText = new JFormattedTextField();
        panel3 = new JPanel();
        pdfCuntPositionSpinner = new JSpinner();
        pdfErrorCodeSpinner = new JSpinner();
        pdfErrorDescriptionSpinner = new JSpinner();
        pdfErrorBeginDateSpinner = new JSpinner();
        pdfErrorEndDateSpinner = new JSpinner();
        pdfOperatorSpinner = new JSpinner();
        label5 = new JLabel();
        label6 = new JLabel();
        label7 = new JLabel();
        label8 = new JLabel();
        label9 = new JLabel();
        label10 = new JLabel();
        label11 = new JLabel();
        label12 = new JLabel();
        label13 = new JLabel();
        label14 = new JLabel();
        label15 = new JLabel();
        label16 = new JLabel();
        label17 = new JLabel();
        label18 = new JLabel();
        pdfCategoryPositionSpinner = new JSpinner();
        label19 = new JLabel();
        label20 = new JLabel();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("\u0410\u0420\u041c \u0443\u0447\u0435\u0442\u0430 \u043e\u0448\u0438\u0431\u043e\u043a");
        setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        setIconImage(new ImageIcon(getClass().getResource("/icons/title.png")).getImage());
        var contentPane = getContentPane();

        //======== menuBar1 ========
        {

            //======== menu1 ========
            {
                menu1.setText("\u0424\u0430\u0439\u043b");

                //---- openFileMenuItem ----
                openFileMenuItem.setText("\u041e\u0442\u043a\u0440\u044b\u0442\u044c \u0444\u0430\u0439\u043b");
                openFileMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/open.png")));
                openFileMenuItem.addActionListener(e -> openFileMenuItemActionPerformed(e));
                menu1.add(openFileMenuItem);

                //---- readDateMenuItem ----
                readDateMenuItem.setText("\u0412\u044b\u0432\u0435\u0441\u0442\u0438 \u043e\u0442\u0447\u0435\u0442");
                readDateMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/readData.png")));
                readDateMenuItem.addActionListener(e -> readDateMenuItemActionPerformed(e));
                menu1.add(readDateMenuItem);

                //---- saveDataMenuItem ----
                saveDataMenuItem.setText("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u043e\u0442\u0447\u0435\u0442");
                saveDataMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/save.png")));
                saveDataMenuItem.addActionListener(e -> saveDataMenuItemActionPerformed(e));
                menu1.add(saveDataMenuItem);
            }
            menuBar1.add(menu1);

            //======== menu3 ========
            {
                menu3.setText("\u041f\u0430\u043d\u0435\u043b\u044c");

                //---- connectToPanelMenuItem ----
                connectToPanelMenuItem.setText("\u041f\u043e\u0434\u043a\u043b\u044e\u0447\u0438\u0442\u044c\u0441\u044f...");
                connectToPanelMenuItem.addActionListener(e -> connectToPanelMenuItemActionPerformed(e));
                menu3.add(connectToPanelMenuItem);
            }
            menuBar1.add(menu3);

            //======== menu2 ========
            {
                menu2.setText("\u041f\u043e\u043c\u043e\u0449\u044c");

                //---- menuItem3 ----
                menuItem3.setText("\u041e \u043f\u0440\u043e\u0433\u0440\u0430\u043c\u043c\u0435");
                menuItem3.setIcon(new ImageIcon(getClass().getResource("/icons/Help.png")));
                menuItem3.addActionListener(e -> menuItem3ActionPerformed(e));
                menu2.add(menuItem3);
            }
            menuBar1.add(menu2);
        }
        setJMenuBar(menuBar1);

        //======== panel1 ========
        {

            //======== tabbedPane4 ========
            {

                //======== panel2 ========
                {

                    //======== panel4 ========
                    {

                        //---- label1 ----
                        label1.setText("\u0414\u0430\u0442\u0430 \u043d\u0430\u0447\u0430\u043b\u0430 \u0432\u044b\u0431\u043e\u0440\u043a\u0438");
                        label1.setFont(new Font("Segoe UI", Font.BOLD, 14));

                        //---- label2 ----
                        label2.setText("\u0414\u0430\u0442\u0430 \u043a\u043e\u043d\u0446\u0430 \u0432\u044b\u0431\u043e\u0440\u043a\u0438");
                        label2.setFont(new Font("Segoe UI", Font.BOLD, 14));

                        //---- readData ----
                        readData.setText("\u0412\u044b\u0432\u0435\u0441\u0442\u0438 \u043e\u0442\u0447\u0435\u0442 ");
                        readData.addActionListener(e -> {
			readDataActionPerformed(e);
			readDataActionPerformed(e);
		});

                        //---- saveToFile ----
                        saveToFile.setText("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u043e\u0442\u0447\u0435\u0442");
                        saveToFile.addActionListener(e -> saveToFileActionPerformed(e));

                        GroupLayout panel4Layout = new GroupLayout(panel4);
                        panel4.setLayout(panel4Layout);
                        panel4Layout.setHorizontalGroup(
                            panel4Layout.createParallelGroup()
                                .add(panel4Layout.createSequentialGroup()
                                    .add(18, 18, 18)
                                    .add(panel4Layout.createParallelGroup(GroupLayout.TRAILING)
                                        .add(label2, GroupLayout.PREFERRED_SIZE, 153, GroupLayout.PREFERRED_SIZE)
                                        .add(label1))
                                    .add(51, 51, 51)
                                    .add(panel4Layout.createParallelGroup()
                                        .add(panel4Layout.createSequentialGroup()
                                            .add(readData, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.UNRELATED)
                                            .add(saveToFile, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE))
                                        .add(spinnerDateBegin, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE)
                                        .add(spinnerDateEnd, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE))
                                    .addContainerGap(26, Short.MAX_VALUE))
                        );
                        panel4Layout.setVerticalGroup(
                            panel4Layout.createParallelGroup()
                                .add(panel4Layout.createSequentialGroup()
                                    .add(14, 14, 14)
                                    .add(panel4Layout.createParallelGroup()
                                        .add(label1)
                                        .add(spinnerDateBegin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .add(18, 18, 18)
                                    .add(panel4Layout.createParallelGroup(GroupLayout.BASELINE)
                                        .add(label2)
                                        .add(spinnerDateEnd, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .add(18, 18, 18)
                                    .add(panel4Layout.createParallelGroup(GroupLayout.BASELINE)
                                        .add(readData, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(saveToFile, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .add(15, 15, 15))
                        );
                    }

                    //======== panel6 ========
                    {

                        //======== scrollPane2 ========
                        {

                            //---- errorTable ----
                            errorTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                            errorTable.setSelectionBackground(new Color(102, 153, 0));
                            scrollPane2.setViewportView(errorTable);
                        }

                        GroupLayout panel6Layout = new GroupLayout(panel6);
                        panel6.setLayout(panel6Layout);
                        panel6Layout.setHorizontalGroup(
                            panel6Layout.createParallelGroup()
                                .add(panel6Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .add(scrollPane2, GroupLayout.PREFERRED_SIZE, 982, GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap(15, Short.MAX_VALUE))
                        );
                        panel6Layout.setVerticalGroup(
                            panel6Layout.createParallelGroup()
                                .add(GroupLayout.TRAILING, scrollPane2, GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
                        );
                    }

                    //======== panel7 ========
                    {

                        //---- iconLabel ----
                        iconLabel.setIcon(new ImageIcon(getClass().getResource("/icons/data.png")));

                        GroupLayout panel7Layout = new GroupLayout(panel7);
                        panel7.setLayout(panel7Layout);
                        panel7Layout.setHorizontalGroup(
                            panel7Layout.createParallelGroup()
                                .add(panel7Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .add(iconLabel)
                                    .addContainerGap(16, Short.MAX_VALUE))
                        );
                        panel7Layout.setVerticalGroup(
                            panel7Layout.createParallelGroup()
                                .add(panel7Layout.createSequentialGroup()
                                    .add(21, 21, 21)
                                    .add(iconLabel)
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        );
                    }

                    //======== panel5 ========
                    {
                        panel5.setBorder(new TitledBorder("\u041a\u0430\u0442\u0435\u0433\u043e\u0440\u0438\u044f \u043e\u0448\u0438\u0431\u043e\u043a"));

                        //---- checkBoxCategory0 ----
                        checkBoxCategory0.setText("\u041a\u0430\u0442\u0435\u0433\u043e\u0440\u0438\u044f 0");

                        //---- checkBoxCategory1 ----
                        checkBoxCategory1.setText("\u041a\u0430\u0442\u0435\u0433\u043e\u0440\u0438\u044f 1");

                        //---- checkBoxCategory2 ----
                        checkBoxCategory2.setText("\u041a\u0430\u0442\u0435\u0433\u043e\u0440\u0438\u044f 2");

                        //---- checkBoxCategory4 ----
                        checkBoxCategory4.setText("\u041a\u0430\u0442\u0435\u0433\u043e\u0440\u0438\u044f4");

                        //---- checkBoxCategory5 ----
                        checkBoxCategory5.setText("\u041a\u0430\u0442\u0435\u0433\u043e\u0440\u0438\u044f 5");

                        //---- checkBoxCategory6 ----
                        checkBoxCategory6.setText("\u041a\u0430\u0442\u0435\u0433\u043e\u0440\u0438\u044f 6");

                        //---- checkBoxCategory3 ----
                        checkBoxCategory3.setText("\u041a\u0430\u0442\u0435\u0433\u043e\u0440\u0438\u044f 3");

                        //---- checkBoxCategory7 ----
                        checkBoxCategory7.setText("\u041a\u0430\u0442\u0435\u0433\u043e\u0440\u0438\u044f 7");

                        GroupLayout panel5Layout = new GroupLayout(panel5);
                        panel5.setLayout(panel5Layout);
                        panel5Layout.setHorizontalGroup(
                            panel5Layout.createParallelGroup()
                                .add(panel5Layout.createSequentialGroup()
                                    .add(14, 14, 14)
                                    .add(panel5Layout.createParallelGroup()
                                        .add(checkBoxCategory3)
                                        .add(checkBoxCategory2)
                                        .add(checkBoxCategory1)
                                        .add(checkBoxCategory0))
                                    .add(28, 28, 28)
                                    .add(panel5Layout.createParallelGroup()
                                        .add(checkBoxCategory4)
                                        .add(checkBoxCategory5)
                                        .add(checkBoxCategory6)
                                        .add(checkBoxCategory7))
                                    .addContainerGap(34, Short.MAX_VALUE))
                        );
                        panel5Layout.setVerticalGroup(
                            panel5Layout.createParallelGroup()
                                .add(GroupLayout.TRAILING, panel5Layout.createSequentialGroup()
                                    .add(0, 6, Short.MAX_VALUE)
                                    .add(panel5Layout.createParallelGroup(GroupLayout.BASELINE)
                                        .add(checkBoxCategory0)
                                        .add(checkBoxCategory4))
                                    .addPreferredGap(LayoutStyle.RELATED)
                                    .add(panel5Layout.createParallelGroup(GroupLayout.BASELINE)
                                        .add(checkBoxCategory1)
                                        .add(checkBoxCategory5))
                                    .addPreferredGap(LayoutStyle.RELATED)
                                    .add(panel5Layout.createParallelGroup(GroupLayout.BASELINE)
                                        .add(checkBoxCategory2)
                                        .add(checkBoxCategory6))
                                    .addPreferredGap(LayoutStyle.RELATED)
                                    .add(panel5Layout.createParallelGroup(GroupLayout.BASELINE)
                                        .add(checkBoxCategory3)
                                        .add(checkBoxCategory7))
                                    .add(14, 14, 14))
                        );
                    }

                    GroupLayout panel2Layout = new GroupLayout(panel2);
                    panel2.setLayout(panel2Layout);
                    panel2Layout.setHorizontalGroup(
                        panel2Layout.createParallelGroup()
                            .add(panel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .add(panel2Layout.createParallelGroup()
                                    .add(panel6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .add(panel2Layout.createSequentialGroup()
                                        .add(panel7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .add(68, 68, 68)
                                        .add(panel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.RELATED)
                                        .add(panel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    );
                    panel2Layout.setVerticalGroup(
                        panel2Layout.createParallelGroup()
                            .add(panel2Layout.createSequentialGroup()
                                .add(panel2Layout.createParallelGroup()
                                    .add(panel7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .add(panel2Layout.createSequentialGroup()
                                        .addContainerGap()
                                        .add(panel2Layout.createParallelGroup()
                                            .add(panel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .add(panel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
                                .add(17, 17, 17)
                                .add(panel6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
                    );
                }
                tabbedPane4.addTab("\u041e\u0442\u0447\u0435\u0442", panel2);

                //======== panel8 ========
                {

                    //======== panel9 ========
                    {
                        panel9.setBorder(new TitledBorder("\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u044f \u043f\u0430\u043d\u0435\u043b\u0438 \u043e\u043f\u0435\u0440\u0430\u0442\u043e\u0440\u0430"));

                        //---- label3 ----
                        label3.setText("IP \u0430\u0434\u0440\u0435\u0441 \u043f\u0430\u043d\u0435\u043b\u0438 \u043e\u043f\u0435\u0440\u0430\u0442\u043e\u0440\u0430");

                        //---- label4 ----
                        label4.setText("\u041f\u0430\u0440\u043e\u043b\u044c \u043f\u0430\u043d\u0435\u043b\u0438 \u043e\u043f\u0435\u0440\u0430\u0442\u043e\u0440\u0430");

                        //---- passwordHMIText ----
                        passwordHMIText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                        passwordHMIText.setText("111111");

                        //---- firstIPOctetText ----
                        firstIPOctetText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                        firstIPOctetText.setText("192");

                        //---- secondIPOctetText ----
                        secondIPOctetText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                        secondIPOctetText.setText("168");

                        //---- thirdIPOctetText ----
                        thirdIPOctetText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                        thirdIPOctetText.setText("0");

                        //---- fourthIPOctetText ----
                        fourthIPOctetText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                        fourthIPOctetText.setText("3");

                        GroupLayout panel9Layout = new GroupLayout(panel9);
                        panel9.setLayout(panel9Layout);
                        panel9Layout.setHorizontalGroup(
                            panel9Layout.createParallelGroup()
                                .add(panel9Layout.createSequentialGroup()
                                    .add(29, 29, 29)
                                    .add(panel9Layout.createParallelGroup()
                                        .add(label3)
                                        .add(label4))
                                    .add(24, 24, 24)
                                    .add(panel9Layout.createParallelGroup()
                                        .add(passwordHMIText, GroupLayout.PREFERRED_SIZE, 214, GroupLayout.PREFERRED_SIZE)
                                        .add(panel9Layout.createSequentialGroup()
                                            .add(firstIPOctetText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.RELATED)
                                            .add(secondIPOctetText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.RELATED)
                                            .add(thirdIPOctetText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.RELATED)
                                            .add(fourthIPOctetText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                    .addContainerGap(468, Short.MAX_VALUE))
                        );
                        panel9Layout.setVerticalGroup(
                            panel9Layout.createParallelGroup()
                                .add(panel9Layout.createSequentialGroup()
                                    .add(27, 27, 27)
                                    .add(panel9Layout.createParallelGroup(GroupLayout.BASELINE)
                                        .add(label3)
                                        .add(firstIPOctetText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .add(secondIPOctetText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .add(thirdIPOctetText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .add(fourthIPOctetText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .add(25, 25, 25)
                                    .add(panel9Layout.createParallelGroup(GroupLayout.BASELINE)
                                        .add(passwordHMIText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .add(label4))
                                    .addContainerGap(28, Short.MAX_VALUE))
                        );
                    }

                    //======== panel3 ========
                    {
                        panel3.setBorder(new TitledBorder("\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u0444\u0430\u0439\u043b\u0430 \u043e\u0442\u0447\u0435\u0442\u0430"));

                        //---- pdfCuntPositionSpinner ----
                        pdfCuntPositionSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                        pdfCuntPositionSpinner.setModel(new SpinnerNumberModel(11.0F, 0.0F, 100.0F, 1.0F));

                        //---- pdfErrorCodeSpinner ----
                        pdfErrorCodeSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                        pdfErrorCodeSpinner.setModel(new SpinnerNumberModel(8.0F, 0.0F, 100.0F, 1.0F));

                        //---- pdfErrorDescriptionSpinner ----
                        pdfErrorDescriptionSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                        pdfErrorDescriptionSpinner.setModel(new SpinnerNumberModel(34.0F, 0.0F, 100.0F, 1.0F));

                        //---- pdfErrorBeginDateSpinner ----
                        pdfErrorBeginDateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                        pdfErrorBeginDateSpinner.setModel(new SpinnerNumberModel(12.0F, 0.0F, 100.0F, 1.0F));

                        //---- pdfErrorEndDateSpinner ----
                        pdfErrorEndDateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                        pdfErrorEndDateSpinner.setModel(new SpinnerNumberModel(12.0F, 0.0F, 100.0F, 1.0F));

                        //---- pdfOperatorSpinner ----
                        pdfOperatorSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                        pdfOperatorSpinner.setModel(new SpinnerNumberModel(15.0F, 0.0F, 100.0F, 1.0F));

                        //---- label5 ----
                        label5.setText("\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430 \u0448\u0438\u0440\u0438\u043d\u044b \u043a\u0430\u0436\u0434\u043e\u0439 \u043a\u043e\u043b\u043e\u043d\u043a\u0438 \u0432 \u0442\u0430\u0431\u043b\u0438\u0446\u0435 \u0444\u0430\u0439\u043b\u0430 \u043e\u0442\u0447\u0435\u0442\u0430 ");
                        label5.setFont(label5.getFont().deriveFont(16f));

                        //---- label6 ----
                        label6.setText("\u041d\u043e\u043c\u0435\u0440 \u043f\u043e\u0437\u0438\u0446\u0438\u0438");

                        //---- label7 ----
                        label7.setText("\u041a\u043e\u0434 \u043e\u0448\u0438\u0431\u043a\u0438");

                        //---- label8 ----
                        label8.setText("\u041e\u043f\u0438\u0441\u0430\u043d\u0438\u0435");

                        //---- label9 ----
                        label9.setText("\u0414\u0430\u0442\u0430 \u043d\u0430\u0447\u0430\u043b\u0430");

                        //---- label10 ----
                        label10.setText("\u0414\u0430\u0442\u0430 \u043a\u043e\u043d\u0446\u0430");

                        //---- label11 ----
                        label11.setText("\u041e\u043f\u0435\u0440\u0430\u0442\u043e\u0440");

                        //---- label12 ----
                        label12.setText("%");
                        label12.setFont(label12.getFont().deriveFont(16f));

                        //---- label13 ----
                        label13.setText("%");
                        label13.setFont(label13.getFont().deriveFont(16f));

                        //---- label14 ----
                        label14.setText("%");
                        label14.setFont(label14.getFont().deriveFont(16f));

                        //---- label15 ----
                        label15.setText("%");
                        label15.setFont(label15.getFont().deriveFont(16f));

                        //---- label16 ----
                        label16.setText("%");
                        label16.setFont(label16.getFont().deriveFont(16f));

                        //---- label17 ----
                        label17.setText("%");
                        label17.setFont(label17.getFont().deriveFont(16f));

                        //---- label18 ----
                        label18.setText("* \u041e\u0431\u0449\u0430\u044f \u0441\u0443\u043c\u043c\u0430 \u0440\u0430\u0437\u043c\u0435\u0440\u043e\u0432  \u043a\u043e\u043b\u043e\u043d\u043e\u043a \u0434\u043e\u043b\u0436\u043d\u0430 \u0441\u043e\u0441\u0442\u0430\u0432\u043b\u044f\u0442\u044c 100%");

                        //---- pdfCategoryPositionSpinner ----
                        pdfCategoryPositionSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                        pdfCategoryPositionSpinner.setModel(new SpinnerNumberModel(8.0F, 0.0F, 100.0F, 1.0F));

                        //---- label19 ----
                        label19.setText("%");
                        label19.setFont(label19.getFont().deriveFont(16f));

                        //---- label20 ----
                        label20.setText("\u041a\u0430\u0442\u0435\u0433\u043e\u0440\u0438\u044f \u043e\u0448\u0438\u0431\u043a\u0438");

                        GroupLayout panel3Layout = new GroupLayout(panel3);
                        panel3.setLayout(panel3Layout);
                        panel3Layout.setHorizontalGroup(
                            panel3Layout.createParallelGroup()
                                .add(panel3Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .add(panel3Layout.createParallelGroup()
                                        .add(panel3Layout.createParallelGroup(GroupLayout.TRAILING)
                                            .add(label5)
                                            .add(GroupLayout.LEADING, label18))
                                        .add(panel3Layout.createSequentialGroup()
                                            .add(12, 12, 12)
                                            .add(panel3Layout.createParallelGroup()
                                                .add(panel3Layout.createSequentialGroup()
                                                    .add(pdfCuntPositionSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(LayoutStyle.RELATED)
                                                    .add(label12))
                                                .add(label6))
                                            .addPreferredGap(LayoutStyle.RELATED, 1, Short.MAX_VALUE)
                                            .add(panel3Layout.createParallelGroup()
                                                .add(panel3Layout.createSequentialGroup()
                                                    .add(23, 23, 23)
                                                    .add(pdfCategoryPositionSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                    .add(9, 9, 9)
                                                    .add(label19))
                                                .add(panel3Layout.createSequentialGroup()
                                                    .add(29, 29, 29)
                                                    .add(label20)))
                                            .add(18, 18, 18)
                                            .add(panel3Layout.createParallelGroup()
                                                .add(panel3Layout.createSequentialGroup()
                                                    .add(pdfErrorCodeSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(LayoutStyle.RELATED)
                                                    .add(label13))
                                                .add(label7))
                                            .add(panel3Layout.createParallelGroup()
                                                .add(panel3Layout.createSequentialGroup()
                                                    .addPreferredGap(LayoutStyle.RELATED, 31, Short.MAX_VALUE)
                                                    .add(pdfErrorDescriptionSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .add(panel3Layout.createSequentialGroup()
                                                    .add(38, 38, 38)
                                                    .add(label8)
                                                    .addPreferredGap(LayoutStyle.RELATED, 19, Short.MAX_VALUE)))
                                            .add(5, 5, 5)
                                            .add(label14)
                                            .addPreferredGap(LayoutStyle.RELATED, 32, Short.MAX_VALUE)
                                            .add(panel3Layout.createParallelGroup()
                                                .add(pdfErrorBeginDateSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .add(label9))
                                            .add(panel3Layout.createParallelGroup(GroupLayout.TRAILING)
                                                .add(panel3Layout.createSequentialGroup()
                                                    .add(54, 54, 54)
                                                    .add(label10)
                                                    .add(30, 30, 30))
                                                .add(panel3Layout.createSequentialGroup()
                                                    .addPreferredGap(LayoutStyle.RELATED)
                                                    .add(label15)
                                                    .addPreferredGap(LayoutStyle.RELATED, 27, Short.MAX_VALUE)
                                                    .add(pdfErrorEndDateSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(LayoutStyle.RELATED)
                                                    .add(label16)))))
                                    .add(18, 18, 18)
                                    .add(panel3Layout.createParallelGroup()
                                        .add(GroupLayout.TRAILING, panel3Layout.createSequentialGroup()
                                            .add(pdfOperatorSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.RELATED)
                                            .add(label17)
                                            .addContainerGap())
                                        .add(GroupLayout.TRAILING, panel3Layout.createSequentialGroup()
                                            .add(label11)
                                            .add(41, 41, 41))))
                        );
                        panel3Layout.setVerticalGroup(
                            panel3Layout.createParallelGroup()
                                .add(panel3Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .add(label5)
                                    .add(30, 30, 30)
                                    .add(panel3Layout.createParallelGroup(GroupLayout.BASELINE)
                                        .add(label11)
                                        .add(label10)
                                        .add(label7)
                                        .add(label8)
                                        .add(label9)
                                        .add(label20)
                                        .add(label6))
                                    .addPreferredGap(LayoutStyle.RELATED)
                                    .add(panel3Layout.createParallelGroup(GroupLayout.BASELINE)
                                        .add(pdfOperatorSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .add(pdfCategoryPositionSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .add(label19)
                                        .add(pdfErrorCodeSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .add(label13)
                                        .add(pdfErrorDescriptionSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .add(label14)
                                        .add(pdfErrorBeginDateSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .add(pdfErrorEndDateSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .add(label15)
                                        .add(label16)
                                        .add(label17)
                                        .add(label12)
                                        .add(pdfCuntPositionSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(LayoutStyle.UNRELATED)
                                    .add(label18)
                                    .addContainerGap(15, Short.MAX_VALUE))
                        );
                    }

                    GroupLayout panel8Layout = new GroupLayout(panel8);
                    panel8.setLayout(panel8Layout);
                    panel8Layout.setHorizontalGroup(
                        panel8Layout.createParallelGroup()
                            .add(GroupLayout.TRAILING, panel8Layout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(panel8Layout.createParallelGroup(GroupLayout.LEADING, false)
                                    .add(panel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(panel9, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .add(75, 75, 75))
                    );
                    panel8Layout.setVerticalGroup(
                        panel8Layout.createParallelGroup()
                            .add(panel8Layout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(panel9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .add(31, 31, 31)
                                .add(panel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(224, Short.MAX_VALUE))
                    );
                }
                tabbedPane4.addTab("\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438", panel8);
            }

            GroupLayout panel1Layout = new GroupLayout(panel1);
            panel1.setLayout(panel1Layout);
            panel1Layout.setHorizontalGroup(
                panel1Layout.createParallelGroup()
                    .add(panel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(tabbedPane4, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                        .add(27, 27, 27))
            );
            panel1Layout.setVerticalGroup(
                panel1Layout.createParallelGroup()
                    .add(GroupLayout.TRAILING, tabbedPane4, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
            );
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .add(panel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .add(panel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        setSize(1030, 695);
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JMenuBar menuBar1;
    private JMenu menu1;
    private JMenuItem openFileMenuItem;
    private JMenuItem readDateMenuItem;
    private JMenuItem saveDataMenuItem;
    private JMenu menu3;
    private JMenuItem connectToPanelMenuItem;
    private JMenu menu2;
    private JMenuItem menuItem3;
    private JPanel panel1;
    private JTabbedPane tabbedPane4;
    private JPanel panel2;
    private JPanel panel4;
    private JLabel label1;
    private JLabel label2;
    private JSpinner spinnerDateBegin;
    private JSpinner spinnerDateEnd;
    private JButton readData;
    private JButton saveToFile;
    private JPanel panel6;
    private JScrollPane scrollPane2;
    private JTable errorTable;
    private JPanel panel7;
    private JLabel iconLabel;
    private JPanel panel5;
    private JCheckBox checkBoxCategory0;
    private JCheckBox checkBoxCategory1;
    private JCheckBox checkBoxCategory2;
    private JCheckBox checkBoxCategory4;
    private JCheckBox checkBoxCategory5;
    private JCheckBox checkBoxCategory6;
    private JCheckBox checkBoxCategory3;
    private JCheckBox checkBoxCategory7;
    private JPanel panel8;
    private JPanel panel9;
    private JLabel label3;
    private JLabel label4;
    private JTextField passwordHMIText;
    private JFormattedTextField firstIPOctetText;
    private JFormattedTextField secondIPOctetText;
    private JFormattedTextField thirdIPOctetText;
    private JFormattedTextField fourthIPOctetText;
    private JPanel panel3;
    private JSpinner pdfCuntPositionSpinner;
    private JSpinner pdfErrorCodeSpinner;
    private JSpinner pdfErrorDescriptionSpinner;
    private JSpinner pdfErrorBeginDateSpinner;
    private JSpinner pdfErrorEndDateSpinner;
    private JSpinner pdfOperatorSpinner;
    private JLabel label5;
    private JLabel label6;
    private JLabel label7;
    private JLabel label8;
    private JLabel label9;
    private JLabel label10;
    private JLabel label11;
    private JLabel label12;
    private JLabel label13;
    private JLabel label14;
    private JLabel label15;
    private JLabel label16;
    private JLabel label17;
    private JLabel label18;
    private JSpinner pdfCategoryPositionSpinner;
    private JLabel label19;
    private JLabel label20;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
