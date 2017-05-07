package dresden.crdt


import scala.util.{Success, Try}

trait CRDTOpSpec[T, V] {

    def create(): T

    def query(state: T): V

    def precondition: Boolean = true

    def prepare(op: Any, state: T): Try[Any] = Success(op)

    def effect(op: Any, state: T): T = state

}
