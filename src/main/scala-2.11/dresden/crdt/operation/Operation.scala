package dresden.crdt.operation

import dresden.crdt.Cursor

/*
    op(
        id : N × ReplicaID,
        deps : P(N × ReplicaID),
        cur : cursor(<k_1, . . . , kn−1>, k_n),
        mut : insert(v) | delete | assign(v) v : VAL
    )
 */
case class Operation(id: OpId, deps: Set[OpId], cur: Cursor, mut: Mutation)
