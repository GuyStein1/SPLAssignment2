
# GurionRock Pro Max Ultra Over 9000
A concurrent Java simulation of a robot's perception and mapping system.

## SPL Assignment 2
**GurionRock Pro Max Ultra Over 9000 Vacuum Robot - Perception and Mapping System**

## Overview
This project simulates a vacuum robot’s perception and mapping system using a custom-built Java MicroService Framework.
It implements concurrent and synchronized operations between different services, mimicking real-world robot components like Camera, LiDAR, GPS, and IMU sensors.

The system collects, processes, and fuses sensor data to build a global map while handling timing, errors, and system termination.

The project was developed following the guidelines of Assignment 2 in the SPL course (Fall 2024), at Ben-Gurion University.

---

## Project Structure

### Main Components
- **MicroService Framework:**
  - `MessageBusImpl`: Thread-safe singleton handling event and broadcast communication.
  - `MicroService`: Abstract base class for services with automatic message dispatching and termination support.
    
- **Services:**
  - `TimeService`: Global clock, broadcasts `TickBroadcast`.
  - `CameraService`: Sends `DetectObjectsEvent` based on detections.
  - `LiDarService`: Processes `DetectObjectsEvent` and sends `TrackedObjectsEvent`.
  - `FusionSlamService`: Builds and updates the global map using `TrackedObjectsEvent` and `PoseEvent`.
  - `PoseService`: Provides the robot’s current pose through `PoseEvent`.

- **Events and Broadcasts:**
  - Events: `DetectObjectsEvent`, `TrackedObjectsEvent`, `PoseEvent`
  - Broadcasts: `TickBroadcast`, `TerminatedBroadcast`, `CrashedBroadcast`

- **Support Classes:**
  - `CrashOutputManager`: Handles crash recovery and output file creation.
  - `StatisticalFolder`: Tracks statistics like system runtime, number of detections, tracks, and landmarks.
  - `FusionSlam`: Manages the internal SLAM mapping process.
  - Other data objects: `Camera`, `LiDarWorkerTracker`, `StampedDetectedObjects`, `TrackedObject`, `Pose`, `CloudPoint`, `LandMark`.

---

## Execution Flow

1. **Initialization:**
   - Parses input JSON configuration using GSON.
   - Spawns all MicroServices and registers them.
   - Starts the simulation with `TimeService`.

2. **Main Loop:**
   - Cameras and LiDARs synchronize actions based on ticks.
   - Cameras detect objects, LiDARs track them, and FusionSlam builds landmarks.
   - PoseService updates the robot’s pose at every tick.
   - Data fusion transforms and updates the global map.

3. **Termination:**
   - Normal termination after all ticks or all sensors finish.
   - Immediate crash termination if a sensor detects an error.

---

## Input and Output

### Input Files
- **Configuration JSON:** Defines cameras, LiDARs, file paths, and simulation parameters.
- **Camera Data JSON:** Lists of detected objects or errors over time.
- **LiDAR Data JSON:** Cloud points captured by LiDARs.
- **Pose Data JSON:** Robot poses over time.

### Output File
- `output_file.json`: Contains system runtime statistics, a list of mapped landmarks, and, in case of crash, error information, last frames, and robot poses.

---

## Technologies
- Java 8 (Threads, Synchronization, Generics)
- GSON (for JSON parsing and serialization)
- Maven (for building and dependency management)
- JUnit (for unit testing)

---

## Build and Run Instructions

1. Ensure Maven is installed.
2. Navigate to the project root.
3. Build the project:
```bash
mvn clean install
```
4. Run the program:
```bash
java -jar target/assignment2.jar path/to/configuration.json
```
5. The output file will be created in the same directory as the input configuration file.

---

## Important Notes

- Ensures proper termination of all services to prevent deadlocks.
- Services react to events and broadcasts asynchronously.
- Any sensor error ("ERROR" ID in input) causes an immediate crash handling procedure.
- Round-robin dispatching is used when multiple services are subscribed to the same event.
- FusionSLAM transforms object coordinates based on the robot's current pose at the time of detection.

---

## Authors

- Guy Stein
- Guy Zilberstein
