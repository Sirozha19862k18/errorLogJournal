import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;

public class HMI {

    FTPClient ftpClient;
    File downloadFile;

    public String connectToHMI()  {
        String server = Constants.HMI_ADRESS;
        int port = Constants.HMI_PORT;
        String user = Constants.HMI_USER;
        String pass = Constants.HMI_PASS;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            copyDBFileFromHMI();
        } catch (IOException e) {
            ErrorLog.showError(e+ "\nНе удалось подключиться к панели опреатора.\n" +
                    "Проверьте сетевые настройки панели \n" +
                    "Адрес панели должен быть 192.168.0.3\n");
            e.printStackTrace();
        }
        finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return downloadFile.getAbsolutePath();
    }
    private void copyDBFileFromHMI(){
        ftpClient.enterLocalPassiveMode();
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            String remoteFile = Constants.PATH_TO_DB_IN_HMI;
            downloadFile = new File(Constants.TEMPORARY_DB_FILE_ON_PC);
            OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile));
            boolean success = ftpClient.retrieveFile(remoteFile, outputStream1);
            outputStream1.close();
            if (success)
            {
                ErrorLog.showOKMessage("База данных ошибок успешно скопирована с панели опретатора");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
