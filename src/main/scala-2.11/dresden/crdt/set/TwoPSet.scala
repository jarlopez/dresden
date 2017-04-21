package dresden.crdt.set

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
class TwoPSet {

}
