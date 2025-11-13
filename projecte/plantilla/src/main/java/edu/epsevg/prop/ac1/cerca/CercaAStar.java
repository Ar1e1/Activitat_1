package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.cerca.heuristica.Heuristica;
import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;

import java.util.*;

/**
 * Implementació de l'algoritme A* (A Estrella), cerca informada amb heurística.
 * Utilitza una PriorityQueue per prioritzar nodes per f = g (cost real) + h (heurística estimada).
 */
public class CercaAStar extends Cerca {

    private final Heuristica heur;

    public CercaAStar(boolean usarLNT, Heuristica heur) { 
        super(usarLNT); 
        this.heur = heur; 
    }

    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        // Frontera: PriorityQueue ordenada per f = g + h (min-heap, menor f primer)
        PriorityQueue<Node> frontera = new PriorityQueue<>(
            (n1, n2) -> {
                double f1 = n1.g + heur.h(n1.estat);
                double f2 = n2.g + heur.h(n2.estat);
                return Double.compare(f1, f2);  // Tie-breaking: menor g si f igual
            }
        );

        // Conjunt per control de cicles configurable
        Set<Mapa> visitatsSimple = null;
        Map<Mapa, Integer> lnt = null;
        if (!usarLNT) {
            visitatsSimple = new HashSet<>();  // Control simple global
        } else {
            lnt = new HashMap<>();  // LNT: estat -> profunditat mínima
        }

        // Node inicial: depth=0, g=0
        Node nodeInicial = new Node(inicial, null, null, 0, 0);
        frontera.add(nodeInicial);

        // Marcar inicial
        if (!usarLNT) {
            visitatsSimple.add(inicial);
        } else {
            lnt.put(inicial, 0);
        }
        rc.incNodesExplorats();  // Node inicial explorat

        boolean trobat = false;
        Node nodeFinal = null;

        while (!frontera.isEmpty() && !trobat) {
            Node actual = frontera.poll();
            rc.incNodesExplorats();  // Cada node extret s'explora

            // Check si és meta
            if (actual.estat.esMeta()) {
                trobat = true;
                nodeFinal = actual;
                break;
            }

            // Generar successors
            List<Moviment> accions = actual.estat.getAccionsPossibles();
            for (Moviment accio : accions) {
                Mapa nouEstat = actual.estat.mou(accio);
                Node successor = new Node(nouEstat, actual, accio, actual.depth + 1, actual.g + 1);

                // Control de cicles configurable
                boolean descartat = false;
                if (!usarLNT) {
                    if (visitatsSimple.contains(nouEstat)) {
                        rc.incNodesTallats();  // Duplicat: tallat
                        descartat = true;
                    } else {
                        visitatsSimple.add(nouEstat);
                        frontera.add(successor);
                    }
                } else {
                    // LNT: si ja visitat a menor o igual profunditat, tallar (però permet reobrir si millor)
                    Integer minProf = lnt.get(nouEstat);
                    if (minProf != null && successor.depth >= minProf) {
                        rc.incNodesTallats();
                        descartat = true;
                    } else {
                        // Actualitzar amb min profunditat (permèt reexpandir si millor)
                        lnt.put(nouEstat, Math.min(minProf != null ? minProf : Integer.MAX_VALUE, successor.depth));
                        frontera.add(successor);
                    }
                }

                if (descartat) {
                    continue;
                }
            }

            // Actualitzar pic de memòria
            long memoriaActual = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            rc.updateMemoria((int) memoriaActual);
        }

        // Reconstruir camí si trobat
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

        // Alliberar memòria
        frontera.clear();
        if (visitatsSimple != null) visitatsSimple.clear();
        if (lnt != null) lnt.clear();
    }
}