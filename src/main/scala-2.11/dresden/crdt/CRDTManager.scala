package dresden.crdt

import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports.CausalOrderReliableBroadcast
import dresden.crdt.CRDT.{CRDTOpMsg, CRDTOperation, OpBasedCRDT}
import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl._
import se.sics.ktoolbox.util.network.KAddress

import scala.util.{Failure, Success, Try}

abstract class CRDTManager[T <: OpBasedCRDT, V](init: Init[CRDTManager[_, _]]) extends ComponentDefinition with StrictLogging {

    val crb = requires[CausalOrderReliableBroadcast]

    val crdts: Map[String, T] = Map.empty[String, T]

    val self = init match {
        case Init(s: KAddress) => s
    }

    def get(id: String): Option[T] = crdts.get(id)

    def ops: CRDTOpSpec[T, V]

    def op(id: String, op: CRDTOperation): Unit = get(id) match {
        case Some(crdt) =>
            ops.prepare(op, crdt) match {
                case Success(msg: KompicsEvent) =>
                    logger.debug(s"$self triggering msg after prepare phase")
                    val wrapper = CRDTOpMsg(id, msg)
                    trigger(wrapper -> crb)
                case Failure(msg) => logger.warn(s"$self Failed to prepare operation $op on $id")
                case any => logger.warn(s"$self Got something else: $any")
            }
        case _ => logger.warn(s"CRDT $id not found")
    }

    crb uponEvent {
        // TODO
        case CRDTOpMsg(crdtId, msg) => handle {
                logger.debug(s"$self Handling CRDT operation")
        }
    }
}
