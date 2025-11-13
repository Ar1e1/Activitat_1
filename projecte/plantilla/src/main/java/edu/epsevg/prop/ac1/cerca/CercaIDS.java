package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;

import java.util.*;

/**
 * Implementació de l'algoritme IDS (Iterative Deepening Search), que combina DFS recursiu
 * amb profunditats creixents per trobar la solució òptima amb baixa memòria.
 * Control de cicles configurable (com en DFS):
 * - Si usarLNT=false: Control local dins de la branca actual (set per camí).
 * - Si usarLNT=true: LNT global per iteració (Mapa<Mapa, Integer> per profunditat mínima dins del límit actual).
 */
public class CercaIDS extends Cerca {
    private static final int MAX_LIMIT = 80;  // Límit màxim per evitar bucles infinits

    public CercaIDS(boolean usarLNT) {
        super(usarLNT);
    }

    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        int limit = 0;  // Comença des de 0 per incloure l'inicial
        while (true) {
            if (limit > MAX_LIMIT) {
                break;
            }
            limit++;

            // Estructura per control de cicles (nova per iteració)
            Set<Mapa> camiActual = new HashSet<>();
            Map<Mapa, Integer> lnt = null;  
            if (usarLNT) {
                lnt = new HashMap<>();  
            }

            Node nodeInicial = new Node(inicial, null, null, 0, 0);

            rc.incNodesExplorats(); 

            if (idsExplorar(nodeInicial, rc, camiActual, lnt, limit)) {
                break;  
            }

            camiActual.clear();
            if (lnt != null) lnt.clear();
        }
    }

    /**
     * Funció recursiva per explorar IDS amb control de cicles
     * Retorna true si s'ha trobat la meta
     */
    private boolean idsExplorar(Node actual, ResultatCerca rc, Set<Mapa> camiActual, Map<Mapa, Integer> lnt, int limit) {
        // Parar si excedeix el límit actual
        if (actual.depth > limit) {
            return false;
        }

        if (!camiActual.add(actual.estat)) {
            rc.incNodesTallats();
            return false;
        }

        if (lnt != null) {
            Integer minProf = lnt.get(actual.estat);
            if (minProf != null && actual.depth >= minProf) {
                rc.incNodesTallats();  
                camiActual.remove(actual.estat); 
                return false;
            }
            lnt.put(actual.estat, Math.min(minProf != null ? minProf : Integer.MAX_VALUE, actual.depth));
        }

        // Check meta
        if (actual.estat.esMeta()) {
            List<Moviment> cami = reconstruirCami(actual);
            rc.setCami(cami);
            camiActual.remove(actual.estat);  
            if (lnt != null) lnt.remove(actual.estat);  
            return true;  
        }

        rc.incNodesExplorats();

        long memoriaActual = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        rc.updateMemoria((int) memoriaActual);

        List<Moviment> accions = actual.estat.getAccionsPossibles();
        for (Moviment accio : accions) {
            Mapa nouEstat = actual.estat.mou(accio);
            Node successor = new Node(nouEstat, actual, accio, actual.depth + 1, actual.g + 1);

            if (idsExplorar(successor, rc, camiActual, lnt, limit)) {
                camiActual.remove(actual.estat);  
                return true;  
            }
        }

        camiActual.remove(actual.estat);

        if (rc.getCami() != null) {
            return true;
        }

        return false;
    }

    /**
     * Reconstrueix el camí de moviments des del node final fins a l'inicial.
     * @param nodeFinal Node on s'ha trobat la meta
     * @return Llista de Moviment en ordre des d'inicial a final
     */
    private List<Moviment> reconstruirCami(Node nodeFinal) {
        List<Moviment> cami = new ArrayList<>();
        Node current = nodeFinal;
        while (current.pare != null) {
            cami.add(current.accio);
            current = current.pare;
        }
        Collections.reverse(cami);
        return cami;
    }
}