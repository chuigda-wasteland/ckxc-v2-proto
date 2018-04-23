package cn.ckxily.ckxc.ast.decl

import cn.ckxily.ckxc.ast.type.Type
import cn.ckxily.ckxc.codegen.ASTConsumer

enum class DeclKind(val str: String) {
	TransUnitDecl("Translation Unit"),
	VarDecl("Variable Declaration"),
	EnumDecl("Enumeration Declaration"),
	EnumeratorDecl("Enumerator"),
	ClassDecl("Class Declaration"),
	FieldDecl("Field")
}

fun DeclKind.getDescription() = str

enum class DeclContextKind(val str: String) {
	TransUnitContext("Top Level Of Translation Unit"),
	EnumContext("Inside Enumeration"),
	ClassContext("Inside Class")
}

fun DeclContextKind.getDescription() = str

abstract class Decl(val declKind: DeclKind, val withinContext: DeclContext?) {
	abstract fun getNameStr(): String?
	abstract fun accept(astConsumer: ASTConsumer): Any?
}

abstract class DeclContext(val declContextKind: DeclContextKind, declKind: DeclKind, withinContext: DeclContext?)
	: Decl(declKind, withinContext) {
	var decls: MutableList<Decl> = ArrayList()

	fun pushDecl(decl: Decl) = decls.add(decl)
	fun lookupDecl(name: String) = decls.filter { decl -> decl.getNameStr() == name }
}

class TransUnitDecl : DeclContext(DeclContextKind.TransUnitContext, DeclKind.TransUnitDecl, null) {
	override fun getNameStr(): String? = null
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitTransUnitDecl(this)
}

open class VarDecl(var name: String, var type: Type, withinContext: DeclContext)
	: Decl(DeclKind.VarDecl, withinContext) {
	override fun getNameStr(): String? = name
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitVarDecl(this)
}

class FieldDecl(name: String, type: Type, withinContext: DeclContext) : VarDecl(name, type, withinContext) {
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitFieldDecl(this)
}

class EnumeratorDecl(var name: String, var init: Int, withinContext: DeclContext)
	: Decl(DeclKind.EnumDecl, withinContext) {
	override fun getNameStr(): String? = name
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitEnumeratorDecl(this)
}

class ClassDecl(val name: String, withinContext: DeclContext)
	: DeclContext(DeclContextKind.ClassContext, DeclKind.ClassDecl, withinContext) {
	override fun getNameStr(): String? = name
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitClassDecl(this)
}

class EnumDecl(val name: String, withinContext: DeclContext)
	: DeclContext(DeclContextKind.EnumContext, DeclKind.EnumDecl, withinContext) {
	override fun getNameStr(): String? = name
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitEnumDecl(this)
}
