package dresden.sim.tests;

import dresden.sim.ScenarioGen;
import dresden.sim.ScenarioSetup;
import dresden.sim.checks.RBChecks;
import org.junit.Test;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

public class RBTest extends BroadcastTestBase {
    // TODO Keep track of which nodes are correct in churny tests

    @Test
    public void noChurn() {
        int numNodes = 3;
        SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario simpleBootScenario = ScenarioGen.rbNoChurn(numNodes);
        simpleBootScenario.simulate(LauncherComp.class);

        RBChecks.checkValidity(numNodes);
        RBChecks.checkNoDuplication(numNodes);
        RBChecks.checkNoCreation(numNodes);
        RBChecks.checkAgreement(numNodes);
    }

}
