package cn.ckxily.ckxc.parser.test

import cn.ckxily.ckxc.codegen.ASTPrinter
import cn.ckxily.ckxc.lex.Lexer
import cn.ckxily.ckxc.parser.Parser

fun Array<String>.main() {
	val lexer = Lexer()
	val parser = Parser()

	parser.parse(lexer.lex("""
		vi8 a;
		vi8 const** c;
		class DClass {}
	""".trimIndent())).accept(ASTPrinter())
}

