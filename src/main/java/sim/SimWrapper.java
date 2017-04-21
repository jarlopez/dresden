package sim;

import dresden.sim.SimHost;
import dresden.sim.SimHostInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId;
import se.sics.ktoolbox.util.network.KAddress;

public class SimWrapper extends ComponentDefinition {
    private static final Logger LOG = LoggerFactory.getLogger(template.kth.system.HostMngrComp.class);
    private String logPrefix = " ";

    Positive<Timer> timerPort = requires(Timer.class);
    Positive<Network> networkPort = requires(Network.class);

    private Component simHost;
    private Init init;

    public SimWrapper(Init init) {
        this.init = init;
        subscribe(handleStart, control);
    }

    Handler handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            LOG.info("{}starting...", logPrefix);
            SimHostInit newInit = new SimHostInit(init.selfAdr, init.bootstrapServer, init.croupierId);
            simHost = create(SimHost.class, newInit);
            connect(simHost.getNegative(Timer.class), timerPort, Channel.TWO_WAY);
            connect(simHost.getNegative(Network.class), networkPort, Channel.TWO_WAY);

        }
    };


    public static class Init extends se.sics.kompics.Init<template.kth.system.HostMngrComp> {

        public final KAddress selfAdr;
        public final KAddress bootstrapServer;
        public final OverlayId croupierId;

        public Init(KAddress selfAdr, KAddress bootstrapServer, OverlayId croupierId) {
            this.selfAdr = selfAdr;
            this.bootstrapServer = bootstrapServer;
            this.croupierId = croupierId;
        }
    }
}
