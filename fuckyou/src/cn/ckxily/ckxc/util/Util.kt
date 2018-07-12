package cn.ckxily.ckxc.util

enum class EitherStatus {
	ContainsT,
	ContainsU
}

class Either<T, U>(var value: Any?, var status: EitherStatus)