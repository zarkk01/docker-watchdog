package gr.aueb.dmst.dockerWatchdog;

public class monitorThread implements Runnable {
    @Override
    public void run(){
        System.out.println("Monitor thread is running");
    }
}
