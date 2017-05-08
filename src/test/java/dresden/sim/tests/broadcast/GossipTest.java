package dresden.sim.tests.broadcast;

import dresden.sim.ScenarioGen;
import dresden.sim.ScenarioSetup;
import dresden.sim.TestBase;
import dresden.sim.checks.BEBChecks;
import org.junit.Test;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

import java.util.Set;

public class GossipTest extends TestBase {
    // TODO Keep track of which nodes are correct in churny tests

    @Test
    public void noChurn() {
        int numNodes = 3;
        SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario simpleBootScenario = ScenarioGen.gossipNoChurn(numNodes);
        simpleBootScenario.simulate(LauncherComp.class);

        for (String key : res.keys()) {
            System.out.println(key + ": " + res.get(key, Set.class));
        }

        BEBChecks.checkValidity(numNodes);
        BEBChecks.checkNoDuplication(numNodes);
        BEBChecks.checkNoCreation(numNodes);
    }
}
