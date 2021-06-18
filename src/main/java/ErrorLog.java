import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import static java.time.ZoneOffset.UTC;


public class ErrorLog extends JFrame {
    ErrorReport errorReport;

    private static DefaultTableModel tableModel;

    public ErrorLog() throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new FlatDarkLaf());
        initComponents();
        prepareUIComponents();
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
            errorReport.getErrorsFromDB();
            fillErrorTable();
        } else {
            showAlert("Дата начала отсчета не может быть больше даты конца отчета");
        }
    }

    public void createReport() {
        errorReport = new ErrorReport();
        errorReport.setReportDateBegin(returnTimestamp(spinnerDateBegin));
        errorReport.setReportDateEnd(returnTimestamp(spinnerDateEnd));
        checkSelectedDateByErrors();
    }


    //Окно ошибки
    public static void showError(String exception) {
        JOptionPane.showMessageDialog(new JFrame(),
                "Произошло исключение" + exception,
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void showAlert(String message) {
        JOptionPane.showMessageDialog(new JFrame(),
                message,
                "Внимание",
                JOptionPane.WARNING_MESSAGE);
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
        prepareErrorTableForNewAction();
        createReport();
        errorTable.setModel(tableModel);
    }

    private void saveToFileActionPerformed(ActionEvent e) {
        PDFGenerator pdfGenerator = new PDFGenerator();
        JFrame parentFrame = new JFrame();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));
        fileChooser.setDialogTitle("Сохранить файл отчета");
        int userSelection = fileChooser.showSaveDialog(parentFrame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (fileToSave == null) {
                return;
            }
            if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".pdf");
                pdfGenerator.saveAsPDF(fileToSave, errorTable);
            }
        }
    }

    private void button1ActionPerformed(ActionEvent e) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new FlatLightLaf());
        initComponents();
        prepareUIComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        menuBar1 = new JMenuBar();
        menu1 = new JMenu();
        readDateMenuItem = new JMenuItem();
        saveDataMenuItem = new JMenuItem();
        menu2 = new JMenu();
        menuItem3 = new JMenuItem();
        panel1 = new JPanel();
        scrollPane1 = new JScrollPane();
        errorTable = new JTable();
        panel2 = new JPanel();
        label1 = new JLabel();
        label2 = new JLabel();
        spinnerDateBegin = new JSpinner();
        spinnerDateEnd = new JSpinner();
        readData = new JButton();
        saveToFile = new JButton();

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

                //---- readDateMenuItem ----
                readDateMenuItem.setText("\u0421\u0447\u0438\u0442\u0430\u0442\u044c \u0434\u0430\u043d\u043d\u044b\u0435");
                readDateMenuItem.addActionListener(e -> readDateMenuItemActionPerformed(e));
                menu1.add(readDateMenuItem);

                //---- saveDataMenuItem ----
                saveDataMenuItem.setText("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u043e\u0442\u0447\u0435\u0442");
                saveDataMenuItem.addActionListener(e -> saveDataMenuItemActionPerformed(e));
                menu1.add(saveDataMenuItem);
            }
            menuBar1.add(menu1);

            //======== menu2 ========
            {
                menu2.setText("\u041f\u043e\u043c\u043e\u0449\u044c");

                //---- menuItem3 ----
                menuItem3.setText("\u041e \u043f\u0440\u043e\u0433\u0440\u0430\u043c\u043c\u0435");
                menuItem3.addActionListener(e -> menuItem3ActionPerformed(e));
                menu2.add(menuItem3);
            }
            menuBar1.add(menu2);
        }
        setJMenuBar(menuBar1);

        //======== panel1 ========
        {

            //======== scrollPane1 ========
            {

                //---- errorTable ----
                errorTable.setSelectionForeground(Color.black);
                errorTable.setSelectionBackground(new Color(102, 255, 51));
                errorTable.setAutoCreateRowSorter(true);
                errorTable.setToolTipText("\u0422\u0430\u0431\u043b\u0438\u0446\u0430 \u0434\u0430\u043d\u043d\u044b\u0445");
                errorTable.setDragEnabled(true);
                errorTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                scrollPane1.setViewportView(errorTable);
            }

            //======== panel2 ========
            {

                //---- label1 ----
                label1.setText("\u0414\u0430\u0442\u0430 \u043d\u0430\u0447\u0430\u043b\u0430 \u0432\u044b\u0431\u043e\u0440\u043a\u0438");
                label1.setFont(new Font("Segoe UI", Font.BOLD, 14));

                //---- label2 ----
                label2.setText("\u0414\u0430\u0442\u0430 \u043a\u043e\u043d\u0446\u0430 \u0432\u044b\u0431\u043e\u0440\u043a\u0438");
                label2.setFont(new Font("Segoe UI", Font.BOLD, 14));

                //---- readData ----
                readData.setText("\u0421\u0447\u0438\u0442\u0430\u0442\u044c \u0434\u0430\u043d\u043d\u044b\u0435");
                readData.addActionListener(e -> {
			readDataActionPerformed(e);
			readDataActionPerformed(e);
		});

                //---- saveToFile ----
                saveToFile.setText("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u043e\u0442\u0447\u0435\u0442");
                saveToFile.addActionListener(e -> saveToFileActionPerformed(e));

                GroupLayout panel2Layout = new GroupLayout(panel2);
                panel2.setLayout(panel2Layout);
                panel2Layout.setHorizontalGroup(
                    panel2Layout.createParallelGroup()
                        .add(panel2Layout.createSequentialGroup()
                            .add(label1)
                            .addPreferredGap(LayoutStyle.RELATED)
                            .add(spinnerDateBegin, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE)
                            .add(0, 0, Short.MAX_VALUE))
                        .add(panel2Layout.createSequentialGroup()
                            .add(label2, GroupLayout.PREFERRED_SIZE, 153, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(panel2Layout.createParallelGroup(GroupLayout.LEADING, false)
                                .add(panel2Layout.createSequentialGroup()
                                    .add(readData, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.RELATED)
                                    .add(saveToFile, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .add(GroupLayout.TRAILING, panel2Layout.createSequentialGroup()
                                    .add(spinnerDateEnd, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE)
                                    .add(125, 125, 125))))
                );
                panel2Layout.setVerticalGroup(
                    panel2Layout.createParallelGroup()
                        .add(panel2Layout.createSequentialGroup()
                            .add(22, 22, 22)
                            .add(panel2Layout.createParallelGroup(GroupLayout.BASELINE)
                                .add(spinnerDateBegin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .add(label1))
                            .addPreferredGap(LayoutStyle.RELATED)
                            .add(panel2Layout.createParallelGroup(GroupLayout.BASELINE)
                                .add(spinnerDateEnd, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .add(label2))
                            .add(18, 18, 18)
                            .add(panel2Layout.createParallelGroup(GroupLayout.BASELINE)
                                .add(readData, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(saveToFile, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addContainerGap())
                );
            }

            GroupLayout panel1Layout = new GroupLayout(panel1);
            panel1.setLayout(panel1Layout);
            panel1Layout.setHorizontalGroup(
                panel1Layout.createParallelGroup()
                    .add(panel1Layout.createSequentialGroup()
                        .add(5, 5, 5)
                        .add(scrollPane1, GroupLayout.DEFAULT_SIZE, 918, Short.MAX_VALUE)
                        .add(5, 5, 5))
                    .add(panel1Layout.createSequentialGroup()
                        .add(201, 201, 201)
                        .add(panel2, GroupLayout.PREFERRED_SIZE, 484, GroupLayout.PREFERRED_SIZE)
                        .add(72, 243, Short.MAX_VALUE))
            );
            panel1Layout.setVerticalGroup(
                panel1Layout.createParallelGroup()
                    .add(panel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(panel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(scrollPane1, GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                        .add(5, 5, 5))
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
        setSize(930, 555);
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JMenuBar menuBar1;
    private JMenu menu1;
    private JMenuItem readDateMenuItem;
    private JMenuItem saveDataMenuItem;
    private JMenu menu2;
    private JMenuItem menuItem3;
    private JPanel panel1;
    private JScrollPane scrollPane1;
    private JTable errorTable;
    private JPanel panel2;
    private JLabel label1;
    private JLabel label2;
    private JSpinner spinnerDateBegin;
    private JSpinner spinnerDateEnd;
    private JButton readData;
    private JButton saveToFile;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
