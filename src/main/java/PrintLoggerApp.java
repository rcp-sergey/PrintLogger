import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class PrintLoggerApp extends Application {
    private Stage mainStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("views/MainView.fxml"));
        mainStage = primaryStage;
        mainStage.getIcons().add(new Image("img/logo.png"));
        primaryStage.setTitle("Print Logger");
        primaryStage.setScene(new Scene(root, 1200, 600));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    /*public Stage getMainStage() {
        return mainStage;
    }*/
}
