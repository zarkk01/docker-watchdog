package gr.aueb.dmst.dockerWatchdog.Application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.shape.Ellipse;

public class TestFXController {

    @FXML
    private Ellipse myEllipse;
    private double x;
    private double y;
    public void up(ActionEvent e) {
        myEllipse.setCenterY(y-=10);
    }

    public void down(ActionEvent e) {
        myEllipse.setCenterY(y+=10);
    }

    public void left(ActionEvent e) {
        myEllipse.setCenterX(x-=10);
    }

    public void right(ActionEvent e) {
        myEllipse.setCenterX(x+=10);
    }
}
