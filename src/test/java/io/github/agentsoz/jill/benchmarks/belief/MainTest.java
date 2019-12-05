package io.github.agentsoz.jill.benchmarks.belief;

import io.github.agentsoz.jill.Main;
import org.junit.Test;

public class MainTest {

    @Test
    public void testBeliefsAgentMemoryConsumption() {
        int repeats = 20;
        int[] optAgents = {10, 100, 1000, 10000, 100000, 1000000};
        int[] optCardinality = {10, 100, 1000, 10000, 100000, 1000000};

        for (int i=0; i < optAgents.length; i++) {
            for (int j=0; j < optCardinality.length; j++) {
                int agents = optAgents[i];
                int cardinality = optCardinality[j];
                String[] args = {"--config",
                        "{" + "\n" + "\"randomSeed\":\"123456\"," + "\n" + "\"agents\":["
                                + "{\"classname\":\"io.github.agentsoz.jill.benchmarks.belief.TestBeliefsAgent\","
                                + " \"args\":[\"--cardinality\", \"" + cardinality + "\"], \"count\":\"" + agents + "\"}" + "]" + "}"};
                double mb = 0;
                for (int k = 0; k < repeats; k++) {
                    Runtime.getRuntime().gc();
                    double beforeMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    Main.main(args);
                    double afterMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    mb += (afterMem - beforeMem) / (1024 * 1024);
                }
                System.err.println(String.format("Repeats:%3d, Agents:%8d, Cardinality:%8d, AvgMem:%5.2fMB",
                        repeats, agents, cardinality, (mb/repeats)));
            }
        }
    }

}
