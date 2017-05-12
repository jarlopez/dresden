package dresden.crdt.graph

import dresden.crdt.CRDT.{CRDTOperation, OpBasedCRDT}
import dresden.crdt.Ports.TwoPTwoPGraphManagement
import dresden.crdt.graph.TwoPTwoPGraphManager._
import dresden.crdt.{CRDTManager, CRDTOpSpec}
import se.sics.kompics.sl.Init
import se.sics.ktoolbox.util.network.KAddress

import scala.util.{Failure, Try}

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
20              pre ∀(u, v) ∈ (EA \ ER) : u != w ∧ v != w   ⊲ Graph precondition: E ⊆ V × V
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
                               ea: Set[(V, V)] = Set.empty[(V, V)],
                               er: Set[(V, V)] = Set.empty[(V, V)]) extends OpBasedCRDT {

    def lookup(v: V): Boolean = (va -- vr).contains(v)

    def lookup(edge: (V, V)): Boolean =  lookup(edge._1) && lookup(edge._2) && (ea -- er).contains(edge)

    def addVertex(w: V): TwoPTwoPGraph[V] = copy(va = va + w)

    def addEdge(edge: (V, V)): TwoPTwoPGraph[V] = copy(ea = ea + edge)

    def removeVertex(w: V): TwoPTwoPGraph[V] = copy(vr = vr + w)

    def removeEdge(edge: (V, V)): TwoPTwoPGraph[V] = copy(er = er + edge)
}

object TwoPTwoPGraph {
    def apply[V]: TwoPTwoPGraph[V] = new TwoPTwoPGraph[V]()

}

object TwoPTwoPGraphManager {
    case class Init(self: KAddress)

    case class AddVertexOperation(e: Any) extends CRDTOperation
    case class AddEdgeOperation(e: Any) extends CRDTOperation
    case class RemoveVertexOperation(e: Any) extends CRDTOperation
    case class RemoveEdgeOperation(e: Any) extends CRDTOperation
    case class QueryVertexOperation(e: Any) extends CRDTOperation
    case class QueryEdgeOperation(e: Any) extends CRDTOperation
}

// TODO Figure out how to represent second type (currently V)
class TwoPTwoPGraphManager[V](init: Init[CRDTManager[TwoPTwoPGraph[V], V]]) extends CRDTManager[TwoPTwoPGraph[V], V](init) {
    override val mgmt = provides[TwoPTwoPGraphManagement]

    // Hack to convert init to CRDTManagers's init
    def this(it: TwoPTwoPGraphManager.Init) = {
        this(new Init[CRDTManager[TwoPTwoPGraph[V], V]](it.self))
    }

    override def ops: CRDTOpSpec[TwoPTwoPGraph[V], V] = new CRDTOpSpec[TwoPTwoPGraph[V], V] {

        override def query(state: TwoPTwoPGraph[V]): ((Set[V], Set[(V, V)])) = (state.va -- state.vr, state.ea -- state.er)

        override def prepare(op: CRDTOperation, state: TwoPTwoPGraph[V]): Try[Option[Any]] = op match {
            case RemoveVertexOperation(w: V) =>
                val check = (state.ea -- state.er).forall(x =>
                    !x._1.equals(w) && !x._2.equals(w)
                )
                if (state.lookup(w) && check) {
                    super.prepare(op, state)
                } else {
                    Failure(new Throwable("Failed precondition"))
                }
            case AddEdgeOperation(it: (V, V)) =>
                if (state.lookup(it._1) && state.lookup(it._2)) {
                    super.prepare(op, state)
                } else {
                    Failure(new Throwable("Failed precondition"))
                }
            case RemoveEdgeOperation(it: (V, V)) =>
                if (state.lookup(it)) {
                    super.prepare(op, state)
                } else {
                    Failure(new Throwable("Failed precondition"))
                }
            case _ => super.prepare(op, state)
        }

        override def create(): TwoPTwoPGraph[V] = TwoPTwoPGraph.apply[V]()

        override def effect(op: CRDTOperation, state: TwoPTwoPGraph[V]): TwoPTwoPGraph[V] = op match {
            case AddVertexOperation(it: V) => state.addVertex(it)
            case AddEdgeOperation(it: (V, V)) => state.addEdge(it)
            case RemoveVertexOperation(it: V) => state.removeVertex(it)
            case RemoveEdgeOperation(it: (V, V)) => state.removeEdge(it)
            case _ =>
                super.effect(op, state)
        }

    }
}