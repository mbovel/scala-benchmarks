package bench

import scala.collection.parallel.CollectionConverters.*
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import java.util.concurrent.*
import scala.util.DynamicVariable

import org.openjdk.jmh.annotations.*

@State(Scope.Benchmark)
@Fork(1, jvmArgs = Array("-Xss10m"))
class MergeSortBenchmark:
  util.Random.setSeed(451L)
  val lst: List[Int] = List.fill(5000)(util.Random.nextInt())
  val vec: Vector[Int] = lst.toVector

  @Benchmark
  def list = mergeSort(lst)

  @Benchmark
  def vector = mergeSort(vec)
