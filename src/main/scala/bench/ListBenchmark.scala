package bench

import org.openjdk.jmh.annotations._

@State(Scope.Benchmark)
class BenchmarkState:
  val bigList: List[Int] = (1 to 1000).toList

class ListBenchmarks:

  @Benchmark
  def prependList(state: BenchmarkState) = (1 to 100).foldLeft(state.bigList)((acc, i) => i :: acc)

  @Benchmark
  def prependSeq(state: BenchmarkState) = (1 to 100).foldLeft(state.bigList)((acc, i) => i +: acc)

  @Benchmark
  def appendSeq(state: BenchmarkState) = (1 to 100).foldLeft(state.bigList)((acc, i) => acc :+ i)

  @Benchmark
  def concatList(state: BenchmarkState) = state.bigList ::: state.bigList

  @Benchmark
  def concatSeq(state: BenchmarkState) = state.bigList ++ state.bigList

  @Benchmark
  def concatList3(state: BenchmarkState) = state.bigList ::: state.bigList ::: state.bigList

  @Benchmark
  def concatSeq3(state: BenchmarkState) = state.bigList ++ state.bigList ++ state.bigList