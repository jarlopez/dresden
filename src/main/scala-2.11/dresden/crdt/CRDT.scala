package dresden.crdt

import dresden.networking.MessageCheck

object CRDT {

    import se.sics.kompics.KompicsEvent

    trait OpBasedCRDT

    trait CRDTOperation extends KompicsEvent

//    case object CRDTOpMsg extends MessageCheck[CRDTOperation]
    case class CRDTOpMsg(id: String, msg: KompicsEvent) extends MessageCheck[CRDTOperation] with KompicsEvent
}