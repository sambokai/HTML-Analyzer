# SuperList

This class is going to perform some custom operations on objects of type Scala _List[Int]_.

It will be a wrapper class for instances of type _List[Int]_.

## Instantiation
Instances of type _SuperList_ should be created as follow:

```
val normalList = List(1, 2, 3)
val superList = new SuperList(normalList)
```

You should be able to instance the class as follows as well:

```
val superList = new SuperList(1, 2, 3)
```

and

```
val normalList = List(1, 2, 3)
val superList = SuperList(normalList)
```

and

```
val superList = SuperList(1, 2, 3)
```

__hint__: Companion objects

## Operations
The _SuperList_ should provide the following operations:
### lastThree
Returns a list containing the last three elements of the superlist. If the list has less than three elements then it should return those elements.

```
val superList = SuperList(1, 2, 3, 4)

println(superList.lastThree) // List(2, 3, 4)
```

### power(n: Int)
Returns a list but with the elements of the superlist powered to _n_.

```
val superList = SuperList(1, 2, 3, 4)

println(superList.power(2) // List(1, 4, 9, 16)
```

### primes

Returns a list containing the prime numbers of the superlist.

```
val superList = SuperList(1, 2, 3, 4, 7)

println(superList.primes // List(2, 3, 7)
```

### partitions(n: Int)
Returns a list with the elements of the superlist splitted in list of size _n_

```
val superList = SuperList(1, 2, 3, 4, 5)

println(superList.partitions(2) // List(List(1, 2), List(3, 4), List(5))
```

### superStar
Returns a _Map_ whose keys are the elements of the list and the values are list of stars of size equals to the key.

```
val superList = SuperList(1, 2, 3, 4, 5)

println(superList.superStar // Map(5 -> List(*, *, *, *, *), 1 -> List(*), 2 -> List(*, *), 3 -> List(*, *, *), 4 -> List(*, *, *, *))
```

__hint__: maps are not sequences therefore the order of the elements when printing out its content is not relevant

#Important
* Google is your best friend.
* Implement tests (ScalaTest) for everything described here, including corner cases, exceptions, etc...
* DO NOT try to implement something if you haven't been able to implement the previous step.
* Lets pair every time you implement something (with the corresponding tests) to get proper feedbacks.
* Do not get frustrated for more than an hour. Let's pair if you feel stuck.