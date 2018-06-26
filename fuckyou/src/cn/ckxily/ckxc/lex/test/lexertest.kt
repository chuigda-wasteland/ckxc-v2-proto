package cn.ckxily.ckxc.lex.test

import cn.ckxily.ckxc.lex.Lexer

fun Array<String>.main() {
	val lexer = Lexer()
	val tokens = lexer.lex("""
		class SomeClass {}

		fn main(vi32 argc, vi8** argv): vi32 {
			vi8 a, b;
			if (argc != 3) {
				return -1;
			}
			a = parseInt(argc[1]);
			b = parseInt(argc[2]);
			print(a + b);
		}
	""".trimIndent())
	for (token in tokens) {
		println(token.toString())
	}
}
