package dresden.sim;


import dresden.sim.compatibility.SimNodeIdExtractor;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.KillNodeEvent;
import se.sics.kompics.simulator.events.system.SetupEvent;
import se.sics.kompics.simulator.events.system.StartNodeEvent;
import se.sics.kompics.simulator.network.identifier.IdentifierExtractor;
import se.sics.ktoolbox.omngr.bootstrap.BootstrapServerComp;
import se.sics.ktoolbox.util.network.KAddress;

import java.util.HashMap;
import java.util.Map;

public class ScenarioGen {
    public enum CRDTTestType {
        GSET,
        TWOPSET,
        ORSET,
        TWOPTWOPGRAPH,
    }
    public enum BroadcastTestType {
        GOSSIP,
        RB,
        CRB,
    }

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
            nodeConfig.put("dresden.dresden.sim.type", "crdt");
            nodeConfig.put("dresden.dresden.sim.crdt.target", "gset");
            nodeConfig.put("system.id", nodeId);
            nodeConfig.put("system.seed", ScenarioSetup.getNodeSeed(nodeId));
            nodeConfig.put("system.port", ScenarioSetup.appPort);
            return nodeConfig;
        }
    };
    static Operation1<StartNodeEvent, Integer> startTwoPSetNode = (Operation1<StartNodeEvent, Integer>) nodeId -> new StartNodeOp(nodeId) {

        @Override
        public Map<String, Object> initConfigUpdate() {
            Map<String, Object> nodeConfig = new HashMap<>();
            nodeConfig.put("dresden.dresden.sim.type", "crdt");
            nodeConfig.put("dresden.dresden.sim.crdt.target", "twopset");
            nodeConfig.put("system.id", nodeId);
            nodeConfig.put("system.seed", ScenarioSetup.getNodeSeed(nodeId));
            nodeConfig.put("system.port", ScenarioSetup.appPort);
            return nodeConfig;
        }
    };
    static Operation1<StartNodeEvent, Integer> startORSetNode = (Operation1<StartNodeEvent, Integer>) nodeId -> new StartNodeOp(nodeId) {

        @Override
        public Map<String, Object> initConfigUpdate() {
            Map<String, Object> nodeConfig = new HashMap<>();
            nodeConfig.put("dresden.dresden.sim.type", "crdt");
            nodeConfig.put("dresden.dresden.sim.crdt.target", "orset");
            nodeConfig.put("system.id", nodeId);
            nodeConfig.put("system.seed", ScenarioSetup.getNodeSeed(nodeId));
            nodeConfig.put("system.port", ScenarioSetup.appPort);
            return nodeConfig;
        }
    };
    static Operation1<StartNodeEvent, Integer> startTwoPTwoPGraphNode = (Operation1<StartNodeEvent, Integer>) nodeId -> new StartNodeOp(nodeId) {

        @Override
        public Map<String, Object> initConfigUpdate() {
            Map<String, Object> nodeConfig = new HashMap<>();
            nodeConfig.put("dresden.dresden.sim.type", "crdt");
            nodeConfig.put("dresden.dresden.sim.crdt.target", "twoptwopgraph");
            nodeConfig.put("system.id", nodeId);
            nodeConfig.put("system.seed", ScenarioSetup.getNodeSeed(nodeId));
            nodeConfig.put("system.port", ScenarioSetup.appPort);
            return nodeConfig;
        }
    };

    static Operation1 killNodeOp = new Operation1<KillNodeEvent, Integer>() {
        @Override
        public KillNodeEvent generate(final Integer nodeId) {
            return new KillNodeEvent() {
                KAddress selfAdr;

                {
                    String nodeIp = ScenarioSetup.HOST_BASE + nodeId;
                    selfAdr = ScenarioSetup.getNodeAdr(nodeIp, nodeId);
                    System.out.println("XXXX Killing " + selfAdr);
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public String toString() {
                    return "KillNode<" + selfAdr.toString() + ">";
                }
            };
        }
    };

    public static SimulationScenario broadcastNoChurn(BroadcastTestType type, int numNodes) {

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
                        Operation1<StartNodeEvent, Integer> op;
                        switch (type) {
                            case GOSSIP:
                                op = startGossipNode;
                                break;
                            case RB:
                                op = startRBNode;
                                break;
                            case CRB:
                                op = startCRBNode;
                                break;
                            default:
                                op = startGossipNode;
                        }
                        eventInterArrivalTime(uniform(1000, 1100));
                        raise(numNodes, op, new BasicIntSequentialDistribution(1));
                    }
                };

                systemSetup.start();
                startBootstrapServer.startAfterTerminationOf(1000, systemSetup);
                startPeers.startAfterTerminationOf(1000, startBootstrapServer);
                terminateAfterTerminationOf(10000, startPeers);
            }
        };
    }
    public static SimulationScenario broadcastWithChurn(BroadcastTestType type, int numNodes, int numChurnNodes) {

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
                        Operation1<StartNodeEvent, Integer> op;
                        switch (type) {
                            case GOSSIP:
                                op = startGossipNode;
                                break;
                            case RB:
                                op = startRBNode;
                                break;
                            case CRB:
                                op = startCRBNode;
                                break;
                            default:
                                op = startGossipNode;
                        }
                        eventInterArrivalTime(uniform(1000, 1100));
                        raise(numNodes, op, new BasicIntSequentialDistribution(1));
                    }
                };
                SimulationScenario.StochasticProcess killNodes = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(numChurnNodes, killNodeOp, new BasicIntSequentialDistribution(numNodes - numChurnNodes + 1));
                    }
                };
                systemSetup.start();
                startBootstrapServer.startAfterTerminationOf(1000, systemSetup);
                startPeers.startAfterTerminationOf(1000, startBootstrapServer);
                killNodes.startAfterTerminationOf(1000, startPeers); // Must be within startPeers runtime
                terminateAfterTerminationOf(20000, killNodes);
            }
        };
    }


    // CRDT
    public static SimulationScenario crdtNoChurn(CRDTTestType type, int numNodes) {

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
                        switch (type) {
                            case GSET:
                                raise(numNodes, startGSetNode, new BasicIntSequentialDistribution(1));
                                break;
                            case TWOPSET:
                                raise(numNodes, startTwoPSetNode, new BasicIntSequentialDistribution(1));
                                break;
                            case ORSET:
                                raise(numNodes, startORSetNode, new BasicIntSequentialDistribution(1));
                                break;
                            case TWOPTWOPGRAPH:
                                raise(numNodes, startTwoPTwoPGraphNode , new BasicIntSequentialDistribution(1));
                                break;
                        }
                    }
                };

                systemSetup.start();
                startBootstrapServer.startAfterTerminationOf(1000, systemSetup);
                startPeers.startAfterTerminationOf(1000, startBootstrapServer);
                terminateAfterTerminationOf(50000, startPeers); // Needs to be long enough to guarantee eventual consistency
            }
        };
    }
}
