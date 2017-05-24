package dresden.sim.checks;

import dresden.sim.SimUtil;
import dresden.sim.TestBase;
import org.apache.log4j.Logger;
import scala.Tuple2;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.HashSet;

import static org.junit.Assert.*;

public class GraphChecks extends TestBase {
    private static Logger log = Logger.getLogger(GraphChecks.class.getName());

    public static void checkConsistency(int numNodes) {
        checkConsistency(numNodes, 0);
    }
    static void checkConsistency(int numNodes, int numChurnNodes) {
        Tuple2<HashSet<String>, HashSet<Tuple2<String, String>>> prevState = null;

        for (int i = 1; i <= numNodes; i++) {
            String query = i + SimUtil.GSET_STR();


            byte[] pickle = res.get(query, byte[].class);

            ByteArrayInputStream bis = new ByteArrayInputStream(pickle);
            try {
                // Deserialize simulation data
                ObjectInputStream in = new ObjectInputStream(bis);
                Tuple2<HashSet<String>, HashSet<Tuple2<String, String>>> state = (Tuple2<HashSet<String>, HashSet<Tuple2<String, String>>>) in.readObject();
                assertNotNull(state);

                if (log.isDebugEnabled()) {
                    log.debug(state);
                }

                if (prevState == null) {
                    prevState = state;
                } else {
                    // Check vertices and edges
                    HashSet<String> verticesA = state._1();
                    HashSet<String> verticesB = prevState._1();
                    HashSet<Tuple2<String, String>> edgesA = state._2();
                    HashSet<Tuple2<String, String>> edgesB = prevState._2();
                    assertTrue("Sizes of vertex lists match",
                            verticesA.size() == verticesB.size());
                    assertTrue("Vertex lists contain same elements",
                            verticesA.containsAll(verticesB) && verticesB.containsAll(verticesA));

                    assertTrue("Edge sets are same size",
                            edgesA.size() == edgesB.size());
                    assertTrue("Edge lists contain same elements",
                            edgesA.containsAll(edgesB) && edgesB.containsAll(edgesA));
                }
            } catch (Exception ex) {
                fail("Could not pickle: " + ex.getMessage());
            }
        }
    }
}
