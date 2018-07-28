package cn.ckxily.ckxc.ast.type

import cn.ckxily.ckxc.ast.decl.ClassDecl
import cn.ckxily.ckxc.ast.decl.EnumDecl
import cn.ckxily.ckxc.sema.isBool
import cn.ckxily.ckxc.util.addressOf
import com.sun.org.apache.xpath.internal.operations.Bool

enum class TypeId {
	Builtin,
	Pointer,
	Reference,
	Class,
	Enum;

	val str get() = name
}

enum class BuiltinTypeId(val str: String, val rank: Int) {
	Int8("8bit Integer", 10),
	Int16("16bit Integer", 20),
	Int32("32bit Integer", 30),
	Int64("64bit Integer", 40),
	Float("Float", 110),
	Double("Double", 120),
	Boolean("Boolean", 5),
	Void("Void", -1)
}

data class TypeQualifiers(var isConst: Boolean, var isVolatile: Boolean) {
	override fun toString() = buildString {
		if (isConst) append("Const")
		if (isVolatile) {
			if (isConst) {
				append(' ')
			}
			append("Volatile")
		}
	}

	/// TODO replace the implementation then
	enum class CompareResult { LessQualified, MoreQualified, Equal, Nonsense }
	fun compareWith(that: TypeQualifiers): CompareResult {
		if (this.isConst == that.isConst && this.isVolatile == that.isVolatile) {
			return CompareResult.Equal
		}
		else if (!this.isConst && that.isConst) {
			if (this.isVolatile && !that.isVolatile) {
				return CompareResult.Nonsense
			}
			return CompareResult.LessQualified
		}
		else if (!this.isVolatile && that.isVolatile) {
			if (this.isConst && !that.isConst) {
				return CompareResult.Nonsense
			}
			return CompareResult.LessQualified
		}
		else if (this.isConst && !that.isConst) {
			if (!this.isVolatile && that.isVolatile) {
				return CompareResult.Nonsense
			}
			return CompareResult.MoreQualified
		}
		else {
			if (!this.isConst && that.isConst) {
				return CompareResult.Nonsense
			}
			return CompareResult.MoreQualified
		}
	}
}

fun getCVSpecifiers() = TypeQualifiers(true, true)
fun getCSpecifier() = TypeQualifiers(true, false)
fun getVSpecifier() = TypeQualifiers(false, true)
fun getNoSpecifier() = TypeQualifiers(false, false)

fun qualType(origin: Type, qualifiers: TypeQualifiers): Type = origin.fork().also{it.qualifiers = qualifiers}

sealed class Type(var typeId: TypeId, var qualifiers: TypeQualifiers) {
	abstract override fun toString(): String
	abstract fun fork(): Type
	abstract fun equalType(type: Type): Boolean
	fun equalQualifier(type: Type): Boolean = qualifiers == type.qualifiers
	fun fullEqual(type: Type): Boolean = equalQualifier(type) && equalType(type)
}

class BuiltinType(val builtinTypeId: BuiltinTypeId, qualifiers: TypeQualifiers) : Type(TypeId.Builtin, qualifiers) {
	override fun toString() = "${builtinTypeId.str} $qualifiers"
	override fun fork(): Type = BuiltinType(builtinTypeId, qualifiers)
	override fun equalType(type: Type): Boolean
			= type.typeId == TypeId.Builtin && (type as BuiltinType).builtinTypeId == builtinTypeId
}

class PointerType(var pointee: Type, qualifiers: TypeQualifiers) : Type(TypeId.Pointer, qualifiers) {
	override fun toString() = "$qualifiers pointer to $pointee"
	override fun fork(): Type = PointerType(pointee, qualifiers)
	override fun equalType(type: Type): Boolean
			= type.typeId == TypeId.Pointer && (type as PointerType).pointee.fullEqual(pointee)
}

class ReferenceType(var referenced: Type, qualifiers: TypeQualifiers) : Type(TypeId.Reference, qualifiers) {
	override fun toString() = "$qualifiers reference to $referenced"
	override fun fork(): Type = ReferenceType(referenced, qualifiers)
	override fun equalType(type: Type): Boolean
			= type.typeId == TypeId.Reference && (type as ReferenceType).referenced.fullEqual(referenced)
}

class ClassType(var decl: ClassDecl, qualifiers: TypeQualifiers) : Type(TypeId.Class, qualifiers) {
	override fun toString() = "${decl.nameStr} ${addressOf(decl)} $qualifiers"
	override fun fork(): Type = ClassType(decl, qualifiers)
	override fun equalType(type: Type): Boolean
			= type.typeId == TypeId.Class && (type as ClassType).decl == decl
}

class EnumType(var decl: EnumDecl, qualifiers: TypeQualifiers) : Type(TypeId.Enum, qualifiers) {
	override fun toString() = "${decl.nameStr} ${addressOf(decl)} $qualifiers"
	override fun fork(): Type = EnumType(decl, qualifiers)
	override fun equalType(type: Type): Boolean
			= type.typeId == TypeId.Enum && (type as EnumType).decl == decl
}
