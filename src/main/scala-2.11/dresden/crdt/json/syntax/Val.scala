package dresden.crdt.syntax

/* Figure 7: Syntax Language
    VAL ::= n                       n ∈ Number
          | str                     str ∈ String
          | true | false | null
          | {} | []
 */

object Val {

    case class N(value: Double) extends Val

    case class Str(value: String) extends Val

    case object True extends Val

    case object False extends Val

    case object Null extends Val

    case object EmptyMap extends Val

    case object EmptyList extends Val

}

sealed trait Val
