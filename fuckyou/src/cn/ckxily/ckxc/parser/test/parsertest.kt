package cn.ckxily.ckxc.parser.test

import cn.ckxily.ckxc.codegen.BetterASTPrinter
import cn.ckxily.ckxc.lex.Lexer
import cn.ckxily.ckxc.parser.Parser

fun Array<String>.main() {
	val lexer = Lexer()
	val parser = Parser()

	parser.parse(lexer.lex("""
		let vi8 a;
		let vi8 const** b;
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

		let ClassC wtf;

		func FuncFuck(vi8 a, vi16 const* const* const b);
		func FuckFunc(vi8 a, vi16* const p) {
			let vi8 b;
			let ClassC wtf;
			let ClassC::EnumE wtf2;
		}
	""".trimIndent())).accept(BetterASTPrinter())
}
