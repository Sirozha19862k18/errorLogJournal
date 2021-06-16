import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;



public class Main extends JFrame {
    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel( new FlatLightLaf() );
        JFrame frame = new ErrorLog("Журнал учёта ошибок");
        frame.setVisible(true);
    }


}
