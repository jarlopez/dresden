package dresden.crdt.set

import dresden.crdt.CRDT.{CRDTOperation, OpBasedCRDT}
import dresden.crdt.{CRDTManager, CRDTOpSpec}
import dresden.crdt.Ports.TwoPSetManagement
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


case class TwoPSet[T](entries: Set[T] = Set.empty[T]) extends OpBasedCRDT {

    def add(e: T): TwoPSet[T] = {
        copy(entries = entries + e)
    }

    def query(e: T): Boolean = {
        entries contains e
    }

    def remove(e: T): TwoPSet[T] = {
        copy(entries = entries - e)
    }
}

object TwoPSet {
    def apply[V]: TwoPSet[V] = new TwoPSet[V]()
}

object TwoPSetManager {
    case class Init(self: KAddress)
}

class TwoPSetManager[V](init: Init[CRDTManager[TwoPSet[V], Set[V]]]) extends CRDTManager[TwoPSet[V], Set[V]](init) {

    case class AddOperation(e: Any) extends CRDTOperation
    case class RemoveOperation(e: Any) extends CRDTOperation
    case class QueryOperation(e: Any) extends CRDTOperation

    // Hack to convert init to CRDTManagers's init
    def this(it: TwoPSetManager.Init) = {
        this(new Init[CRDTManager[TwoPSet[V], Set[V]]](it.self))
    }

    override val mgmt = provides[TwoPSetManagement]

    override def ops: CRDTOpSpec[TwoPSet[V], Set[V]] = new CRDTOpSpec[TwoPSet[V], Set[V]] {
        override def query(state: TwoPSet[V]): Set[V] = {
            state.entries
        }

        override def prepare(op: CRDTOperation, state: TwoPSet[V]): Try[Option[Any]] = op match {
            case AddOperation(it: V) =>
                if (state.query(it)) Success(Some(op))
                else Failure(new Exception("The set does not contain the element."))
            case _ => super.prepare(op, state)
        }

        override def create(): TwoPSet[V] = TwoPSet.apply[V]()

        override def effect(op: CRDTOperation, state: TwoPSet[V]): TwoPSet[V] = op match {
            case AddOperation(it: V) =>
                //XXX Type erasure! What if two clients use same ID but different type?
                state.add(it)
            case RemoveOperation(it: V) =>
                // Causal-order ensures that add(it) has been delivered
                state.remove(it)
            case _ =>
                super.effect(op, state)
        }
    }
}

