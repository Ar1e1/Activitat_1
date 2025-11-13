package edu.epsevg.prop.ac1.cerca.heuristica;
import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.model.Mapa;


/** 
 * Distància de Manhattan a la clau més propera 
 * (si queden per recollir) o a la sortida.
 */
public class HeuristicaBasica implements Heuristica {
    @Override
    public int h(Mapa estat) {
        if (estat.esMeta()) {
            return 0;
        }

        Posicio sortida = estat.getSortidaPosicio();
        int minDist = Integer.MAX_VALUE;

        // Calcular Manhattan mínim entre qualsevol agent i la sortida
        for (Posicio agent : estat.getAgents()) {
            int dist = Math.abs(agent.x - sortida.x) + Math.abs(agent.y - sortida.y);
            if (dist < minDist) {
                minDist = dist;
            }
        }

        return minDist;
    }
}
