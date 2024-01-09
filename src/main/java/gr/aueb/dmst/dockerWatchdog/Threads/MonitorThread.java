package gr.aueb.dmst.dockerWatchdog.Threads;

import java.io.Closeable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import com.github.dockerjava.core.command.EventsResultCallback;

import gr.aueb.dmst.dockerWatchdog.Main;
import gr.aueb.dmst.dockerWatchdog.Models.MyImage;
import gr.aueb.dmst.dockerWatchdog.Models.MyInstance;
import gr.aueb.dmst.dockerWatchdog.Models.MyVolume;

public class MonitorThread implements Runnable {

    @Override
    public void run() {
        try {
            fillLists();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in fill lists.");
        }
        liveMeasure();
        startListening();
    }

    public void startListening() {
        Main.dockerClient.eventsCmd().exec(new EventsResultCallback() {
            @Override
            public void onNext(Event event) {

                EventType eventType = event.getType();
                String eventAction = event.getAction();
                String id = event.getActor().getId();

                switch (eventType) {
                    case CONTAINER:
                        try {
                            handleContainerEvent(eventAction, id,event);
                            DatabaseThread.keepTrackOfImages();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case IMAGE:
                        try {
                            handleImageEvent(eventAction, id);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case VOLUME:
                        handleVolumeEvent(eventAction, id);
                        break;
                }
            }
        });
    }

    public void handleContainerEvent(String eventAction, String containerId, Event event) throws SQLException {
        MyInstance instance = MyInstance.getInstanceByid(containerId);
        switch (eventAction) {
            case "start":
            case "unpause":
                if (instance != null) {
                    instance.setStatus("running");
                }
                DatabaseThread.keepTrackOfInstances();
                break;
            case "stop":
            case "die":
                if (instance != null) {
                    instance.setStatus("exited");
                }
                DatabaseThread.keepTrackOfInstances();
                break;
            case "pause":
                if (instance != null) {
                    instance.setStatus("paused");
                }
                DatabaseThread.keepTrackOfInstances();
                break;
            case "rename":
                if (instance != null) {
                    instance.setName(event.getActor().getAttributes().get("name"));
                }
                DatabaseThread.keepTrackOfInstances();
                break;
            case "destroy":
                boolean isThere = false;
                if (instance != null) {
                    Main.myInstances.remove(instance);
                    for(MyInstance inst : Main.myInstances){
                        if(inst.getImage().equals(instance.getImage())){
                            isThere = true;
                        }
                    }
                    if(!isThere){
                        MyImage imageToSetUnused = MyImage.getImageByName(instance.getImage());
                        imageToSetUnused.setStatus("Unused");
                    }
                    DatabaseThread.deleteInstance(instance);

                    for(String volumeName : instance.getVolumes()){
                        MyVolume vol = MyVolume.getVolumeByName(volumeName);
                        if(vol != null){vol.removeContainerNameUsing(instance.getName());}
                    }
                    DatabaseThread.keepTrackOfVolumes();
                }
                break;
            case "create":
                InspectContainerResponse container = Main.dockerClient.inspectContainerCmd(containerId).exec();

                MyInstance newInstance = new MyInstance(
                        container.getId(),
                        container.getName(),
                        MyImage.getImageByID(container.getImageId()).getName(),
                        container.getState().getStatus(),
                        0, 0, 0, 0, 0,
                        new ArrayList<String>(),
                        container.getNetworkSettings().getIpAddress(),container.getNetworkSettings().getGateway(),
                        container.getNetworkSettings().getIpPrefixLen()
                );

                if(container.getMounts() != null){
                    for(InspectContainerResponse.Mount volumeName : container.getMounts()){
                        if(volumeName.getName() == null){continue;}
                        newInstance.addVolume(volumeName.getName());
                        MyVolume vol = MyVolume.getVolumeByName(volumeName.getName());
                        assert vol != null;
                        vol.addContainerNameUsing(newInstance.getName());
                        DatabaseThread.keepTrackOfVolumes();
                    }
                }

                for(MyImage image : Main.myImages) {
                    if(newInstance.getImage().equals(image.getName())){
                        image.setStatus("In use");
                    }
                }

                Main.myInstances.add(newInstance);
                liveMeasureForNewContainer(newInstance.getId());
                DatabaseThread.keepTrackOfInstances();

                break;
        }
    }

    private void handleImageEvent(String eventAction, String imageName) throws SQLException {
        switch (eventAction) {
            case "pull":
                // Add the new image to the list
                InspectImageResponse image = Main.dockerClient.inspectImageCmd(imageName).exec();
                boolean isThere = false;
                for(MyImage ima : Main.myImages){
                    if(ima.getId().equals(image.getId())){
                        isThere = true;
                    }
                }
                if (!isThere) {
                    MyImage newImage = new MyImage(
                            image.getRepoTags().get(0),
                            image.getId(),
                            image.getSize(),
                            getImageUsageStatus(image.getRepoTags().get(0))
                    );
                    Main.myImages.add(newImage);
                    DatabaseThread.keepTrackOfImages();
                }
                break;
            case "delete":
            case "untag":
                MyImage imageToRemove = MyImage.getImageByID(imageName);
                if (imageToRemove != null) {
                    DatabaseThread.deleteImage(imageToRemove);
                    Main.myImages.remove(imageToRemove);
                }
                break;
        }
    }

    public void handleVolumeEvent(String eventAction, String name){
        switch (eventAction) {
            case "create":
                // Add the new volume to the list
                InspectVolumeResponse volume = Main.dockerClient.inspectVolumeCmd(name).exec();
                MyVolume newVolume = new MyVolume(
                        volume.getName(),
                        volume.getDriver(),
                        volume.getMountpoint(),
                        new ArrayList<String>()
                );
                for(Container container : Main.dockerClient.listContainersCmd().withShowAll(true).exec()){
                    for(ContainerMount volumeName : container.getMounts()){
                        if(volumeName.equals(volume.getName())){
                            newVolume.addContainerNameUsing(container.getNames()[0]);
                        }
                    }
                }
                Main.myVolumes.add(newVolume);
                DatabaseThread.keepTrackOfVolumes();
                break;
            case "destroy":
                // Remove the corresponding volume from the list
                MyVolume volumeToRemove = MyVolume.getVolumeByName(name);
                if (volumeToRemove != null) {
                    DatabaseThread.deleteVolume(volumeToRemove);
                    Main.myVolumes.remove(volumeToRemove);
                }
                DatabaseThread.deleteVolume(volumeToRemove);
                break;
        }
    }

    public void fillLists() {
        List<Container> containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();
        List<Image> images = Main.dockerClient.listImagesCmd().withShowAll(true).exec();
        List<InspectVolumeResponse> volumes = Main.dockerClient.listVolumesCmd().exec().getVolumes();

        for (Image image : images) {
            InspectImageResponse imageInfo = Main.dockerClient.inspectImageCmd(image.getId()).exec();
            MyImage newImage = new MyImage(
                    imageInfo.getRepoTags().get(0),
                    imageInfo.getId(),
                    imageInfo.getSize(),
                    getImageUsageStatus(imageInfo.getRepoTags().get(0))
            );
            Main.myImages.add(newImage);
        }

        for (Container container : containers) {
            InspectContainerResponse containerInfo = Main.dockerClient.inspectContainerCmd(container.getId()).exec();
            MyInstance newInstance = new MyInstance(
                    containerInfo.getId(),
                    containerInfo.getName(),
                    MyImage.getImageByID(containerInfo.getImageId()).getName(),
                    containerInfo.getState().getStatus(),
                    0, 0, 0, 0, 0,
                     new ArrayList<String>()
                    ,containerInfo.getNetworkSettings().getIpAddress(),
                    containerInfo.getNetworkSettings().getGateway(),
                    containerInfo.getNetworkSettings().getIpPrefixLen()
            );

            if(containerInfo.getMounts() != null){
                for(InspectContainerResponse.Mount volumeName : containerInfo.getMounts()){
                    newInstance.addVolume(volumeName.getName());
                }
            }
            Main.myInstances.add(newInstance);
        }

        for (InspectVolumeResponse volume : volumes) {
            MyVolume newVolume = new MyVolume(
                    volume.getName(),
                    volume.getDriver(),
                    volume.getMountpoint(),
                    new ArrayList<String>()
            );
            for(Container container : Main.dockerClient.listContainersCmd().withShowAll(true).exec()){
                for(ContainerMount volumeName : container.getMounts()){
                    if(volumeName.getName() == null){continue;}
                    if(volumeName.getName().equals(volume.getName())){
                        newVolume.addContainerNameUsing(container.getNames()[0]);
                    }
                }
            }
            Main.myVolumes.add(newVolume);
        }
        DatabaseThread.createAllTables();
    }

    public String getImageUsageStatus(String name){
        for(Container container : Main.dockerClient.listContainersCmd().withShowAll(true).exec()){
            if(container.getImage().equals(name)){
                return "In use";
            }
        }
        return "Unused";
    }

    public static void liveMeasure() {
        List < Container > containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();

        for (Container container: containers) {
            String id = container.getId();
            AsyncDockerCmd<StatsCmd, Statistics > asyncStatsCmd = Main.dockerClient.statsCmd(id);
            try {
                asyncStatsCmd.exec(new CustomResultCallback(id, asyncStatsCmd));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void liveMeasureForNewContainer(String id) {
        AsyncDockerCmd < StatsCmd, Statistics > asyncStatsCmd = Main.dockerClient.statsCmd(id);
        try {
            asyncStatsCmd.exec(new CustomResultCallback(id, asyncStatsCmd));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class CustomResultCallback extends ResultCallbackTemplate<CustomResultCallback, Statistics > {

        public String id;
        private AsyncDockerCmd < StatsCmd,Statistics > asyncStatsCmd;

        public CustomResultCallback(String id, AsyncDockerCmd < StatsCmd, Statistics > asyncStatsCmd) {
            this.id = id;
            this.asyncStatsCmd = asyncStatsCmd;

        }

        @Override
        public void onNext(Statistics stats) {
            long cpuUsage = getCpuUsageInNanos(stats);

            if (MyInstance.getInstanceByid(id) != null) {
                if (cpuUsage != 0) {
                    MyInstance.getInstanceByid(id).setCpuUsage((double) cpuUsage / 1_000_000_000);
                } else {
                    MyInstance.getInstanceByid(id).setCpuUsage(0.0);
                }
            }

            Long usage = stats.getMemoryStats().getUsage();
            long memoryUsage = (usage != null) ? usage / (1024 * 1024) : 0L;
            if (MyInstance.getInstanceByid(id) != null) {
                MyInstance.getInstanceByid(id).setMemoryUsage(memoryUsage);
            }

            Long pids = stats.getPidsStats().getCurrent();
            if (MyInstance.getInstanceByid(id) != null) {
                if (pids != null) {
                    MyInstance.getInstanceByid(id).setPids(pids);
                } else {
                    MyInstance.getInstanceByid(id).setPids(0);
                }
            }

            List < BlkioStatEntry > ioServiceBytes = stats.getBlkioStats().getIoServiceBytesRecursive();
            Long readBytes = null;
            if (ioServiceBytes != null) {
                readBytes = getIoServiceBytesValue(ioServiceBytes, "Read");
            }
            Long writeBytes = null;
            if (ioServiceBytes != null) {
                writeBytes = getIoServiceBytesValue(ioServiceBytes, "Write");
            }
            if (MyInstance.getInstanceByid(id) != null) {
                MyInstance.getInstanceByid(id).setBlockI(readBytes != null ? (double) readBytes / (1024 * 1024) : 0.0);
                MyInstance.getInstanceByid(id).setBlockO(writeBytes != null ? (double) writeBytes / (1024 * 1024) : 0.0);
            }
        }

        private Long getCpuUsageInNanos(Statistics stats) {
            Long cpuDelta = stats.getCpuStats().getCpuUsage().getTotalUsage() -
                    stats.getPreCpuStats().getCpuUsage().getTotalUsage();
            return cpuDelta >= 0 ? cpuDelta : 0L;
        }

        private Long getIoServiceBytesValue(List < BlkioStatEntry > ioServiceBytes, String type) {
            for (BlkioStatEntry entry: ioServiceBytes) {
                if (entry.getOp().equalsIgnoreCase(type)) {
                    return entry.getValue();
                }
            }
            return null;
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onComplete() {
            asyncStatsCmd.close();
        }

        @Override
        public void onStart(Closeable closeable) {}
    }
}
