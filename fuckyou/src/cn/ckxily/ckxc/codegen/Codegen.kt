package cn.ckxily.ckxc.codegen

import cn.ckxily.ckxc.ast.decl.*
import cn.ckxily.ckxc.ast.expr.*
import cn.ckxily.ckxc.ast.stmt.*
import cn.ckxily.ckxc.util.addressOf

interface ASTConsumer {
	fun visitTransUnitDecl(transUnitDecl: TransUnitDecl): Any?
	fun visitVarDecl(varDecl: VarDecl): Any?
	fun visitEnumDecl(enumDecl: EnumDecl): Any?
	fun visitEnumeratorDecl(enumeratorDecl: EnumeratorDecl): Any?
	fun visitClassDecl(classDecl: ClassDecl): Any?
	fun visitFuncDecl(funcDecl: FuncDecl): Any?
	fun visitCompoundStmt(compoundStmt: CompoundStmt): Any?
	fun visitDeclStmt(declStmt: DeclStmt): Any?
	fun visitExprStmt(exprStmt: ExprStmt): Any?
	fun visitDeclRefExpr(declRefExpr: DeclRefExpr): Any?
}

class BetterASTPrinter(private var indentation: Int = 0) : ASTConsumer {
	private fun indent() {
		var i = 0;
		while (i < indentation) { print("  "); ++i }
	}

	override fun visitTransUnitDecl(transUnitDecl: TransUnitDecl): Any? {
		println("TranslationUnitDecl")
		indentation++
		for (topLevelDecl in transUnitDecl.decls) topLevelDecl.accept(this)
		indentation--
		return null
	}

	override fun visitVarDecl(varDecl: VarDecl): Any? {
		indent(); println("VarDecl ${varDecl.nameStr} ${addressOf(varDecl)} of type ${varDecl.type}")
		return null
	}

	override fun visitEnumDecl(enumDecl: EnumDecl): Any? {
		indent(); println("EnumDecl ${enumDecl.nameStr} ${addressOf(enumDecl)}")
		indentation++
		for (subDecl in enumDecl.decls) subDecl.accept(this)
		indentation--
		return null
	}

	override fun visitEnumeratorDecl(enumeratorDecl: EnumeratorDecl): Any? {
		indent(); println("${enumeratorDecl.nameStr} ${addressOf(enumeratorDecl)}= ${enumeratorDecl.init}")
		return null
	}

	override fun visitClassDecl(classDecl: ClassDecl): Any? {
		indent(); println("ClassDecl ${classDecl.nameStr} ${addressOf(classDecl)}")
		indentation++
		for (subDecl in classDecl.decls) subDecl.accept(this)
		indentation--
		return null
	}

	override fun visitFuncDecl(funcDecl: FuncDecl): Any? {
		indent(); println("FuncDecl ${funcDecl.nameStr} ${addressOf(funcDecl)}")
		indentation++
		for (paramDecl in funcDecl.paramList) paramDecl.accept(this)
		indentation--
		indent(); println("ReturnType ${funcDecl.retType}")
		if (funcDecl.funcBody != null) {
			indent(); println("FunctionBody")
			indentation++
			funcDecl.funcBody!!.accept(this)
			indentation--
		}
		return null
	}

	override fun visitCompoundStmt(compoundStmt: CompoundStmt): Any? {
		indent(); println("CompoundStmt")
		indentation++
		for (stmt in compoundStmt.stmtList) stmt.accept(this)
		indentation--
		return null
	}

	override fun visitDeclStmt(declStmt: DeclStmt): Any? {
		indent(); println("DeclStmt")
		indentation++
		declStmt.decl.accept(this)
		indentation--
		return null
	}

	override fun visitExprStmt(exprStmt: ExprStmt): Any? {
		indent(); println("ExprStmt")
		indentation++
		exprStmt.expr.accept(this)
		indentation--
		return null
	}

	override fun visitDeclRefExpr(declRefExpr: DeclRefExpr): Any? {
		indent(); println("DeclRefExpr referencing ${declRefExpr.decl.nameStr} ${addressOf(declRefExpr.decl)}")
		return null
	}
}
