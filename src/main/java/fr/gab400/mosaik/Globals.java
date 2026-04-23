package fr.gab400.mosaik;

import java.util.Scanner;

public class Globals {

    public static final int WIDTH = 1280;
    public static final int HEIGHT = WIDTH * 9 / 16;

    // Dimensions de la mosaïque (maintenant choisies par l'utilisateur)
    public static int GRID_WIDTH = 2;
    public static int GRID_HEIGHT = 1;

    public static final float CELL_SIZE = 0.3f;
    public static final float CELL_SPACING = 0.02f;
    public static final float ZOOM_LEVEL = 0.01f;

    // Couleurs (on importe directement Color au lieu de *)
    public static final java.awt.Color BACK_COLOR = java.awt.Color.BLACK;
    public static final java.awt.Color GRID_COLOR = java.awt.Color.DARK_GRAY;
    public static final java.awt.Color BLUE_COLOR = java.awt.Color.BLUE;
    public static final java.awt.Color GREEN_COLOR = java.awt.Color.GREEN;
    public static final java.awt.Color YELLOW_COLOR = java.awt.Color.YELLOW;
    public static final java.awt.Color RED_COLOR = java.awt.Color.RED;

    /**
     * On demande à l'utilisateur les dimensions de la mosaïque au démarrage
     */
    public static void demanderDimensions() {
        Scanner sc = new Scanner(System.in);

        System.out.println("");
        System.out.println(" ***** CONFIGURATION DE LA MOSAÏQUE ***** ");
        System.out.println("\n");

        GRID_WIDTH = demanderEntier(sc, "Nombre de colonnes (GRID_WIDTH) : ", 1, 100);
        GRID_HEIGHT = demanderEntier(sc, "Nombre de lignes   (GRID_HEIGHT) : ", 1, 100);

        System.out.println("\nMosaïque configurée avec succès !");
        System.out.println("   → " + GRID_WIDTH + " colonnes × " + GRID_HEIGHT + " lignes");
        System.out.println("   → La fenêtre va maintenant s'ouvrir.\n");
    }

	/**
	 * message d'erreur pour dire que l'utilisateur est complètement con !
	 */

    private static int demanderEntier(Scanner sc, String message, int min, int max) {
        int valeur;
        do {
            System.out.print(message);
            while (!sc.hasNextInt()) {
                System.out.println("Veuillez entrer un nombre entier.");
                sc.next();
            }
            valeur = sc.nextInt();

            if (valeur < min || valeur > max) {
                System.out.println("La valeur doit être comprise entre " + min + " et " + max + ".");
            }
        } while (valeur < min || valeur > max);

        return valeur;
    }
}