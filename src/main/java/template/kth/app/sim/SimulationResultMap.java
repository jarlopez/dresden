package template.kth.app.sim;

import java.util.Set;

public interface SimulationResultMap {
    void put(String key, Object o);
    <T> T get(String key, Class<T> tpe);
    Set<String> keys();
}