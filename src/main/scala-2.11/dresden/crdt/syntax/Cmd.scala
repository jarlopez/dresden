package dresden.crdt.syntax

import dresden.crdt.syntax.Expr.Var

/* Figure 7: Syntax Language
    CMD ::= let x = EXPR            x ∈ VAR
          | EXPR := v               v ∈ VAL
          | EXPR.insertAfter(v)     v ∈ VAL
          | EXPR.delete
          | yield
          | CMD; CMD
 */
object Cmd {
    case class Let(x: Var, expression: Expr) extends Cmd
    case class Assignment(expression: Expr, value: Val) extends Cmd
    case class InsertAfter(expression: Expr, value: Val) extends Cmd
    case class Delete(expression: Expr) extends Cmd
    case class Yield(expression: Expr) extends Cmd
    case class Chain(c0: Cmd, c1: Cmd) extends Cmd
}

sealed trait Cmd