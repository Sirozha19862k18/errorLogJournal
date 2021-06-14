import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;


public class Main {
    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel( new FlatLightLaf() );
        JFrame frame = new ErrorLog("Журнал учёта ошибок");
        frame.setVisible(true);
    }
}
