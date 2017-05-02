package template.kth.app.sim;

import org.junit.Test;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

import java.util.Set;

public class GossipTests extends BroadcastTest {
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

        checkBEBValidity(numNodes);
        checkBEBNoDuplication(numNodes);
        checkBEBNoCreation(numNodes);
    }
}
