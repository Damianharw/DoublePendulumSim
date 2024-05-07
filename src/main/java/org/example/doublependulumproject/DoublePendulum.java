package org.example.doublependulumproject;


import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * This class represenets double pendulum and calculates
 * equations of motion for double pendulum based on set
 * initial paramters.
 *
 * Example usage:
 * <pre>
 *     DPController dpcp = new DPController();
 *     double m1 = 2, m2 = 4;
 *     double L1 = 10, L2 = 15;
 *     double theta1 = 90, theta2 = 120;
 *     double dt = 0.01;
 *     double speed = 1.5;
 *     DoublePendulum myPendulum = DoublePendulum(dpcp, m1, L1, theta1, m2, L2, theta2, dt, speed)
 * </pre>
 * @author Damian Harenčák
 * @version 1.0
 * @since 2024-05-07
 */
public class DoublePendulum{
    private double m1, L1, theta1;
    private double m2, L2, theta2;
    private double dt;
    boolean high_fps = true;
    private DPController dpController;
    private double dtheta1, dtheta2;
    private double speed = 1;
    private final double g = 9.98;
    private Timeline anim, updateAnim;

    /**
     * Double pendulum constructor.
     * @param dpController - Controller instance for fxml file.
     * @param m1 - Mass of first pendulum.
     * @param L1 - Length of first bar.
     * @param theta1 - Angle of the first pendulum relative to vertical axis in degrees.
     * @param m2 - Mass of the second pendulum.
     * @param L2 - Length of second bar.
     * @param theta2 - Angle of the second pendulum relative to vertical axis in degrees.
     * @param dt - The rate of calculating next position of pendulums as well as accuracy constant in numerical integration.
     * @param speed - speed of the animation of the pendulum simulation.
     */
    public DoublePendulum(DPController dpController,double m1, double L1, double theta1,
                          double m2, double L2, double theta2, double dt, double speed){
        this.m1 = m1;
        this.L1 = L1;
        this.theta1 = theta1;
        this.m2 = m2;
        this.L2 = L2;
        this.theta2 = theta2;
        this.dt = dt;
        this.speed = speed;
        this.dpController = dpController;
        dtheta1 = 0;
        dtheta2 = 0;
        startSimulation();
    }
    public DoublePendulum(DPController dpController){
        m1 = 10;
        m2 = 10;
        L1 = 15;
        L2 = 15;
        theta1 = Math.PI/2;
        theta2 = Math.PI/2;
        dt = 0.1;
        this.dpController = dpController;
        dtheta1 = 0;
        dtheta2 = 0;
        startSimulation();
    }

    private void startSimulation(){
        anim = new Timeline(new KeyFrame(Duration.millis(dt*1000/speed), e ->
            update()));
        updateAnim = new Timeline(new KeyFrame(Duration.millis(high_fps ? 8.3 : 16.6), e ->
                dpController.update(theta1, theta2)));
        anim.setCycleCount(Timeline.INDEFINITE);
        updateAnim.setCycleCount(Timeline.INDEFINITE);
        anim.play();
        updateAnim.play();

    }

    /**
     * Pauses the animation and calcuations of the simulation.
     */
    public void pause(){
        anim.pause();
        updateAnim.pause();
    }

    /**
     * Resumes the animation and calculations of the simulation.
     */
    public void play(){
        anim.playFromStart();
        updateAnim.playFromStart();
    }


    private void update(){
        double ddtheta1 = nextddTheta1();
        double ddtheta2 = nextddTheta2();
        dtheta1 += ddtheta1*dt;
        dtheta2 += ddtheta2*dt;
        theta1 += dtheta1*dt;
        theta2 += dtheta2*dt;
    }

    /**
     * Calculates next iteration of angular acceleration for first pendulum using conditions set and
     * last calculated angles and angular velocities.
     * @return value of angular acceleration of first pendulum.
     */
    public double nextddTheta1(){
        double a = m1 + m2*Math.pow(Math.sin(theta1-theta2),2);
        return (-Math.sin(theta1-theta2)*(m2*L1*Math.pow(dtheta1,2)*Math.cos(theta1 - theta2)
                + m2*L2*Math.pow(dtheta2,2)) - g*((m1+m2)*Math.sin(theta1) -
                m2*Math.sin(theta2)*Math.cos(theta1-theta2)))/(L1*a);
    }

    /**
     * Calculates next iteration of angular acceleration for second pendulum using conditions set and
     * last calculated angles and angular velocities.
     * @return value of angular acceleration of second pendulum.
     */
    public double nextddTheta2(){
        double a = m1 + m2*Math.pow(Math.sin(theta1-theta2),2);
        return (Math.sin(theta1-theta2)*((m1+m2)*L1*Math.pow(dtheta1,2) + m2*L2*
                Math.pow(dtheta2,2)*Math.cos(theta1-theta2)) + g*((m1+m2)*Math.sin(theta1)*
                Math.cos(theta1-theta2) - (m1+m2)*Math.sin(theta2)))/(L2*a);
    }

    /**
     * Sets the mass of first pendulum.
     * @param m1 Mass value.
     */
    public void setMass1(double m1){
        this.m1 = m1;

    }

    /**
     * Sets the mass of second pendulum.
     * @param m2 Mass value.
     */
    public void setMass2(double m2){
        this.m2 = m2;
    }
    /**
     * Sets the length of first bar.
     * @param L1 Length value.
     */
    public void setL1(double L1){
        this.L1 = L1;
    }
    /**
     * Sets the length of second bar.
     * @param L2 Length value.
     */
    public void setL2(double L2){
        this.L2 = L2;
    }
    /**
     * Sets the speed of animation.
     * @param speed Speed value.
     */
    public void setSpeed(double speed){
        this.speed = speed;
        boolean wasPaused = anim.getStatus() == Animation.Status.PAUSED;
        anim.stop();
        anim.getKeyFrames().clear();
        anim.getKeyFrames().add(new KeyFrame(Duration.millis(dt*1000/this.speed), e ->
                update()));
        if(!wasPaused)
            anim.playFromStart();
    }
    /**
     * Sets the accuracy of calculations. Lower means more accurate.
     * @param dt Aaccuracy.
     */
    public void setDt(double dt){
        this.dt = dt;
        boolean wasPaused = anim.getStatus() == Animation.Status.PAUSED;
        anim.stop();
        anim.getKeyFrames().clear();
        anim.getKeyFrames().add(new KeyFrame(Duration.millis(this.dt*1000/speed), e ->
                update()));
        if(!wasPaused)
            anim.playFromStart();
    }
}
