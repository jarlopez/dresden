package dresden.crdt.set

import dresden.crdt.CRDT.{CRDTOperation, OpBasedCRDT}
import dresden.crdt.Ports._
import dresden.crdt.{CRDTManager, CRDTOpSpec}
import se.sics.kompics.sl._
import se.sics.ktoolbox.util.network.KAddress

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
    def apply[V]: GSet[V] = new GSet[V]()
}

object GSetManager {
    case class Init(self: KAddress)
}

class GSetManager[V](init: Init[CRDTManager[GSet[V], Set[V]]]) extends CRDTManager[GSet[V], Set[V]](init) {

    // Hack to convert init to CRDTManagers's init
    def this(it: GSetManager.Init) = {
        this(new Init[CRDTManager[GSet[V], Set[V]]](it.self))
    }

    override val mgmt = provides[GSetManagement]

    override def ops: CRDTOpSpec[GSet[V], Set[V]] = new CRDTOpSpec[GSet[V], Set[V]] {
        override def query(state: GSet[V]): Set[V] = {
            state.entries
        }

        override def create(): GSet[V] = GSet.apply[V]()

        override def effect(op: CRDTOperation, state: GSet[V]): GSet[V] = op match {
            case AddOperation(it: V) =>
                //XXX Type erasure! What if two clients use same ID but different type?
                state.add(it)
            case _ =>
                super.effect(op, state)
        }
    }
}

