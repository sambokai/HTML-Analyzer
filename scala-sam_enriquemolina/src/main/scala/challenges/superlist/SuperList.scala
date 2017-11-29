package challenges.superlist

class SuperList(val list: List[Int]) {
  import SuperList._

  require(list != null)

  def this(args: Int*) {
    this(args.toList)
  }

  lazy val lastThree: List[Int] = list.slice(list.length - 3, list.length)

  def power(n: Int): List[Double] = list.map(math.pow(_, n))

  lazy val primes: List[Int] = list.filter(isPrime(_))

  def partition(size: Int): List[List[Int]] = list.grouped(size).toList

  lazy val superStar: Map[Int, List[Char]] = list.map(a => a -> List.fill(a)(Star)).toMap
}

object SuperList {
  val Star = '*'

  def apply(list: List[Int]) = new SuperList(list)

  def apply(args: Int*) = new SuperList(args.toList)

  private[superlist] def isPrime(n: Int) = {
    if (n <= 1)
      false
    else if (n == 2)
      true
    else
      !(2 until n).exists(x => n % x == 0)
  }
}