package org.example.doublependulumproject;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.net.URL;
import java.util.ResourceBundle;

public class DPController implements Initializable{
    @FXML
    private Circle pend1, pend2, anchor;
    @FXML
    private Line line1, line2;
    @FXML
    private Button settingButton, mainButton, resetButton;
    @FXML
    private Pane settingPane, trailPane;
    @FXML
    private Spinner<Integer> m1_spinner, m2_spinner, L1_spinner, L2_spinner, theta1_spinner, theta2_spinner, deltat_spinner;
    @FXML
    private Slider speed_slider;

    private boolean settingsOpen = false;
    private final double LINE_LEN_FACTOR = 5.0;
    private DoublePendulum dp;
    double[] init = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Image settingIcon = new Image("/images/settingIcon.png");
        BackgroundSize settingIconSize = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true);
        BackgroundImage settingIconBgImage = new BackgroundImage(settingIcon, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                                                        BackgroundPosition.CENTER, settingIconSize);
        Background settingIconBg = new Background(settingIconBgImage);
        settingButton.setBackground(settingIconBg);

        m1_spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,1000));
        m1_spinner.getValueFactory().setValue(10);
        m2_spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,1000));
        m2_spinner.getValueFactory().setValue(10);
        L1_spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,1000));
        L1_spinner.getValueFactory().setValue(15);
        L2_spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,1000));
        L2_spinner.getValueFactory().setValue(15);
        theta1_spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-180,180));
        theta1_spinner.getValueFactory().setValue(90);
        theta2_spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-180,180));
        theta2_spinner.getValueFactory().setValue(90);
        deltat_spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000));
        deltat_spinner.getValueFactory().setValue(10);
        deltat_spinner.valueProperty().addListener(e -> {
            if(dp != null)
                dp.setDt(deltat_spinner.getValue()*0.001);
        });

        m1_spinner.valueProperty().addListener(e -> setPend1Mass((double) m1_spinner.getValue()));
        m2_spinner.valueProperty().addListener(e -> setPend2Mass((double) m2_spinner.getValue()));
        L1_spinner.valueProperty().addListener(e -> setLine1Length(LINE_LEN_FACTOR*L1_spinner.getValue()));
        L2_spinner.valueProperty().addListener(e -> setLine2Length(LINE_LEN_FACTOR*L2_spinner.getValue()));
        theta1_spinner.valueProperty().addListener(e -> setPend1Angle(toRadians((double) theta1_spinner.getValue())));
        theta2_spinner.valueProperty().addListener(e -> setPend2Angle(toRadians((double) theta2_spinner.getValue())));
        speed_slider.valueProperty().addListener(e -> {
            if(dp != null)
                dp.setSpeed(speed_slider.getValue());
        });
    }
    @FXML
    public void toggleSettings(){
        TranslateTransition translate = new TranslateTransition();
        translate.setNode(settingPane);
        if(settingsOpen){
            translate.setByX(-settingPane.getWidth());
            translate.setInterpolator(Interpolator.EASE_IN);
            settingButton.setDisable(true);
            translate.setOnFinished(e -> settingButton.setDisable(false));
            translate.play();
            settingsOpen = false;
        }
        else {
            translate.setByX(settingPane.getWidth());
            translate.setInterpolator(Interpolator.EASE_OUT);
            settingButton.setDisable(true);
            translate.setOnFinished(e -> settingButton.setDisable(false));
            translate.play();
            settingsOpen = true;
        }
    }

    @FXML
    public void toggleTrailVisibility(){
        trailPane.visibleProperty().setValue(!trailPane.isVisible());
    }

    @FXML
    private void startSimulation() {
        init = saveInitial();
        dp = new DoublePendulum(this, m1_spinner.getValue(), L1_spinner.getValue(),
                toRadians(theta1_spinner.getValue()), m2_spinner.getValue(), L2_spinner.getValue(),
                toRadians(theta2_spinner.getValue()), deltat_spinner.getValue() * 0.001, speed_slider.getValue());
        mainButton.setOnAction(e -> pauseSimulation());
        mainButton.setText("Pause Simulation");
        resetButton.visibleProperty().setValue(true);
    }

    private double[] saveInitial(){
        return new double[]{m1_spinner.getValue(), L1_spinner.getValue(), theta1_spinner.getValue(),
                            m2_spinner.getValue(), L2_spinner.getValue(), theta2_spinner.getValue()};
    }

    public void update(double angle1, double angle2){
        setPend1Angle(angle1);
        setPend2Angle(angle2);
        appendTrail();
    }

    private void pauseSimulation(){
        dp.pause();
        mainButton.setOnAction(e -> playSimulation());
        mainButton.setText("Play Simulation");
    }

    private void playSimulation(){
        dp.play();
        mainButton.setOnAction(e -> pauseSimulation());
        mainButton.setText("Pause Simulation");
    }

    public void setLine1Length(double len){
        double[] vec1 = new double[]{pend1.getCenterX(), pend1.getCenterY()};
        double vec1len = Math.sqrt(vec1[0]*vec1[0] + vec1[1]*vec1[1]);
        double[] unitVec1 = new double[]{vec1[0]/vec1len, vec1[1]/vec1len};
        double[] newVec1 = new double[]{unitVec1[0]*len, unitVec1[1]*len};
        line1.setEndX(newVec1[0]);
        line1.setEndY(newVec1[1]);
        pend1.setCenterX(line1.getEndX());
        pend1.setCenterY(line1.getEndY());
        pend2.setLayoutX(anchor.getLayoutX() + pend1.getCenterX());
        pend2.setLayoutY(anchor.getLayoutY() + pend1.getCenterY());
        line2.setLayoutX(pend2.getLayoutX());
        line2.setLayoutY(pend2.getLayoutY());
        if(dp != null)
            dp.setL1(len);
    }

    public void setLine2Length(double len){
        double[] vec2 = new double[]{pend2.getCenterX(), pend2.getCenterY()};
        double vec2len = Math.sqrt(vec2[0]*vec2[0] + vec2[1]*vec2[1]);
        double[] unitVec2 = new double[]{vec2[0]/vec2len, vec2[1]/vec2len};
        double[] newVec2 = new double[]{unitVec2[0]*len, unitVec2[1]*len};
        line2.setEndX(newVec2[0]);
        line2.setEndY(newVec2[1]);
        pend2.setCenterX(line2.getEndX());
        pend2.setCenterY(line2.getEndY());
        if(dp != null)
            dp.setL2(len);
    }

    public void setPend1Angle(double a){
        double line1len = Math.sqrt(line1.getEndX()*line1.getEndX() + line1.getEndY()*line1.getEndY());
        line1.setEndX(line1len*Math.cos(a - Math.PI/2));
        line1.setEndY(-line1len*Math.sin(a - Math.PI/2));
        pend1.setCenterX(line1.getEndX());
        pend1.setCenterY(line1.getEndY());
        line2.setLayoutX(anchor.getLayoutX() + pend1.getCenterX());
        line2.setLayoutY(anchor.getLayoutY() + pend1.getCenterY());
        pend2.setLayoutX(line2.getLayoutX());
        pend2.setLayoutY(line2.getLayoutY());
    }

    public void setPend2Angle(double a){
        double line2len = Math.sqrt(line2.getEndX()*line2.getEndX() + line2.getEndY()*line2.getEndY());
        line2.setEndX(line2len*Math.cos(a - Math.PI/2));
        line2.setEndY(-line2len*Math.sin(a - Math.PI/2));
        pend2.setCenterX(line2.getEndX());
        pend2.setCenterY(line2.getEndY());
    }

    public void setPend1Mass(double m){
        pend1.setRadius(m);
        if(dp != null)
            dp.setMass1(m);
    }

    public void setPend2Mass(double m){
        pend2.setRadius(m);
        if(dp != null)
            dp.setMass2(m);
    }

    private void appendTrail(){
        Circle c = new Circle(1.5, Color.WHITE);
        c.setCenterX(pend2.getLayoutX() + pend2.getCenterX());
        c.setCenterY(pend2.getLayoutY() + pend2.getCenterY());
        trailPane.getChildren().add(c);
    }
    @FXML
    public void reset(){
        dp.pause();
        dp = null;
        resetButton.visibleProperty().setValue(false);
        mainButton.setText("Start simulation");
        mainButton.setOnAction(e -> startSimulation());
        m1_spinner.getValueFactory().setValue((int)init[0]);
        L1_spinner.getValueFactory().setValue((int)init[1]);
        theta1_spinner.getValueFactory().setValue((int)init[2]);
        m2_spinner.getValueFactory().setValue((int)init[3]);
        L2_spinner.getValueFactory().setValue((int)init[4]);
        theta2_spinner.getValueFactory().setValue((int)init[5]);
        setLine1Length(L1_spinner.getValue()*LINE_LEN_FACTOR);
        setLine2Length(L2_spinner.getValue()*LINE_LEN_FACTOR);
        setPend1Angle(toRadians(theta1_spinner.getValue()));
        setPend2Angle(toRadians(theta2_spinner.getValue()));
        setPend1Mass(m1_spinner.getValue());
        setPend2Mass(m2_spinner.getValue());
        trailPane.getChildren().clear();
    }

    static double toRadians(double degrees){
        return degrees * 2 * Math.PI / 360;
    }
}