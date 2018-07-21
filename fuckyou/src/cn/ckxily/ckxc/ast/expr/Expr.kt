package cn.ckxily.ckxc.ast.expr

import cn.ckxily.ckxc.codegen.ASTConsumer
import cn.ckxily.ckxc.ast.decl.Decl
import cn.ckxily.ckxc.ast.decl.VarDecl
import cn.ckxily.ckxc.ast.type.BuiltinType
import cn.ckxily.ckxc.ast.type.BuiltinTypeId
import cn.ckxily.ckxc.ast.type.Type
import cn.ckxily.ckxc.ast.type.getNoSpecifier

enum class ExprId(val desc: String) {
	DeclRefExpr("Declaration reference expression"),
	MemberAccessExpr("Member access expression"),
	IntegralLiteralExpr("Integral literal"),
	FloatingLiteralExpr("Floating literal"),
	UnaryExpr("Unary expression"),
	BinaryExpr("Binary expression"),
	ImplicitCastExpr("Implicit cast expression")
}

enum class ValueCategory(val desc: String) {
	LValue("Left value"),
	RValue("Right value")
}

enum class UnaryOpCode(val str: String, val desc: String) {
	PreInc("++", "Pre increment"),
	PreDec("--", "Pre decrement"),
	Positive("+", "Unary positive"),
	Negative("-", "Unary negative"),
	Not("!", "Logical not"),
	BitwiseRevert("!", "Bitwise revert"),
	AddressOf("&", "Taking address"),
	DePointer("*", "Dereferencing pointer")
}

enum class BinaryOpCode(val str: String, val desc: String, val prec: Int) {
	Assign("=", "Assignment", 0),
	LogicAnd("&&", "Logical and", 10),
	LogicOr("||", "Logical or", 10),
	Less("<", "Less than", 20),
	Greater(">", "Greater than", 20),
	Equal("==", "Equal to", 20),
	LEQ("<=", "Less than or equal to", 20),
	GEQ(">=", "Greater than or equal to", 20),
	NEQ("!=", "Not equal to", 20),
	BitwiseAnd("&", "Bitwise and", 30),
	BitwiseOr("|", "Bitwise or", 30),
	BitwiseXor("!", "Bitwise Xor", 30),
	Add("+", "Add", 40),
	Sub("-", "Substract", 40),
	Mul("*", "Multiply", 50),
	Div("/", "Divide", 50),
	Mod("%", "Mod", 50),
	UnaryOperation("", "Unary operations", 110)
}

val ExprId.description get() = desc

val UnaryOpCode.description get() = desc

abstract class Expr(val exprId: ExprId) {
	abstract fun accept(astConsumer: ASTConsumer): Any?

	fun getValueCategory(): ValueCategory {
		if (cachedValueCategory == null) {
			cachedValueCategory = getValueCategoryImpl()
		}
		return cachedValueCategory!!
	}

	fun getType(): Type {
		if (cachedType == null) {
			cachedType = getTypeImpl()
		}
		return cachedType!!
	}

	private var cachedType: Type? = null

	private var cachedValueCategory: ValueCategory? = null

	protected abstract fun getTypeImpl(): Type

	protected abstract fun getValueCategoryImpl(): ValueCategory
}

class DeclRefExpr(val decl: VarDecl) : Expr(ExprId.DeclRefExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitDeclRefExpr(this)

	override fun getValueCategoryImpl(): ValueCategory = ValueCategory.LValue

	override fun getTypeImpl(): Type = decl.type
}

class MemberAccessExpr(val decl: Decl, val base: Expr, val byPointer: Boolean)
	: Expr(ExprId.MemberAccessExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getValueCategoryImpl(): ValueCategory = ValueCategory.LValue

	override fun getTypeImpl(): Type {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}

class IntegralLiteralExpr(val value: Long) : Expr(ExprId.IntegralLiteralExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? = astConsumer.visitIntegralLiteralExpr(this)

	override fun getValueCategoryImpl(): ValueCategory = ValueCategory.RValue

	override fun getTypeImpl(): Type = when (value) {
		in (Byte.MIN_VALUE .. Byte.MAX_VALUE) -> BuiltinType(BuiltinTypeId.Int8, getNoSpecifier())
		in (Short.MIN_VALUE .. Short.MAX_VALUE) -> BuiltinType(BuiltinTypeId.Int16, getNoSpecifier())
		in (Int.MIN_VALUE .. Int.MAX_VALUE) -> BuiltinType(BuiltinTypeId.Int32, getNoSpecifier())
		else -> BuiltinType(BuiltinTypeId.Int64, getNoSpecifier())
	}
}

class FloatingLiteralExpr(val value: Double) : Expr(ExprId.FloatingLiteralExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getValueCategoryImpl(): ValueCategory = ValueCategory.RValue

	override fun getTypeImpl(): Type = BuiltinType(BuiltinTypeId.Float, getNoSpecifier())
}

class UnaryExpr(val opCode: UnaryOpCode, val expr: Expr) : Expr(ExprId.UnaryExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getValueCategoryImpl(): ValueCategory = when (opCode) {
		UnaryOpCode.DePointer -> ValueCategory.LValue
		else -> ValueCategory.RValue
	}

	override fun getTypeImpl(): Type = expr.getType()
}

class BinaryExpr(val opCode: BinaryOpCode, val lhs: Expr, val rhs: Expr) : Expr(ExprId.BinaryExpr) {
	override fun accept(astConsumer: ASTConsumer): Any? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getValueCategoryImpl(): ValueCategory = when (opCode) {
		BinaryOpCode.Assign -> ValueCategory.LValue
		else -> ValueCategory.RValue
	}

	override fun getTypeImpl(): Type = lhs.getType()
}

enum class CastOperation(val desc: String) {
	UpgradeCast(""),
	DowngradeCast(""),
	LValueToRValueDecay(""),
	AddConst(""),
	RemoveConst("")
}

class ImplicitCastExpr(val castOp: CastOperation, val expr: Expr, val destType: Type)
