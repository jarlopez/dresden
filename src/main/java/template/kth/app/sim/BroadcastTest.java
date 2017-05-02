package template.kth.app.sim;

import dresden.sim.SimUtil;

import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BroadcastTest {
    protected final SimulationResultMap res = SimulationResultSingleton.getInstance();

    protected void checkBEBValidity(int numNodes) {
        checkBEBValidity(numNodes, 0);
    }
    protected void checkBEBValidity(int numNodes, int numChurnNodes) {
        //  TODO Handle churn nodes
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

    // If a correct process p broadcasts a message m, then p eventually delivers m
    protected void checkRBValidity(int numNodes) {
        checkRBValidity(numNodes, 0);
    }
    protected void checkRBValidity(int numNodes, int numChurnNodes) {
        // TODO Make it actually handle validity
        // TODO Handle churn nodes
        for (int i = 1; i <= numNodes; i++) {
            String query = i + SimUtil.SEND_STR();
            Set<String> sends = res.get(query, Set.class);
            assertNotNull(sends);

            assertTrue("Only one message sent", sends.size() == 1);
            for (String id : sends) {
                query = i + SimUtil.RECV_STR();
                Set<String> recvs = res.get(query, Set.class);
                assertNotNull(recvs);
                assertTrue("Message is delivered at p", recvs.contains(id));
            }
        }
    }

    protected static <T> T getOnlyElement(Iterable<T> iterable) {
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
