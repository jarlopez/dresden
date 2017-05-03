package dresden.sim.checks;

import dresden.sim.tests.BroadcastTestBase;

import static junit.framework.TestCase.fail;

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
        fail("TODO");
    }
}
