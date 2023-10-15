package bench

package bench.Merge

class MergeSortTest extends munit.FunSuite:
  def noPar(a: => Unit, b: => Unit) =
    a
    b

  test("InPlaceMergeSort.msort [3, 2, 1]"):
    val input: IArray[Int] = IArray(3, 2, 1)
    val output: IArray[Int] = InPlaceMergeSort.msort(input, noPar)
    assertIArrayEquals(output, IArray(1, 2, 3))

  test("InPlaceMergeSort.msort [3, 2, 1, 4]"):
    val input: IArray[Int] = IArray(3, 2, 1, 4)
    val output: IArray[Int] = InPlaceMergeSort.msort(input, noPar)
    assertIArrayEquals(output, IArray(1, 2, 3, 4))

  test("InPlaceMergeSort.msort [3, 2, 7, 5, 1, 4, 6, 9, 8]"):
    val input: IArray[Int] = IArray(3, 2, 7, 5, 1, 4, 6, 9, 8)
    val output: IArray[Int] = InPlaceMergeSort.msort(input, noPar)
    assertIArrayEquals(output, IArray(1, 2, 3, 4, 5, 6, 7, 8, 9))

  test("InPlaceMergeSort.merge [3] ++ [1, 2]"):
    val a1 = Array(3, 1, 2)
    val a2 = Array(0, 0, 0)
    InPlaceMergeSort.merge(a1, 0, 1, 1, 3, a2, 0)
    assertArrayEquals(a2, Array(1, 2, 3))

  test("InPlaceMergeSort.merge [] ++ [1, 2]"):
    val a1 = Array(3, 1, 2)
    val a2 = Array(0, 0, 0)
    InPlaceMergeSort.merge(a1, 1, 1, 1, 3, a2, 1)
    assertArrayEquals(a2, Array(0, 1, 2))

  test("InPlaceMergeSort.merge [3,5] ++ [2, 4, 6]"):
    val a1 = Array(3, 5, 2, 4, 6)
    val a2 = Array(0, 0, 0, 0, 0)
    InPlaceMergeSort.merge(a1, 0, 2, 2, 5, a2, 0)
    assertArrayEquals(a2, Array(2, 3, 4, 5, 6))

  def assertIArrayEquals[T](actual: IArray[T], expected: IArray[T]) =
    assertEquals(actual.toList, expected.toList)

  def assertArrayEquals[T](actual: Array[T], expected: Array[T]) =
    assertEquals(actual.toList, expected.toList)
