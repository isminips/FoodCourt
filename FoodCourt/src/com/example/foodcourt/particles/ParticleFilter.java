package com.example.foodcourt.particles;

import com.example.foodcourt.LocalizationActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ParticleFilter {

    public static Cloud filter(Cloud cloud, Movement movement, HashMap<String, RoomInfo> rooms) {
        List<Particle> particleList = cloud.getParticles();

        // Move the cloud
        particleList = moveCloud(particleList, movement.getMovement());

        // Kill infeasible particles
        particleList = weightCloud(particleList, rooms, movement.getMovement());

        // Resample particles randomly, fill list randomly from initial particles
        particleList = reSample(particleList, cloud.getParticles());

        //Update
        return new Cloud(particleList);
    }

    private static List<Particle> weightCloud(List<Particle> particles, HashMap<String, RoomInfo> rooms, double[] movement) throws NullPointerException {
        List<Particle> survivors = new ArrayList<Particle>();

        // We sort particles by weight
        for (Particle particle : particles) {
            // Cut out the particles leaving the screen
            if (particle.getX() < 0 || particle.getY() < 0 || particle.getX() > LocalizationActivity.TOTAL_DRAW_SIZE.getX() || particle.getY() > LocalizationActivity.TOTAL_DRAW_SIZE.getY()) {
                particle.kill();
            }
            // Cut out the empty corners
            if ((particle.getX() < 12 && particle.getY() > 8.2) || (particle.getX() > 56 && particle.getY() < 6.1)) {
                particle.kill();
            }

            for (RoomInfo room : rooms.values()) {
                if (room.containsLocation(particle.beforeMoving(movement))) {
                    if (room.collidesWithWall(particle.getPoint())) {
                        particle.kill();
                    }
                    if (room.enterInfeasibleRoom(particle.getPoint())) {
                        particle.kill();
                    }
                }
            }

            if (particle.isAlive()) {
                survivors.add(particle);
            }
        }

        return survivors;
    }

    // Return a list of N particles, containing the survivors plus a random set of the previous sample of particles
    private static List<Particle> reSample(List<Particle> particles, List<Particle> initialParticles) {
        List<Particle> newList = new ArrayList<Particle>();

        // add all surviving particles
        newList.addAll(particles);

        // randomly resample from initial particles to fill the list
        Random random = new Random();
        while (newList.size() < LocalizationActivity.NUMBER_PARTICLES) {
            newList.add(initialParticles.get(random.nextInt(initialParticles.size())));
        }

        return newList;
    }

    // Return a list of moved copies of the particles
    private static List<Particle> moveCloud(List<Particle> particles, double[] movement) {
        List<Particle> newList = new ArrayList<Particle>();

        for (Particle particle : particles) {
            newList.add(particle.copy().move(movement));
        }

        return newList;
    }

}
