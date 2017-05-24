package dresden.sim.tests.broadcast;

import dresden.sim.ScenarioGen;
import dresden.sim.ScenarioSetup;
import dresden.sim.TestBase;
import dresden.sim.checks.RBChecks;
import org.junit.Test;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

public class RBTest extends TestBase {

    @Test
    public void noChurn() {
        SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario simpleBootScenario = ScenarioGen.broadcastNoChurn(ScenarioGen.BroadcastTestType.RB, numNodes);
        simpleBootScenario.simulate(LauncherComp.class);

        RBChecks.checkValidity(numNodes);
        RBChecks.checkNoDuplication(numNodes);
        RBChecks.checkNoCreation(numNodes);
        RBChecks.checkAgreement(numNodes);
    }

    @Test
    public void withChurn() {
        SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario simpleBootScenario = ScenarioGen.broadcastWithChurn(
                ScenarioGen.BroadcastTestType.RB, numNodes, numChurnNodes);
        simpleBootScenario.simulate(LauncherComp.class);

        RBChecks.checkValidity(numNodes, numChurnNodes);
        RBChecks.checkNoDuplication(numNodes, numChurnNodes);
        RBChecks.checkNoCreation(numNodes, numChurnNodes);
        RBChecks.checkAgreement(numNodes, numChurnNodes);
    }

}
