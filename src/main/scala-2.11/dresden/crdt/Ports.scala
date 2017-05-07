package dresden.crdt

import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl.Port
import se.sics.ktoolbox.util.network.KAddress

object Ports {
    // CRDT Management
    case class GSet_Add(src: KAddress, payload: KompicsEvent) extends KompicsEvent

    case class CRDT_OpDelivery(payload: KompicsEvent) extends KompicsEvent

    class GSetManagement extends Port {
        indication[GSet_Add]
        request[CRDT_OpDelivery ]
    }
}
