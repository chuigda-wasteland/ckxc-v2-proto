package cn.ckxily.ckxc.main

import cn.ckxily.ckxc.ast.decl.*
import cn.ckxily.ckxc.ast.type.ClassType
import cn.ckxily.ckxc.ast.type.EnumType
import cn.ckxily.ckxc.codegen.ASTPrinter

fun Array<String>.main() {
	val transUnit = TransUnitDecl()

	run {
		val class1 = ClassDecl("Ice1kClass", transUnit)
		run {
			val class2 = ClassDecl("Light1kClass", class1)
			val enum1 = EnumDecl("JoinedGroupChat", class1)
			run {
				val enumerator1 = EnumeratorDecl("Compiler1k", 0, enum1)
				val enumerator2 = EnumeratorDecl("Suspended1k", 1, enum1)
				with(enum1) {
					pushDecl(enumerator1)
					pushDecl(enumerator2)
				}
				null
			}
			val var1 = VarDecl("light1k", ClassType(class2), class1)
			val var2 = VarDecl("activeGroup", EnumType(enum1), class1)

			with(class1) {
				pushDecl(class2)
				pushDecl(enum1)
				pushDecl(var1)
				pushDecl(var2)
			}
			null
		}
		val var3 = VarDecl("ice1k", ClassType(class1), transUnit)

		with(transUnit) {
			pushDecl(class1)
			pushDecl(var3)
		}
		null
	}

	val astConsumer = ASTPrinter()
	transUnit.accept(astConsumer)
}
