package cn.ckxily.ckxc.parser.test

import cn.ckxily.ckxc.codegen.BetterASTPrinter
import cn.ckxily.ckxc.lex.Lexer
import cn.ckxily.ckxc.parser.Parser

fun Array<String>.main() {
	val lexer = Lexer()
	val parser = Parser()

	parser.parse(lexer.lex("""
		func test() {
			let vi32 a;
			let vi32 b;
			let vi32 c;
			c = a + b;
		}
	""".trimIndent())).accept(BetterASTPrinter())
}
