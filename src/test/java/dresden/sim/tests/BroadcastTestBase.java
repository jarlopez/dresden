package dresden.sim.tests;


import dresden.sim.SimUtil;
import dresden.sim.SimulationResultMap;
import dresden.sim.SimulationResultSingleton;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class BroadcastTestBase {

    protected static final SimulationResultMap res = SimulationResultSingleton.getInstance();

    /**
     * Stupid O(n) search for message ID based on peerId::msgId
     */
    public static boolean containsMessage(List<String> contents, String msgId) {
        for (String it : contents) {
            String[] data = SimUtil.split(it);
            assertTrue(data.length == 2);
            if (data[1].equals(msgId)) return true;
        }
        return false;
    }

    public static void printRes() {
        for (String key : res.keys()) {
            System.out.println(key + ": " + res.get(key, Set.class));
        }
    }
}
