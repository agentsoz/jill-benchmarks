package io.github.agentsoz.jill.benchmarks.belief;

import io.github.agentsoz.jill.Main;
import io.github.agentsoz.jill.core.GlobalState;
import io.github.agentsoz.jill.util.AObjectCatalog;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MainTest {

    @Test
    @Ignore
    public void testBeliefsAgent() {
        final int repeats = 5;
        final int[] optAgents = {1000000, 100000, 10000, 1000, 100, 10};
        final int[] optCardinality = {10000, 1000, 100, 10, 1};
        final int[] optBeliefs = {100, 10, 1, 0};
        final String outfile = "./testBeliefsAgent-JillInts.csv";
        final String basefile = "./testBeliefsAgent-JavaInts.csv";
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
                            String[] args = {"--config",
                                    "{" + "\n" + "\"randomSeed\":\"123456\"," + "\n" + "\"agents\":["
                                            + "{\"classname\":\"io.github.agentsoz.jill.benchmarks.belief.TestBeliefsAgent\","
                                            + " \"args\":["
                                            + "\"--as-java-ints\", \"true\","
                                            + "\"--beliefs\", \"" + beliefs + "\","
                                            + "\"--cardinality\", \"" + cardinality
                                            + "\"], \"count\":\"" + agents + "\"}" + "]" + "}"};
                            // Burn in, run a few cycles (not measured)
                            //runTest(args, 5, null, beliefs, cardinality, agents);
                            // Run the baseline, storing beliefs as Java ints
                            runTest(args, repeats, out2, beliefs, cardinality, agents);

                        }
                        {
                            String[] args = {"--config",
                                    "{" + "\n" + "\"randomSeed\":\"123456\"," + "\n" + "\"agents\":["
                                            + "{\"classname\":\"io.github.agentsoz.jill.benchmarks.belief.TestBeliefsAgent\","
                                            + " \"args\":["
                                            + "\"--beliefs\", \"" + beliefs + "\","
                                            + "\"--cardinality\", \"" + cardinality
                                            + "\"], \"count\":\"" + agents + "\"}" + "]" + "}"};
                            // Burn in, run a few cycles (not measured)
                            //runTest(args, 5, null, beliefs, cardinality, agents);
                            // Run with Jill beliefs
                            runTest(args, repeats, out, beliefs, cardinality, agents);

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

    private void runTest(String[] args, int repeats, PrintWriter out, int beliefs, int cardinality, int agents) {
        double mb = 0;
        double ms = 0;
        for (int r = 0; r < repeats; r++) {
            Runtime.getRuntime().gc();
            final double beforeMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            final double beforeTime = System.currentTimeMillis();
            // Get a handle to all agents, so that mem is not released after Jill finishes
            AObjectCatalog jillagents = GlobalState.agents;
            Main.main(args);
            final double afterMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            final double afterTime = System.currentTimeMillis();
            mb += ((afterMem - beforeMem) / (1024.0 * 1024.0));
            ms += (afterTime - beforeTime);
            jillagents = null; // release it after we have measured memory usage
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
