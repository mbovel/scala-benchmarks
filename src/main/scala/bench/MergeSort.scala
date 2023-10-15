package bench

import scala.collection.parallel.CollectionConverters.*
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import java.util.concurrent.*
import scala.util.DynamicVariable

import org.openjdk.jmh.annotations.*
import scala.annotation.tailrec

val forkJoinPool = ForkJoinPool()

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

val cutoff = 2000


object ListMergeSort:
  def merge[T](xs: List[T], ys: List[T])(using ord: Ordering[T]): List[T] =
    (xs, ys) match
      case (Nil, ys) => ys
      case (xs, Nil) => xs
      case (x :: xs1, y :: ys1) =>
        if ord.lt(x, y) then x :: merge(xs1, ys)
        else y :: merge(xs, ys1)

  def msort[T](xs: List[T])(using ord: Ordering[T]): List[T] =
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

object SeqMergeSort:
  @scala.annotation.tailrec
  def merge[T](acc: Seq[T], xs: Seq[T], ys: Seq[T])(using ord: Ordering[T]): Seq[T] =
    if xs.isEmpty then acc ++ ys
    else if ys.isEmpty then acc ++ xs
    else
      val x = xs.head
      val y = ys.head
      if ord.lt(x, y) then
        merge(acc :+ x, xs.tail, ys)
      else
        merge(acc :+ y, xs, ys.tail)

  def msort[T](xs: Seq[T])(using ord: Ordering[T]): Seq[T] =
    val n = xs.length / 2
    if n == 0 then xs
    else
      val (fst, snd) = xs.splitAt(n)
      if n < cutoff then
        merge(Vector(), msort(fst), msort(snd))
      else
        val (s1, s2) = parallel(() => msort(fst), () => msort(snd))
        merge(Vector(), s1, s2)
end SeqMergeSort
