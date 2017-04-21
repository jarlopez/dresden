package dresden.components

import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl.Port
import se.sics.ktoolbox.util.network.KAddress

object Ports {

    // Perfect Link
    case class PL_Deliver(src: KAddress, payload: KompicsEvent) extends KompicsEvent
    case class PL_Send(dest: KAddress, payload: KompicsEvent) extends KompicsEvent

    class PerfectLink extends Port {
        indication[PL_Deliver]
        request[PL_Send]
    }

    // Best-Effort Broadcast
    case class BEB_Deliver(src: KAddress, payload: KompicsEvent) extends KompicsEvent
    case class BEB_Broadcast(payload: KompicsEvent) extends KompicsEvent

    class BestEffortBroadcast extends Port {
        indication[BEB_Deliver]
        request[BEB_Broadcast]
    }

    // Best-Effort Broadcast
    case class GBEB_Deliver(src: KAddress, payload: KompicsEvent) extends KompicsEvent
    case class GBEB_Broadcast(payload: KompicsEvent) extends KompicsEvent

    class GossippingBestEffortBroadcast extends Port {
        indication[GBEB_Deliver]
        request[GBEB_Broadcast]
    }

    //  Reliable Broadcast
    case class RB_Deliver(src: KAddress, payload: KompicsEvent) extends KompicsEvent
    case class RB_Broadcast(payload: KompicsEvent) extends KompicsEvent

    class ReliableBroadcast extends Port {
        indication[RB_Deliver]
        request[RB_Broadcast]
    }

    // Causal-Order Reliable Broadcast
    case class CRB_Deliver(src: KAddress, payload: KompicsEvent) extends KompicsEvent
    case class CRB_Broadcast(payload: KompicsEvent) extends KompicsEvent

    class CausalOrderReliableBroadcast extends Port {
        indication[CRB_Deliver]
        request[CRB_Broadcast]
    }

}
