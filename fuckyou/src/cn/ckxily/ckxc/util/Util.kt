package cn.ckxily.ckxc.util

sealed class Either<out T, out U> {
	// `is` is not provided since `when` is more convenient, see main function
	fun asLeft() = (this as Left<T>).obj
	fun asRight() = (this as Right<U>).obj

	// returns nullable objects, like dynamic_cast
	fun asLeftSafe() = (this as? Left<T>)?.obj
	fun asRightSafe() = (this as? Right<U>)?.obj
}

// TODO change var to val if you don't need mutating obj
class Left<T>(var obj: T) : Either<T, Nothing>()
class Right<U>(var obj: U) : Either<Nothing, U>()

fun main(args: Array<String>) {
	fun useEither(either: Either<String, Int>) {
		when (either) {
			// here `either.obj` is String, and you can convert it to a char list
			is Left -> println(either.obj.toCharArray().toList())
			// here `either.obj` is Int, and you can plus 1 on it
			is Right -> println(either.obj + 1)
		}
		var left: String = either.asLeft() // cast and extract
		var right: Int = either.asRight() // cast and extract
	}
	useEither(Left("Hi!"))
	// useEither(Left(1)) // type error, can't compile
	useEither(Right(233))
	// useEither(Right("233")) // type error, can't compile
}
