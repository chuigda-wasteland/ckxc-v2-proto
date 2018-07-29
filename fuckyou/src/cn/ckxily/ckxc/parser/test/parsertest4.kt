package cn.ckxily.ckxc.parser.test

import cn.ckxily.ckxc.codegen.BetterASTPrinter
import cn.ckxily.ckxc.lex.Lexer
import cn.ckxily.ckxc.parser.Parser

fun Array<String>.main() {
	val lexer = Lexer()
	val parser = Parser()

	parser.parse(lexer.lex("""
		func add(vi8 a, vi8 b): vi8;
		func add(vi32 a, vi32 b): vi32;

		func test() {
			let vi32 a = 1;
			let vi32 b = 2;
			let vi8 c = 1;
			let vi8 d = 2;

			let vi8 e = add(c, d);
			let vi32 f = add(a, b);
		}
	""".trimIndent())).accept(BetterASTPrinter())
}
