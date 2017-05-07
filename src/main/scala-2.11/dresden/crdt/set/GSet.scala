package dresden.crdt.set

import dresden.crdt.CRDT.OpBasedCRDT
import dresden.crdt.Ports.{GSetManagement, GSet_Add}
import dresden.crdt.{CRDTManager, CRDTOpSpec}
import se.sics.kompics.sl._

/*
        Σ = P(V )
        σ^0_i = {}
        prepare_i(o, σ) = o
        effect_i([add, v], s) = s ∪ {v}
        eval_i(rd, s) = s
  */
case class AddOperation(e: Any)

case class QueryOperation(e: Any)

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
    }
}

class GSetManager[V](init: Init[CRDTManager[_, _]])(implicit val ops: CRDTOpSpec[GSet[V], Set[V]]) extends CRDTManager[GSet[V], Set[V]](init) {
    val mgmt = provides[GSetManagement]

    private var crdt: GSet[V] = GSet.apply[V]

    def run(): Unit = {
        var set: GSet[String] = GSet.apply[String]()
        set = set.add("me")
        set = set.add("you")
    }

}

