import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HardwareAbstractionLayer;

public class License {
    boolean licenseStatus;
    private String hardwareID;

    public boolean getLicenseStatus() {
        boolean isLicensedSoftware = false;
        String serialMB = getHardwareID();
        if (serialMB.equals(Constants.SERIAL_NUMBER_MOTHERBOARD)) {
            isLicensedSoftware = true;
        }
        return isLicensedSoftware;
    }

    public void setLicenseStatus(boolean licenseStatus) {
        this.licenseStatus = licenseStatus;
    }

    public String getHardwareID() {
        SystemInfo sysInfo = new SystemInfo();
        HardwareAbstractionLayer hardwareLayer = sysInfo.getHardware();
        ComputerSystem csys = hardwareLayer.getComputerSystem();
        return csys.getSerialNumber();
    }


}
