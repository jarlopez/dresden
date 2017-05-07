package dresden.sim.checks;

import dresden.sim.SimUtil;
import dresden.sim.tests.broadcast.BroadcastTestBase;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CRBChecks extends BroadcastTestBase {
    // CRB1 - CRB4 same as RB1 - RB4

    public static void checkValidity(int numNodes) {
        checkValidity(numNodes, 0);
    }
    static void checkValidity(int numNodes, int numChurnNodes) {
        RBChecks.checkValidity(numNodes, numChurnNodes);
    }

    public static void checkNoDuplication(int numNodes) {
        checkNoDuplication(numNodes, 0);
    }
    static void checkNoDuplication(int numNodes, int numChurnNodes) {
        RBChecks.checkNoDuplication(numNodes, numChurnNodes);
    }

    public static void checkNoCreation(int numNodes) {
        checkNoCreation(numNodes, 0);
    }
    static void checkNoCreation(int numNodes, int numChurnNodes) {
        RBChecks.checkNoCreation(numNodes, numChurnNodes);
    }

    public static void checkAgreement(int numNodes) {
        checkAgreement(numNodes, 0);
    }
    static void checkAgreement(int numNodes, int numChurnNodes) {
        RBChecks.checkAgreement(numNodes, numChurnNodes);
    }

    // For any message m1 that happened-before a messaage m2,
    // no process delivers m2 unless it already delivered m1
    public static void checkCausalDelivery(int numNodes) {
        checkCausalDelivery(numNodes, 0);
    }
    public static void checkCausalDelivery(int numNodes, int numChurnNodes) {
        for (int i = 1; i < numNodes; i++) {
            String query = i + SimUtil.CAUSAL_STR();
            List<String> causations = res.get(query, List.class);

            for (String pair : causations) {
                String[] relation = SimUtil.split(pair);
                assertTrue(relation.length == 3);
                String sender = relation[0];
                String parent = relation[1];
                String child = relation[2];
                // Loop over all recipient nodes. If it has delivered peer::child, check peer::parent
                boolean foundParent = false;
                for (int j = 1; j < numNodes; j++) {
                    query = j + SimUtil.RECV_STR();
                    List<String> delivers = res.get(query, List.class);
                    // Loop over delivers and ensure that parent -> child, if child is delivered
                    for (String it : delivers) {
                        String msgid = SimUtil.split(it)[1];
                        if (msgid.equals(parent)) foundParent = true;
                        if (msgid.equals(child) && !foundParent)
                            fail("Child delivered before causally preceding message at " + query + ": " + parent + " -> " + child);
                    }
                }

            }
        }
    }
}
