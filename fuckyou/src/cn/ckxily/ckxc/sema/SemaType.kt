package cn.ckxily.ckxc.sema

import cn.ckxily.ckxc.ast.type.*
import com.sun.org.apache.xpath.internal.operations.Bool

fun Type.isInteger(): Boolean {
	if (this.typeId == TypeId.Builtin) {
		val builtinType = this as BuiltinType
		if (builtinType.builtinTypeId == BuiltinTypeId.Int8
				|| builtinType.builtinTypeId == BuiltinTypeId.Int16
				|| builtinType.builtinTypeId == BuiltinTypeId.Int32
				|| builtinType.builtinTypeId == BuiltinTypeId.Int64) {
			return true
		}
	}
	return false
}

fun Type.isFloating(): Boolean = typeId == TypeId.Builtin && (this as BuiltinType).builtinTypeId == BuiltinTypeId.Float

fun Type.isBool(): Boolean = typeId == TypeId.Builtin && (this as BuiltinType).builtinTypeId == BuiltinTypeId.Boolean

fun Type.isPointer(): Boolean = typeId == TypeId.Pointer

fun Type.isReference(): Boolean = typeId == TypeId.Reference

fun Type.isVoid(): Boolean = typeId == TypeId.Builtin && (this as BuiltinType).builtinTypeId == BuiltinTypeId.Void

fun Type.isBuiltin(): Boolean = typeId == TypeId.Builtin

class TypeUtility {
	companion object {
		fun commonBuiltinType(ty1: BuiltinType, ty2: BuiltinType): Type? {
			if (ty1.isInteger() && ty2.isInteger()
					|| (ty1.isFloating() && ty2.isFloating())
					|| (ty1.isBool() && ty2.isBool())) {
				val resultType = maxRankingType(ty1, ty2)
				maxQualifier(ty1.qualifiers, ty2.qualifiers, resultType)
				return resultType
			}
			return null
		}

		fun maxRankingType(ty1: BuiltinType, ty2: BuiltinType): BuiltinType =
			if (ty1.builtinTypeId.rank > ty2.builtinTypeId.rank) ty1 else ty2

		fun maxQualifier(qual1: TypeQualifiers, qual2: TypeQualifiers, writeToType: Type) {
			writeToType.qualifiers.isConst = qual1.isConst || qual2.isConst
			writeToType.qualifiers.isVolatile = qual1.isVolatile || qual2.isVolatile
		}
	}
}
