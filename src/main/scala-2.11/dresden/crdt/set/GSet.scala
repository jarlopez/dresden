package dresden.crdt.set

import scala.collection.mutable

/* State-based grow-only Set (G-Set)

1   payload set A
2       initial ∅
3   update add (element e)
4       A := A ∪ {e}
5   query lookup (element e) : boolean b
6       let b = (e ∈ A)

7   compare (S, T) : boolean b
8       let b = (S.A ⊆ T.A)
9   merge (S, T) : payload U
10      let U.A = S.A ∪ T.A

*/
class GSet {
    private val payload:mutable.HashSet[Any] = mutable.HashSet[Any]()

    def add(e: Any): Unit = {
        payload add e
    }

    def query(e: Any): Boolean = {
        payload contains e
    }
}

object GSet {
    def compare(S: GSet, T:GSet): Boolean = {
        S.payload subsetOf T.payload
    }

    def merge(S: GSet, T:GSet): GSet = {
        val rv = new GSet
        rv.payload ++= S.payload union T.payload
        rv
    }
}