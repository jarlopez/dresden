package dresden.crdt.set

import dresden.crdt.{CRDT, CRDTManager}
import se.sics.ktoolbox.util.network.KAddress

case class AddOperation(e: Any)
case class QueryOperation(e: Any)

case class GSet[T](entries: Set[T] = Set.empty[T]) extends CRDT {

    def add(e: T): Unit = {
        copy(entries = entries + e)
    }

    def query(e: T): Boolean = {
        entries contains e
    }
}

object GSet {
    def apply[T]: GSet[T] = {
        new GSet[T]()
    }
}

class GSetManager[T](id: String, host: KAddress) extends CRDTManager[GSet[T]] {
    private var crdt: GSet[T] = GSet.apply[T]

}

