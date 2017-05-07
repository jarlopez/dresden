package dresden.crdt.set

import com.typesafe.scalalogging.StrictLogging
import dresden.components.Ports.{CRB_Broadcast, CRB_Deliver, CausalOrderReliableBroadcast}
import dresden.crdt.CRDT.{CRDTOpMsg, CRDTOperation, OpBasedCRDT}
import dresden.crdt.Ports.{GSetManagement, Get, Op, Response}
import dresden.crdt.{CRDTManager, CRDTOpSpec}
import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl._
import se.sics.ktoolbox.util.network.KAddress

import scala.collection.mutable
import scala.util.{Failure, Success}

/*
        Σ = P(V )
        σ^0_i = {}
        prepare_i(o, σ) = o
        effect_i([add, v], s) = s ∪ {v}
        eval_i(rd, s) = s
  */

case class AddOperation(e: Any) extends CRDTOperation
case class QueryOperation(e: Any) extends CRDTOperation

case class GSet[T](entries: Set[T] = Set.empty[T]) extends OpBasedCRDT {


    def add(e: T): GSet[T] = {
        copy(entries = entries + e)
    }

    def query(e: T): Boolean = {
        entries contains e
    }
}

object GSet {
    def apply[V]: GSet[V] = {
        new GSet[V]()
    }

    implicit def GSetOpsSpec[V] = new CRDTOpSpec[GSet[V], Set[V]] {
        override def query(state: GSet[V]): Set[V] = {
            state.entries
        }

        override def create(): GSet[V] = GSet.apply[V]()

        override def effect(op: Any, state: GSet[V]): GSet[V] = op match {
            case AddOperation(it: V) =>
                state.add(it)
            case _ =>
                super.effect(op, state)
        }
    }
}

class GSetManager[V](init: Init[GSetManager[V]]) extends ComponentDefinition with StrictLogging {
    type T = GSet[V]

    val mgmt = provides[GSetManagement]

    val crb = requires[CausalOrderReliableBroadcast]

    val crdts: mutable.Map[String, T] = mutable.Map.empty[String, T]

    val self = init match {
        case Init(s) => s
        case wut => logger.warn(s"wut: $wut")
    }

    def get(id: String): T = crdts.get(id) match {
        case Some(crdt) => crdt
        case None =>
            logger.debug(s"Creating new CRDT($id)")
            val crdt = ops.create()
            crdts.put(id, crdt)
            crdt
    }

    def ops: CRDTOpSpec[T, Set[V]] = new CRDTOpSpec[GSet[V], Set[V]] {
        override def query(state: GSet[V]): Set[V] = {
            state.entries
        }

        override def create(): GSet[V] = GSet.apply[V]()

        override def effect(op: Any, state: GSet[V]): GSet[V] = op match {
            case AddOperation(it: V) =>
                state.add(it)
            case _ =>
                super.effect(op, state)
        }
    }

    def op(id: String, op: CRDTOperation): Unit = crdts.get(id) match {
        case Some(crdt) =>
            ops.prepare(op, crdt) match {
                case Success(msg: CRDTOperation) =>
                    logger.debug(s"$self triggering msg after prepare phase")
                    val wrapper = CRB_Broadcast(Op(id, msg))
                    trigger(wrapper -> crb)
                case Failure(msg) => logger.warn(s"$self Failed to prepare operation $op on $id")
                case any => logger.warn(s"$self Got something else: $any")
            }
        case _ => logger.warn(s"CRDT $id not found")
    }

    crb uponEvent {
        // TODO
        case CRB_Deliver(from, Op(id, msg)) => handle {
            logger.debug(s"$self Handling CRDT operation")
            val updated = ops.effect(msg, get(id))
            crdts.put(id, updated)
            logger.info(s"$self now has: ${get(id)}")
        }
    }

    mgmt uponEvent {
        case Get(id) => handle {
            logger.debug(s"Received GET$id request")
            val res = get(id)
            trigger(Response(id, res) -> mgmt)
        }
        case Op(id, add: AddOperation) => handle {
            logger.debug(s"Handling add operation for $id")
            op(id, add)
        }
        case whatever => handle {
            logger.debug(s"Received whatever: $whatever")
        }
    }

}

