package dresden.crdt.syntax

/* Figure 7: Syntax Language
    EXPR ::= doc
           | x                  x ∈ VAR
           | EXPR.get(key)      key ∈ String
           | EXPR.idx(i)        i ≥ 0
           | EXPR.keys
           | EXPR.values
 */
object Expr {
    case object Doc extends Expr
    case class Var(name: String) extends Expr
    case class Get(expression: Expr, key: String) extends Expr
    case class Keys(expression: Expr) extends Expr
    case class Values(expression: Expr) extends Expr
}

sealed trait Expr