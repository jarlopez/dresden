package dresden.crdt

import dresden.crdt.CRDT.OpBasedCRDT

import scala.util.{Success, Try}

trait CRDTOpSpec[T <: OpBasedCRDT, V] {

    def query(state: T): V

    def precondition: Boolean = true

    def prepare(op: Any, state: T): Try[Any] = Success(op)

    def effect(op: Any, state: T): T = state

}
