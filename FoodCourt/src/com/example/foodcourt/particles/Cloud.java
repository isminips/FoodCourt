package com.example.foodcourt.particles;


import java.util.List;

/**
 * Created with IntelliJ IDEA. User: johanchateau Date: 04/10/13 Time: 12:17 To
 * change this template use File | Settings | File Templates.
 */
public class Cloud {

    private Point estimatedPosition;
    private List<Particle> particles;

    public Cloud(Point estimatedPosition, List<Particle> particles) {
        this.estimatedPosition = estimatedPosition;
        this.particles = particles;
    }

    public Cloud(List<Particle> particles) {
        this.particles = particles;
        this.estimatedPosition = Cloud.calculateCenter(particles);
    }

    public void setEstimatedPosition(Point estimatedPosition) {
        this.estimatedPosition = estimatedPosition;
    }

    public void setParticles(List<Particle> particles) {
        this.particles = particles;
    }

    public String getParticleCount() {
        return "ParticleNb : " + particles.size();
    }

    public Point getEstimatedPosition() {
        return estimatedPosition;
    }

    public List<Particle> getParticles() {
        return particles;
    }

    // Barycentre of particle cloud
    public static Point calculateCenter(List<Particle> particles) {
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
    public double calculateSpread() {
        double diff = 0;

        for (Particle particle : particles) {
            diff += particle.euclideanDistance(estimatedPosition);
        }

        return diff / particles.size();
    }
}
