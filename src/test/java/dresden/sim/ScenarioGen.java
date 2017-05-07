package dresden.sim;


import dresden.sim.compatibility.SimNodeIdExtractor;
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
    static Operation1<StartNodeEvent, Integer> startGossipNode = (Operation1<StartNodeEvent, Integer>) nodeId -> new StartNodeOp(nodeId) {

        @Override
        public Map<String, Object> initConfigUpdate() {
            Map<String, Object> nodeConfig = new HashMap<>();
            nodeConfig.put("dresden.dresden.sim.type", "gossip");
            nodeConfig.put("system.id", nodeId);
            nodeConfig.put("system.seed", ScenarioSetup.getNodeSeed(nodeId));
            nodeConfig.put("system.port", ScenarioSetup.appPort);
            return nodeConfig;
        }
    };
    static Operation1<StartNodeEvent, Integer> startRBNode = (Operation1<StartNodeEvent, Integer>) nodeId -> new StartNodeOp(nodeId) {

        @Override
        public Map<String, Object> initConfigUpdate() {
            Map<String, Object> nodeConfig = new HashMap<>();
            nodeConfig.put("dresden.dresden.sim.type", "rb");
            nodeConfig.put("system.id", nodeId);
            nodeConfig.put("system.seed", ScenarioSetup.getNodeSeed(nodeId));
            nodeConfig.put("system.port", ScenarioSetup.appPort);
            return nodeConfig;
        }
    };
    static Operation1<StartNodeEvent, Integer> startCRBNode = (Operation1<StartNodeEvent, Integer>) nodeId -> new StartNodeOp(nodeId) {

        @Override
        public Map<String, Object> initConfigUpdate() {
            Map<String, Object> nodeConfig = new HashMap<>();
            nodeConfig.put("dresden.dresden.sim.type", "crb");
            nodeConfig.put("system.id", nodeId);
            nodeConfig.put("system.seed", ScenarioSetup.getNodeSeed(nodeId));
            nodeConfig.put("system.port", ScenarioSetup.appPort);
            return nodeConfig;
        }
    };
    static Operation1<StartNodeEvent, Integer> startGSetNode = (Operation1<StartNodeEvent, Integer>) nodeId -> new StartNodeOp(nodeId) {

        @Override
        public Map<String, Object> initConfigUpdate() {
            Map<String, Object> nodeConfig = new HashMap<>();
            nodeConfig.put("dresden.dresden.sim.type", "gset");
            nodeConfig.put("system.id", nodeId);
            nodeConfig.put("system.seed", ScenarioSetup.getNodeSeed(nodeId));
            nodeConfig.put("system.port", ScenarioSetup.appPort);
            return nodeConfig;
        }
    };

    // Broadcasting
    public static SimulationScenario gossipNoChurn(int numNodes) {

        return new SimulationScenario() {
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
                        eventInterArrivalTime(uniform(1000, 1100));
                        raise(numNodes, startGossipNode, new BasicIntSequentialDistribution(1));
                    }
                };

                systemSetup.start();
                startBootstrapServer.startAfterTerminationOf(1000, systemSetup);
                startPeers.startAfterTerminationOf(1000, startBootstrapServer);
                terminateAfterTerminationOf(10000, startPeers);
            }
        };
    }
    public static SimulationScenario rbNoChurn(int numNodes) {

        return new SimulationScenario() {
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
                        eventInterArrivalTime(uniform(1000, 1100));
                        raise(numNodes, startRBNode, new BasicIntSequentialDistribution(1));
                    }
                };

                systemSetup.start();
                startBootstrapServer.startAfterTerminationOf(1000, systemSetup);
                startPeers.startAfterTerminationOf(1000, startBootstrapServer);
                terminateAfterTerminationOf(10000, startPeers);
            }
        };
    }
    public static SimulationScenario crbNoChurn(int numNodes) {

        return new SimulationScenario() {
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
                        eventInterArrivalTime(uniform(1000, 1100));
                        raise(numNodes, startCRBNode, new BasicIntSequentialDistribution(1));
                    }
                };

                systemSetup.start();
                startBootstrapServer.startAfterTerminationOf(1000, systemSetup);
                startPeers.startAfterTerminationOf(1000, startBootstrapServer);
                terminateAfterTerminationOf(10000, startPeers);
            }
        };
    }

    // CRDT
    public static SimulationScenario gsetNoChurn(int numNodes) {

        return new SimulationScenario() {
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
                        eventInterArrivalTime(uniform(1000, 1100));
                        raise(numNodes, startGSetNode, new BasicIntSequentialDistribution(1));
                    }
                };

                systemSetup.start();
                startBootstrapServer.startAfterTerminationOf(1000, systemSetup);
                startPeers.startAfterTerminationOf(1000, startBootstrapServer);
                terminateAfterTerminationOf(10000, startPeers);
            }
        };
    }
}
