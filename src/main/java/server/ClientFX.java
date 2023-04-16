package server;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import server.models.Course;
import server.models.RegistrationForm;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * La classe ClientFX gère l'interface graphique de l'application et envoit les requêtes du client au serveur.
 */
public class ClientFX extends Application {

    public final static int PORT = 1337;

    public final static String IPAdress="127.0.0.1";

    /**
     * La méthode main lance l'application graphique.
     * @param args (pas utilisé)
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        launch(args);
    }

    /**
     * Cette méthode affiche à les composants graphiques à l'écran et envoit les requêtes de l'application
     * au serveur.
     *
     * @param stage La fenêtre de l'application
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception {
        GridPane root = new GridPane();
        Scene scene = new Scene(root);
        Separator vSeparator = new Separator();
        vSeparator.setOrientation(Orientation.VERTICAL);

        Separator hSeparator = new Separator();
        hSeparator.setOrientation(Orientation.HORIZONTAL);
        hSeparator.setPadding(new Insets(15, 0, 0, 0));



        // ------------ LISTE DES COURS -----------------

        GridPane listCourseBox = new GridPane();
        listCourseBox.setStyle("-fx-background-color: #EEEEE6");
        Label titreLDC = new Label("Liste des cours");
        titreLDC.setFont(new Font("Arial", 20));
        titreLDC.setPadding(new Insets(10, 0, 20, 0));


        TableView<Course> table = new TableView<>();
        table.setMaxSize(300, 350);
        ObservableList<Course> data = FXCollections.observableArrayList(new Course("Nom", "Code", "Session"));

        TableColumn codeCol = new TableColumn<Course, String>("Code");
        codeCol.setMinWidth(100);
        codeCol.setCellValueFactory(new PropertyValueFactory<Course, String>("code"));

        TableColumn courseCol = new TableColumn<Course, String>("Cours");
        courseCol.setMinWidth(200);
        courseCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        table.getColumns().addAll(codeCol, courseCol);


        GridPane courseListBottom = new GridPane();

        ChoiceBox sessionCB= new ChoiceBox();
        sessionCB.getItems().addAll("Automne", "Hiver", "Ete");
        sessionCB.setValue("Automne");

        //Bouton 'Charger'
        Button chargerButton = new Button("Charger");
        chargerButton.setOnMouseClicked((event) -> {
            try {
                table.getItems().clear();
                String arg =  (String) sessionCB.getValue();

                ArrayList<Course> arrCourse = F1(arg);

                for(int i=0; i<arrCourse.size(); i++){
                    table.getItems().add(new Course(arrCourse.get(i).getName(), arrCourse.get(i).getCode(), arrCourse.get(i).getSession()));
                }

            } catch (IOException | ClassNotFoundException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Erreur au chargement. Veuillez réessayer plus tard.");
                alert.setHeaderText("Erreur");
                alert.showAndWait();
            }
        });

        listCourseBox.add(titreLDC, 0, 0);
        GridPane.setHalignment(titreLDC, HPos.CENTER);
        listCourseBox.add(table, 0, 1);
        listCourseBox.add(hSeparator, 0, 2);
        listCourseBox.add(courseListBottom, 0, 3);
        listCourseBox.setMargin(titreLDC, new Insets(20, 0, 0, 0));
        listCourseBox.setMargin(table, new Insets(0, 30, 0, 30));

        courseListBottom.add(sessionCB, 0, 0);
        courseListBottom.add(chargerButton, 1, 0);
        courseListBottom.setMargin(chargerButton, new Insets(20, 20, 20, 60));
        courseListBottom.setMargin(sessionCB, new Insets(20, 20, 20,60));


        // ------------ INSCRIPTION ----------------

        VBox inscriptionBox = new VBox();
        inscriptionBox.setStyle("-fx-background-color: #EEEEE6");

        Label titreInscription= new Label("Formulaire d'inscription");
        titreInscription.setFont(new Font("Arial", 20));
        titreInscription.setPadding(new Insets(10, 0, 20, 0));

        Label prenomLabel = new Label("Prénom");
        TextField prenomText = new TextField();

        Label nomLabel = new Label("Nom");
        TextField nomText = new TextField();

        Label emailLabel = new Label("Email");
        TextField emailText = new TextField();

        Label matriculeLabel = new Label("Matricule");
        TextField matriculeText = new TextField();

        //Bouton 'envoyer'
        Button sendButton = new Button("Envoyer");
        ArrayList<String> messageErrForm = new ArrayList<>();
        sendButton.setOnMouseClicked((event) -> {
            try {
                String prenom = prenomText.getText();
                String nom = nomText.getText();

                String email = emailText.getText();
                String matricule = matriculeText.getText();

                if(!email.matches("^\\S+@\\S+$") || !matricule.matches("^[0-9]{6}$")){
                    if(!email.matches("^\\S+@\\S+$")){
                        emailText.setStyle("-fx-text-box-border: red;");
                        messageErrForm.add("Le champ 'email' est invalide!");
                    } else
                        emailText.setStyle(null);

                    if(!matricule.matches("^[0-9]{8}$")){
                        matriculeText.setStyle("-fx-text-box-border: red;");
                        messageErrForm.add("Le champ 'matricule' est invalide!");
                    } else
                        matriculeText.setStyle(null);
                    throw new IllegalArgumentException();
                }


                Course course = table.getSelectionModel().getSelectedItem();
                String code = course.getCode();

                resetInterface(matriculeText, emailText, table, prenomText, nomText);
                F2(prenom, nom, email, matricule, course, code);

            } catch (NullPointerException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Le formulaire est invalide.\nVous devez sélectionner un cours!");
                table.setStyle("-fx-border-color: red;");
                alert.setHeaderText("Erreur");
                alert.showAndWait();
            }
                catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Serveur temporairement indisponible. Veuillez réessayer ultérieurement.");
                alert.setHeaderText("Erreur");
                alert.showAndWait();
            } catch (IllegalArgumentException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");

                String messageErreur = "Le formulaire est invalide!\n";
                for(int i=0; i<messageErrForm.size(); i++){
                    messageErreur += (messageErrForm.get(i) + "\n");
                }

                alert.setContentText(messageErreur);
                alert.showAndWait();
                messageErrForm.clear();
            }
        });

        GridPane inscriptionGrid = new GridPane();
        inscriptionGrid.setMinSize(250, 300);

        inscriptionGrid.setVgap(5);
        inscriptionGrid.setHgap(5);


        inscriptionGrid.add(titreInscription, 0, 0, 2, 1);
        GridPane.setHalignment(titreInscription, HPos.CENTER);
        inscriptionGrid.add(prenomLabel, 0, 1);
        inscriptionGrid.add(prenomText, 1, 1);


        inscriptionGrid.add(nomLabel, 0, 2);
        inscriptionGrid.add(nomText, 1, 2);

        inscriptionGrid.add(emailLabel, 0, 3);
        inscriptionGrid.add(emailText, 1, 3);

        inscriptionGrid.add(matriculeLabel, 0, 4);
        inscriptionGrid.add(matriculeText, 1, 4);

        inscriptionGrid.add(sendButton, 1, 5);
        GridPane.setHalignment(sendButton, HPos.CENTER);

        inscriptionGrid.setHgap(30);

        inscriptionGrid.setMargin(sendButton, new Insets(20, 0, 0, 0));
        inscriptionBox.getChildren().add(inscriptionGrid);
        inscriptionBox.setMargin(inscriptionGrid, new Insets(20, 30, 30, 40));

        //----------------------------------------

        root.add(listCourseBox, 0,0);
        root.add(vSeparator, 1, 0);
        root.add(inscriptionBox, 2,0);


        stage.setTitle("Inscription UdeM");
        stage.setScene(scene);
        stage.show();

    }

    /**
     *
     * Cette méthode permet de remettre à zéro le style de l'application après avoir été modifié si l'étudiant
     * a fait une ou plusieurs erreurs dans le formulaire
     *
     * @param matriculeText Le TextField du matricule de l'étudiant
     * @param emailText Le TextField du courriel de l'étudiant
     * @param table La TableView qui contient le nom des cours et leur code
     * @param prenomText Le TextField du prénom de l'étudiant
     * @param nomText Le TextField du nom de famille de l'étudiant
     */
    private void resetInterface(TextField matriculeText, TextField emailText, TableView<Course> table, TextField prenomText, TextField nomText) {
        matriculeText.setStyle(null);
        emailText.setStyle(null);
        table.setStyle(null);

        prenomText.clear();
        nomText.clear();
        emailText.clear();
        matriculeText.clear();
    }

    /**
     *
     * Cette fonction permet d'envoyer une requête au serveur pour charger la liste des cours de la session passée en argument
     *
     * @param arg La session d'étude (Automne, Ete et Hiver)
     * @return Le tableau de la liste des cours pour une session donnée
     * @throws IOException
     * @throws ClassNotFoundException
     */

    public static ArrayList<Course> F1(String arg) throws IOException, ClassNotFoundException {
        Socket clientSocket = new Socket(IPAdress,PORT);

        ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());
        os.writeObject("CHARGER " + arg);

        ObjectInputStream is = new ObjectInputStream(clientSocket.getInputStream());
        ArrayList<Course> courseArr = (ArrayList<Course>) is.readObject();

        return courseArr;
    }


    /**
     *
     * Cette fonction permet d'envoyer une requête au serveur pour envoyer l'inscription de l'étudiant.
     * Elle envoit un message de réussite lorsque l'inscription est complétée.
     *
     * @param prenom Le prénom de l'étudiant
     * @param nom Le nom de l'étudiant
     * @param email Le courriel de l'étudiant
     * @param matricule Le matricule de l'étudiant
     * @param course Le cours choisi par l'étudiant
     * @param code Le code du cours (ex: 'IFT1025')
     * @throws IOException
     */

    public static void F2(String prenom, String nom, String email, String matricule, Course course, String code) throws IOException {
        Socket clientSocket = new Socket(IPAdress,PORT);

        ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());
        os.writeObject("INSCRIRE");

        RegistrationForm inscription = new RegistrationForm(prenom, nom, email,matricule, course);
        os.writeObject(inscription);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message");
        alert.setContentText("Félicitation! " + nom + " " + prenom + " est inscrit(e) \navec succès au cours " + code + "!");
        alert.setHeaderText("Message");
        alert.showAndWait();

    }


}
