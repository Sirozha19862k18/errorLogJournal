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

import static java.time.ZoneOffset.UTC;


public class ErrorLog extends JFrame {
    ErrorReport errorReport;

    private static DefaultTableModel tableModel;

    public ErrorLog(String title) throws HeadlessException {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        mainPanel.setSize(800, 600);
        this.setBounds(400, 400, 800, 600);
        initUI();

        //Кнопка СЧИТАТЬ ДАННЫЕ
        readData.addActionListener(e -> {
            prepareErrorTableForNewAction();
            createReport();
            errorTable.setModel(tableModel);
        });

        //Кнопка сохранить отчет
        saveToFile.addActionListener(e -> {
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
        });
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
            if (width > 400)
                width = 400;
            tcm.getColumn(column).setPreferredWidth(width);
        }
    }

    //Установка текущей даты в поля выбра даты и времени
    public void setDateInSpinner() {
        setSpinnerModel(spinnerDateBegin);
        setSpinnerModel(spinnerDateEnd);
    }


    public void initUI() {
        initTableModel();
        addComonentToPane();
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

    public void addComonentToPane() {
        GridBagLayout myLayout = new GridBagLayout();
        mainPanel.setLayout(myLayout);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        JPanel selectDatePanel = new JPanel();
        selectDatePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Выбор даты"));
        GridBagLayout selectDatePanelLayout = new GridBagLayout();
        mainPanel.add(selectDatePanel, gbc);
        selectDatePanel.setLayout(selectDatePanelLayout);
        GridBagConstraints selectDatePanelGBC = new GridBagConstraints();
        selectDatePanelGBC.insets = new Insets(10, 10, 20, 10);
        selectDatePanelGBC.gridx = 0;
        selectDatePanelGBC.gridy = 0;
        selectDatePanelGBC.gridwidth = 1;
        selectDatePanelGBC.gridheight = 1;
        selectDatePanel.add(labelTextBegin, selectDatePanelGBC);
        selectDatePanelGBC.gridx = 1;
        selectDatePanelGBC.gridy = 0;
        selectDatePanelGBC.gridwidth = 1;
        selectDatePanelGBC.gridheight = 1;
        selectDatePanel.add(spinnerDateBegin, selectDatePanelGBC);
        selectDatePanelGBC.gridx = 0;
        selectDatePanelGBC.gridy = 1;
        selectDatePanelGBC.gridwidth = 1;
        selectDatePanelGBC.gridheight = 1;
        selectDatePanel.add(labelTextEnd, selectDatePanelGBC);
        selectDatePanelGBC.gridx = 1;
        selectDatePanelGBC.gridy = 1;
        selectDatePanelGBC.gridwidth = 1;
        selectDatePanelGBC.gridheight = 1;
        selectDatePanel.add(spinnerDateEnd, selectDatePanelGBC);
        //Button Синхронизация
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(20, 10, 20, 10);
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(synchronizeButton, gbc);
        //Button Считать данные
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(20, 10, 20, 10);
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(readData, gbc);
        //Button Сохранить
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(20, 10, 20, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(saveToFile, gbc);
        JScrollPane jScrollPane = new JScrollPane(errorTable);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.gridheight = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(jScrollPane, gbc);
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

    private JPanel mainPanel;
    private JSpinner spinnerDateBegin;
    private JSpinner spinnerDateEnd;
    private JLabel labelTextEnd;
    private JTable errorTable;
    private JButton readData;
    private JButton saveToFile;
    private JLabel labelTextBegin;
    private JButton synchronizeButton;

}
