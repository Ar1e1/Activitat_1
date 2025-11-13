package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;

import java.util.*;

/**
 * Implementació de l'algoritme DFS (Depth-First Search) utilitzant Nodes per a l'arbre d'exploració.
 * Explora profundament primer, amb límit de profunditat 50 per evitar bucles infinitos.
 * Control de cicles
 * - Si usarLNT=false: Control local dins de la branca actual (recorregut recursiu cap amunt o set equivalent per estalviar memòria).
 * - Si usarLNT=true: Control global amb LNT (Llista de Nodes Tancats): Mapa<Mapa, Integer> per registrar profunditat mínima visitada.
 */
public class CercaDFS extends Cerca {
    private static final int LIMIT_PROFUNDITAT = 80;  // Límit per evitar explosió recursiva (segons espec)

    public CercaDFS(boolean usarLNT) {
        super(usarLNT);
    }

    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {

        Set<Mapa> camiActual = new HashSet<>(); 
        Map<Mapa, Integer> lnt = null;  
        if (usarLNT) {
            lnt = new HashMap<>();  
        }

        // Node inicial: depth=0, g=0
        Node nodeInicial = new Node(inicial, null, null, 0, 0);
        rc.incNodesExplorats();  

        explorar(nodeInicial, rc, camiActual, lnt);

        camiActual.clear();
        if (lnt != null) lnt.clear();
    }

    /**
     * Funció recursiva per explorar DFS amb límit de profunditat i control de cicles configurable.
     * @param actual Node actual a explorar
     * @param rc Resultats per actualitzar mètriques
     * @param camiActual Conjunt d'estats en el camí actual (per detectar cicles locals)
     * @param lnt Mapa global per profunditat mínima (si usarLNT=true)
     */
    private void explorar(Node actual, ResultatCerca rc, Set<Mapa> camiActual, Map<Mapa, Integer> lnt) {
        if (actual.depth > LIMIT_PROFUNDITAT) {
            return;
        }

        if (!camiActual.add(actual.estat)) {
            rc.incNodesTallats();  
            return;
        }

        // Control global (si usarLNT=true): si ja visitat a menor profunditat, tallar
        if (lnt != null) {
            Integer minProf = lnt.get(actual.estat);
            if (minProf != null && actual.depth >= minProf) {
                rc.incNodesTallats(); 
                camiActual.remove(actual.estat);  
                return;
            }
            // Actualitzar LNT amb profunditat actual (si millor)
            lnt.put(actual.estat, Math.min(minProf != null ? minProf : Integer.MAX_VALUE, actual.depth));
        }

        // Comprovar si és meta
        if (actual.estat.esMeta()) {
            List<Moviment> cami = reconstruirCami(actual);
            rc.setCami(cami);
            camiActual.remove(actual.estat);  
            if (lnt != null) lnt.remove(actual.estat);  
            return;
        }

        rc.incNodesExplorats();  

        long memoriaActual = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        rc.updateMemoria((int) memoriaActual);

        List<Moviment> accions = actual.estat.getAccionsPossibles();
        for (Moviment accio : accions) {
            Mapa nouEstat = actual.estat.mou(accio);
            Node successor = new Node(nouEstat, actual, accio, actual.depth + 1, actual.g + 1);

            explorar(successor, rc, camiActual, lnt);
        }

        camiActual.remove(actual.estat);

        if (rc.getCami() != null) {
            return;
        }
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