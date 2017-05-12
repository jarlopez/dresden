package dresden.crdt.graph

import java.util.UUID

import dresden.crdt.CRDT.{CRDTOperation, OpBasedCRDT}
import dresden.crdt.{CRDTManager, CRDTOpSpec}
import dresden.crdt.Ports.TwoPTwoPGraphManagement
import dresden.crdt.graph.TwoPTwoPGraphManager.{AddOperation, RemoveOperation}
import se.sics.kompics.sl.Init
import se.sics.ktoolbox.util.network.KAddress

import scala.util.{Failure, Success, Try}

/*
1       payload set VA, VR, EA, ER
2                                                           ⊲ V : vertices; E: edges; A: added; R: removed
3           initial ∅, ∅, ∅, ∅
4       query lookup (vertex v) : boolean b
5           let b = (v ∈ (VA \ VR))
6       query lookup (edge (u, v)) : boolean b
7           let b = (lookup(u) ∧ lookup(v) ∧ (u, v) ∈ (EA \ ER))
8       update addVertex (vertex w)
9           atSource (w)
10          downstream (w)
11              VA := VA ∪ {w}
12      update addEdge (vertex u, vertex v)
13          atSource (u, v)
14              pre lookup(u) ∧ lookup(v)                   ⊲ Graph precondition: E ⊆ V × V
15          downstream (u, v)
16              EA := EA ∪ {(u, v)}
17      update removeVertex (vertex w)
18          atSource (w)
19              pre lookup(w) ⊲ 2P-Set precondition
20              pre ∀(u, v) ∈ (EA \ ER) : u 6= w ∧ v 6= w   ⊲ Graph precondition: E ⊆ V × V
21          downstream (w)
22              pre addVertex(w) delivered                  ⊲ 2P-Set precondition
23              VR := VR ∪ {w}
24      update removeEdge (edge (u, v))
25          atSource ((u, v))
26              pre lookup((u, v))                          ⊲ 2P-Set precondition
27          downstream (u, v)
28              pre addEdge(u, v) delivered                 ⊲ 2P-Set precondition
29              ER := ER ∪ {(u, v)}
 */

case class TwoPTwoPGraph[V](
                               va: Set[V] = Set.empty[V],
                               vr: Set[V] = Set.empty[V],
                               ea: Set[V] = Set.empty[V],
                               er: Set[V] = Set.empty[V]) extends OpBasedCRDT {

}

object TwoPTwoPGraph {
    def apply[V]: TwoPTwoPGraph[V] = new TwoPTwoPGraph[V]()

}

object TwoPTwoPGraphManager {
    case class Init(self: KAddress)

    case class AddOperation(e: Any) extends CRDTOperation
    case class RemoveOperation(e: Any) extends CRDTOperation
    case class QueryOperation(e: Any) extends CRDTOperation
}

// TODO Figure out how to represent second type (currently V)
class TwoPTwoPGraphManager[V](init: Init[CRDTManager[TwoPTwoPGraph[V], V]]) extends CRDTManager[TwoPTwoPGraph[V], V](init) {
    override val mgmt = provides[TwoPTwoPGraphManagement]

    // Hack to convert init to CRDTManagers's init
    def this(it: TwoPTwoPGraphManager.Init) = {
        this(new Init[CRDTManager[TwoPTwoPGraph[V], V]](it.self))
    }

    override def ops: CRDTOpSpec[TwoPTwoPGraph[V], V] = new CRDTOpSpec[TwoPTwoPGraph[V], V] {

        override def prepare(op: CRDTOperation, state: TwoPTwoPGraph[V]): Try[Option[Any]] = op match {
            case AddOperation(it: V) => Failure(new Throwable("Not implemented"))

            case RemoveOperation(it: V) => Failure(new Throwable("Not implemented"))

            case _ => super.prepare(op, state)
        }

        override def create(): TwoPTwoPGraph[V] = TwoPTwoPGraph.apply[V]()

        override def effect(op: CRDTOperation, state: TwoPTwoPGraph[V]): TwoPTwoPGraph[V] = op match {
            case AddOperation(pair: (V, String)) => state           // TODO
            case RemoveOperation(els: Set[(V, String)]) => state    // TODO
            case _ =>
                super.effect(op, state)
        }

        override def query(state: TwoPTwoPGraph[V]): V = ???        // TODO
    }
}