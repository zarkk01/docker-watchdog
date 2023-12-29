package gr.aueb.dmst.dockerWatchdog;

import java.util.ArrayList;
import java.util.Map;

public class MyVolume {

    private final String name;
    private final String driver;
    private final String mountpoint;
    private final Map<String,String> options;
    private ArrayList<String> containerNamesUsing;

    public MyVolume(String name, String driver, String mountpoint, Map<String,String> options,ArrayList<String> containerNamesUsing) {
        this.name = name;
        this.driver = driver;
        this.mountpoint = mountpoint;
        this.options = options;
        this.containerNamesUsing = containerNamesUsing;
    }

    @Override
    public String toString() {
        return "Name = " + name + " , Driver = " + driver + " , Mountpoint = " + mountpoint + " , Options = " + options;
    }

    public String getName() {
        return name;
    }

    public String getDriver() {
        return driver;
    }

    public String getMountpoint() {
        return mountpoint;
    }

    public Map<String,String> getOptions() {
        return options;
    }

    public ArrayList<String> getContainerNamesUsing() {
        return containerNamesUsing;
    }

    public void setContainerNamesUsing(ArrayList<String> containerNamesUsing) {
        this.containerNamesUsing = containerNamesUsing;
    }
}