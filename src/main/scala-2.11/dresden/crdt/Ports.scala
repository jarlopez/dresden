package dresden.crdt

import dresden.crdt.CRDT.CRDTOperation
import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl.Port
import se.sics.ktoolbox.util.network.KAddress

object Ports {
    // CRDT Management

    case class Get(id: String) extends KompicsEvent
    case class Op(id: String, op: CRDTOperation) extends KompicsEvent
    case class Response(id: String, payload: Any) extends KompicsEvent

    trait CRDTManagement extends Port
    class GSetManagement extends CRDTManagement {
        indication[Response]
        request[Op]
        request[Get]
    }
}
