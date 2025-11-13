package edu.epsevg.prop.ac1.cerca.heuristica;

import edu.epsevg.prop.ac1.model.Mapa;
import edu.epsevg.prop.ac1.model.Posicio;


/**
 * Heurística avançada: Suma de distàncies de Manhattan.
 * - Distància mínima d'un agent a la sortida (com en HeuristicaBasica).
 * - Més cada distància mínima d'un agent a les claus pendents (no recollides).
 * Admissible: No sobreestima el cost real (cada distància és un límit inferior).
 * Més informativa que la bàsica en mapes amb claus/portes, guiant cap a recollir-les primer.
 */
public class HeuristicaAvancada implements Heuristica {
    @Override
    public int h(Mapa estat) {
        if (estat.esMeta()) {
            return 0;
        }

        int heuristica = 0;
        Posicio sortida = estat.getSortidaPosicio();

        // Part 1: Manhattan mínim a la sortida (com bàsica)
        int minDistSortida = Integer.MAX_VALUE;
        for (Posicio agent : estat.getAgents()) {
            int dist = Math.abs(agent.x - sortida.x) + Math.abs(agent.y - sortida.y);
            minDistSortida = Math.min(minDistSortida, dist);
        }
        heuristica += minDistSortida;

        // Part 2: Distància a claus pendents
        // Escanejar grid per trobar i calcular distàncies
        for (int i = 0; i < estat.getN(); i++) {
            for (int j = 0; j < estat.getM(); j++) {
                int cell = estat.getCell(i, j);
                if (Character.isLowerCase(cell)) {
                    char clauChar = (char) cell;
                    if (!estat.teClau(clauChar)) {
                        Posicio posClau = new Posicio(i, j);
                        int minDistClau = Integer.MAX_VALUE;
                        // Manhattan mínim d'un agent a aquesta clau
                        for (Posicio agent : estat.getAgents()) {
                            int dist = Math.abs(agent.x - posClau.x) + Math.abs(agent.y - posClau.y);
                            minDistClau = Math.min(minDistClau, dist);
                        }
                        heuristica += minDistClau;
                    }
                }
            }
        }

        return heuristica;
    }
}
