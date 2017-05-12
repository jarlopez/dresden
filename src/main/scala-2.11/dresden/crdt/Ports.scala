package dresden.crdt

import dresden.crdt.CRDT.CRDTOperation
import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl.Port

object Ports {
    // CRDT Management
    // TODO Could merge Get into Op

    case class Get(id: String) extends KompicsEvent
    case class Op(id: String, op: CRDTOperation) extends KompicsEvent
    case class Response(id: String, payload: Any) extends KompicsEvent
    case class Update(id: String, payload: Any) extends KompicsEvent

    trait CRDTManagement extends Port {
        indication[Response]
        indication[Update]
        request[Op]
    }
    class GSetManagement extends CRDTManagement {
        request[Get]
    }
    class TwoPSetManagement extends CRDTManagement {
        request[Get]
    }
    class ORSetManagement extends CRDTManagement {
        request[Get]
    }
    class TwoPTwoPGraphManagement extends CRDTManagement {
        request[Get]
    }
}
