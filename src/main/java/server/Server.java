package server;

import javafx.util.Pair;
import server.models.Course;
import server.models.RegistrationForm;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * La classe Server gère les requêtes du clients selon les événements envoyés par la classe ClientFX et ClientSimple.
 */
public class Server {

    public final static String REGISTER_COMMAND = "INSCRIRE";
    public final static String LOAD_COMMAND = "CHARGER";
    private final ServerSocket server;
    private Socket client;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private final ArrayList<EventHandler> handlers;

    public Server(int port) throws IOException {
        this.server = new ServerSocket(port, 1);
        this.handlers = new ArrayList<EventHandler>();
        this.addEventHandler(this::handleEvents);
    }

    /**
     * Cette méthode ajoute les EventHandlers au tableau d'EventHandler
     * @param h L'EventHandler à ajouter au tableau d'EventHandler
     */
    public void addEventHandler(EventHandler h) {
        this.handlers.add(h);
    }

    /**
     * Cette méthode appelle les EventHandlers du tableau d'EventHandlers et applique le bon à la commande du client.
     *
     * @param cmd La commande envoyé par le client.
     * @param arg L'argument envoyé par le client.
     * @throws IOException
     * @throws ClassNotFoundException
     */

    private void alertHandlers(String cmd, String arg) throws IOException, ClassNotFoundException {
        for (EventHandler h : this.handlers) {
            h.handle(cmd, arg);
        }
    }

    /**
     * Cette méthode attend qu'un client se connecte au serveur et attend une commande du client.
     * <p>
     * Après la réception de la commande, le serveur déconnecte le client.
     */

    public void run() {
        while (true) {
            try {
                client = server.accept();
                System.out.println("Connecté au client: " + client);
                objectInputStream = new ObjectInputStream(client.getInputStream());
                objectOutputStream = new ObjectOutputStream(client.getOutputStream());
                listen();
                disconnect();
                System.out.println("Client déconnecté!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cette méthode traite la commande envoyé par le client.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */

    public void listen() throws IOException, ClassNotFoundException {
        String line;
        if ((line = this.objectInputStream.readObject().toString()) != null) {
            Pair<String, String> parts = processCommandLine(line);
            String cmd = parts.getKey();
            String arg = parts.getValue();
            this.alertHandlers(cmd, arg);
        }
    }

    /**
     * Cette méthode lis la commande du client et la sépare en format (commande, argument).
     *
     * @param line Ligne
     * @return Retourne l'argument sous forme (commande, argument)
     */

    public Pair<String, String> processCommandLine(String line) {
        String[] parts = line.split(" ");
        String cmd = parts[0];
        String args = String.join(" ", Arrays.asList(parts).subList(1, parts.length));
        return new Pair<>(cmd, args);
    }

    /**
     * Cette méthode déconnecte le client du serveur.
     * @throws IOException
     */

    public void disconnect() throws IOException {
        objectOutputStream.close();
        objectInputStream.close();
        client.close();
    }

    /**
     * Cette méthode appelle la méthode appropriée selon la commande envoyé par le client.
     *
     * @param cmd La commande envoyé par le client
     * @param arg L'argument envoyé par le client
     * @throws IOException
     * @throws ClassNotFoundException
     */


    public void handleEvents(String cmd, String arg) throws IOException, ClassNotFoundException {
        if (cmd.equals(REGISTER_COMMAND)) {
            handleRegistration();
        } else if (cmd.equals(LOAD_COMMAND)) {
            handleLoadCourses(arg);
        }
    }

    /**
     Lire un fichier texte contenant des informations sur les cours et les transofmer en liste d'objets 'Course'.
     La méthode filtre les cours par la session spécifiée en argument.
     Ensuite, elle renvoie la liste des cours pour une session au client en utilisant l'objet 'objectOutputStream'.
     @param arg la session pour laquelle on veut récupérer la liste des cours
     @throws Exception si une erreur se produit lors de la lecture du fichier ou de l'écriture de l'objet dans le flux
     */

    public void handleLoadCourses(String arg) {
        ArrayList<Course> courseArr = new ArrayList<>();

        try {
            Scanner scan = new Scanner(new File("src/main/java/server/data/cours.txt"));
            while (scan.hasNext()) {
                String[] lineSplit = scan.nextLine().split("\\s+");
                Course cours = new Course(lineSplit[1], lineSplit[0], lineSplit[2]);

                if (cours.getSession().equals(arg))
                    courseArr.add(cours);
            }
            objectOutputStream.writeObject(courseArr);
            objectOutputStream.close();

        } catch (FileNotFoundException e) {
            System.out.println("Erreur à l'ouverture du fichier");
        } catch(IOException e){
            System.out.println("Erreur à l'écriture");
        }
    }

    /**
     Récupérer l'objet 'RegistrationForm' envoyé par le client en utilisant 'objectInputStream', l'enregistrer dans un fichier texte
     et renvoyer un message de confirmation au client.
     @throws Exception si une erreur se produit lors de la lecture de l'objet, l'écriture dans un fichier ou dans le flux de sortie.
     */
    public void handleRegistration() throws IOException, ClassNotFoundException {
        RegistrationForm rf = (RegistrationForm) objectInputStream.readObject();

        try {
            FileOutputStream fos = new FileOutputStream("src/main/java/server/data/inscription.txt", true);
            fos.write((rf.getCourse().getSession() + "\t" + rf.getCourse().getCode() + "\t" + rf.getMatricule() + "\t" +
                                     rf.getPrenom() + "\t" + rf.getNom() + "\t" + rf.getEmail()).getBytes());
            fos.write(10);


            fos.close();
        } catch (IOException e){
            System.out.println("Erreur à l'écriture du fichier");
        }
    }

}

