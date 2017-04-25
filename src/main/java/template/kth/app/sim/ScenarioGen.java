package template.kth.app.sim;

import dresden.sim.GossippingSimParent;
import dresden.sim.GossipSimInit;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.SetupEvent;
import se.sics.kompics.simulator.events.system.StartNodeEvent;
import se.sics.kompics.simulator.network.identifier.IdentifierExtractor;
import se.sics.ktoolbox.omngr.bootstrap.BootstrapServerComp;
import se.sics.ktoolbox.util.network.KAddress;
import template.kth.sim.compatibility.SimNodeIdExtractor;
import template.kth.system.HostMngrComp;

import java.util.HashMap;
import java.util.Map;

public class ScenarioGen {

    // TODO Pull out common ones
    static Operation<SetupEvent> systemSetupOp = (Operation<SetupEvent>) () -> new SetupEvent() {
        @Override
        public IdentifierExtractor getIdentifierExtractor() {
            return new SimNodeIdExtractor();
        }
    };

    static Operation<StartNodeEvent> startBootstrapServerOp = (Operation<StartNodeEvent>) () -> new StartNodeEvent() {
        KAddress selfAdr;

        {
            selfAdr = ScenarioSetup.bootstrapServer;
        }

        @Override
        public Address getNodeAddress() {
            return selfAdr;
        }

        @Override
        public Class getComponentDefinition() {
            return BootstrapServerComp.class;
        }

        @Override
        public BootstrapServerComp.Init getComponentInit() {
            return new BootstrapServerComp.Init(selfAdr);
        }
    };

    static Operation1<StartNodeEvent, Integer> startNodeOp = (Operation1<StartNodeEvent, Integer>) nodeId -> new StartNodeEvent() {
        KAddress selfAdr;

        {
            String nodeIp = "193.0.0." + nodeId;
            selfAdr = ScenarioSetup.getNodeAdr(nodeIp, nodeId);
        }

        @Override
        public Address getNodeAddress() {
            return selfAdr;
        }

        @Override
        public Class getComponentDefinition() {
            return HostMngrComp.class;
        }

        @Override
        public HostMngrComp.Init getComponentInit() {
            return new HostMngrComp.Init(selfAdr, ScenarioSetup.bootstrapServer, ScenarioSetup.croupierOId);
        }

        @Override
        public Map<String, Object> initConfigUpdate() {
            Map<String, Object> nodeConfig = new HashMap<>();
            nodeConfig.put("system.id", nodeId);
            nodeConfig.put("system.seed", ScenarioSetup.getNodeSeed(nodeId));
            nodeConfig.put("system.port", ScenarioSetup.appPort);
            return nodeConfig;
        }
    };


    static Operation1<StartNodeEvent, Integer> startGossipNode = (Operation1<StartNodeEvent, Integer>) nodeId -> new StartNodeEvent() {
        KAddress selfAdr;

        {
            // 193.0.0.1 is restricted to bootstrap node
            String nodeIp = "194.0.0." + nodeId;
            selfAdr = ScenarioSetup.getNodeAdr(nodeIp, nodeId);
        }

        @Override
        public Address getNodeAddress() {
            return selfAdr;
        }

        @Override
        public Class getComponentDefinition() {
            return GossippingSimParent.class;
        }

        @Override
        public GossipSimInit getComponentInit() {
            return new GossipSimInit(selfAdr, ScenarioSetup.bootstrapServer, ScenarioSetup.croupierOId);
        }

        @Override
        public Map<String, Object> initConfigUpdate() {
            Map<String, Object> nodeConfig = new HashMap<>();
            nodeConfig.put("system.id", nodeId);
            nodeConfig.put("system.seed", ScenarioSetup.getNodeSeed(nodeId));
            nodeConfig.put("system.port", ScenarioSetup.appPort);
            return nodeConfig;
        }
    };

    public static SimulationScenario simpleBoot() {
        return simpleBoot(false);
    }

    public static SimulationScenario simpleBoot(boolean useScala) {
        SimulationScenario scen = new SimulationScenario() {
            {
                StochasticProcess systemSetup = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, systemSetupOp);
                    }
                };
                StochasticProcess startBootstrapServer = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startBootstrapServerOp);
                    }
                };
                StochasticProcess startPeers = new StochasticProcess() {
                    {
//                        eventInterArrivalTime(uniform(1000, 1100));
                        eventInterArrivalTime(constant(1000));
                        raise(3, startGossipNode, new BasicIntSequentialDistribution(1));
                    }
                };

                systemSetup.start();
                startBootstrapServer.startAfterTerminationOf(1000, systemSetup);
                startPeers.startAfterTerminationOf(1000, startBootstrapServer);
                terminateAfterTerminationOf(1000 * 1000 * 3, startPeers);
            }
        };

        return scen;
    }
}
