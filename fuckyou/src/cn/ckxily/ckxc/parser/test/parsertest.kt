package cn.ckxily.ckxc.parser.test

import cn.ckxily.ckxc.codegen.ASTPrinter
import cn.ckxily.ckxc.lex.Lexer
import cn.ckxily.ckxc.parser.Parser

fun Array<String>.main() {
	val lexer = Lexer()
	val parser = Parser()

	parser.parse(lexer.lex("""
		vi8 a;
		vi8 const** b;
		class ClassC {
			enum EnumE {
				Enumerator1 = 3,
				Enumerator2 = 5
			}
			class NestedClassD {
				vi8 volatile* const* const volatile e;
			}
			vr32 f;
		}

		ClassC wtf;

		func FuncFuck(vi8 a, vi16 const* const* const b);
		func FuckFunc(vi8 a, vi16* const p) {
			vi8 b;
			ClassC wtf;
		}
	""".trimIndent())).accept(ASTPrinter())
}
