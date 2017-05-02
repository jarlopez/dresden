package template.kth.app.sim;

import dresden.sim.SimUtil;
import org.junit.Test;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GossipTests {
    // TODO Keep track of which nodes are correct in churny tests

    private final SimulationResultMap res = SimulationResultSingleton.getInstance();
    @Test
    public void noChurn() {
        int numNodes = 100;
        SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        SimulationScenario simpleBootScenario = ScenarioGen.gossipNoChurn(numNodes);
        simpleBootScenario.simulate(LauncherComp.class);

        for (int i = 1; i <= numNodes; i++) {
            String query = i + SimUtil.SEND_STR();
            Set<String> sends = res.get(query, Set.class);
            assertNotNull(sends);
            assertTrue("Only one message sent", sends.size() == 1);
            String id = GossipTests.getOnlyElement(sends);

            for (int j = 1; j <= numNodes; j++) {
                if (j == i) continue;
                query = j + SimUtil.RECV_STR();
                Set<String> recvs = res.get(query, Set.class);

                assertNotNull(recvs);
                assertTrue("All messages received at all nodes", recvs.size() == numNodes - 1);
                assertTrue("Message is received at all other nodes", recvs.contains(id));
            }
        }
    }

    public static <T> T getOnlyElement(Iterable<T> iterable) {
        Iterator<T> iterator = iterable.iterator();

        if (!iterator.hasNext()) {
            throw new RuntimeException("Collection is empty");
        }

        T element = iterator.next();

        if (iterator.hasNext()) {
            throw new RuntimeException("Collection contains more than one item");
        }

        return element;
    }
}
