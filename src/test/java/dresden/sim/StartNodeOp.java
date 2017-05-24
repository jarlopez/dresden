package dresden.sim;

import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.events.system.StartNodeEvent;
import se.sics.ktoolbox.util.network.KAddress;

import static dresden.sim.ScenarioSetup.HOST_BASE;

public abstract class StartNodeOp extends StartNodeEvent {
    private Integer nodeId;
    KAddress selfAdr;

    public StartNodeOp(Integer it) {
        this.nodeId = it;

        String nodeIp = HOST_BASE + nodeId;
        this.selfAdr = ScenarioSetup.getNodeAdr(nodeIp, nodeId);
    }


    @Override
    public Address getNodeAddress() {
        return selfAdr;
    }

    @Override
    public Class getComponentDefinition() {
        return HostManager.class;
    }

    @Override
    public HostManager.Init getComponentInit() {
        return new HostManager.Init(selfAdr, ScenarioSetup.bootstrapServer, ScenarioSetup.croupierOId);
    }

}
