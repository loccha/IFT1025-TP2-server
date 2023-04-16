package server;

import server.models.Course;
import server.models.RegistrationForm;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;


public class ClientSimple {
    public final static int PORT = 1337;
    public final static String IPAdress="127.0.0.1";

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Scanner scan = new Scanner(System.in);
        ArrayList<Course> arrCourse;

        System.out.println("*** Bienvenue au portail d'inscription de cours de l'UDEM ***");
        System.out.println("Veuillez choisir la session pour laquelle vous voulez consulter la liste des cours:");
        System.out.println("1. Automne \n2. Hiver \n3. Ete");
        System.out.print(">Choix: ");

        while(scan.hasNext()){
            Socket clientSocket = new Socket(IPAdress,PORT);
            int number = scan.nextInt();

            String arg = null;
            switch(number){
                case 1:
                    arg = "Automne";
                    break;
                case 2:
                    arg = "Hiver";
                    break;
                case 3:
                    arg = "Ete";
                    break;
                default:
                    System.out.println("erreur");
            }

            arrCourse = F1(clientSocket, arg);

            System.out.println(">Choix: ");
            System.out.println("1. Consulter les courts offerts pour une autre session");
            System.out.println("2. Inscription à un cours");
            System.out.print(">Choix: ");

            number = scan.nextInt();
            scan.nextLine();
            switch(number){
                case 1:
                    System.out.println("Choix 1");
                    break;
                case 2:
                    System.out.print("\nVeuillez saisir votre prénom: ");
                    String prenom = scan.nextLine();
                    System.out.print("Veuillez saisir votre nom: ");
                    String nom = scan.nextLine();
                    System.out.print("Veuillez saisir votre email: ");
                    String email = scan.nextLine();
                    System.out.print("Veuillez saisir votre matricule: ");
                    String matricule = scan.nextLine();
                    System.out.print("Veuillez saisir le code du cours: ");
                    String code = scan.nextLine();

                    Course course = findCourse(code, arrCourse);
                    if(course==null){
                        System.out.println("Le cours ne se trouve pas dans la liste.");
                    } else {
                       F2(prenom, nom, email, matricule, course, code);
                    }
                    break;
                default:
                    System.out.println("erreur");
            }

        }

    }

    public static Course findCourse(String code, ArrayList<Course> arrCourse){
        Course course = null;
        for(int i=0; i<arrCourse.size(); i++){
            if(code.equals(arrCourse.get(i).getCode())){
                course = arrCourse.get(i);
                break;
            }
        }
        return course;
    }

    public static ArrayList<Course> F1(Socket clientSocket, String arg) throws IOException, ClassNotFoundException {
        ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());
        os.writeObject("CHARGER " + arg);

        ObjectInputStream is = new ObjectInputStream(clientSocket.getInputStream());
        ArrayList<Course> courseArr = (ArrayList<Course>) is.readObject();

        System.out.println("Les cours offerts pendant la session d'" + arg + " sont:");
        for(int i=0; i<courseArr.size(); i++){
            System.out.println((i+1) + ". " + courseArr.get(i).getCode() + " " + courseArr.get(i).getName());
        }
        return courseArr;
    }


    public static void F2(String prenom, String nom, String email, String matricule, Course course, String code) throws IOException {
        Socket clientSocket = new Socket(IPAdress,PORT);
        ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());

        os.writeObject("INSCRIRE");
        RegistrationForm inscription = new RegistrationForm(prenom, nom, email,matricule, course);

        os.writeObject(inscription);
        System.out.println("Félicitations! Inscription réussie de " + prenom + " au cours " + code);
        os.close();
    }





}