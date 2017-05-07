package dresden.crdt

import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports.CausalOrderReliableBroadcast
import dresden.crdt.CRDT.{CRDTOpMsg, CRDTOperation}
import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl._
import se.sics.ktoolbox.util.network.KAddress

import scala.collection.mutable
import scala.util.{Failure, Success}

case class CRDTInit()

abstract class CRDTManager[T, V](init: CRDTManager.Init[T, V]) extends ComponentDefinition with StrictLogging {

    val crb = requires[CausalOrderReliableBroadcast]

    val crdts: mutable.Map[String, T] = mutable.Map.empty[String, T]

    val self = init match {
        case CRDTManager.Init(s) => s
    }

    def get(id: String): T = crdts.get(id) match {
        case Some(crdt) => crdt
        case None =>
            logger.debug(s"Creating new CRDT($id)")
            val crdt = ops.create()
            crdts.put(id, crdt)
            crdt
    }

    def ops: CRDTOpSpec[T, V]

    def op(id: String, op: CRDTOperation): Unit = crdts.get(id) match {
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

object CRDTManager {
    case class Init[T, V](self: KAddress) extends se.sics.kompics.Init[CRDTManager[T, V]]
}
