package dresden.sim.tests.broadcast;

import dresden.sim.ScenarioGen;
import dresden.sim.ScenarioSetup;
import dresden.sim.TestBase;
import dresden.sim.checks.CRBChecks;
import org.junit.Test;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

public class CRBTest extends TestBase {
    // TODO Keep track of which nodes are correct in churny tests

    @Test
    public void noChurn() {
        int numNodes = 10;
        SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario simpleBootScenario = ScenarioGen.crbNoChurn(numNodes);
        simpleBootScenario.simulate(LauncherComp.class);

        CRBChecks.checkValidity(numNodes);
        CRBChecks.checkNoDuplication(numNodes);
        CRBChecks.checkNoCreation(numNodes);
        CRBChecks.checkAgreement(numNodes);
        CRBChecks.checkCausalDelivery(numNodes);
    }
}
