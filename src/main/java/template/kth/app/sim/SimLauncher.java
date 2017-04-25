package template.kth.app.sim;

import org.junit.Test;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

import java.util.Set;

public class SimLauncher {

    private final SimulationResultMap res = SimulationResultSingleton.getInstance();

    @Test
    public void simpleOpsTest() {
        SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario simpleBootScenario = ScenarioGen.simpleBoot(true);
        simpleBootScenario.simulate(LauncherComp.class);

        System.out.println("Done!"  + SimulationResultSingleton.getInstance().toString());
        for (String key : res.keys()) {
            System.out.println(key + ": " + res.get(key, Set.class));
        }
    }
}
