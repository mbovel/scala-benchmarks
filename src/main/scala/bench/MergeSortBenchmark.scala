package bench

import scala.collection.parallel.CollectionConverters.*
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import java.util.concurrent.*
import scala.util.DynamicVariable

import org.openjdk.jmh.annotations.*

val forkJoinPool = ForkJoinPool()

val cutoff = 2000

abstract class TaskScheduler:
  def schedule[T](body: => T): ForkJoinTask[T]
  def parallel[A, B](taskA: () => A, taskB: () => B): (A, B) =
    val right = task {
      taskB
    }
    val left = taskA()
    (left, right.join())

class DefaultTaskScheduler extends TaskScheduler:
  def schedule[T](body: => T): ForkJoinTask[T] =
    val t = new RecursiveTask[T]:
      def compute = body
    Thread.currentThread match
      case wt: ForkJoinWorkerThread =>
        t.fork()
      case _ =>
        forkJoinPool.execute(t)
    t

val scheduler =
  DynamicVariable[TaskScheduler](DefaultTaskScheduler())

def task[T](body: () => T): ForkJoinTask[T] =
  scheduler.value.schedule(body())

def parallel[A, B](taskA: () => A, taskB: () => B): (A, B) =
  scheduler.value.parallel(taskA, taskB)

/*
inline def parallel[A](inline a: () => A, inline b: () => A): (A,A) =
  (a(), b())
 */
/*
inline def parallel[A](inline a: () => A, inline b: () => A): (A,A) =
  val two = List(a, b).par.map(f => f())
  (two.head, two.tail.head)
 */

type T = String

object ListMergeSort:
  def merge(xs: List[T], ys: List[T]): List[T] =
    (xs, ys) match
      case (Nil, ys) => ys
      case (xs, Nil) => xs
      case (x :: xs1, y :: ys1) =>
        if x < y then x :: merge(xs1, ys)
        else y :: merge(xs, ys1)

  def msort(xs: List[T]): List[T] =
    val n = xs.length / 2
    if n == 0 then xs
    else
      val (fst, snd) = xs.splitAt(n)
      if n < cutoff then
        merge(msort(fst), msort(snd))
      else
        val (s1, s2) = parallel(() => msort(fst), () => msort(snd))
        merge(s1, s2)
end ListMergeSort

object VectorMergeSort:
  @scala.annotation.tailrec
  def merge(acc: Vector[T], xs: Vector[T], ys: Vector[T]): Vector[T] =
    if xs.isEmpty then acc ++ ys
    else if ys.isEmpty then acc ++ xs
    else
      val x = xs.head
      val y = ys.head
      if x < y then
        merge(acc :+ x, xs.tail, ys)
      else
        merge(acc :+ y, xs, ys.tail)

  def msort(xs: Vector[T]): Vector[T] =
    val n = xs.length / 2
    if n == 0 then xs
    else
      val (fst, snd) = xs.splitAt(n)
      if n < cutoff then
        merge(Vector(), msort(fst), msort(snd))
      else
        val (s1, s2) = parallel(() => msort(fst), () => msort(snd))
        merge(Vector(), s1, s2)
end VectorMergeSort

@State(Scope.Benchmark)
class MergeSortBenchmark:
  val lst: List[T] = (15000 to 1 by -3).toList.map(n => f"${n}%5d")
  val vec: Vector[T] = lst.toVector

  @Benchmark
  def list = ListMergeSort.msort(lst)

  @Benchmark
  def vector = VectorMergeSort.msort(vec)
