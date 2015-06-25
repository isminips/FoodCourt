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

        // If long stretched line, make small spread along the axis with smallest variance
        particleList = checkSpread(particleList);

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

            for (RoomInfo room : rooms.values()) {
                if (room.isBlocked() && room.containsLocation(particle)) {
                    particle.kill();
                } else if (room.containsLocation(particle.beforeMoving(movement)) && room.collidesWithWall(particle)) {
                    particle.kill();
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

    // Return a list of spread particles
    private static List<Particle> checkSpread(List<Particle> particles) {
        double sumx = 0, sumy = 0;

        for (Particle p : particles) {
            sumx += p.getX();
            sumy += p.getY();
        }

        double meanx = sumx / particles.size();
        double meany = sumy / particles.size();

        double varx = 0, vary = 0;

        for (Particle p : particles) {
            varx += Math.pow(p.getX() - meanx, 2);
            vary += Math.pow(p.getY() - meany, 2);
        }

        varx /= particles.size();
        vary /= particles.size();

        double spreadRatio = 0.2;
        Random rnd = new Random();

        if (varx > spreadRatio && vary > spreadRatio)
            return particles;

        for (Particle p : particles) {
            if (varx < spreadRatio)
                p.move(getRandomDisplacement(rnd), 0);

            if (vary < spreadRatio)
                p.move(0, getRandomDisplacement(rnd));
        }

        return particles;
    }

    private static double getRandomDisplacement(Random rnd) {
        return (rnd.nextDouble() - 0.5) / 1.5;
    }

}
