package challenges.superlist

import org.scalatest.Matchers._
import org.scalatest._

class SuperListSpec extends WordSpec {
  "A SuperList" should {
    "on instantiation" should {
      "wrap a non empty list on instantiation" in {
        // given
        val list = List(1, 2, 3)

        // when
        val result = new SuperList(list)

        // then
        result.list shouldBe list
      }

      "be able to wrap an empty list" in {
        // given
        val list = List.empty

        // when
        val result = new SuperList(list)

        // then
        result.list shouldBe list
      }

      "throw an exception if passing a null value" in {
        intercept[IllegalArgumentException] {
          new SuperList(null)
        }
      }

      "wrap int parameters" in {
        //then
        new SuperList(1, 2, 3).list shouldBe List(1, 2, 3)
      }
    }

    "provide a factory method" should {
      "work with list of integers" in {
        val list = List(1, 2, 3)
        val result = SuperList(list)
        result.list shouldBe list
      }

      "work with sequence of integers" in {
        val result = SuperList(1, 2, 3)
        result.list shouldBe List(1, 2, 3)
      }
    }

    "when invoking lastThree()" should {

      "return the last three elements" in {
        // given
        val list = SuperList(List(1, 2, 3, 4, 5))

        //when
        val result = list.lastThree

        //then
        result shouldBe List(3, 4, 5)
      }

      "return all elements when the SuperList has less than three elements" in {
        val result = SuperList(List(1, 2))
        result.lastThree shouldBe List(1, 2)
      }

      "return an empty list when the SuperList has no elements" in {
        val result = SuperList(List.empty)
        result.lastThree shouldBe List.empty
      }
    }

    "when invoking power(n: Int)" should {
      "return a list with all elements of the superlist powered to n" in {
        val powers = Seq(2, 58, 4647, 97, 1000000)
        val superlist = SuperList(2, 4, 8)

        powers.foreach { p =>
          val result = superlist.power(p)
          val expected = superlist.list.map(math.pow(_, p))

          result shouldBe expected
        }
      }
    }

    "when invoking method isPrime(n: Int)" should {
      "return true if n is prime" in {
        val primes = Seq(2, 37, 41, 59, 127)

        primes.foreach { p =>
          val result = SuperList.isPrime(p)

          result shouldBe (true)
        }
      }

      "return false if n is not prime" in {
        val primes = Seq(4, 60, 55, 1000, 1255)

        primes.foreach { p =>
          val result = SuperList.isPrime(p)

          result shouldBe (false)
        }
      }
    }

    "when invoking primes()" should {

      "return a list, with only the elements of a superlist that are prime numbers" in {
        val result = SuperList(List(1, 2, 3, 4, 5)).primes

        result shouldBe List(2, 3, 5)
      }

      "return an empty list on an empty superlist" in {
        SuperList(List()).primes shouldBe List.empty
      }
    }

    "when invoking partition(size: Int)" should {
      "return superlist, splitted in lists of predefined size" in {
        val list = List(1, 2, 3, 4)
        val result = SuperList(list)

        result.partition(2) shouldBe List(List(1, 2), List(3, 4))
      }

      "return empty list on an empty superlist" in {
        SuperList(List()).partition(2) shouldBe List.empty
      }

      "return smaller smaller last partition, if the number of elements in superlist is not divisible by 'size'-parameter" in {
        val list = List(1, 2, 3)
        val result = SuperList(list)

        result.partition(2) shouldBe List(List(1, 2), List(3))
      }
    }

    "when invoking superStar()" should {
      "return a map, whose keys are the elements of the superlist and values are key-times '*'" in {
        val result = SuperList(2, 5)
        result.superStar shouldBe Map(2 -> List('*', '*'), 5 -> List('*', '*', '*', '*', '*'))
      }

      "only return one (k,v) pair if there is more than one occurence of an element in the superlist" in {
        val result = SuperList(2, 2)
        result.superStar shouldBe Map(2 -> List('*', '*'))
      }
    }
  }
}
