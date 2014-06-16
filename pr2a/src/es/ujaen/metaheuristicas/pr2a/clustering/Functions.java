/**
 * En este paquete se encuentra las clases necesarias para gestionar problemas
 * de clustering.
 */
package es.ujaen.metaheuristicas.pr2a.clustering;

import es.ujaen.metaheuristicas.pr2a.utils.Pair;
import es.ujaen.metaheuristicas.pr2a.utils.ReadFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Raúl Moya Reyes <www.raulmoya.es>
 */
public class Functions {

    /**
     * Método para calcular el coste de una solución.
     *
     * @param clusters List de {@link Cluster} con los patrones asignados.
     * @param centroids List de {@link Pattern} representando los centroides de
     * los clusters.
     * @return Float con el coste de la solución.
     */
    public static Float objectiveFunction(List<Cluster> clusters, List<Pattern> centroids) {

        /* Coste de la solución */
        float cost = 0;
        /* Recorre los clusters */
        for (int i = 0; i < clusters.size(); i++) {
            /* Recorre los patrones de cada cluster */
            Cluster cluster = clusters.get(i);
            for (int j = 0; j < cluster.size(); j++) {
                cost += distance(centroids.get(i), cluster.get(j));
            }
        }

        return cost;
    }

    /**
     * Método para calcular la mejora moviendo un patrón.
     *
     * @param clusters List de {@link Cluster} con todos los patrones asignados.
     * @param centroids List de {@link Pattern} que representan los centroides
     * de los clusters.
     * @param assigned Posición del cluster en el listado de cluster al que se
     * va a asignar el patrón indicado.
     * @param i Posición del cluster en el listado de cluster del que se va a
     * eliminar el patrón indicado.
     * @param j Posición del patrón que se va a mover.
     * @return Float con la mejora que produce el cambio del patrón.
     */
    public static Float factorize(List<Cluster> clusters, List<Pattern> centroids,
            int assigned, int i, int j) {

        /* Patrón que se va a mover */
        Pattern pattern = clusters.get(i).get(j);
        /* Centroide del cluster de origen */
        Pattern centroid = centroids.get(i);
        /* Centroide del cluster al que se va a asignar el patrón */
        Pattern centroidAssigned = centroids.get(assigned);
        /* Cálculo de la distancia a los dos centroides */
        Float add = 0f;
        Float addAssigned = 0f;
        for (int k = 0; k < pattern.size(); k++) {
            Float sub = pattern.get(k) - centroid.get(k);
            add += sub * sub;
            sub = pattern.get(k) - centroidAssigned.get(k);
            addAssigned += sub * sub;
        }
        /* Mejora que se produce al mover el patrón de cluster */
        Float objective = -1 * ((clusters.get(i).size() * add) / clusters.get(i).size())
                + ((clusters.get(assigned).size() * addAssigned) / clusters.get(assigned).size());

        return objective;
    }

    /**
     * Método para calcular la distancia entre dos patrones.
     *
     * @param first Un patrón con el que comparar (corresponde al centroide).
     * @param second Un patrón con el que comparar (corresponde al patrón).
     */
    private static Float distance(Pattern first, Pattern second) {
        /* Distancia entre el patrón y el centroide */
        float add = 0;
        for (int k = 0; k < second.size(); k++) {
            /* Resta de la distancia de cada componente entre el patrón y el centroide */
            float sub = second.get(k) - first.get(k);
            add += sub * sub;
        }
        return add;
    }

    /**
     * Método que calcula los centroides de los clusters.
     *
     * @param clusters List de {@link Cluster} con todos los patrones asignados.
     * @return List de {@link Pattern} que representan los centroides de los
     * clusters.
     */
    public static List<Pattern> calculateCentroids(List<Cluster> clusters) {
        /* ArrayList para crear los centroides de todos los clusters */
        List<Pattern> centroids = new ArrayList<>();
        /* Recorre los clusters */
        for (Cluster c : clusters) {
            centroids.add(calculateCentroid(c.get()));
        }
        return centroids;
    }

    /**
     * Método que calcula el centroide de un cluster.
     *
     * @param patterns List de {@link Pattern} para calcular un centroide.
     * @return {@link Pattern} que representan el centroide de un cluster.
     */
    public static Pattern calculateCentroid(List<Pattern> patterns) {

        Pattern centroid = new Pattern();
        /* Recorre cada patrón sumando cada dimension y realizando la media */
        for (int i = 0; patterns.size() > 0 && i < patterns.get(0).size(); i++) {
            float avg = 0;
            for (Pattern p : patterns) {
                avg += p.get(i);
            }
            centroid.add(avg / patterns.size());
        }

        return centroid;
    }

    /**
     *
     * @param patterns Todos los patrones para asignarlos a los clusters.
     * @param rand Números aleatorios a partir de una semilla.
     * @param numberClusters Número de clusters a generar.
     * @return List de {@link Cluster} con todos los patrones asignados.
     * @deprecated
     */
    public static List<Cluster> setInitial(List<Pattern> patterns, Random rand, Integer numberClusters) {
        /* Crea una copia de la lista de patrones */
        List<Pattern> restPattern = new ArrayList(patterns);
        /* Inicializa los clusters y los centroides */
        List<Cluster> clusters = new ArrayList<>();
        List<Pattern> centroids = new ArrayList<>();

        /* Calcula el centroide de cada cluster */
        for (int i = 0; i < numberClusters; i++) {
            int positionCandidate = rand.nextInt(restPattern.size());
            /* Asigna el patrón a un cluster */
            Pattern candidate = restPattern.remove(positionCandidate);
            clusters.add(new Cluster());
            clusters.get(i).add(candidate);
            centroids.add(i, candidate);
        }

        /* Asigna el resto de patrones al cluster que menos distancia tenga */
        for (int j = 0; j < restPattern.size(); j++) {
            float distance = 0;
            int assigned = 0;
            for (int i = 0; i < centroids.size(); i++) {
                float actualDistance = distance(centroids.get(i), restPattern.get(j));
                if (actualDistance < distance || distance == 0) {
                    distance = actualDistance;
                    assigned = i;
                }
            }
            clusters.get(assigned).add(restPattern.remove(j));
            j--;
        }

        return clusters;
    }

    /**
     * Método para crear un cromosoma a partir de una lista de clusters
     *
     * @param clusters List de {@link Cluster} con todos los patrones asignados.
     * @param patterns
     * @param rand Números aleatorios a partir de una semilla.
     * @return Chromoseme generado a partir de los clusters.
     * @deprecated comentar y probar.
     */
    public static Chromosome setChromosome(List<Cluster> clusters, List<Pattern> patterns, Random rand) {
        // copia de clusters
        List<Cluster> auxCluster = new ArrayList(clusters);
        /* Inicializa el cromosoma */
        Chromosome chromosome = new Chromosome();
//        for (Pattern pattern : patterns) {
//            chromosome.add(0);
//        }
        /* Crea un gen por cada patrón */
        while (auxCluster.size() > 0) {

            /* Selecciona un patrón de forma aleatoria y lo asigna a un gen */
            int positionCluster = rand.nextInt(auxCluster.size());
            int positionPatternCluster = rand.nextInt(auxCluster.get(positionCluster).size());
            Pattern pattern = auxCluster.get(positionCluster).remove(positionPatternCluster);
//            int positionPattern = patterns.indexOf(pattern);
            chromosome.add(pattern, positionCluster);

            if (auxCluster.get(positionCluster).size() == 0) {
                auxCluster.remove(positionCluster);
            }

        }
        return chromosome;
    }

    /**
     *
     * @param patterns
     * @param rand
     * @return null
     * @deprecated No implementada
     */
    public static Chromosome setChromosome(List<Pattern> patterns, Random rand, Integer numberClusters) {
        //mirar en el excel y programar la generación de soluciones
        List<Pattern> auxPatterns = new ArrayList(patterns);
        Chromosome chromosome = new Chromosome();
        while (auxPatterns.size() > 0) {
            int pattern = rand.nextInt(auxPatterns.size());
            int cluster = rand.nextInt(numberClusters);
            chromosome.add(new Pair(auxPatterns.remove(pattern), cluster));
        }
        return chromosome;
    }

    /**
     * @deprecated Falta comentar, comprobar que
     */
    public static List<Chromosome> crossing(List<Chromosome> populationChromosomes, Integer father, Integer mother, Random rand) {

        // cromosoma padre y madre
        Chromosome fatherChromosome = new Chromosome(populationChromosomes.get(father)); //realizar copias y no usar referencias
        Chromosome motherChromosome = new Chromosome(populationChromosomes.get(mother));

        // cortes
        int upperCut = rand.nextInt(fatherChromosome.size());
        int lowerCut = rand.nextInt(fatherChromosome.size());

        // comprobamos si upper es mayor que lower o igual
        if (upperCut == lowerCut) {
            upperCut++;
        } else if (upperCut < lowerCut) {
            int aux = upperCut;
            upperCut = lowerCut;
            lowerCut = aux;
        }

        // busca los elementos no comunes
        List<Pair<Pattern, Integer>> motherSubList = populationChromosomes.get(mother).subList(lowerCut, upperCut);
        fatherChromosome.removeAll(motherSubList); // 30 segundos en hacer este método
        List<Pair<Pattern, Integer>> fatherSubList = populationChromosomes.get(father).subList(lowerCut, upperCut);
        motherChromosome.removeAll(fatherSubList);

        Chromosome son = new Chromosome();
        Chromosome daughter = new Chromosome();
//        // crea los hijos
        for (int i = lowerCut; i < upperCut; i++) {
            son.add(populationChromosomes.get(father).get(i));
            daughter.add(populationChromosomes.get(mother).get(i));
        }
        for (int i = 0; i < populationChromosomes.get(mother).size() - upperCut; i++) {
            son.add(motherChromosome.get(i));
            daughter.add(fatherChromosome.get(i));
        }
        for (int i = 0; i < lowerCut; i++) {
            son.add(i, motherChromosome.get(i + populationChromosomes.get(mother).size() - upperCut));
            daughter.add(i, fatherChromosome.get(i + populationChromosomes.get(mother).size() - upperCut));
        }

        // los hijos
        List<Chromosome> children = new ArrayList();
        children.add(son);
        children.add(daughter);
        return children;
    }

    /**
     *
     * @param populationDistances
     * @param rand
     * @deprecated
     */
    public static Integer selected(List<Float> populationDistances, Random rand) {
        int father = rand.nextInt(populationDistances.size());
        int aux = father;
        do {
            aux = rand.nextInt(populationDistances.size());
        } while (father == aux);

        if (populationDistances.get(aux) < populationDistances.get(father)) {
            father = aux;
        }

        return father;
    }

    /**
     * @deprecated No implementado
     */
    public static List<Chromosome> mutation(List<Chromosome> children, Random rand, Integer numberCluster) {

        int chromosome = rand.nextInt(children.size());
        int gen = rand.nextInt(children.get(chromosome).size());
        int cluster = rand.nextInt(numberCluster);

        children.get(chromosome).get(gen).second = cluster;

        return children;
    }

    /**
     * @deprecated
     */
    public static Integer positionBest(List<Float> populationDistances) {
        float distance = 0;
        int best = 0;
        for (int i = 0; i < populationDistances.size(); i++) {
            if (populationDistances.get(i) < distance || distance == 0) {
                distance = populationDistances.get(i);
                best = i;
            }
        }
        return best;
    }

    /**
     * @deprecated
     */
    public static Integer positionWorst(List<Float> populationDistances) {
        float distance = 0;
        int worst = 0;
        for (int i = 0; i < populationDistances.size(); i++) {
            if (populationDistances.get(i) > distance || distance == 0) {
                distance = populationDistances.get(i);
                worst = i;
            }
        }
        return worst;
    }

    /**
     *
     * @param chromosome
     * @param numberClusters
     * @return a
     * @deprecated No terminado
     */
    public static List<Cluster> getClusterChromosome(Chromosome chromosome, Integer numberClusters) {
        List<Cluster> clusters = new ArrayList();
        for (int i = 0; i < numberClusters; i++) {
            clusters.add(new Cluster());
        }
        for (int i = 0; i < chromosome.size(); i++) {
            clusters.get(chromosome.get(i).second).add(chromosome.get(i).first);
        }
        return clusters;
    }

    /**
     * Método para calcular el umbral de patrones aceptados en la lista
     * restringida.
     *
     * @param distances Distancia de cada patrón.
     * @return Double que representa el umbral para aceptar patrones en la lista
     * restringida.
     */
    public static float bestDistance(List<Float> distances) {
        float best = 0;
        for (Float distance : distances) {
            if (distance > best || best == 0) {
                best = distance;
            }
        }
        return best;
    }

    /**
     *
     * @return a
     * @deprecated No implementada.
     */
    public static Integer positionBestDistance(List<Float> distances) {
        float best = 0;
        int position = 0;
        for (int i = 0; i < distances.size(); i++) {
            if (distances.get(i) > best || best == 0) {
                best = distances.get(i);
                position = i;
            }
        }
        return position;
    }

    /**
     * Método para cargar un dataset en un List de {@link Pattern}.
     *
     * @param fileName Ruta del archivo.
     * @return List de {@link Pattern} con todos los patrones del dataset
     * indicado en el fileName.
     */
    public static List<Pattern> readData(String fileName) {
        /* ArrayList con todos los patrones del dataset */
        List<Pattern> patterns = new ArrayList<>();

        try {
            /* Url relativa al paquete */
            fileName = URLDecoder.decode(Functions.class
                    .getResource("../instances/" + fileName).getFile(), "UTF-8");
            /* Archivo con los datos */
            ReadFile file = new ReadFile(fileName);
            /* Líneas del archivo que corresponden con cada patrón */
            String line;
            while ((line = file.readLine()) != null) {
                /* División de la línea por espacios que corresponde con cada dato del patrón */
                String[] datas = line.split(" ");
                /* Parse de string a Pattern */
                Pattern p = new Pattern();
                for (String data : datas) {
                    p.add(Float.parseFloat(data));
                }
                /* Añadido el patrón al listado de patrones */
                patterns.add(p);
            }
        } catch (UnsupportedEncodingException ex) {
            System.out.println("An error has ocurred with encoding URL. Error message: " + ex.toString());
        }

        return patterns;
    }

    /**
     * Método para imprimir en consola el resultado de una ejecución.
     *
     * @param fileName Nombre del dataset utilizado.
     * @param patterns List de patrones utilizados.
     * @param clusters list de clusters utilizados con los patrones asignados a
     * cada uno.
     * @param centroids List de patrones que representa los centroides de cada
     * cluster.
     * @param seed Semilla utilizada.
     * @param initialCost Coste de la solución inicial.
     * @param finalCost Coste de la solución tras aplicar el algoritmo.
     * @param time Tiempo de ejecución del algoritmo.
     */
    public static void print(String fileName, List<Pattern> patterns,
            List<Cluster> clusters, List<Pattern> centroids, Integer seed,
            Float initialCost, Float finalCost, Long time) {
        System.out.println("======================================================================\n"
                + "    " + fileName + "\n"
                + "======================================================================");
        System.out.println("Lectura del dataset " + fileName);
        System.out.println("Número de patrones: " + patterns.size());
        System.out.println("Dimensión de los patrones: " + patterns.get(0).size());
        System.out.println("Número de clusters: " + clusters.size());
        System.out.println("Semilla: " + seed);
        System.out.println("Tiempo de ejecución: " + time + " milisegundos");
//        System.out.println("Número de centroides: " + centroids.size());
//        System.out.println("    Centroide inicial 1: " + centroids.get(0).toString());
//        System.out.println("    Centroide inicial 2: " + centroids.get(1).toString());
        System.out.println("Coste inicial: " + initialCost);
        System.out.println("===================== Coste final: " + finalCost + " ======================");
        System.out.println("\n");

    }

}
