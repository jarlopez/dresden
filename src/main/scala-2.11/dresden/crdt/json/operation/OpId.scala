package dresden.crdt.operation

import dresden.crdt.json.ReplicaId

case class OpId(ts: Integer, p: ReplicaId)
