package dresden.crdt

import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports.{CRB_Broadcast, CRB_Deliver, CausalOrderReliableBroadcast}
import dresden.crdt.CRDT.CRDTOperation
import dresden.crdt.Ports._
import se.sics.kompics.Start
import se.sics.kompics.sl._

import scala.collection.mutable
import scala.util.{Failure, Success}


abstract class CRDTManager[T, V](init: Init[CRDTManager[T, V]]) extends ComponentDefinition with StrictLogging {

    val mgmt: NegativePort[_ <: CRDTManagement]

    val crb = requires[CausalOrderReliableBroadcast]

    val crdts: mutable.Map[String, T] = mutable.Map.empty[String, T]

    val self = init match {
        case Init(s) => s
    }

    def get(id: String): T = crdts.get(id) match {
        case Some(crdt) => crdt
        case None =>
            val crdt = ops.create()
            crdts.put(id, crdt)
            crdt
    }

    def ops: CRDTOpSpec[T, V]

    def op(id: String, op: CRDTOperation): Unit = crdts.get(id) match {
        case Some(crdt) =>
            ops.prepare(op, crdt) match {
                case Success(Some(msg: CRDTOperation)) =>
                    logger.debug(s"$self triggering msg after prepare phase")
                    val wrapper = CRB_Broadcast(Op(id, msg))
                    trigger(wrapper -> crb)
                case Success(None) =>
                    logger.debug(s"$self got None back from prepare, skipping effects.")
                case Failure(msg) => logger.warn(s"$self Failed to prepare operation $op on $id")
                case any => logger.warn(s"$self Got something else: $any")
            }
        case _ => logger.warn(s"CRDT $id not found")
    }

    ctrl uponEvent {
        case _: Start => handle {
            mgmt uponEvent {
                case Get(id) => handle {
                    logger.debug(s"Received GET$id request")
                    val res = get(id)
                    trigger(Response(id, res) -> mgmt)
                }
                case Op(id, cmd: CRDTOperation) => handle {
                    logger.debug(s"Handling cmd for $id")
                    op(id, cmd)
                }
            }
        }
    }

    crb uponEvent {
        case CRB_Deliver(from, Op(id, msg)) => handle {
            logger.debug(s"$self Handling CRDT operation")
            val updated = ops.effect(msg, get(id))
            crdts.put(id, updated)
            logger.info(s"$self now has: ${get(id)}")
        }
    }

}
