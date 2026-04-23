package fr.gab400.mosaik;

import java.awt.Color;
import java.util.*;

/**
 * Calculateur de possibilités pour la mosaïque.
 * Applique la règle des 4 couleurs distinctes par cellule et l'uniformité des bordures extérieures.
 */
public class PossibilityCalculator {

    private final Grid grid;
    private List<List<Color>> validPermutations;

    public PossibilityCalculator(Grid grid) {
        this.grid = grid;
        initializeValidPermutations();
    }

    /**
     * Pré-calcule les 24 permutations possibles (4! = 24).
     * Chaque petit carré contient les 4 couleurs distinctes.
     */
    private void initializeValidPermutations() {
        Color[] colors = {Globals.BLUE_COLOR, Globals.GREEN_COLOR, Globals.YELLOW_COLOR, Globals.RED_COLOR};
        validPermutations = new ArrayList<>();
        permute(Arrays.asList(colors), 0, validPermutations);
    }

    private void permute(List<Color> arr, int k, List<List<Color>> result) {
        if (k == arr.size()) {
            result.add(new ArrayList<>(arr));
            return;
        }
        for (int i = k; i < arr.size(); i++) {
            Collections.swap(arr, k, i);
            permute(arr, k + 1, result);
            Collections.swap(arr, k, i);
        }
    }

    public long calculateMaxPossibilities() {
        return backtrack(0);
    }

    private long backtrack(int cellIndex) {
        if (cellIndex == grid.getCells().size()) {
            return 1; // Une configuration valide trouvée
        }

        Grid.Cell cell = grid.getCells().get(cellIndex);
        long totalWays = 0;

        for (List<Color> candidate : validPermutations) {
            if (isCompatible(cell, candidate)) {
                // Sauvegarde pour le backtracking
                Color[] backup = saveState(cell);
                
                // Application de la configuration
                applyCandidate(cell, candidate);

                // Récursion
                totalWays += backtrack(cellIndex + 1);

                // Nettoyage (Retour en arrière)
                restoreState(cell, backup);
            }
        }

        return totalWays;
    }

    /**
     * Vérifie si la configuration proposée respecte :
     * 1. La correspondance avec les voisins internes.
     * 2. L'uniformité des bordures sur les faces extérieures (Contrainte Super-Carré).
     */
    private boolean isCompatible(Grid.Cell cell, List<Color> candidate) {
        for (int i = 0; i < 4; i++) {
            Grid.Cell.Border currentBorder = cell.getDivision()[i];
            Color proposedColor = candidate.get(i);
            
            Grid.Cell.Border neighbor = grid.getNeighbor(currentBorder);
            
            if (neighbor != null) {
                // CAS 1 : Bordure INTERNE
                // Si le voisin est déjà colorié, les couleurs doivent correspondre
                if (neighbor.getColor() != Globals.GRID_COLOR) {
                    if (!neighbor.getColor().equals(proposedColor)) {
                        return false;
                    }
                }
            } else {
                // CAS 2 : Bordure EXTERNE (Bord de la mosaïque)
                // On impose que toute la ligne/colonne extérieure ait la même couleur
                Grid.Cell.Border[] line = grid.getLineOrColumn(currentBorder);
                for (Grid.Cell.Border faceBorder : line) {
                    // Si une autre cellule sur cette face extérieure a déjà été fixée
                    if (faceBorder.getColor() != Globals.GRID_COLOR) {
                        if (!faceBorder.getColor().equals(proposedColor)) {
                            return false; // La face extérieure n'est pas unie
                        }
                    }
                }
            }
        }
        return true;
    }

    private Color[] saveState(Grid.Cell cell) {
        Color[] state = new Color[4];
        for (int i = 0; i < 4; i++) state[i] = cell.getDivision()[i].getColor();
        return state;
    }

    private void applyCandidate(Grid.Cell cell, List<Color> colors) {
        for (int i = 0; i < 4; i++) cell.getDivision()[i].color(colors.get(i));
    }

    private void restoreState(Grid.Cell cell, Color[] backup) {
        for (int i = 0; i < 4; i++) cell.getDivision()[i].color(backup[i]);
    }
}