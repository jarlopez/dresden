package dresden.crdt.operation

import dresden.crdt.ReplicaId

case class OpId(ts: Integer, p: ReplicaId)
