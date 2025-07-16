package cinelux.bioskopcinelux;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class CineLux extends Application {
    @Override
    public void start(Stage stage) throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader(CineLux.class.getResource("/cinelux/bioskopcinelux/view/Menu/MenuAdmin.fxml"));
//        FXMLLoader fxmlLoader = new FXMLLoader(CineLux.class.getResource("/cinelux/bioskopcinelux/view/Menu/MenuCashier.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(CineLux.class.getResource("/cinelux/bioskopcinelux/view/Menu/MenuManager.fxml"));
//        FXMLLoader fxmlLoader = new FXMLLoader(CineLux.class.getResource("/cinelux/bioskopcinelux/view/Login/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1920, 1080);
        stage.setTitle("BIOSKOP CINELUX");
        stage.initStyle(StageStyle.UNDECORATED); // Untuk manmpilkan fullscreen
        stage.setFullScreen(false);
        stage.setFullScreenExitHint("");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}