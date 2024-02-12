# Watchdog - Docker Monitoring Application v0.1

## Table of Contents
- [Promotional Introduction To Watchdog](#promotional-introduction-to-watchdog)
- [Technical Description](#a-more-technical-description)
- [Youtube Ad](#roll-the-film-)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Package/Files Descriptions](#packagefiles-descriptions)
- [REST Endpoints](#rest-endpoints)
- [Libraries and Versions](#libraries-and-versions)
- [Visuals](#visuals)
- [Authors](#authors)
- [Contributing](#contributing)

## Promotional Introduction To Watchdog

Embark on a seamless journey through the world of Docker and Kubernetes management with Watchdog – your loyal companion, tirelessly watching your back to simplify and optimize your containerized applications. 🚀

🔭 Keep a Vigilant Eye:
Watchdog provides real-time monitoring, keeping a vigilant eye on the health and performance of your Docker and Kubernetes cluster. Stay ahead of potential issues with live updates and detailed metrics, ensuring a smooth and uninterrupted development experience.

🐕 Watchdog Features:

- Container Caretaker: Manage your containers effortlessly with Watchdog's intuitive interface, overseeing volumes, images, and all interactions between them. It's your go-to companion for simplifying complex tasks and ensuring a harmonious container ecosystem.
- Volume Patrol: Watchdog goes beyond the surface, keeping a keen eye on volumes to facilitate seamless data management. Effortlessly manage and organize your volumes to ensure your containerized applications run smoothly.
- Image Inspector: Explore and manage your container images with ease. Watchdog allows you to interact with images effortlessly, ensuring that your applications are built on a solid foundation.
- Kubernetes Support: Watchdog extends its loyalty to Kubernetes, offering support for seamless interaction. Manage your Kubernetes clusters with the same ease and confidence, as Watchdog becomes your trusted guide in the world of orchestration.
- Tail-Wagging Interface: Experience the joy of a user-friendly design that tailors to both beginners and seasoned users, making interaction a walk in the park.


## Roll the Film! 🎥

We've put together a video that showcases our application in action. It's not just descriptive, it's downright entertaining! We promise it's worth your time. Click [here](https://www.youtube.com/watch?v=_BFrwwLwNy8)


## A More Technical Description

Watchdog is a desktop Spring Boot application designed to monitor your docker cluster and let you interact with your containers and images in an intuitive way. In this version the extracted data is stored in a local mySQL database. It uses callbacks to get data from your docker deamon and it dynamically creates data-tranfer-objects that are pushed to the database. After, a springboot application uses the REST protocol to extract the data-tranfer-objects back into the desktop application and display them in a user-friendly way while also giving the user the ability to interact with his containers and images using designated endpoints.

## Getting Started

To view instructions on how to run Watchdog for the first time Click [here](docs/gettingStarted.md)


## Project Structure

```
.
├── LICENSE
├── README.md
├── docs
│   ├── gettingStarted.md
│   └── visuals.md
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── gr
│   │   │       └── aueb
│   │   │           └── dmst
│   │   │               └── dockerWatchdog
│   │   │                   ├── Main.java
│   │   │                   ├── api
│   │   │                   │   ├── WebApplication.java
│   │   │                   │   ├── controllers
│   │   │                   │   │   └── ApiController.java
│   │   │                   │   ├── entities
│   │   │                   │   │   ├── Image.java
│   │   │                   │   │   ├── Instance.java
│   │   │                   │   │   ├── InstanceId.java
│   │   │                   │   │   ├── Metric.java
│   │   │                   │   │   └── Volume.java
│   │   │                   │   ├── repositories
│   │   │                   │   │   ├── ImagesRepository.java
│   │   │                   │   │   ├── InstancesRepository.java
│   │   │                   │   │   ├── MetricsRepository.java
│   │   │                   │   │   └── VolumesRepository.java
│   │   │                   │   └── services
│   │   │                   │       └── ApiService.java
│   │   │                   ├── exceptions
│   │   │                   │   ├── ChartException.java
│   │   │                   │   ├── ContainerActionFailedException.java
│   │   │                   │   ├── ContainerCreationException.java
│   │   │                   │   ├── ContainerNameConflictException.java
│   │   │                   │   ├── ContainerNotFoundException.java
│   │   │                   │   ├── ContainerNotModifiedException.java
│   │   │                   │   ├── ContainerRunningException.java
│   │   │                   │   ├── DatabaseOperationException.java
│   │   │                   │   ├── EventHandlingException.java
│   │   │                   │   ├── ImageActionException.java
│   │   │                   │   ├── ImageNotFoundException.java
│   │   │                   │   ├── ListFillingException.java
│   │   │                   │   ├── LiveStatsException.java
│   │   │                   │   ├── VolumeFetchException.java
│   │   │                   │   └── VolumeNotModifiedException.java
│   │   │                   ├── gui
│   │   │                   │   ├── GuiApplication.java
│   │   │                   │   ├── fxcontrollers
│   │   │                   │   │   ├── ComposeController.java
│   │   │                   │   │   ├── ContainersController.java
│   │   │                   │   │   ├── GraphicsController.java
│   │   │                   │   │   ├── ImagesController.java
│   │   │                   │   │   ├── IndividualContainerController.java
│   │   │                   │   │   ├── KubernetesController.java
│   │   │                   │   │   ├── PullImageController.java
│   │   │                   │   │   ├── UserController.java
│   │   │                   │   │   └── VolumesController.java
│   │   │                   │   └── models
│   │   │                   │       ├── DeploymentScene.java
│   │   │                   │       ├── ImageScene.java
│   │   │                   │       ├── InstanceScene.java
│   │   │                   │       ├── PodScene.java
│   │   │                   │       ├── SearchResultScene.java
│   │   │                   │       ├── ServiceScene.java
│   │   │                   │       ├── StatefulSetScene.java
│   │   │                   │       └── VolumeScene.java
│   │   │                   ├── models
│   │   │                   │   ├── MyImage.java
│   │   │                   │   ├── MyInstance.java
│   │   │                   │   └── MyVolume.java
│   │   │                   └── threads
│   │   │                       ├── DatabaseThread.java
│   │   │                       ├── ExecutorThread.java
│   │   │                       └── MonitorThread.java
│   │   └── resources
│   │       ├── application.properties
│   │       ├── composeScene.fxml
│   │       ├── containersScene.fxml
│   │       ├── fonts
│   │       ├── graphicsScene.fxml
│   │       ├── images
│   │       │   ├── Box.png
│   │       │   ├── BoxClick.png
│   │       │   ├── BoxHover.png
│   │       │   ├── CheckBox.png
│   │       │   ├── CheckBoxClick.png
│   │       │   ├── CheckBoxHover.png
│   │       │   ├── Copy.png
│   │       │   ├── CopyClick.png
│   │       │   ├── Pause.png
│   │       │   ├── PauseClick .png
│   │       │   ├── PauseHover.png
│   │       │   ├── UML.jpg
│   │       │   ├── back.png
│   │       │   ├── backClick.png
│   │       │   ├── backHover.png
│   │       │   ├── background.png
│   │       │   ├── bin.png
│   │       │   ├── binClick.png
│   │       │   ├── binHover.png
│   │       │   ├── binRed.png
│   │       │   ├── blur.jpg
│   │       │   ├── blurred.jpg
│   │       │   ├── container.png
│   │       │   ├── containerGrey.png
│   │       │   ├── containersPanel.png
│   │       │   ├── copyHover.png
│   │       │   ├── create.png
│   │       │   ├── createHover.png
│   │       │   ├── docker.png
│   │       │   ├── exraPanel.png
│   │       │   ├── graphics.png
│   │       │   ├── graphicsGrey.png
│   │       │   ├── graphicsPanel.png
│   │       │   ├── hills.png
│   │       │   ├── image.png
│   │       │   ├── imageGrey.png
│   │       │   ├── imagesPanel.png
│   │       │   ├── individualPanel.png
│   │       │   ├── kubernetes.png
│   │       │   ├── kubernetesGrey.png
│   │       │   ├── loading.gif
│   │       │   ├── play.png
│   │       │   ├── playClick.png
│   │       │   ├── playHover.png
│   │       │   ├── search.png
│   │       │   ├── shoppingCart.png
│   │       │   ├── stop.png
│   │       │   ├── stopClick.png
│   │       │   ├── stopHover.png
│   │       │   ├── stopRed.png
│   │       │   ├── user.png
│   │       │   ├── volumes.png
│   │       │   ├── volumesGrey.png
│   │       │   ├── volumesPanel.png
│   │       │   ├── watchdog.png
│   │       │   └── watchdogRed.png
│   │       ├── imagesScene.fxml
│   │       ├── individualContainerScene.fxml
│   │       ├── kubernetesScene.fxml
│   │       ├── pullImageScene.fxml
│   │       ├── styles
│   │       │   ├── buttons.css
│   │       │   ├── chartColor.css
│   │       │   ├── containersHover.css
│   │       │   ├── curvedPane.css
│   │       │   ├── darkButtons.css
│   │       │   ├── graphicsHover.css
│   │       │   ├── hoverPane.css
│   │       │   ├── hoverRunningPane.css
│   │       │   ├── imagesHover.css
│   │       │   ├── kubernetesHover.css
│   │       │   ├── loginButton.css
│   │       │   ├── pieChart.css
│   │       │   ├── redButton.css
│   │       │   ├── scrollPane.css
│   │       │   ├── splitPane.css
│   │       │   ├── styles.css
│   │       │   ├── textArea.css
│   │       │   └── volumesHover.css
│   │       ├── userScene.fxml
│   │       └── volumesScene.fxml
│   └── test
└── target
    


```

## Package/Files Descriptions

Watchdog is structured into several packages, each with a specific role in the application:

- **`gr.aueb.dmst.dockerWatchdog`**:
    - **`Main.java`**: The main entry point of the application.

- **`gui`**:
    - Contains the class that boots up the desktop app and all fxcontrollers and models that support it.

- **`api`**:
    - Contains all related 

- **`exceptions`**:
    - Contains custom exception classes .

- **`models`**:
    - Contains our custom My... models that helps us have full access and control on docker components.

- **`threads`**:
    - Contains the 3 threads that are constantly running in the applicaton.

- **`resources`**:
    - Contains application properties and resources.

- **`test`**:
    - Test package containing test cases.

## REST Endpoints

| Endpoint                            | HTTP Method | Description                                              |
|-------------------------------------|-------------|----------------------------------------------------------|
| `/images`                           | GET         | Retrieves all pulled Docker images                       |
| `/volumes`                          | GET         | Retrieves all Docker volumes                             |
| `/images/create/{imageName}`        | POST        | Creates a container for the specified image              |
| `/containers/{containerId}/start`   | POST        | Starts the specified container                           |
| `/containers/{containerId}/stop`    | POST        | Stops the specified container                            |
| `/containers/{containerId}/restart` | POST        | Restarts the specified container                         |
| `/containers/{containerId}/delete`  | POST        | Deletes the specified container                          |
| `/containers/{containerId}/rename`  | POST        | Renames the specified container                          |
| `/containers/{containerId}/pause`   | POST        | Pauses the specified container                           |
| `/containers/{containerId}/unpause` | POST        | Unpauses the specified container                         |
| `/containers/instances`             | GET         | Retrieves all the containers in your cluster             |
| `/containers/metrics`               | GET         | Retrieves metrics for a given timestamp                  |
| `/containers/lastMetricId`          | GET         | Returns the metric Id with the highest value             |
| `/containers/{containerId}/info`    | GET         | Checks if a container exists (for testing)               |
| `/containers/startAll/{imageName}`  | POST        | Starts all containers that belong to the specified image |
| `/containers/stopAll/{imageName}`   | POST        | Stops all containers that belong to the specified image  |
| `/images/pull/{imageName}`          | POST        | Pulls an image                                           |
| `/images/remove/{imageName}`        | POST        | Removes the image with the specified name                |

## Libraries and Versions

| Library                                                 | Version     | Scope   |
|---------------------------------------------------------|-------------|---------|
| org.springframework.boot:spring-boot-starter-web        | 3.2.0       | compile |
| javax.annotation:javax.annotation-api                   | 1.3.2       | compile |
| com.github.docker-java:docker-java                      | 3.3.4       | compile |
| junit:junit                                             | 4.13.2      | test    |
| mysql:mysql-connector-java                              | 8.0.28      | compile |
| ch.qos.logback:logback-classic                          | 1.4.12      | compile |
| org.springframework.boot:spring-boot-starter-data-jpa   | 3.2.0       | compile |
| javax.xml.bind:jaxb-api                                 | 2.3.1       | compile |
| org.openjfx:javafx-base                                 | 21          | compile |
| org.openjfx:javafx-controls                             | 21          | compile |
| org.openjfx:javafx-graphics                             | 21          | compile |
| org.openjfx:javafx-fxml                                 | 21          | compile |
| org.glassfish.jaxb:jaxb-runtime                         | 2.3.1       | compile |
| com.squareup.okhttp3:okhttp                             | 4.12.0      | compile |
| io.kubernetes:client-java                               | 19.0.0      | compile |
| org.yaml:snakeyaml                                      | 2.2         | compile |
| org.apache.logging.log4j:log4j-core                     | 3.0.0-beta1 | compile |
| org.testng:testng                                       | 7.9.0       | compile |
| org.junit.jupiter:junit-jupiter                         | 5.10.0      | test    |
| org.mockito:mockito-core                                | 5.7.0       | test    |
| org.springframework.boot:spring-boot-test-autoconfigure | 3.2.0       | test    |
| org.springframework:spring-test                         | 6.1.1       | test    |
| org.testfx:testfx-junit                                 | 4.0.1-alpha | test    |

## Visuals

To view our Code UML and Visuals click [here](docs/visuals.md)


## Authors:

[Ioannis Tampakis](mailto:giannistampakis04@gmail.com),
[Thodoris Zarkalis](mailto:zarkalhs@gmail.com),
[Iasonas Passam](mailto:),
[Dionysis Glytsos](mailto:)

Contributors:

## Contributing

Pull requests are welcome. For major changes, please open an issue first
to discuss what you would like to change.

Please make sure to update tests as appropriate.
