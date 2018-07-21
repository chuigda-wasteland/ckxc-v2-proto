package cn.ckxily.ckxc.ast.type

import cn.ckxily.ckxc.ast.decl.ClassDecl
import cn.ckxily.ckxc.ast.decl.EnumDecl
import cn.ckxily.ckxc.util.addressOf
import java.awt.Point

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
	Boolean("Boolean", -1)
}

data class TypeSpecifiers(var isConst: Boolean, var isVolatile: Boolean) {
	override fun toString() = buildString {
		if (isConst) append("Const")
		if (isVolatile) {
			if (isConst) {
				append(' ')
			}
			append("Volatile")
		}
	}
}

fun getCVSpecifiers() = TypeSpecifiers(true, true)
fun getCSpecifier() = TypeSpecifiers(true, false)
fun getVSpecifier() = TypeSpecifiers(false, true)
fun getNoSpecifier() = TypeSpecifiers(false, false)

fun qualType(origin: Type, specifiers: TypeSpecifiers): Type = origin.deepCopy().also{it.specifiers = specifiers}

sealed class Type(var typeId: TypeId, var specifiers: TypeSpecifiers) {
	abstract override fun toString(): String
	abstract fun deepCopy(): Type
}

class BuiltinType(val builtinTypeId: BuiltinTypeId, specifiers: TypeSpecifiers) : Type(TypeId.Builtin, specifiers) {
	override fun toString() = "${builtinTypeId.str} $specifiers"
	override fun deepCopy(): Type = BuiltinType(builtinTypeId, specifiers)
}

class PointerType(var pointee: Type, specifiers: TypeSpecifiers) : Type(TypeId.Pointer, specifiers) {
	override fun toString() = "$specifiers pointer to $pointee"
	override fun deepCopy(): Type = PointerType(pointee, specifiers)
}

class ReferenceType(var referenced: Type, specifiers: TypeSpecifiers) : Type(TypeId.Reference, specifiers) {
	override fun toString() = "$specifiers reference to $referenced"
	override fun deepCopy(): Type = ReferenceType(referenced, specifiers)
}

class ClassType(var decl: ClassDecl, specifiers: TypeSpecifiers) : Type(TypeId.Class, specifiers) {
	override fun toString() = "${decl.nameStr} ${addressOf(decl)} $specifiers"
	override fun deepCopy(): Type = ClassType(decl, specifiers)
}

class EnumType(var decl: EnumDecl, specifiers: TypeSpecifiers) : Type(TypeId.Enum, specifiers) {
	override fun toString() = "${decl.nameStr} ${addressOf(decl)} $specifiers"
	override fun deepCopy(): Type = EnumType(decl, specifiers)
}
