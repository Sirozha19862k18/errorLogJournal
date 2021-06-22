import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;

public class HMI {

    FTPClient ftpClient;
    File downloadFile;
    String ip;
    String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String connectToHMI()  {
        int port = Constants.HMI_PORT;
        String user = Constants.HMI_USER;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip, port);
            ftpClient.login(user, password);


            copyDBFileFromHMI();
        } catch (IOException e) {
            ErrorLog.showError(e+ "\nНе удалось подключиться к панели опреатора.\n" +
                    "Проверьте сетевые настройки панели \n" +
                    "Адрес панели должен быть " +ip);
            e.printStackTrace();
        }
        finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ErrorLog.showError(ex.toString());
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
            else               {
                ErrorLog.showOKMessage("Фигня");
            }
        } catch (IOException e) {
            ErrorLog.showError(e.toString());
        }
    }
}
