package dresden.crdt.operation

import dresden.crdt.syntax.Val

/* 4.2.2  Operation structure
    mut : insert(v) | delete | assign(v) v : VAL
 */
object Mutation {

    case class Insert(v: Val) extends Mutation

    case class Assign(v: Val) extends Mutation

    case object Delete extends Mutation

}

sealed trait Mutation
