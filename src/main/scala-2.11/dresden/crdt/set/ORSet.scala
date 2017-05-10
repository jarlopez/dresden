package dresden.crdt.set

import java.util.UUID

import dresden.crdt.CRDT.{CRDTOperation, OpBasedCRDT}
import dresden.crdt.{CRDTManager, CRDTOpSpec}
import dresden.crdt.Ports.ORSetManagement
import dresden.crdt.set.ORSetManager.{AddOperation, RemoveOperation}
import dresden.networking.MessageCheck
import se.sics.kompics.KompicsEvent
import se.sics.kompics.sl.Init
import se.sics.ktoolbox.util.network.KAddress

import scala.util.{Failure, Success, Try}

/* Op-Based Observed-Remove Set
1   payload set S                           ⊲ set of pairs { (element e, unique-tag u) ... }
2       initial ∅
3   query lookup (element e) : boolean b
4       let b = (∃u : (e, u) ∈ S)
5   update add (element e)
6       atSource (e)
7           let a = unique()                ⊲ unique() returns a unique value
8       downstream (e, a)
9           S := S ∪ {(e, a)}
10  update remove (element e)
11      atSource (e)
12          pre lookup(e)                   ⊲ 2P-Set precondition
13          let R = {(e, u)| ∃u : (e, u) ∈ S}
14      downstream (R)
15          pre ∀(e, u) ∈ R: add(e) has been delivered  ⊲ Causal order suffices
16          S := S \ {e}                    ⊲ Remove pairs observed at source
 */


case class ORSet[T](entries: Set[(T, String)] = Set.empty[(T, String)]) extends OpBasedCRDT {

    def add(e: (T, String)): ORSet[T] = {
        copy(entries = entries + e)
    }

    def query(e: T): Boolean = {
        entries.toMap.get(e) match {
            case Some(_) => true
            case None => false
        }
    }

    def elements(): Set[T] = {
        entries.map(it => it._1)
    }

    def remove(e: Set[(T, String)]): ORSet[T] = {
        copy(entries = entries  -- e)
    }
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
        override def query(state: ORSet[V]): Set[V] = {
            state.elements()
        }
        
        override def prepare(op: CRDTOperation, state: ORSet[V]): Try[Option[Any]] = op match {
            case AddOperation(it: V) =>
                val alpha: String = UUID.randomUUID().toString
                Success(Some(AddOperation((it, alpha))))
            case RemoveOperation(it: V) =>
                if (state.query(it)) {
                    state.entries.find(it => it._1.equals(it)) match {
                        case Some(els) => Success(Some(RemoveOperation(els)))
                        case None => Failure(new Throwable("No data to remove"))
                    }
                } else {
                    Failure(new Throwable("Not found, won't push downstream."))
                }
            case _ => super.prepare(op, state)
        }

        override def create(): ORSet[V] = ORSet.apply[V]()

        override def effect(op: CRDTOperation, state: ORSet[V]): ORSet[V] = op match {
            case AddOperation(pair: (V, String)) =>
                //XXX Type erasure! What if two clients use same ID but different type?
                logger.debug(s"adding pair $pair")
                state.add(pair)
            case RemoveOperation(els: Set[(V, String)]) =>
                // Causal-order ensures that add(it) has been delivered
                logger.debug(s"removing els $els")
                state.remove(els)
            case _ =>
                super.effect(op, state)
        }
    }
}


