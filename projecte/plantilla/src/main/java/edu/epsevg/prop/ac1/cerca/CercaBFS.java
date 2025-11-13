package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;

import java.util.*;

/**
 * Implementació de l'algoritme BFS (Breadth-First Search) utilitzant Nodes per a l'arbre d'exploració.
 * Explora nivell a nivell, garantint la solució òptima en termes de cost uniforme (cost = 1 per moviment).
 * Control de cicles
 * - Si usarLNT=false: Control simple global amb Set<Mapa> (evita reexpandir qualsevol estat).
 * - Si usarLNT=true: LNT global amb Map<Mapa, Integer> per profunditat mínima visitada.
 */
public class CercaBFS extends Cerca {
    public CercaBFS(boolean usarLNT) {
        super(usarLNT);
    }

    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        // Estructura de la frontera: cua FIFO per BFS
        Queue<Node> frontera = new LinkedList<>();

        // Conjunt per control de cicles
        Set<Mapa> visitatsSimple = null;
        Map<Mapa, Integer> lnt = null;
        if (!usarLNT) {
            visitatsSimple = new HashSet<>();  
        } else {
            lnt = new HashMap<>();  
        }

        // Node inicial: depth=0, g=0 (cost acumulat)
        Node nodeInicial = new Node(inicial, null, null, 0, 0);
        frontera.add(nodeInicial);

        if (!usarLNT) {
            visitatsSimple.add(inicial);
        } else {
            lnt.put(inicial, 0); 
        }
        rc.incNodesExplorats();  

        boolean trobat = false;
        Node nodeFinal = null;

        while (!frontera.isEmpty() && !trobat) {
            Node actual = frontera.poll();
            rc.incNodesExplorats();  

            if (actual.estat.esMeta()) {
                trobat = true;
                nodeFinal = actual;
                break;
            }

            List<Moviment> accions = actual.estat.getAccionsPossibles();
            for (Moviment accio : accions) {
                Mapa nouEstat = actual.estat.mou(accio);
                Node successor = new Node(nouEstat, actual, accio, actual.depth + 1, actual.g + 1);

                boolean descartat = false;
                if (!usarLNT) {
                    if (visitatsSimple.contains(nouEstat)) {
                        rc.incNodesTallats();
                        descartat = true;
                    } else {
                        visitatsSimple.add(nouEstat);
                        frontera.add(successor);
                    }
                } else {
                    Integer minProf = lnt.get(nouEstat);
                    if (minProf != null && successor.depth >= minProf) {
                        rc.incNodesTallats();
                        descartat = true;
                    } else {
                        lnt.put(nouEstat, Math.min(minProf != null ? minProf : Integer.MAX_VALUE, successor.depth));
                        frontera.add(successor);
                    }
                }

                if (!descartat) {
                }
            }

            long memoriaActual = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            rc.updateMemoria((int) memoriaActual);
        }

        if (trobat && nodeFinal != null) {
            List<Moviment> cami = new ArrayList<>();
            Node current = nodeFinal;
            while (current.pare != null) {
                cami.add(current.accio);
                current = current.pare;
            }
            Collections.reverse(cami); 
            rc.setCami(cami);
        }

        frontera.clear();
        if (visitatsSimple != null) visitatsSimple.clear();
        if (lnt != null) lnt.clear();
    }
}