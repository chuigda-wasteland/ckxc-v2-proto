package cn.ckxily.ckxc.codegen

import cn.ckxily.ckxc.ast.decl.*
import cn.ckxily.ckxc.ast.stmt.CompoundStmt
import cn.ckxily.ckxc.ast.stmt.DeclStmt

interface ASTConsumer {
	fun visitTransUnitDecl(transUnitDecl: TransUnitDecl): Any?
	fun visitVarDecl(varDecl: VarDecl): Any?
	fun visitEnumDecl(enumDecl: EnumDecl): Any?
	fun visitEnumeratorDecl(enumeratorDecl: EnumeratorDecl): Any?
	fun visitClassDecl(classDecl: ClassDecl): Any?
	fun visitFuncDecl(funcDecl: FuncDecl): Any?
	fun visitCompoundStmt(compoundStmt: CompoundStmt): Any?
	fun visitDeclStmt(declStmt: DeclStmt): Any?
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
		indent(); println("${varDecl.nameStr}(${varDecl.hashCode()}) of type ${varDecl.type}")
		indentation--
		indent(); println("Variable declaration end!")
		return null
	}

	override fun visitEnumDecl(enumDecl: EnumDecl): Any? {
		indent(); println("Enum declaration begin!")
		indent(); println("enum ${enumDecl.nameStr}(${enumDecl.hashCode()})")
		indentation++
		for (subDecl in enumDecl.decls) subDecl.accept(this)
		indentation--
		indent(); println("Enum declaration end!")
		return null
	}

	override fun visitEnumeratorDecl(enumeratorDecl: EnumeratorDecl): Any? {
		indent(); println("${enumeratorDecl.nameStr}(${enumeratorDecl.hashCode()}) = ${enumeratorDecl.init}")
		return null
	}

	override fun visitClassDecl(classDecl: ClassDecl): Any? {
		indent(); println("Class declaration begin!")
		indent(); println("class ${classDecl.nameStr}(${classDecl.hashCode()})")
		indentation++
		for (subDecl in classDecl.decls) subDecl.accept(this)
		indentation--
		indent(); println("Class declaration end!")
		return null
	}

	override fun visitFuncDecl(funcDecl: FuncDecl): Any? {
		indent(); println("Function declaration begin!")
		indent(); println("function ${funcDecl.nameStr}(${funcDecl.hashCode()})")
		indentation++
		for (paramDecl in funcDecl.paramList) paramDecl.accept(this)
		indentation--
		indent(); println("Return type is ${funcDecl.retType}")
		if (funcDecl.funcBody != null) {
			indent(); println("Function body!")
			indentation++
			funcDecl.funcBody!!.accept(this)
			indentation--
		}
		indent(); println("Function declaration end!")
		return null
	}

	override fun visitCompoundStmt(compoundStmt: CompoundStmt): Any? {
		indent(); println("Compound statement begin!")
		indentation++
		for (stmt in compoundStmt.stmtList) stmt.accept(this)
		indentation--
		indent(); println("Compound statement end!")
		return null
	}

	override fun visitDeclStmt(declStmt: DeclStmt): Any? {
		indent(); println("Declaration statement begin!")
		indentation++
		declStmt.decl.accept(this)
		indentation--
		indent(); println("Declaration statement end!")
		return null
	}
}
