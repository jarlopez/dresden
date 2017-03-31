package dresden.crdt
/*
    cursor(<k_1, . . . , k_n−1>, k_n)
 */
// ex: cursor(<mapT(doc), listT(“shopping”)>, id1)
case class Cursor(keys: List[BranchableKey], finalKey: Key)
