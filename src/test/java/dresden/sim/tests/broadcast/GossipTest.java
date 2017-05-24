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

    @Test
    public void noChurn() {
        SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario simpleBootScenario = ScenarioGen.broadcastNoChurn(ScenarioGen.BroadcastTestType.GOSSIP, numNodes);
        simpleBootScenario.simulate(LauncherComp.class);

        for (String key : res.keys()) {
            System.out.println(key + ": " + res.get(key, Set.class));
        }

        BEBChecks.checkValidity(numNodes);
        BEBChecks.checkNoDuplication(numNodes);
        BEBChecks.checkNoCreation(numNodes);
    }

    @Test
    public void withChurn() {
        SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario simpleBootScenario = ScenarioGen
                .broadcastWithChurn(ScenarioGen.BroadcastTestType.GOSSIP, numNodes, numChurnNodes);
        simpleBootScenario.simulate(LauncherComp.class);

        for (String key : res.keys()) {
            System.out.println(key + ": " + res.get(key, Set.class));
        }

        BEBChecks.checkValidity(numNodes, numChurnNodes);
        BEBChecks.checkNoDuplication(numNodes, numChurnNodes);
        BEBChecks.checkNoCreation(numNodes, numChurnNodes);
    }
}
