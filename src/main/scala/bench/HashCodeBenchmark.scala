package bench

import org.openjdk.jmh.annotations.*

@State(Scope.Benchmark)
class HashCodeBenchmark:
  @Param(Array("Tree", "TreeCached", "TreeCachedLazy"))
  var className: String = _

  @Param(Array("small", "medium"))
  var size: String = _

  var tree: Any = _

  @Setup(Level.Iteration)
  def setup =
    val sizeInt = size match
      case "small"  => 2
      case "medium" => 5
    tree = className match
      case "Tree"           => makeTree(sizeInt)
      case "TreeCached"     => makeTreeCached(sizeInt)
      case "TreeCachedLazy" => makeTreeCachedLazy(sizeInt)

    def makeTree(depth: Int): Tree | Null =
      if depth == 0 then null
      else Tree(depth, makeTree(depth - 1), makeTree(depth - 1))

    def makeTreeCached(depth: Int): TreeCached | Null =
      if depth == 0 then null
      else TreeCached(depth, makeTreeCached(depth - 1), makeTreeCached(depth - 1))

    def makeTreeCachedLazy(depth: Int): TreeCached | Null =
      if depth == 0 then null
      else TreeCached(depth, makeTreeCachedLazy(depth - 1), makeTreeCachedLazy(depth - 1))

  @Benchmark
  def hashCodeBench = tree.hashCode
