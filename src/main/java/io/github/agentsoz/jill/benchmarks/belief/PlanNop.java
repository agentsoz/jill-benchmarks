package io.github.agentsoz.jill.benchmarks.belief;

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
import io.github.agentsoz.jill.lang.Agent;
import io.github.agentsoz.jill.lang.Goal;
import io.github.agentsoz.jill.lang.Plan;
import io.github.agentsoz.jill.lang.PlanStep;
import io.github.agentsoz.jill.util.Log;

import java.util.Map;

public class PlanNop extends Plan {

  /**
   * Creates a plan that does nothing
   * 
   * @param agent the agent to whom this plan belongs
   * @param goal the goal that this plan handles
   * @param name a name for this plan
   */
  public PlanNop(Agent agent, Goal goal, String name) {
    super(agent, goal, name);
    body = steps;
  }

  @Override
  public boolean context() {
    return true;
  }

  @Override
  public void setPlanVariables(Map<String, Object> vars) {
  }

  PlanStep[] steps = {
          () -> {
            // NOP; plan step intentionally empty
          },
  };

}
