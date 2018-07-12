package cn.ckxily.ckxc.util

sealed class Either<out T, out U> {
	fun asLeft() = (this as Left<T>).obj

	fun asRight() = (this as Right<U>).obj

	fun asLeftSafe() = (this as? Left<T>)?.obj

	fun asRightSafe() = (this as? Right<U>)?.obj
}

class Left<T>(var obj: T) : Either<T, Nothing>()

class Right<U>(var obj: U) : Either<Nothing, U>()