package com.example.foodcourt.particles;

import com.example.foodcourt.LocalizationActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA. User: johanchateau Date: 03/10/13 Time: 17:23 To
 * change this template use File | Settings | File Templates.
 */
public class ParticleFilter {

    public static Cloud filter(Cloud cloud, Point center, InertialPoint inertialPoint, HashMap<String, RoomInfo> rooms) {
        // Kill infeasible particles
        List<Particle> survivorList = weightCloud(cloud.getParticles(), center, rooms, inertialPoint, cloud.getInerPoint());

        // Resample particles randomly
        List<Particle> reSampleList = reSample(survivorList);

        // Move the cloud
        List<Particle> moveCloudList = moveCloud(reSampleList, cloud.getInerPoint(), inertialPoint);

        // Spread the cloud a little
        List<Particle> newRandomCloudList = newRandomCloud(moveCloudList, LocalizationActivity.CLOUD_DISPLACEMENT);

        // Calculation of center
        Point centerPoint = calculateCenter(newRandomCloudList);

        //Update
        return new Cloud(centerPoint, newRandomCloudList, inertialPoint.getPoint());
    }

    private static List<Particle> weightCloud(List<Particle> particles, Point center, HashMap<String, RoomInfo> rooms, InertialPoint inertialPoint, Point prevInerPoint) throws NullPointerException {

        List<Particle> finalList = new ArrayList<Particle>();

        double averageSpread = ParticleFilter.calculateSpread(particles, center);

        double[] movement = inertialPoint.getMovement(prevInerPoint);

        // We sort particles by weight
        for (Particle particle : particles) {
            boolean alive = true;

            // Cut out the particles leaving the screen
            if (particle.getX() + movement[0] < 0 || particle.getY() + movement[1] < 0 || particle.getX() + movement[0] > LocalizationActivity.TOTAL_DRAW_SIZE.getX() || particle.getY() + movement[1] > LocalizationActivity.TOTAL_DRAW_SIZE.getY()) {
                alive = false;
            }
            // Cut out the empty corners
            if ((particle.getX() + movement[0] < 12 && particle.getY() + movement[1] > 8.2) || (particle.getX() + movement[0] > 56 && particle.getY() + movement[1] < 6.1)) {
                alive = false;
            }

            for (RoomInfo room : rooms.values()) {
                if (room.containsLocation(particle.getPoint())) {
                    if (room.collidesWithWall(particle.getPoint(), movement[0], movement[1])) {
                        alive = false;
                    }
                    if (room.enterInfeasibleRoom(particle.getPoint(), movement[0], movement[1])) {
                        alive = false;
                    }
                }
            }

            if (particle.euclideanDistance(center) > averageSpread * 3) {
                alive = false;
            }

            if (alive) {
                finalList.add(particle);
            }
        }

        return finalList;
    }

    private static List<Particle> reSample(List<Particle> particles) {

        List<Particle> newList = new ArrayList<Particle>();

        Random random = new Random();

        while (newList.size() < LocalizationActivity.NUMBER_PARTICLES) {
            newList.add(particles.get(random.nextInt(particles.size())));
        }

        return newList;
    }

    private static List<Particle> moveCloud(List<Particle> particles, Point prevInerPoint, InertialPoint inertialPoint) {
        double[] movement = inertialPoint.getMovement(prevInerPoint);

        for (Particle particle : particles) {
            particle.move(movement[0], movement[1]);
        }

        System.out.println("Movement: X:"+movement[0]+" Y:"+movement[1]);

        return particles;
    }

    private static List<Particle> newRandomCloud(List<Particle> particles, double cloudDisplacement) {

        List<Particle> newParticles = new ArrayList<Particle>();
        for (Particle particle : particles) {

            double dx = cloudDisplacement * Math.sqrt(-Math.log(1 - Math.random()));
            double dy = cloudDisplacement * Math.sqrt(-Math.log(1 - Math.random()));

            Random generator = new Random();

            int randomIndex = 0;
            while (randomIndex == 0) {
                randomIndex = generator.nextInt(3) - 1;
            }
            double x = particle.getX() + randomIndex * dx;

            randomIndex = 0;
            while (randomIndex == 0) {
                randomIndex = generator.nextInt(3) - 1;
            }
            double y = particle.getY() + randomIndex * dy;

            newParticles.add(new Particle(x, y));
        }

        return newParticles;
    }

    //Barycentre particles
    private static Point calculateCenter(List<Particle> particles) {

        double x = 0;
        double y = 0;
        double sum = 0;

        for (Particle result : particles) {
            x += result.getX();
            y += result.getY();
            sum ++;
        }
        return new Point(x / sum, y / sum);
    }

    //Spread of particles
    public static double calculateSpread(List<Particle> particles) {
        Point center = calculateCenter(particles);

        return calculateSpread(particles, center);
    }

    //Spread of particles
    public static double calculateSpread(List<Particle> particles, Point center) {
        double diff = 0;

        for (Particle particle : particles) {
            diff += particle.euclideanDistance(center);
        }

        return diff / particles.size();
    }

}
