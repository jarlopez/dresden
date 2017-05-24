package dresden.sim.checks;

import dresden.sim.SimUtil;
import dresden.sim.SimulationResultMap;
import dresden.sim.SimulationResultSingleton;

import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BEBChecks {

    protected static final SimulationResultMap res = SimulationResultSingleton.getInstance();


    // If a correct process broadcast a message m,
    // then every correct process eventually delivers m
    public static void checkValidity(int numNodes) {
        checkValidity(numNodes, 0);
    }
    public static void checkValidity(int numNodes, int numChurnNodes) {
        for (int i = 1; i <= numNodes; i++) {
            String query = i + SimUtil.SEND_STR();
            List<String> sends = res.get(query, List.class);
            boolean senderCorrect = SimUtil.isCorrectNode(i, numNodes, numChurnNodes);
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
                    boolean receiverCorrect = SimUtil.isCorrectNode(j, numNodes, numChurnNodes);

                    if (senderCorrect && receiverCorrect) {
                        assertNotNull(recvs);
                        assertTrue("Message is received at all other nodes", recvs.contains(sendStr));
                    }
                }
            }
        }
    }
    // No message is delivered more than once
    public static void checkNoDuplication(int numNodes) {
        checkNoDuplication(numNodes, 0);
    }

    public static void checkNoDuplication(int numNodes, int numChurnNodes) {
        for (int i = 1; i <= numNodes; i++) {
            String query = i + SimUtil.RECV_STR();
            List<String> delivers = res.get(query, List.class);
            boolean receiverIncorrect = SimUtil.isChurnNode(i, numNodes, numChurnNodes);
            if (delivers == null) {
                assertTrue(receiverIncorrect);
            } else {
                assertTrue("No duplicate messages were delivered", new HashSet<>(delivers).size() == delivers.size());
            }
        }
    }

    // If a process delivers a message m with sender s,
    // then m was previously broadcast by process s
    public static void checkNoCreation(int numNodes) {
        checkNoCreation(numNodes, 0);
    }
    public static void checkNoCreation(int numNodes, int numChurnNodes) {
        for (int i = 1; i < numNodes; i++) {
            String query = i + SimUtil.RECV_STR();
            List<String> delivers = res.get(query, List.class);
            boolean receiverIncorrect = SimUtil.isChurnNode(i, numNodes, numChurnNodes);
            if (delivers == null) {
                assertTrue(receiverIncorrect);
            } else {
                for (String deliverStr : delivers) {
                    String[] parts = SimUtil.getPeerAndId(deliverStr);
                    String host = parts[0];
                    String peerId = getPeerNum(host);
                    String sendQuery = peerId + SimUtil.SEND_STR();
                    List<String> sends = res.get(sendQuery, List.class);
                    assertNotNull(delivers);
                    assertTrue("Message " + deliverStr + " was broadcast by process s(" + sendQuery + ")", sends.contains(deliverStr));
                }
            }
        }
    }

    private static String getPeerNum(String hostIdString) {
        assertTrue("Input conforms to expected format", hostIdString.contains("<") && hostIdString.contains(">"));
        return hostIdString.substring(hostIdString.lastIndexOf('<') + 1, hostIdString.lastIndexOf('>'));
    }
}
