package dresden.sim;


import org.junit.Before;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class TestBase {

    protected static final SimulationResultMap res = SimulationResultSingleton.getInstance();

    @Before
    public void initObjects() {
        res.clear();
    }

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

    public static void printResAsSet() {
        for (String key : res.keys()) {
            System.out.println(key + ": " + res.get(key, Set.class));
        }
    }
}
