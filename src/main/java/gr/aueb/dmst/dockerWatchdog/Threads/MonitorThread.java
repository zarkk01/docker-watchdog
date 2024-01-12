package gr.aueb.dmst.dockerWatchdog.Threads;

import java.io.Closeable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import com.github.dockerjava.core.command.EventsResultCallback;

import gr.aueb.dmst.dockerWatchdog.Exceptions.DatabaseOperationException;
import gr.aueb.dmst.dockerWatchdog.Exceptions.EventHandlingException;
import gr.aueb.dmst.dockerWatchdog.Exceptions.ListFillingException;
import gr.aueb.dmst.dockerWatchdog.Main;
import gr.aueb.dmst.dockerWatchdog.Models.MyImage;
import gr.aueb.dmst.dockerWatchdog.Models.MyInstance;
import gr.aueb.dmst.dockerWatchdog.Models.MyVolume;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gr.aueb.dmst.dockerWatchdog.Exceptions.LiveStatsException;

public class MonitorThread implements Runnable {
    private static final Logger logger = LogManager.getLogger(MonitorThread.class);

    @Override
    public void run() {
        try {
            fillLists();
            liveMeasure(null);
        } catch (ListFillingException | LiveStatsException | DatabaseOperationException e) {
            logger.error(e.getMessage());
        }
        startListening();
    }

    public void fillLists() throws ListFillingException, DatabaseOperationException {
        try {
            List<Image> images = Main.dockerClient.listImagesCmd().withShowAll(true).exec();
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
        } catch (Exception e) {
            throw new ListFillingException("Images");
        }

        try {
            List<Container> containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();
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
        } catch (Exception e) {
            throw new ListFillingException("Containers");
        }

        try {
            List<InspectVolumeResponse> volumes = Main.dockerClient.listVolumesCmd().exec().getVolumes();
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
        } catch (Exception e) {
            throw new ListFillingException("Volumes");
        }
        DatabaseThread.createAllTables();
    }

    public static void liveMeasure(String containerId) throws LiveStatsException {
        List<Container> containers = Main.dockerClient.listContainersCmd().withShowAll(true).exec();

        for (Container container : containers) {
            if (containerId == null || container.getId().equals(containerId)) {
                AsyncDockerCmd<StatsCmd, Statistics> asyncStatsCmd = Main.dockerClient.statsCmd(container.getId());
                try {
                    asyncStatsCmd.exec(new CustomResultCallback(container.getId(), asyncStatsCmd));
                } catch (Exception e) {
                    throw new LiveStatsException();
                }
            }
        }
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
                            DatabaseThread.keepTrackOfInstances();
                            DatabaseThread.keepTrackOfImages();
                        } catch (LiveStatsException | EventHandlingException | DatabaseOperationException e) {
                            logger.error(e.getMessage());
                        }
                        break;
                    case IMAGE:
                        try {
                            handleImageEvent(eventAction, id);
                        } catch (EventHandlingException e) {
                            logger.error(e.getMessage());
                        }
                        break;
                    case VOLUME:
                        try {
                            handleVolumeEvent(eventAction, id);
                        } catch (EventHandlingException e) {
                            logger.error(e.getMessage());
                        }
                        break;
                }
            }
        });
    }

    public void handleContainerEvent(String eventAction, String containerId, Event event) throws LiveStatsException, EventHandlingException {
        MyInstance instance = MyInstance.getInstanceByid(containerId);
        try {
            switch (eventAction) {
                case "start":
                case "unpause":
                    updateContainerStatus(instance, "running");
                    break;
                case "stop":
                case "die":
                    updateContainerStatus(instance, "exited");
                    break;
                case "pause":
                    updateContainerStatus(instance, "paused");
                    break;
                case "rename":
                    instance.setName(event.getActor().getAttributes().get("name"));
                    break;
                case "destroy":
                    handleContainerDestroyEvent(instance);
                    break;
                case "create":
                    handleContainerCreateEvent(containerId);
                    break;
            }
        } catch (Exception e) {
            throw new EventHandlingException("container");
        }
    }

    private void updateContainerStatus(MyInstance instance, String status) {
        instance.setStatus(status);
    }

    private void handleContainerDestroyEvent(MyInstance instance) throws DatabaseOperationException {
        Main.myInstances.remove(instance);
        for(MyInstance checkingInstance : Main.myInstances){
            if(checkingInstance.getImage().equals(instance.getImage())){
                MyImage imageToSetUnused = MyImage.getImageByName(instance.getImage());
                imageToSetUnused.setStatus("Unused");
            }
        }

        for(String volumeName : instance.getVolumes()){
            MyVolume vol = MyVolume.getVolumeByName(volumeName);
            if(vol != null){vol.removeContainerNameUsing(instance.getName());}
        }
        DatabaseThread.keepTrackOfVolumes();
    }

    private void handleContainerCreateEvent(String containerId) throws LiveStatsException, DatabaseOperationException {
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
        liveMeasure(newInstance.getId());
    }

    private void handleImageEvent(String eventAction, String imageName) throws EventHandlingException {
        try {
            switch (eventAction) {
                case "pull":
                    handleImagePullEvent(imageName);
                    break;
                case "delete":
                case "untag":
                    handleImageDeleteEvent(imageName);
                    break;
            }
        } catch (EventHandlingException e) {
            throw new EventHandlingException("image");
        }
    }

    private void handleImagePullEvent(String imageName) throws EventHandlingException {
        try {
            InspectImageResponse image = Main.dockerClient.inspectImageCmd(imageName).exec();
            MyImage newImage = new MyImage(
                    image.getRepoTags().get(0),
                    image.getId(),
                    image.getSize(),
                    getImageUsageStatus(image.getRepoTags().get(0))
            );
            Main.myImages.add(newImage);
            DatabaseThread.keepTrackOfImages();
        } catch (Exception e) {
            throw new EventHandlingException("image");
        }
    }

    private void handleImageDeleteEvent(String imageName) throws EventHandlingException {
        try {
            MyImage imageToRemove = MyImage.getImageByID(imageName);
            if (imageToRemove != null) {
                DatabaseThread.deleteImage(imageToRemove);
                Main.myImages.remove(imageToRemove);
            }
        } catch (Exception e) {
            throw new EventHandlingException("image");
        }
    }

    public void handleVolumeEvent(String eventAction, String volumeName) throws EventHandlingException {
        try {
            switch (eventAction) {
                case "create":
                    handleVolumeCreateEvent(volumeName);
                    break;
                case "destroy":
                    handleVolumeDestroyEvent(volumeName);
                    break;
            }
        } catch (Exception e) {
            throw new EventHandlingException("volume");
        }
    }

    private void handleVolumeCreateEvent(String volumeName) throws EventHandlingException {
        try {
            InspectVolumeResponse volume = Main.dockerClient.inspectVolumeCmd(volumeName).exec();
            MyVolume newVolume = new MyVolume(
                    volume.getName(),
                    volume.getDriver(),
                    volume.getMountpoint(),
                    new ArrayList<String>()
            );
            for(Container container : Main.dockerClient.listContainersCmd().withShowAll(true).exec()){
                for(ContainerMount volumeMount : container.getMounts()){
                    if(volumeMount.getName() != null && volumeMount.getName().equals(volume.getName())){
                        newVolume.addContainerNameUsing(container.getNames()[0]);
                    }
                }
            }
            Main.myVolumes.add(newVolume);
            DatabaseThread.keepTrackOfVolumes();
        } catch (Exception e) {
            throw new EventHandlingException("volume");
        }
    }

    private void handleVolumeDestroyEvent(String volumeName) throws EventHandlingException {
        try {
            MyVolume volumeToRemove = MyVolume.getVolumeByName(volumeName);
            if (volumeToRemove != null) {
                DatabaseThread.deleteVolume(volumeToRemove);
                Main.myVolumes.remove(volumeToRemove);
            } else {
                throw new EventHandlingException("null volume");
            }
        } catch (Exception e) {
            throw new EventHandlingException("volume");
        }
    }

    public String getImageUsageStatus(String name){
        for(Container container : Main.dockerClient.listContainersCmd().withShowAll(true).exec()){
            if(container.getImage().equals(name)){
                return "In use";
            }
        }
        return "Unused";
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
