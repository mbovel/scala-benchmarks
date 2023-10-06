package bench

import org.openjdk.jmh.annotations.*

@State(Scope.Benchmark)
class ListBenchmark:
  val bigList: List[Int] = (1 to 1000).toList

  @Benchmark
  def prependList = 0 :: bigList

  @Benchmark
  def prependSeq = 0 +: bigList

  @Benchmark
  def appendSeq = bigList :+ 0

  @Benchmark
  def concatList = bigList ::: bigList

  @Benchmark
  def concatSeq = bigList ++ bigList

  @Benchmark
  def concatList3 = bigList ::: bigList ::: bigList

  @Benchmark
  def concatSeq3 = bigList ++ bigList ++ bigList
