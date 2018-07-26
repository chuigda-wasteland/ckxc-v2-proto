package cn.ckxily.ckxc.sema

import cn.ckxily.ckxc.ast.type.*

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

fun Type.isFloating(): Boolean {
	return typeId == TypeId.Builtin && (this as BuiltinType).builtinTypeId == BuiltinTypeId.Float
}

fun Type.isBool(): Boolean {
	return typeId == TypeId.Builtin && (this as BuiltinType).builtinTypeId == BuiltinTypeId.Boolean
}


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
