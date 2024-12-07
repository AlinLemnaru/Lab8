import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Pb_Main {
    public static void displayMeniu() {
        System.out.println(
                """
                        1. Adauga o persoana
                        2. Adauga o excursie
                        3. Afisarea tuturor persoanelor si a excursiilor acestora
                        4. Afiseaza excursiile unei persoane
                        5. Afisarea tuturor persoanelor care au vizitat o destinatie
                        6. Afisarea persoanelor care au calatorit intr-un anumit an
                        7. Stergerea unei excursii
                        8. Stergerea unei persoane
                        0. Iesire
                        """
        );
    }

    public static int citireVarsta(Scanner scanner) throws ExceptieVarsta {
        System.out.print("Introduceti varsta: ");
        try {
            int varsta = scanner.nextInt();
            if (varsta <= 0 || varsta >= 120) { // Limitare
                throw new ExceptieVarsta("Varsta trebuie sa fie cuprinsa intre 0 si 120!");
            }
            return varsta;
        } catch (NumberFormatException | InputMismatchException e) {
            throw new ExceptieVarsta("Varsta trebuie sa fie un numar intreg!");
        }
    }

    public static int citireAnExcursie(Scanner scanner, int anNastere) throws ExceptieAnExcursie {
        System.out.print("Introduceti anul excursiei: ");
        try {
            int an = scanner.nextInt();
            int anCurent = java.time.Year.now().getValue();
            if (an <= anNastere || an > anCurent) { // Limitare
                throw new ExceptieAnExcursie("Anul trebuie sa fie cuprinsa intre data nasterii si anul curent!");
            }
            return an;
        } catch (NumberFormatException | InputMismatchException e) {
            throw new ExceptieAnExcursie("Anul trebuie sa fie un numar intreg!");
        }
    }

    public static void main(String[] args) throws SQLException {
        // Scanner
        Scanner sc = new Scanner(System.in);

        // Conexiune Server
        String url = "jdbc:mysql://localhost:3306/lab8";
        Connection connection = DriverManager.getConnection(url, "root", "root");

        int optiune = 0, randuriAfectate;

        while (true) {
            displayMeniu();

            while (true) {
                System.out.print("Optiunea dvs: ");

                try {
                    optiune = sc.nextInt();
                    break;
                } catch (InputMismatchException e) {
                    System.out.println("\n\nOptiune invalida!\n");
                    sc.next();
                }
            }

            System.out.println("\n");

            switch (optiune) {
                case 1:
                    System.out.println("### Adauga o persoana ###");

                    String sql = "INSERT INTO persoane (nume, varsta) VALUES (?, ?)";
                    String nume;
                    int varsta;

                    PreparedStatement ps = connection.prepareStatement(sql);

                    sc.nextLine();

                    System.out.print("Introduceti numele: ");
                    nume = sc.nextLine();

                    try {
                        varsta = citireVarsta(sc);
                    } catch (ExceptieVarsta e) {
                        System.out.println(e.getMessage());
                        break;
                    }

                    ps.setString(1, nume);
                    ps.setInt(2, varsta);

                    randuriAfectate = ps.executeUpdate();
                    System.out.println("Randuri inserate: " + randuriAfectate);

                    ps.close();

                    System.out.println("#########################");
                    break;
                case 2:
                    System.out.println("### Adauga o excursie ###");

                    String sqlVerifPersoana = "SELECT id, varsta FROM persoane WHERE nume = ?";
                    String sqlInsertExcursie = "INSERT INTO excursii (id_persoana, destinatia, anul) VALUES (?, ?, ?)";

                    sc.nextLine();

                    System.out.print("Introduceti numele persoanei: ");
                    String numePersoana = sc.nextLine();

                    PreparedStatement psVerificare = connection.prepareStatement(sqlVerifPersoana);
                    psVerificare.setString(1, numePersoana);
                    ResultSet rs = psVerificare.executeQuery();

                    if (rs.next()) {
                        int idPersoana = rs.getInt("id");
                        int varstaPersoana = rs.getInt("varsta");

                        System.out.print("Introduceți destinația excursiei: ");
                        String destinatia = sc.nextLine();

                        int anulExcursiei;
                        try {
                            int anulNasterii = java.time.Year.now().getValue() - varstaPersoana;
                            anulExcursiei = citireAnExcursie(sc, anulNasterii);
                        } catch (ExceptieAnExcursie e) {
                            System.out.println(e.getMessage());
                            rs.close();
                            psVerificare.close();
                            break;
                        }

                        PreparedStatement psInserare = connection.prepareStatement(sqlInsertExcursie);
                        psInserare.setInt(1, idPersoana);
                        psInserare.setString(2, destinatia);
                        psInserare.setInt(3, anulExcursiei);

                        randuriAfectate = psInserare.executeUpdate();
                        System.out.println("Randuri inserate in excursii: " + randuriAfectate);

                        psInserare.close();
                    } else {
                        System.out.println("Persoana nu exita în tabel.");
                    }

                    rs.close();
                    psVerificare.close();

                    System.out.println("#########################");
                    break;
                case 3:
                    System.out.println("### Afisare detaliata ###");

                    String sqlPers = "SELECT * FROM persoane";
                    String sqlExc = "SELECT destinatia, anul FROM excursii WHERE id_persoana = ?";

                    PreparedStatement psPers = connection.prepareStatement(sqlPers);
                    PreparedStatement psExc = connection.prepareStatement(sqlExc);

                    ResultSet rsPers = psPers.executeQuery();

                    while (rsPers.next()) {
                        int idPers = rsPers.getInt("id");

                        System.out.println("\n" + idPers + ", "
                                + rsPers.getString("nume") + ", "
                                + rsPers.getString("varsta") + "\nExcursii:");

                        psExc.setInt(1, idPers);
                        ResultSet rsExc = psExc.executeQuery();

                        while (rsExc.next()) {
                            String destinatia = rsExc.getString("destinatia");
                            int anulExcursiei = rsExc.getInt("anul");

                            System.out.println("- " + destinatia + ", " + anulExcursiei);
                        }

                        rsExc.close();

                        System.out.println("--------------------");
                    }

                    psPers.close();
                    psExc.close();
                    rsPers.close();

                    System.out.println("#########################");
                    break;
                case 4:
                    System.out.println("### Afisarea excursiilor unei persoane ###");

                    String sqlPers1 = "SELECT * FROM persoane WHERE nume = ?";
                    String sqlExc1 = "SELECT destinatia, anul FROM excursii WHERE id_persoana = ?";

                    PreparedStatement psPers1 = connection.prepareStatement(sqlPers1);
                    PreparedStatement psExc1 = connection.prepareStatement(sqlExc1);

                    sc.nextLine();

                    System.out.print("Introduceti numele persoanei: ");
                    String numePers1 = sc.nextLine();

                    psPers1.setString(1, numePers1);

                    ResultSet rsPers1 = psPers1.executeQuery();

                    while (rsPers1.next()) {
                        int idPers = rsPers1.getInt("id");

                        System.out.println("\n" + idPers + ", "
                                + rsPers1.getString("nume") + ", "
                                + rsPers1.getString("varsta") + "\nExcursii:");

                        psExc1.setInt(1, idPers);
                        ResultSet rsExc = psExc1.executeQuery();

                        while (rsExc.next()) {
                            String destinatia = rsExc.getString("destinatia");
                            int anulExcursiei = rsExc.getInt("anul");

                            System.out.println("- " + destinatia + ", " + anulExcursiei);
                        }

                        rsExc.close();

                        System.out.println("--------------------");
                    }

                    psPers1.close();
                    psExc1.close();
                    rsPers1.close();

                    System.out.println("##########################################");
                    break;
                case 5:
                    System.out.println("### Afisarea persoanelor ce au vizitat o destinatie ###");

                    String sqlPers2 = "SELECT * FROM persoane WHERE id = ?";
                    String sqlExc2 = "SELECT * FROM excursii WHERE destinatia = ?";

                    PreparedStatement psPers2 = connection.prepareStatement(sqlPers2);
                    PreparedStatement psExc2 = connection.prepareStatement(sqlExc2);

                    sc.nextLine();

                    System.out.print("Introduceti destinatia: ");
                    String destinatia = sc.nextLine();

                    psExc2.setString(1, destinatia);

                    ResultSet rsExc2 = psExc2.executeQuery();

                    while (rsExc2.next()) {
                        int idPers = rsExc2.getInt("id_persoana");

                        psPers2.setInt(1, idPers);
                        ResultSet rsPers2 = psPers2.executeQuery();

                        while (rsPers2.next()) {
                            System.out.println(rsPers2.getInt("id") + ", "
                                    + rsPers2.getString("nume") + ", "
                                    + rsPers2.getString("varsta"));
                        }

                        rsPers2.close();

                        System.out.println("--------------------");
                    }

                    psPers2.close();
                    psExc2.close();
                    rsExc2.close();

                    System.out.println("#######################################################");
                    break;
                case 6:
                    System.out.println("### Afisarea celor ce au calatorit intr-un anumit an ###");

                    String sqlPers3 = "SELECT * FROM persoane WHERE id = ?";
                    String sqlExc3 = "SELECT * FROM excursii WHERE anul = ?";

                    PreparedStatement psPers3 = connection.prepareStatement(sqlPers3);
                    PreparedStatement psExc3 = connection.prepareStatement(sqlExc3);

                    System.out.print("Introduceti destinatia: ");
                    int anul = sc.nextInt();

                    psExc3.setInt(1, anul);

                    ResultSet rsExc3 = psExc3.executeQuery();

                    while (rsExc3.next()) {
                        int idPers = rsExc3.getInt("id_persoana");

                        psPers3.setInt(1, idPers);
                        ResultSet rsPers3 = psPers3.executeQuery();

                        while (rsPers3.next()) {
                            System.out.println(rsPers3.getInt("id") + ", "
                                    + rsPers3.getString("nume") + ", "
                                    + rsPers3.getString("varsta"));
                        }

                        rsPers3.close();

                        System.out.println("--------------------");
                    }

                    psPers3.close();
                    psExc3.close();
                    rsExc3.close();

                    System.out.println("########################################################");
                    break;
                case 7:
                    System.out.println("### Stergerea unei excursii ###");

                    String sqlVerificare = "SELECT COUNT(*) FROM excursii WHERE id_excursie = ?";
                    String sqlStergere = "DELETE FROM excursii WHERE id_excursie = ?";

                    System.out.print("Introduceti ID-ul: ");
                    int idExcursie = sc.nextInt();

                    PreparedStatement psVerificare1 = connection.prepareStatement(sqlVerificare);
                    psVerificare1.setInt(1, idExcursie);
                    ResultSet rs1 = psVerificare1.executeQuery();

                    rs1.next();
                    int count = rs1.getInt(1);

                    if (count == 0) {
                        System.out.println("Excursia cu ID-ul specificat nu exista.");
                    } else {
                        PreparedStatement psStergere = connection.prepareStatement(sqlStergere);
                        psStergere.setInt(1, idExcursie);

                        int randuriSterse = psStergere.executeUpdate();

                        if (randuriSterse > 0) {
                            System.out.println("Excursia a fost stearsa cu succes.");
                        } else {
                            System.out.println("Problema la stergerea excursiei");
                        }

                        psStergere.close();
                    }

                    psVerificare1.close();
                    rs1.close();

                    System.out.println("###############################");
                    break;
                case 8:
                    System.out.println("### Stergerea unei persoane ###");

                    String sqlVerificarePersoana = "SELECT COUNT(*) FROM persoane WHERE id = ?";
                    String sqlStergereExcursii = "DELETE FROM excursii WHERE id_persoana = ?";
                    String sqlStergerePersoana = "DELETE FROM persoane WHERE id = ?";

                    System.out.print("Introduceti ID-ul: ");
                    int idPersoana = sc.nextInt();

                    PreparedStatement psVerificarePersoana = connection.prepareStatement(sqlVerificarePersoana);
                    psVerificarePersoana.setInt(1, idPersoana);
                    ResultSet rsVerificare = psVerificarePersoana.executeQuery();

                    rsVerificare.next();
                    int countPersoana = rsVerificare.getInt(1);

                    if (countPersoana == 0) {
                        System.out.println("Persoana cu ID-ul specificat nu exista.");
                    } else {
                        PreparedStatement psStergereExcursii = connection.prepareStatement(sqlStergereExcursii);
                        psStergereExcursii.setInt(1, idPersoana);
                        psStergereExcursii.executeUpdate();

                        PreparedStatement psStergerePersoana = connection.prepareStatement(sqlStergerePersoana);
                        psStergerePersoana.setInt(1, idPersoana);

                        int randuriSterse = psStergerePersoana.executeUpdate();

                        if (randuriSterse > 0) {
                            System.out.println("Persoana si excursiile au fost șterse cu succes.");
                        } else {
                            System.out.println("Problema la ștergerea persoanei.");
                        }

                        psStergerePersoana.close();
                    }

                    psVerificarePersoana.close();
                    rsVerificare.close();

                    System.out.println("###############################");
                    break;
                case 0:
                    System.out.println("Iesire...");
                    connection.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Optiune invalida!");
                    break;
            }

            System.out.println("\n");
        }
    }
}
