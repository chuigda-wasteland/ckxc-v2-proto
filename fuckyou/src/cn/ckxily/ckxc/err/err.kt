package cn.ckxily.ckxc.err

import java.lang.System.exit

fun error(desc: String) {
	println("Error: ${desc}")
	exit(1)
}

fun assertionFailed(desc: String) {
	println("Assertion failed: ${desc}")
	exit(-1)
}
