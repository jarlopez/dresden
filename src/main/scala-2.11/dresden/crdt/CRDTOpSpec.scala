package dresden.crdt


import dresden.crdt.CRDT.CRDTOperation

import scala.util.{Success, Try}

trait CRDTOpSpec[T, V] {

    def create(): T

    def query(state: T): V

    def precondition: Boolean = true

    def prepare(op: CRDTOperation, state: T): Try[CRDTOperation] = Success(op)

    def effect(op: CRDTOperation, state: T): T = state

}
