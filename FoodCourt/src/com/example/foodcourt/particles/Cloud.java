package com.example.foodcourt.particles;

import java.util.List;

public class Cloud {

    private Point estimatedPosition;
    private List<Particle> particles;
    private double spread;

    public Cloud(List<Particle> particles) {
        this.particles = particles;
        this.estimatedPosition = calculateCenter();
    }

    public void setParticles(List<Particle> particles) {
        this.particles = particles;
        this.estimatedPosition = calculateCenter();
        this.spread = calculateSpread();
    }

    public int getParticleCount() {
        return particles.size();
    }

    public Point getEstimatedPosition() {
        return estimatedPosition;
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public double getSpread() {
        if (spread == 0) {
            spread = calculateSpread();
        }
        return spread;
    }

    // Barycentre of particle cloud
    private Point calculateCenter() {
        double x = 0;
        double y = 0;
        double sum = 0;

        for (Particle result : particles) {
            x += result.getX();
            y += result.getY();
            sum ++;
        }

        estimatedPosition = new Point(x / sum, y / sum);
        return estimatedPosition;
    }

    //Spread of particles
    private double calculateSpread() {
        double diff = 0;

        for (Particle particle : particles) {
            diff += particle.euclideanDistance(estimatedPosition);
        }

        spread = diff / particles.size();
        return spread;
    }
}
