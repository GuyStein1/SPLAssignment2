package bgu.spl.mics;

import bgu.spl.mics.application.objects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

class FusionSlamTest {

    private FusionSlam fusionSlam;

    @BeforeEach
    void setUp() {
        // Get the singleton instance and reset it
        fusionSlam = FusionSlam.getInstance();
        fusionSlam.reset();
    }

    @Test
    void testTransformToGlobal() {
        // Set up a pose
        Pose pose = new Pose(1, 3.0f, 4.0f, 30.0f); // time = 1, x = 3, y = 4, yaw = 30 degrees

        // Set up local coordinates
        List<CloudPoint> localCoordinates = Arrays.asList(
                new CloudPoint(1.0, 2.0),
                new CloudPoint(3.0, 4.0)
        );

        // Transform to global
        List<CloudPoint> globalCoordinates = fusionSlam.transformToGlobal(localCoordinates, pose);

        // Assert the global coordinates
        assertNotNull(globalCoordinates);
        assertEquals(2, globalCoordinates.size());

        // Expected calculations based on radians:
        // yaw (in radians) = 30 * π / 180 ≈ 0.5236
        // cos(yaw) ≈ 0.866
        // sin(yaw) ≈ 0.5

        // For the first point (1.0, 2.0):
        // xGlobal = 1.0 * cos(0.5236) - 2.0 * sin(0.5236) + 3.0 ≈ 2.866
        // yGlobal = 1.0 * sin(0.5236) + 2.0 * cos(0.5236) + 4.0 ≈ 6.232
        assertEquals(2.866, globalCoordinates.get(0).getX(), 0.001);
        assertEquals(6.232, globalCoordinates.get(0).getY(), 0.001);

        // For the second point (3.0, 4.0):
        // xGlobal = 3.0 * cos(0.5236) - 4.0 * sin(0.5236) + 3.0 ≈ 3.598
        // yGlobal = 3.0 * sin(0.5236) + 4.0 * cos(0.5236) + 4.0 ≈ 8.964
        assertEquals(3.598, globalCoordinates.get(1).getX(), 0.001);
        assertEquals(8.964, globalCoordinates.get(1).getY(), 0.001);
    }

    @Test
    void testProcessTrackedObject() {
        // Set up the first pose
        Pose pose1 = new Pose(1, 2.0f, 3.0f, 30.0f); // time = 1, x = 2, y = 3, yaw = 30 degrees

        // Set up the first tracked object
        List<CloudPoint> objectCoordinates1 = Arrays.asList(
                new CloudPoint(1.0, 1.0),
                new CloudPoint(-1.0, -1.0)
        );
        TrackedObject trackedObject1 = new TrackedObject("obj1", 1, "Test Object", objectCoordinates1);

        // Process the first tracked object
        fusionSlam.processTrackedObject(trackedObject1, pose1);

        // Assert the landmark was added
        List<LandMark> landmarks = fusionSlam.getLandmarks();
        assertEquals(1, landmarks.size());
        LandMark landmark = landmarks.get(0);

        assertEquals("obj1", landmark.getId());
        assertEquals("Test Object", landmark.getDescription());
        assertEquals(2, landmark.getCoordinates().size());

        // Validate transformed coordinates for CloudPoints
        double yawRadiansPose1 = Math.toRadians(30.0);
        double cosYawPose1 = Math.cos(yawRadiansPose1);
        double sinYawPose1 = Math.sin(yawRadiansPose1);

        // For (1.0, 1.0) with pose1:
        double xGlobal1Pose1 = 1.0 * cosYawPose1 - 1.0 * sinYawPose1 + 2.0;
        double yGlobal1Pose1 = 1.0 * sinYawPose1 + 1.0 * cosYawPose1 + 3.0;
        assertEquals(xGlobal1Pose1, landmark.getCoordinates().get(0).getX(), 0.001);
        assertEquals(yGlobal1Pose1, landmark.getCoordinates().get(0).getY(), 0.001);

        // For (-1.0, -1.0) with pose1:
        double xGlobal2Pose1 = -1.0 * cosYawPose1 - (-1.0) * sinYawPose1 + 2.0;
        double yGlobal2Pose1 = -1.0 * sinYawPose1 + (-1.0) * cosYawPose1 + 3.0;
        assertEquals(xGlobal2Pose1, landmark.getCoordinates().get(1).getX(), 0.001);
        assertEquals(yGlobal2Pose1, landmark.getCoordinates().get(1).getY(), 0.001);

        // Set up the second pose for time 2
        Pose pose2 = new Pose(2, 3.0f, 4.0f, 45.0f); // time = 2, x = 3, y = 4, yaw = 45 degrees

        // Set up the second tracked object
        List<CloudPoint> objectCoordinates2 = Arrays.asList(
                new CloudPoint(2.0, 2.0),
                new CloudPoint(-2.0, -2.0)
        );
        TrackedObject trackedObject2 = new TrackedObject("obj1", 2, "Test Object", objectCoordinates2);

        // Process the second tracked object
        fusionSlam.processTrackedObject(trackedObject2, pose2);

        // Assert the landmark was updated (not duplicated)
        assertEquals(1, landmarks.size());
        LandMark updatedLandmark = landmarks.get(0);

        assertEquals("obj1", updatedLandmark.getId());
        assertEquals("Test Object", updatedLandmark.getDescription());
        assertEquals(2, updatedLandmark.getCoordinates().size());

        // Validate updated coordinates (averaged):
        double yawRadiansPose2 = Math.toRadians(45.0);
        double cosYawPose2 = Math.cos(yawRadiansPose2);
        double sinYawPose2 = Math.sin(yawRadiansPose2);

        // Global coordinates for (2.0, 2.0) with pose2:
        double xGlobal1Pose2 = 2.0 * cosYawPose2 - 2.0 * sinYawPose2 + 3.0;
        double yGlobal1Pose2 = 2.0 * sinYawPose2 + 2.0 * cosYawPose2 + 4.0;

        // Average the coordinates:
        double updatedX1 = (xGlobal1Pose1 + xGlobal1Pose2) / 2.0;
        double updatedY1 = (yGlobal1Pose1 + yGlobal1Pose2) / 2.0;
        assertEquals(updatedX1, updatedLandmark.getCoordinates().get(0).getX(), 0.001);
        assertEquals(updatedY1, updatedLandmark.getCoordinates().get(0).getY(), 0.001);

        // Global coordinates for (-2.0, -2.0) with pose2:
        double xGlobal2Pose2 = -2.0 * cosYawPose2 - (-2.0) * sinYawPose2 + 3.0;
        double yGlobal2Pose2 = -2.0 * sinYawPose2 + (-2.0) * cosYawPose2 + 4.0;

        // Average the coordinates:
        double updatedX2 = (xGlobal2Pose1 + xGlobal2Pose2) / 2.0;
        double updatedY2 = (yGlobal2Pose1 + yGlobal2Pose2) / 2.0;
        assertEquals(updatedX2, updatedLandmark.getCoordinates().get(1).getX(), 0.001);
        assertEquals(updatedY2, updatedLandmark.getCoordinates().get(1).getY(), 0.001);
    }

}

