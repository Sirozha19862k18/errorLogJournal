import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;


import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PDFGenerator {
    private  Font reportTableFont;
    private  Font headerTableFont;
    private  com.itextpdf.text.Document document;
    private  File file;
    private JTable errorTable;

    public void saveAsPDF(File file, JTable errorTable){
        this.file=file;
        this.errorTable = errorTable;
        System.out.println(file.getAbsolutePath());
        if(!file.isFile()){
                try {
                    generatePDF();
                } catch (Exception e) {
                    ErrorLog.showError(e.getMessage());
                    e.printStackTrace();
                }
        }
        else {
            System.out.println("Файл существует");

        }
    }

    public void generatePDF(){
        prepareFontSet();
        openPDFDocumentToWrite();
        generateHeaderOfReport();
        generateErrorTable();
        document.close();
    }

    public void generateHeaderOfReport()  {
        Paragraph paragraph = new Paragraph("Отчет по ошибкам за период ",  headerTableFont);
        paragraph.setAlignment(Paragraph.ALIGN_CENTER);
        try {
            document.add(paragraph);
            document.add(new Chunk());
        } catch (DocumentException e) {
            ErrorLog.showError(e.getMessage());
            e.printStackTrace();
        }

    }
    private void generateErrorTable()  {
        PdfPTable table = new PdfPTable(new float[]{11f, 8f, 42f, 12f, 12f, 15f});
        table.setWidthPercentage(100f );
        for (int rowCounter=0; rowCounter<errorTable.getRowCount(); rowCounter++){
            for (int columnCouner=0; columnCouner<errorTable.getColumnCount(); columnCouner++){
                PdfPCell pcell = new PdfPCell(new Phrase((errorTable.getModel().getValueAt(rowCounter,columnCouner).toString()), reportTableFont));
                pcell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                if(rowCounter<1){
                    pcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                }

                table.addCell(pcell);
            }}
        try {
            document.add(table);
        } catch (DocumentException e) {
            ErrorLog.showError(e.getMessage());
            e.printStackTrace();
        }
    }


    private void prepareFontSet()  {
        BaseFont baseFont = null;
        try {
            baseFont = BaseFont.createFont("font.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (DocumentException | IOException e )  {
            ErrorLog.showError(e.getMessage());
            e.printStackTrace();
        }
        reportTableFont = new Font(baseFont, 10);
         headerTableFont = new Font(baseFont,  12,  Font.BOLD);
    }

    private void openPDFDocumentToWrite(){
        document = new com.itextpdf.text.Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
        } catch (DocumentException | FileNotFoundException e ) {
            ErrorLog.showError(e.getMessage());
            e.printStackTrace();
        }
        document.open();
    }
}
