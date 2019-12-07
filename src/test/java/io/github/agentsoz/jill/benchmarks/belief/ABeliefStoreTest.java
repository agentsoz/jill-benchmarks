package io.github.agentsoz.jill.benchmarks.belief;

import io.github.agentsoz.jill.core.beliefbase.BeliefBase;
import io.github.agentsoz.jill.core.beliefbase.BeliefBaseException;
import io.github.agentsoz.jill.core.beliefbase.BeliefSetField;
import io.github.agentsoz.jill.core.beliefbase.abs.ABeliefStore;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class ABeliefStoreTest {

    @Test
    public void testTheoreticalMemoryUsage() {
        final int repeats = 20;
        final int[] optAgents = {1000000, 100000, 10000, 1000, 100, 10};
        final int[] optCardinality = {10000, 1000, 100, 10, 1};
        final int[] optBeliefs = {100, 10, 1};

        final String outfile = "./testABeliefStore-Theoretical-Ints.csv";
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(outfile), true);
            out.println("type,repeats,beliefs,cardinality,agents,mem_mb");

            for (int i = 0; i < optBeliefs.length; i++) {
                for (int j = 0; j < optCardinality.length; j++) {
                    for (int k = 0; k < optAgents.length; k++) {
                        int beliefs = optBeliefs[i];
                        int cardinality = optCardinality[j];
                        int agents = optAgents[k];

                        // Calculate the cost of storing each belief as a java int (4 bytes); repeats don't matter
                        double javaSize = 4 * agents / (1024.0 * 1024); // size in MB
                        javaSize *= beliefs; // break the computation so that we don't overflow double on large nums

                        // Now calculate the cost of storing the same in Jill; repeat to account for random effect
                        double jillSize = 0;
                        for (int r=0; r < repeats; r++) {
                            // Create a new belief set with an attribute with the given cardinality
                            BeliefSetField[] fields = {
                                    new BeliefSetField("value", Integer.class, false)};
                            try {
                                final String beliefset = "beliefset";
                                final ABeliefStore beliefbase =
                                        new ABeliefStore(agents, Runtime.getRuntime().availableProcessors());
                                final Random rand = new Random();
                                for (int agent = 0; agent < agents; agent++) {
                                    // Attach this belief set to this agent
                                    beliefbase.createBeliefSet(agent, beliefset, fields);
                                    // Add beliefs
                                    for (int b = 0; b < beliefs; b++) {
                                        beliefbase.addBelief(agent, beliefset, rand.nextInt(cardinality));
                                    }
                                }

                                // get mem upper bound given current beliefs
                                jillSize += beliefbase.memoryUpperBoundInBytes() / (1024.0 * 1024); // size in MB

                            } catch (BeliefBaseException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        jillSize /= repeats;
                        System.out.println(String.format("Type:%5s, Repeats:%3d, Beliefs:%7d, Cardinality:%7d, Agents:%8d, Mem_MB(avg):%8.3f MB",
                                "java", repeats, beliefs, cardinality, agents, javaSize));
                        System.out.println(String.format("Type:%5s, Repeats:%3d, Beliefs:%7d, Cardinality:%7d, Agents:%8d, Mem_MB(avg):%8.3f MB",
                                "jill", repeats, beliefs, cardinality, agents, jillSize));
                        System.out.flush();
                        out.println(String.format("%s,%d,%d,%d,%d,%.3f",
                                "java", repeats, beliefs, cardinality, agents, javaSize));
                        out.println(String.format("%s,%d,%d,%d,%d,%.3f",
                                "java", repeats, beliefs, cardinality, agents, jillSize));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.close();
    }

    @Test
    @Ignore
    public void testABeliefStore() {
        final int repeats = 1;
//        final int[] optAgents = {1000000, 100000, 10000, 1000, 100, 10};
//        final int[] optCardinality = {10000, 1000, 100, 10, 1};
//        final int[] optBeliefs = {100, 10, 1};
        final int[] optAgents = {1000000};
        final int[] optCardinality = {10};
        final int[] optBeliefs = {10};

        final String outfile = "./testABeliefStore-JillInts.csv";
        final String basefile = "./testABeliefStore-JavaInts.csv";
        try {
            System.out.println("Writing results to " + outfile + " and " + basefile);
            PrintWriter out = new PrintWriter(new FileWriter(outfile), true);
            PrintWriter out2 = new PrintWriter(new FileWriter(basefile), true);
            out.println("repeats,beliefs,cardinality,agents,avg_mem_mb,avg_time_ms");
            for (int i = 0; i < optBeliefs.length; i++) {
                for (int j = 0; j < optCardinality.length; j++) {
                    for (int k = 0; k < optAgents.length; k++) {
                        int beliefs = optBeliefs[i];
                        int cardinality = optCardinality[j];
                        int agents = optAgents[k];
                        {
                            // Burn in, run a few cycles (not measured)
                            //runTest(args, 5, null, beliefs, cardinality, agents);
                            // Run the baseline, storing beliefs as Java ints
                            runTest(true, repeats, out2, beliefs, cardinality, agents);

                        }
                        {
                            // Burn in, run a few cycles (not measured)
                            //runTest(args, 5, null, beliefs, cardinality, agents);
                            // Run with Jill beliefs
                            runTest(false, repeats, out, beliefs, cardinality, agents);

                        }
                    }
                }
            }
            out.close();
            out2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runTest(boolean javaInts, int repeats, PrintWriter out, int beliefs, int cardinality, int agents) {
        double mb = 0;
        double ms = 0;
        for (int r = 0; r < repeats; r++) {
            final String beliefset = "beliefset";
            final BeliefBase beliefbase = new ABeliefStore(agents, Runtime.getRuntime().availableProcessors());
            final Random rand = new Random();

            Runtime.getRuntime().gc();
            final double beforeMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            final double beforeTime = System.currentTimeMillis();

            int[][] javaBeliefs = null;

            if (beliefs != 0 && cardinality != 0) {
                if (javaInts) {
                    javaBeliefs = new int[agents][beliefs];
                    // Add javaBeliefs
                    for (int agent = 0; agent < agents; agent++) {
                        for (int i = 0; i < beliefs; i++) {
                            javaBeliefs[agent][i] = rand.nextInt(cardinality);
                        }
                    }
                } else {
                    // Create a new belief set with an attribute with the given cardinality
                    BeliefSetField[] fields = {new BeliefSetField("value", Integer.class, false)};

                    try {
                        for (int agent = 0; agent < agents; agent++) {
                            // Attach this belief set to this agent
                            beliefbase.createBeliefSet(agent, beliefset, fields);
                            // Add beliefs
                            for (int i = 0; i < beliefs; i++) {
                                beliefbase.addBelief(agent, beliefset, rand.nextInt(cardinality));
                            }
                        }

                    } catch (BeliefBaseException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            final double afterMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            final double afterTime = System.currentTimeMillis();
            mb += ((afterMem - beforeMem) / (1024.0 * 1024.0));
            ms += (afterTime - beforeTime);
        }
        if (out != null) {
            System.out.println(String.format("Repeats:%3d, Beliefs:%7d, Cardinality:%7d, Agents:%8d, Mem(avg):%8.2f MB, Time(avg):%8.2f ms",
                    repeats, beliefs, cardinality, agents, (mb / repeats), (ms / repeats)));
            System.out.flush();
            out.println(String.format("%d,%d,%d,%d,%.2f,%.2f",
                    repeats, beliefs, cardinality, agents, (mb / repeats), (ms / repeats)));
        }
    }

}
