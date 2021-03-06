package dresden.sim.tests.crdt;

import dresden.sim.ScenarioGen;
import dresden.sim.ScenarioSetup;
import dresden.sim.TestBase;
import org.junit.Test;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

import java.util.Set;

import static org.junit.Assert.assertTrue;

public class CRDTSetTest extends TestBase {
    @Test
    public void gset() {
        SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario simpleBootScenario = ScenarioGen.crdtNoChurn(ScenarioGen.CRDTTestType.GSET, numNodes);
        simpleBootScenario.simulate(LauncherComp.class);

        assertAllSetsSame();
    }
    @Test
    public void gsetWithChurn() {
        SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario simpleBootScenario = ScenarioGen.crdtWithChurn(ScenarioGen.CRDTTestType.GSET, numNodes, numChurnNodes);
        simpleBootScenario.simulate(LauncherComp.class);

        assertAllSetsSame();
    }

    @Test
    public void twopset() {
        SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario simpleBootScenario = ScenarioGen.crdtNoChurn(ScenarioGen.CRDTTestType.TWOPSET, numNodes);
        simpleBootScenario.simulate(LauncherComp.class);

        assertAllSetsSame();
    }

    @Test
    public void twopsetWithChurn() {
        SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario simpleBootScenario = ScenarioGen.crdtWithChurn(ScenarioGen.CRDTTestType.TWOPSET, numNodes, numChurnNodes);
        simpleBootScenario.simulate(LauncherComp.class);

        assertAllSetsSame();
    }

    @Test
    public void orset() {
        SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario simpleBootScenario = ScenarioGen.crdtNoChurn(ScenarioGen.CRDTTestType.ORSET, numNodes);
        simpleBootScenario.simulate(LauncherComp.class);

        assertAllSetsSame();
    }

    @Test
    public void orsetWithChurn() {
        SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario simpleBootScenario = ScenarioGen.crdtWithChurn(ScenarioGen.CRDTTestType.ORSET, numNodes, numChurnNodes);
        simpleBootScenario.simulate(LauncherComp.class);

        assertAllSetsSame();
    }


    private void assertAllSetsSame() {
        Set<String> prev = null;
        for (String key : res.keys()) {
            Set<String> it = res.get(key, Set.class);
            if (prev == null) {
                prev = it;
            } else {
                assertTrue(prev.containsAll(it));
            }
        }
    }
}
