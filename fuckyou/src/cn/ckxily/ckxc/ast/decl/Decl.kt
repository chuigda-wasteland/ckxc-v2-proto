package cn.ckxily.ckxc.ast.decl

import cn.ckxily.ckxc.ast.type.Type
import cn.ckxily.ckxc.ast.stmt.Stmt
import cn.ckxily.ckxc.ast.stmt.CompoundStmt
import cn.ckxily.ckxc.codegen.ASTConsumer

enum class DeclKind(val str: String) {
	TransUnitDecl("Translation Unit"),
	VarDecl("Variable Declaration"),
	FuncDecl("Function Declaration"),
	EnumDecl("Enumeration Declaration"),
	EnumeratorDecl("Enumerator"),
	ClassDecl("Class Declaration"),
	FieldDecl("Field")
}

val DeclKind.description get() = str

enum class DeclContextKind(val str: String) {
	TransUnitContext("Top Level Of Translation Unit"),
	EnumContext("Inside Enumeration"),
	ClassContext("Inside Class")
}

val DeclContextKind.description get() = str

abstract class Decl(val declKind: DeclKind) {
	abstract val nameStr: String?
	abstract fun accept(astConsumer: ASTConsumer): Any?
}

abstract class DeclContext(val declContextKind: DeclContextKind, declKind: DeclKind) : Decl(declKind) {
	var decls: MutableList<Decl> = ArrayList()
	fun addDecl(decl: Decl) = decls.add(decl)
	fun removeDecl(decl: Decl) = decls.remove(decl)
	fun lookupLocalDecl(name: String) = decls.filter { decl -> decl.nameStr == name }
}

class TransUnitDecl : DeclContext(DeclContextKind.TransUnitContext, DeclKind.TransUnitDecl) {
	override val nameStr: String? get() = null
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitTransUnitDecl(this)
}

open class VarDecl(override var nameStr: String, var type: Type)
	: Decl(DeclKind.VarDecl) {
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitVarDecl(this)
}

class EnumeratorDecl(override var nameStr: String, var init: Int)
	: Decl(DeclKind.EnumDecl) {
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitEnumeratorDecl(this)
}

class ClassDecl(override val nameStr: String)
	: DeclContext(DeclContextKind.ClassContext, DeclKind.ClassDecl) {
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitClassDecl(this)
}

class EnumDecl(override val nameStr: String)
	: DeclContext(DeclContextKind.EnumContext, DeclKind.EnumDecl) {
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitEnumDecl(this)
}

class FuncDecl(override val nameStr: String, val paramList: MutableList<VarDecl>, val retType: Type,
							 var funcBody: CompoundStmt? = null)
	: Decl(DeclKind.FuncDecl) {
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitFuncDecl(this)
}
