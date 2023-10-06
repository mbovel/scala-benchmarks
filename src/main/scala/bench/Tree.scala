package bench

case class Tree(value: Int, left: Tree | Null, right: Tree | Null)

class TreeCached(value: Int, left: Tree | Null, right: Tree | Null) extends Tree(value, left, right):
  override val hashCode: Int = super.hashCode()

class TreeCachedLazy(value: Int, left: Tree | Null, right: Tree | Null) extends Tree(value, left, right):
  override lazy val hashCode: Int = super.hashCode()
