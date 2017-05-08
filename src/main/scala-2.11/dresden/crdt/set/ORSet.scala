package dresden.crdt.set

import dresden.crdt.CRDT.{CRDTOperation, OpBasedCRDT}
import dresden.crdt.{CRDTManager, CRDTOpSpec}
import dresden.crdt.Ports.ORSetManagement
import dresden.crdt.set.ORSetManager.{AddOperation, RemoveOperation}
import dresden.networking.MessageCheck
import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl.Init
import se.sics.ktoolbox.util.network.KAddress

import scala.util.{Failure, Success, Try}

/* U-Set: Op-based 2P-Set with unique elements
1   payload set S
2       initial ∅
3   query lookup (element e) : boolean b
4       let b = (e ∈ S)
5   update add (element e)
6       atSource (e)
7           pre e is unique
8       downstream (e)
9           S := S ∪ {e}
10  update remove (element e)
11      atSource (e)
12          pre lookup(e) ⊲ 2P-Set precondition
13      downstream (e)
14          pre add(e) has been delivered ⊲ Causal order suffices
15          S := S \ {e}
 */


case class ORSet[T](entries: Set[(T, String)] = Set.empty[(T, String)]) extends OpBasedCRDT {

    def add(e: T): ORSet[T] = ???

    def query(e: T): Boolean = ???

    def remove(e: T): ORSet[T] = ???
}

object ORSet {
    def apply[V]: ORSet[V] = new ORSet[V]()
}

object ORSetManager {
    case class Init(self: KAddress)

    case class AddOperation(e: Any) extends CRDTOperation
    case class RemoveOperation(e: Any) extends CRDTOperation
    case class QueryOperation(e: Any) extends CRDTOperation
}

class ORSetManager[V](init: Init[CRDTManager[ORSet[V], Set[V]]]) extends CRDTManager[ORSet[V], Set[V]](init) {

    override val mgmt = provides[ORSetManagement]

    // Hack to convert init to CRDTManagers's init
    def this(it: ORSetManager.Init) = {
        this(new Init[CRDTManager[ORSet[V], Set[V]]](it.self))
    }


    override def ops: CRDTOpSpec[ORSet[V], Set[V]] = new CRDTOpSpec[ORSet[V], Set[V]] {
        override def query(state: ORSet[V]): Set[V] = ???
        
        override def prepare(op: CRDTOperation, state: ORSet[V]): Try[Option[Any]] = op match {
            // TODO
            case _ => super.prepare(op, state)
        }

        override def create(): ORSet[V] = ORSet.apply[V]()

        override def effect(op: CRDTOperation, state: ORSet[V]): ORSet[V] = op match {
            case _ =>
                super.effect(op, state)
        }
    }
}


