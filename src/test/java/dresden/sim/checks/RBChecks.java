package dresden.sim.checks;

import dresden.sim.SimUtil;
import dresden.sim.TestBase;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RBChecks extends TestBase {

    // If a correct process p broadcasts a message m,
    // then p eventually delivers m
    public static void checkValidity(int numNodes) {
        checkValidity(numNodes, 0);
    }
    static void checkValidity(int numNodes, int numChurnNodes) {
        // TODO Handle correctness of node
        for (int i = 1; i <= numNodes; i++) {
            String query = i + SimUtil.SEND_STR();
            List<String> sends = res.get(query, List.class);
            assertNotNull(sends);

            for (String id : sends) {
                query = i + SimUtil.RECV_STR();
                List<String> recvs = res.get(query, List.class);
                assertNotNull(recvs);
                assertTrue("Message is delivered at p", recvs.contains(id));
            }
        }
    }


    // No message is delivered more than once
    public static void checkNoDuplication(int numNodes) {
        checkNoDuplication(numNodes, 0);
    }
    static void checkNoDuplication(int numNodes, int numChurnNodes) {
        BEBChecks.checkNoDuplication(numNodes, numChurnNodes);
    }

    // If a process delivers a message m with sender s,
    // then m was previously broadcast by process s
    public static void checkNoCreation(int numNodes) {
        checkNoCreation(numNodes, 0);
    }
    static void checkNoCreation(int numNodes, int numChurnNodes) {
        BEBChecks.checkNoCreation(numNodes, numChurnNodes);
    }

    // If a message m is delivered by some correct process,
    // then m is eventually delivered by every correct process
    public static void checkAgreement(int numNodes) {
        checkAgreement(numNodes, 0);
    }
    static void checkAgreement(int numNodes, int numChurnNodes) {
        //  TODO Handle correctness of nodes
        for (int i = 1; i <= numNodes; i++) {
            String query = i + SimUtil.RECV_STR();
            List<String> delivers = res.get(query, List.class);
            assertNotNull(delivers);

            for (String id : delivers) {
                //  TODO Handle correctness of nodes
                for (int j = 1; j <= numNodes; j++) {
                    query = j + SimUtil.RECV_STR();
                    List<String> recvs = res.get(query, List.class);

                    assertNotNull(recvs);
                    assertTrue("Message is delivered at correct nodes", recvs.contains(id));

                }
            }

        }
    }
}
