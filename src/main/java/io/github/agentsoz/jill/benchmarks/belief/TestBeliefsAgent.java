package io.github.agentsoz.jill.benchmarks.belief;

import io.github.agentsoz.jill.core.beliefbase.Belief;

/*
 * #%L
 * Jill Cognitive Agents Platform
 * %%
 * Copyright (C) 2014 - 2019 by its authors. See AUTHORS file.
 * %%
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import io.github.agentsoz.jill.core.beliefbase.BeliefBaseException;
import io.github.agentsoz.jill.core.beliefbase.BeliefSetField;
import io.github.agentsoz.jill.lang.Agent;
import io.github.agentsoz.jill.lang.AgentInfo;
import io.github.agentsoz.jill.lang.MetaPlan;
import io.github.agentsoz.jill.lang.Plan;
import io.github.agentsoz.jill.lang.PlanBindings;
import io.github.agentsoz.jill.util.Log;

import java.io.PrintStream;
import java.util.*;

@SuppressWarnings("PMD")
@AgentInfo(hasGoals = {"io.github.agentsoz.jill.benchmarks.belief.GoalNop"})
public class TestBeliefsAgent extends Agent {

  protected PrintStream writer;


  // command line arguments
  static final String optCardinality = "--cardinality";

  // usage message
  private static final String usageMessage = "usage:\n"
          + String.format("%10s %-6s %s\n", optCardinality, "NUM", "cardinality (number of unique values) of beliefs")
          ;

  // Defaults

  private static Random rand = new Random();
  private static int valCardinality = 2;

  private static final String beliefset = "beliefset";

  public TestBeliefsAgent(String name) {
    super(name);
  }

  @Override
  public void start(PrintStream writer, String[] params) {
    this.writer = writer;

    // Parse the arguments
    parse(params);

    if (valCardinality != 0) {
      // Create a new belief set with an attribute with the given cardinality
      BeliefSetField[] fields = {new BeliefSetField("value", Integer.class, true)};

      try {
        // Attach this belief set to this agent
        this.createBeliefSet(beliefset, fields);

        // Add beliefs about neighbours
        this.addBelief(beliefset, rand.nextInt(valCardinality));

      } catch (BeliefBaseException e) {
        Log.error(e.getMessage());
      }
    }
  }


  /**
   * Parse the command line options
   * @param args command line options
   * @return key value pairs of known options
   */
  void parse(String[] args) {
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case optCardinality:
          if (i + 1 < args.length) {
            valCardinality = Integer.parseInt(args[++i]);
          }
          break;
        default:
          throw new RuntimeException("unknown config option: " + args[i]) ;
      }
    }
  }
}
