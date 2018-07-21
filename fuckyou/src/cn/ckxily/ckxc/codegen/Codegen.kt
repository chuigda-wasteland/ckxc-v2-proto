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
	fun visitIntegralLiteralExpr(integralLiteralExpr: IntegralLiteralExpr): Any?
	fun visitBinaryExpr(binaryExpr: BinaryExpr): Any?
	fun visitImplicitDecay(expr: ImplicitDecayExpr): Any?
	fun visitImplicitCastExpr(expr: ImplicitCastExpr): Any?
}

class BetterASTPrinter(private var indentation: Int = 0) : ASTConsumer {
	private fun indent() = print("  ".repeat(indentation))
	private inline fun withIndent(func: () -> Unit) {
		indentation++
		func()
		indentation--
	}

	override fun visitTransUnitDecl(transUnitDecl: TransUnitDecl): Any? {
		println("TranslationUnitDecl")
		withIndent { for (topLevelDecl in transUnitDecl.decls) topLevelDecl.accept(this) }
		return null
	}

	override fun visitVarDecl(varDecl: VarDecl): Any? {
		indent(); println("VarDecl ${varDecl.nameStr} ${addressOf(varDecl)} of type ${varDecl.type}")
		return null
	}

	override fun visitEnumDecl(enumDecl: EnumDecl): Any? {
		indent(); println("EnumDecl ${enumDecl.nameStr} ${addressOf(enumDecl)}")
		withIndent { for (subDecl in enumDecl.decls) subDecl.accept(this) }
		return null
	}

	override fun visitEnumeratorDecl(enumeratorDecl: EnumeratorDecl): Any? {
		indent(); println("${enumeratorDecl.nameStr} ${addressOf(enumeratorDecl)}= ${enumeratorDecl.init}")
		return null
	}

	override fun visitClassDecl(classDecl: ClassDecl): Any? {
		indent(); println("ClassDecl ${classDecl.nameStr} ${addressOf(classDecl)}")
		withIndent { for (subDecl in classDecl.decls) subDecl.accept(this) }
		return null
	}

	override fun visitFuncDecl(funcDecl: FuncDecl): Any? {
		indent(); println("FuncDecl ${funcDecl.nameStr} ${addressOf(funcDecl)}")
		withIndent { for (paramDecl in funcDecl.paramList) paramDecl.accept(this) }
		indent(); println("ReturnType ${funcDecl.retType}")
		if (funcDecl.funcBody != null) {
			indent(); println("FunctionBody")
			withIndent { funcDecl.funcBody!!.accept(this) }
		}
		return null
	}

	override fun visitCompoundStmt(compoundStmt: CompoundStmt): Any? {
		indent(); println("CompoundStmt")
		withIndent { for (stmt in compoundStmt.stmtList) stmt.accept(this) }
		return null
	}

	override fun visitDeclStmt(declStmt: DeclStmt): Any? {
		indent(); println("DeclStmt")
		withIndent { declStmt.decl.accept(this) }
		return null
	}

	override fun visitExprStmt(exprStmt: ExprStmt): Any? {
		indent(); println("ExprStmt")
		withIndent { exprStmt.expr.accept(this) }
		return null
	}

	override fun visitDeclRefExpr(declRefExpr: DeclRefExpr): Any? {
		indent(); println("DeclRefExpr referencing ${declRefExpr.decl.nameStr} ${addressOf(declRefExpr.decl)}")
		return null
	}

	override fun visitIntegralLiteralExpr(integralLiteralExpr: IntegralLiteralExpr): Any? {
		indent(); println("IntegralLiteralExpr ${integralLiteralExpr.value} of type ${integralLiteralExpr.type}")
		return null
	}

	override fun visitBinaryExpr(binaryExpr: BinaryExpr): Any? {
		indent(); println("BinaryExpr ${binaryExpr.opCode}")
		withIndent {
			binaryExpr.lhs.accept(this)
			binaryExpr.rhs.accept(this)
		}
		return null
	}

	override fun visitImplicitDecay(expr: ImplicitDecayExpr): Any? {
		indent(); println("LValueToRValueDecay")
		withIndent { expr.expr.accept(this) }
		return null
	}

	override fun visitImplicitCastExpr(expr: ImplicitCastExpr): Any? {
		indent()
		when (expr.castOp) {
			CastOperation.AddConst -> println("ImplicitQualifyConst")
			CastOperation.AddVolatile -> println("ImplicitQualifyVolatile")
			CastOperation.IntegerWidenCast -> {
				println("ImplicitIntegerWiden from ${expr.expr.type} to ${expr.destType}")
				withIndent { expr.expr.accept(this) }
			}
			CastOperation.FloatingWidenCast -> {
				println("ImplicitFloatingWiden from ${expr.expr.type} to ${expr.destType}")
				withIndent { expr.expr.accept(this) }
			}
		}
		return null
	}
}
