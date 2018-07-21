package cn.ckxily.ckxc.parser.test

import cn.ckxily.ckxc.codegen.BetterASTPrinter
import cn.ckxily.ckxc.lex.Lexer
import cn.ckxily.ckxc.parser.Parser

fun Array<String>.main() {
	val lexer = Lexer()
	val parser = Parser()

	parser.parse(lexer.lex("""
		/*
			This is a block comment
		*/
		class ClassA {
			ClassA *pA;
			class ClassB {
				ClassB /* Data */ *pB;
				ClassA &rA;
			}
		}

		let ClassA::ClassB someFuck;

		func test() {
			someFuck;
			{
				let ClassA::ClassB *someFuck;
				someFuck;
				42;
				42;
			}
			someFuck;
			// someFuck
		}
	""".trimIndent())).accept(BetterASTPrinter())
}
