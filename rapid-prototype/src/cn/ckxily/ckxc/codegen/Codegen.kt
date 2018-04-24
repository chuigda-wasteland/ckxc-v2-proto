package cn.ckxily.ckxc.codegen

import cn.ckxily.ckxc.ast.decl.*
import cn.ckxily.ckxc.ast.type.*

interface ASTConsumer {
	fun visitTransUnitDecl(transUnitDecl: TransUnitDecl): Any?
	fun visitVarDecl(varDecl: VarDecl): Any?
	fun visitEnumDecl(enumDecl: EnumDecl): Any?
	fun visitEnumeratorDecl(enumeratorDecl: EnumeratorDecl): Any?
	fun visitClassDecl(classDecl: ClassDecl): Any?
	fun visitFieldDecl(fieldDecl: FieldDecl): Any?
}

fun typeToString(type: Type) = when (type) {
	is BuiltinType -> type.builinTypeId.str
	is ClassType -> type.decl.nameStr
	is EnumType -> type.decl.nameStr
}

class ASTPrinter(var indentation: Int = 0) : ASTConsumer {
	fun indent() {
		var i = 0; while (i < indentation * 3) { print(" "); ++i }
	}

	override fun visitTransUnitDecl(transUnitDecl: TransUnitDecl): Any? {
		println("Translation unit start!")
		indentation++
		for (topLevelDecl in transUnitDecl.decls) topLevelDecl.accept(this)
		indentation--
		println("Translation unit end!")
		return null
	}

	override fun visitVarDecl(varDecl: VarDecl): Any? {
		indent(); println("Variable declaration begin!")
		indentation++
		indent(); println("${varDecl.nameStr} of type ${typeToString(varDecl.type)}")
		indentation--
		indent(); println("Variable declaration end!")
		return null
	}

	override fun visitEnumDecl(enumDecl: EnumDecl): Any? {
		indent(); println("Enum declaration begin!")
		indent(); println("enum ${enumDecl.nameStr}")
		indentation++
		for (subDecl in enumDecl.decls) subDecl.accept(this)
		indentation--
		indent(); println("Enum declaration end!")
		return null
	}

	override fun visitEnumeratorDecl(enumeratorDecl: EnumeratorDecl): Any? {
		indent(); println("${enumeratorDecl.nameStr} = ${enumeratorDecl.init}")
		return null
	}

	override fun visitClassDecl(classDecl: ClassDecl): Any? {
		indent(); println("Class declaration begin!")
		indent(); println("class ${classDecl.nameStr}")
		indentation++
		for (subDecl in classDecl.decls) subDecl.accept(this)
		indentation--
		indent(); println("Class declaration end!")
		return null
	}

	override fun visitFieldDecl(fieldDecl: FieldDecl): Any? {
		visitVarDecl(fieldDecl)
		return null
	}
}
