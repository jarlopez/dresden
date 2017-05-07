package dresden.sim.tests.crdt;

import dresden.sim.ScenarioGen;
import dresden.sim.ScenarioSetup;
import org.junit.Test;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

public class GSetTest {
    @Test
    public void basic() {
        int numNodes = 2;
        SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario simpleBootScenario = ScenarioGen.gsetNoChurn(numNodes);
        simpleBootScenario.simulate(LauncherComp.class);
    }
}
