import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import com.formdev.flatlaf.FlatDarkLaf;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import static java.time.ZoneOffset.UTC;

public class ErrorLog extends JFrame {
    ErrorReport errorReport;
    private DefaultTableModel tableModel;

    public ErrorLog() throws UnsupportedLookAndFeelException, ParseException {
        UIManager.setLookAndFeel(new FlatDarkLaf());
        initComponents();
        prepareUIComponents();
        errorReport = new ErrorReport();
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


    public void prepareUIComponents() {
        initTableModel();
        setDateInSpinner();
    }

    public void initTableModel() {
        tableModel = new DefaultTableModel(0, 6);
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
            showAlert("Дата начала отсчета не может быть больше даты конца отчета");
        }
    }

    public void createReport() {
        errorReport.setReportDateBegin(returnTimestamp(spinnerDateBegin));
        errorReport.setReportDateEnd(returnTimestamp(spinnerDateEnd));
        checkSelectedDateByErrors();
    }

    //Получение ошибок с БД
    public void getErrorsFromDB() {
        SQLQuery sqlQery = new SQLQuery();
        errorReport.setReportBody(sqlQery.viewErrorBySelectDate(errorReport));
    }

    //Окно ошибки
    public static void showError(String exception) {
        JOptionPane.showMessageDialog(new JFrame(),
                "Произошло исключение " + exception,
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void showAlert(String message) {
        JOptionPane.showMessageDialog(new JFrame(),
                message,
                "Внимание",
                JOptionPane.WARNING_MESSAGE);
    }

    public static void showOKMessage(String message){
        JOptionPane.showMessageDialog(new JFrame(),
                message,
                "Успешное завершение операции",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean  checkDatabasePathToExist(){
        boolean result ;
        result = errorReport.getErrordatabaseFilePath() != null;
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
        if (!checkDatabasePathToExist()){
            showAlert("Не выбран файл журнала ошибок");
        }
        else{
        prepareErrorTableForNewAction();
        createReport();
        errorTable.setModel(tableModel);}
    }

    private void saveToFileActionPerformed(ActionEvent e) {
        PDFGenerator pdfGenerator = new PDFGenerator();
        JFileChooser fileChooser = new JFileChooser();
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
                pdfGenerator.saveAsPDF(fileToSave, errorTable, errorReport);
                JOptionPane.showMessageDialog(new JFrame(),
                        "Файл '" + fileChooser.getSelectedFile() +
                                "  сохранен");
            }
            else {
                JOptionPane.showMessageDialog(new JFrame(),
                        "Файл  '" + fileChooser.getSelectedFile() +
                                "  не был сохранен \n" +
                                "Вероятно, файл с таким именем уже существует",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openFileMenuItemActionPerformed(ActionEvent e) {
        JFileChooser openFileChooser = new JFileChooser();
        openFileChooser.setDialogTitle("Открыть файл отчета");
        openFileChooser.setFileFilter(new FileNameExtensionFilter("Файл журнала ошибок", "db"));
        if(openFileChooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION){
            File fileToOpen = openFileChooser.getSelectedFile();
                errorReport.setErrordatabaseFilePath(fileToOpen.getPath());
        }
    }

    private void connectToPanelMenuItemActionPerformed(ActionEvent e) {
       HMI connection = new HMI();
       connection.setIp(constructHMIIP());
       connection.setPassword(passwordHMIText.getText());
       errorReport.setErrordatabaseFilePath(connection.connectToHMI());
    }

    private String constructHMIIP(){
        String ipAdress;
        String first = firstIPOctetText.getText();
        String second = secondIPOctetText.getText();
        String three = thirdIPOctetText.getText();
        String four = fourthIPOctetText.getText();
        ipAdress = first+"."+second+"."+three+"."+four;
        return ipAdress;
    }



    private void initComponents() throws ParseException {
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
        panel8 = new JPanel();
        panel9 = new JPanel();
        label3 = new JLabel();
        label4 = new JLabel();
        passwordHMIText = new JTextField();
        firstIPOctetText = new JFormattedTextField();
        secondIPOctetText = new JFormattedTextField();
        thirdIPOctetText = new JFormattedTextField();
        fourthIPOctetText = new JFormattedTextField();

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
                readDateMenuItem.setText("\u0421\u0447\u0438\u0442\u0430\u0442\u044c \u0434\u0430\u043d\u043d\u044b\u0435");
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
                                    .add(scrollPane2, GroupLayout.DEFAULT_SIZE, 965, Short.MAX_VALUE)
                                    .addContainerGap())
                        );
                        panel6Layout.setVerticalGroup(
                            panel6Layout.createParallelGroup()
                                .add(panel6Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .add(scrollPane2, GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
                                    .addContainerGap())
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

                    GroupLayout panel2Layout = new GroupLayout(panel2);
                    panel2.setLayout(panel2Layout);
                    panel2Layout.setHorizontalGroup(
                        panel2Layout.createParallelGroup()
                            .add(panel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .add(panel2Layout.createParallelGroup()
                                    .add(panel6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(panel2Layout.createSequentialGroup()
                                        .add(panel7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .add(68, 68, 68)
                                        .add(panel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap(259, Short.MAX_VALUE))))
                    );
                    panel2Layout.setVerticalGroup(
                        panel2Layout.createParallelGroup()
                            .add(panel2Layout.createSequentialGroup()
                                .add(panel2Layout.createParallelGroup()
                                    .add(panel7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .add(panel2Layout.createSequentialGroup()
                                        .addContainerGap()
                                        .add(panel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(LayoutStyle.UNRELATED)
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
                                    .addContainerGap(451, Short.MAX_VALUE))
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

                    GroupLayout panel8Layout = new GroupLayout(panel8);
                    panel8.setLayout(panel8Layout);
                    panel8Layout.setHorizontalGroup(
                        panel8Layout.createParallelGroup()
                            .add(panel8Layout.createSequentialGroup()
                                .addContainerGap(45, Short.MAX_VALUE)
                                .add(panel9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(62, Short.MAX_VALUE))
                    );
                    panel8Layout.setVerticalGroup(
                        panel8Layout.createParallelGroup()
                            .add(panel8Layout.createSequentialGroup()
                                .addContainerGap(18, Short.MAX_VALUE)
                                .add(panel9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(418, Short.MAX_VALUE))
                    );
                }
                tabbedPane4.addTab("\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438", panel8);
            }

            GroupLayout panel1Layout = new GroupLayout(panel1);
            panel1.setLayout(panel1Layout);
            panel1Layout.setHorizontalGroup(
                panel1Layout.createParallelGroup()
                    .add(tabbedPane4, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
            );
            panel1Layout.setVerticalGroup(
                panel1Layout.createParallelGroup()
                    .add(panel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(tabbedPane4, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))
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
        setSize(985, 695);
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
    private JPanel panel8;
    private JPanel panel9;
    private JLabel label3;
    private JLabel label4;
    private JTextField passwordHMIText;
    private JFormattedTextField firstIPOctetText;
    private JFormattedTextField secondIPOctetText;
    private JFormattedTextField thirdIPOctetText;
    private JFormattedTextField fourthIPOctetText;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
