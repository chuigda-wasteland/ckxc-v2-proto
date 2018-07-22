package cn.ckxily.ckxc.parser.test

import cn.ckxily.ckxc.codegen.BetterASTPrinter
import cn.ckxily.ckxc.lex.Lexer
import cn.ckxily.ckxc.parser.Parser

fun Array<String>.main() {
	val lexer = Lexer()
	val parser = Parser()

	parser.parse(lexer.lex("""
		// TODO We cannot merge two declarations till now
		// func test();

		func test() {
			let vi32 a = 1;
			let vi32 b = 2;
			let vi32 c = 3;
			let vi16 d = a;
			let vi8 e;
			let bool result;
			c = a + b * 42 + d;
			result = c > e;
			let vi32 n=b=c=d=15;
		}
	""".trimIndent())).accept(BetterASTPrinter())
}
