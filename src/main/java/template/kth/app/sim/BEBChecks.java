package template.kth.app.sim;

import dresden.sim.SimUtil;

import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

public class BEBChecks {
    // TODO Store tuples of (from, msgId) to allow for BEB3

    protected static final SimulationResultMap res = SimulationResultSingleton.getInstance();


    // If a correct process broadcast a message m,
    // then every correct process eventually delivers m
    protected static void checkBEBValidity(int numNodes) {
        checkBEBValidity(numNodes, 0);
    }
    protected static void checkBEBValidity(int numNodes, int numChurnNodes) {
        // TODO Move format generation/parsing into helper obj for cinsistency
        //  TODO Handle correctness of nodes
        for (int i = 1; i <= numNodes; i++) {
            String query = i + SimUtil.SEND_STR();
            List<String> sends = res.get(query, List.class);
            assertNotNull(sends);
            assertTrue("Only one message sent", sends.size() == 1);
            for (String it : sends) {
                //  TODO Handle correctness of nodes
                String[] parts = it.split(SimUtil.DELIM_STR());
                assertTrue(parts.length == 2);
                String host = parts[0];
                String id = parts[1];
                for (int j = 1; j <= numNodes; j++) {
                    if (j == i) continue;
                    query = j + SimUtil.RECV_STR();
                    List<String> recvs = res.get(query, List.class);
                    String sendStr = SimUtil.genPeerToIdStr(host, id);
                    assertNotNull(recvs);
                    assertTrue("Message is received at all other nodes", recvs.contains(sendStr));
                }
            }
        }
    }
    // No message is delivered more than once
    protected static void checkBEBNoDuplication(int numNodes) {
        checkBEBNoDuplication(numNodes, 0);
    }

    protected static void checkBEBNoDuplication(int numNodes, int numChurnNodes) {
        for (int i = 1; i <= numNodes; i++) {
            String query = i + SimUtil.RECV_STR();
            List<String> delivers = res.get(query, List.class);
            assertNotNull(delivers);

            assertTrue("No duplicate messages were delivered", new HashSet<>(delivers).size() == delivers.size());
        }
    }

    protected static void checkBEBNoCreation(int numNodes) {
        checkBEBNoCreation(numNodes, 0);
    }
    protected static void checkBEBNoCreation(int numNodes, int numChurnNodes) {
        // TODO
        fail("Not implemented");
    }
}
