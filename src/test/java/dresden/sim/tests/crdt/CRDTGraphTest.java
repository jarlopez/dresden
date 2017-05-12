package dresden.sim.tests.crdt;

import dresden.sim.ScenarioGen;
import dresden.sim.ScenarioSetup;
import dresden.sim.TestBase;
import org.junit.Test;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

public class CRDTGraphTest extends TestBase {
    @Test
    public void twoPTwoPGraph() {
        int numNodes = 2;
        SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario simpleBootScenario = ScenarioGen.crdtNoChurn(ScenarioGen.CRDTTestType.TWOPTWOPGRAPH, numNodes);
        simpleBootScenario.simulate(LauncherComp.class);
    }
}
