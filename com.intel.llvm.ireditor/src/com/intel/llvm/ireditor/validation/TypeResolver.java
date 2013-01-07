/*
Copyright (c) 2013, Intel Corporation

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of Intel Corporation nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.intel.llvm.ireditor.validation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import com.intel.llvm.ireditor.lLVM_IR.Alias;
import com.intel.llvm.ireditor.lLVM_IR.ArrayConstant;
import com.intel.llvm.ireditor.lLVM_IR.ArrayType;
import com.intel.llvm.ireditor.lLVM_IR.BinaryInstruction;
import com.intel.llvm.ireditor.lLVM_IR.BitwiseBinaryInstruction;
import com.intel.llvm.ireditor.lLVM_IR.ConversionInstruction;
import com.intel.llvm.ireditor.lLVM_IR.FloatingType;
import com.intel.llvm.ireditor.lLVM_IR.FunctionHeader;
import com.intel.llvm.ireditor.lLVM_IR.FunctionRef;
import com.intel.llvm.ireditor.lLVM_IR.FunctionTypeOrPointerToFunctionTypeSuffix;
import com.intel.llvm.ireditor.lLVM_IR.GlobalValueRef;
import com.intel.llvm.ireditor.lLVM_IR.Instruction_alloca;
import com.intel.llvm.ireditor.lLVM_IR.Instruction_atomicrmw;
import com.intel.llvm.ireditor.lLVM_IR.Instruction_load;
import com.intel.llvm.ireditor.lLVM_IR.Instruction_phi;
import com.intel.llvm.ireditor.lLVM_IR.IntType;
import com.intel.llvm.ireditor.lLVM_IR.LocalValueRef;
import com.intel.llvm.ireditor.lLVM_IR.MetadataType;
import com.intel.llvm.ireditor.lLVM_IR.NamedMiddleInstruction;
import com.intel.llvm.ireditor.lLVM_IR.NamedTerminatorInstruction;
import com.intel.llvm.ireditor.lLVM_IR.NonLeftRecursiveNonVoidType;
import com.intel.llvm.ireditor.lLVM_IR.NonLeftRecursiveType;
import com.intel.llvm.ireditor.lLVM_IR.NonVoidType;
import com.intel.llvm.ireditor.lLVM_IR.OpaqueType;
import com.intel.llvm.ireditor.lLVM_IR.Parameter;
import com.intel.llvm.ireditor.lLVM_IR.ParameterType;
import com.intel.llvm.ireditor.lLVM_IR.SimpleConstant;
import com.intel.llvm.ireditor.lLVM_IR.Star;
import com.intel.llvm.ireditor.lLVM_IR.StructType;
import com.intel.llvm.ireditor.lLVM_IR.StructureConstant;
import com.intel.llvm.ireditor.lLVM_IR.Type;
import com.intel.llvm.ireditor.lLVM_IR.TypeDef;
import com.intel.llvm.ireditor.lLVM_IR.TypedConstant;
import com.intel.llvm.ireditor.lLVM_IR.TypedValue;
import com.intel.llvm.ireditor.lLVM_IR.VectorConstant;
import com.intel.llvm.ireditor.lLVM_IR.VectorType;
import com.intel.llvm.ireditor.lLVM_IR.VoidType;
import com.intel.llvm.ireditor.lLVM_IR.X86mmxType;
import com.intel.llvm.ireditor.lLVM_IR.util.LLVM_IRSwitch;
import com.intel.llvm.ireditor.resolvedtypes.ResolvedAnyType;
import com.intel.llvm.ireditor.resolvedtypes.ResolvedFloatingType;
import com.intel.llvm.ireditor.resolvedtypes.ResolvedFunctionType;
import com.intel.llvm.ireditor.resolvedtypes.ResolvedIntegerType;
import com.intel.llvm.ireditor.resolvedtypes.ResolvedPointerType;
import com.intel.llvm.ireditor.resolvedtypes.ResolvedType;
import com.intel.llvm.ireditor.resolvedtypes.ResolvedTypeReference;
import com.intel.llvm.ireditor.resolvedtypes.ResolvedVoidType;

import static com.intel.llvm.ireditor.validation.LLVM_IRJavaValidator.*;

/**
 * Converts an EObject to a String representing its type.
 */
public class TypeResolver extends LLVM_IRSwitch<ResolvedType> {
	private final LinkedList<TypeDef> enclosing = new LinkedList<TypeDef>();
	private final Map<String, ResolvedType> simpleTypes = new HashMap<String, ResolvedType>();
	private static final Map<String, Integer> floatingTypes = new HashMap<String, Integer>();

	static {
		floatingTypes.put("half", 16);
		floatingTypes.put("float", 32);
		floatingTypes.put("double", 64);
		floatingTypes.put("fp128", 128);
		floatingTypes.put("x86_fp80", 80);
		floatingTypes.put("ppc_fp128", 128);
	}
	
	public ResolvedType resolve(EObject object) {
		return doSwitch(object);
	}
	
	@Override
	public ResolvedType defaultCase(EObject object) {
		// TODO change to ResolvedUnknownType once everything is covered?
		return new ResolvedAnyType();
	}
	
	@Override
	public ResolvedType caseType(Type object) {
		ResolvedType result = resolve(object.getBaseType());
		buildPointersTo(result, object.getStars());
		FunctionTypeOrPointerToFunctionTypeSuffix suffix = object.getFunctionSuffix();
		return suffix == null? result : buildTypeFromSuffix(result, suffix);
	}
	
	@Override
	public ResolvedType caseNonVoidType(NonVoidType object) {
		ResolvedType result = resolve(object.getBaseType());
		buildPointersTo(result, object.getStars());
		FunctionTypeOrPointerToFunctionTypeSuffix suffix = object.getFunctionSuffix();
		return suffix == null? result : buildTypeFromSuffix(result, suffix);
	}
	
	@Override
	public ResolvedType caseNonLeftRecursiveType(NonLeftRecursiveType object) {
		TypeDef typeDef = object.getTypedef();
		if (typeDef != null) {
			if (enclosing.contains(typeDef)) {
				return new ResolvedTypeReference(typeDef.getName());
			}
			enclosing.push(typeDef);
			ResolvedType result = resolve(typeDef.getType());
			enclosing.pop();
			return result;
		}
		return resolve(object.getType());
	}
	
	@Override
	public ResolvedType caseNonLeftRecursiveNonVoidType(NonLeftRecursiveNonVoidType object) {
		return resolve((object).getType());
	}
	
	@Override
	public ResolvedType caseIntType(IntType object) {
		int bits = Integer.valueOf(textOf(object).substring(1));
		return new ResolvedIntegerType(bits);
	}
	
	@Override
	public ResolvedType caseFloatingType(FloatingType object) {
		return getSimpleType(textOf(object));
	}
	
	@Override
	public String caseX86mmxType(X86mmxType object) {
		return textOf(object);
	}
	
	@Override
	public String caseVoidType(VoidType object) {
		return "void";
	}
	
	@Override
	public String caseMetadataType(MetadataType object) {
		return textOf(object);
	}
	
	@Override
	public String caseOpaqueType(OpaqueType object) {
		return textOf(object);
	}
	
	@Override
	public String caseFunctionTypeOrPointerToFunctionTypeSuffix(
			FunctionTypeOrPointerToFunctionTypeSuffix object) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		if (object.getContainedTypes().isEmpty() && object.getVararg() != null) {
			sb.append("...");
		} else {
			boolean first = true;
			for (ParameterType p : object.getContainedTypes()) {
				if (first == false) sb.append(", ");
				first = false;
				sb.append(doSwitch(p.getType()));
			}
			if (object.getVararg() != null) sb.append(", ...");
		}
		sb.append(")");
		addStars(object.getStars(), sb);
		return sb.toString();
	}
	
	@Override
	public String caseVectorType(VectorType object) {
		return "<" + object.getSize() + " x " + doSwitch(object.getElemType()) + ">";
	}
	
	@Override
	public String caseArrayType(ArrayType object) {
		return "[" + object.getSize() + " x " + doSwitch(object.getElemType()) + "]";
	}
	
	@Override
	public String caseStructType(StructType object) {
		EList<Type> types = object.getTypes();
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		for (Type t : types) {
			if (first == false) sb.append(", ");
			first = false;
			sb.append(doSwitch(t));
		}
		sb.append("}");
		return sb.toString();
	}
	
	@Override
	public String caseTypedValue(TypedValue object) {
		return doSwitch(object.getType());
	}
	
	@Override
	public String caseLocalValueRef(LocalValueRef object) {
		return doSwitch(object.getRef());
	}
	
	@Override
	public String caseGlobalValueRef(GlobalValueRef object) {
		if (object.getConstant() != null) return doSwitch(object.getConstant());
		if (object.getIntrinsic() != null) return TYPE_ANY;
		if (object.getMetadata() != null) return doSwitch(object.getMetadata());
		if (object.getRef() != null) return doSwitch(object.getRef());
		return TYPE_UNKNOWN;
	}
	
	@Override
	public String caseParameter(Parameter object) {
		return doSwitch(object.getType().getType());
	}
	
	@Override
	public String caseNamedMiddleInstruction(NamedMiddleInstruction object) {
		return doSwitch(object.getInstruction());
	}
	
	@Override
	public String caseNamedTerminatorInstruction(
			NamedTerminatorInstruction object) {
		return doSwitch(object.getInstruction());
	}
	
	@Override
	public String caseAlias(Alias object) {
		return doSwitch(object.getType());
	}
	
	@Override
	public String caseFunctionHeader(FunctionHeader object) {
		StringBuilder sb = new StringBuilder();
		sb.append(doSwitch(object.getRettype()));
		sb.append(" (");
		boolean first = true;
		for (Parameter p : object.getParameters()) {
			if (first == false) sb.append(", ");
			first = false;
			sb.append(doSwitch(p.getType()));
		}
		sb.append(")");
		return sb.toString();
	}
	
	@Override
	public String caseFunctionRef(FunctionRef object) {
		if (object.getIntrinsic() != null) return TYPE_ANY;
		return doSwitch(object.getRef()) + "*";
	}
	
	@Override
	public String caseTypedConstant(TypedConstant object) {
		return doSwitch(object.getType());
	}
	
	@Override
	public String caseSimpleConstant(SimpleConstant object) {
		String content = textOf(object);
		if (content.startsWith("c\"")) {
			return TYPE_CSTRING;
		} else if (content.matches("-?\\d+\\.\\d+(e-?\\d+)?") ||
				content.matches("0x[klmhKLMH]?[0-9a-fA-F]+")) {
			return TYPE_FLOATING;
		} else if (content.matches("-?\\d+")) {
			return TYPE_INTEGER;
		} else if (content.equals("true") || content.equals("false")) {
			return "i1";
		}
		return TYPE_UNKNOWN;
	}
	
	@Override
	public String caseVectorConstant(VectorConstant object) {
		EList<TypedConstant> values = object.getList().getTypedConstants();
		return "<" + values.size() + " x " + doSwitch(values.get(0).getType()) + ">";
	}
	
	@Override
	public String caseArrayConstant(ArrayConstant object) {
		EList<TypedConstant> values = object.getList().getTypedConstants();
		return "[" + values.size() + " x " + doSwitch(values.get(0).getType()) + "]";
	}
	
	@Override
	public String caseStructureConstant(StructureConstant object) {
		EList<TypedConstant> values = object.getList().getTypedConstants();
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		for (TypedConstant tc : values) {
			if (first == false) sb.append(", ");
			first = false;
			sb.append(doSwitch(tc.getType()));
		}
		sb.append("}");
		return sb.toString();
	}
	
	
	@Override
	public String caseBinaryInstruction(BinaryInstruction object) {
		return doSwitch(object.getType());
	}
	
	@Override
	public String caseBitwiseBinaryInstruction(BitwiseBinaryInstruction object) {
		return doSwitch(object.getType());
	}
	
	@Override
	public String caseConversionInstruction(ConversionInstruction object) {
		return doSwitch(object.getTargetType());
	}
	
	@Override
	public String caseInstruction_alloca(Instruction_alloca object) {
		return doSwitch(object.getType()) + "*";
	}
	
	@Override
	public String caseInstruction_atomicrmw(Instruction_atomicrmw object) {
		return doSwitch(object.getArgument().getType());
	}
	
	@Override
	public String caseInstruction_phi(Instruction_phi object) {
		return doSwitch(object.getType());
	}
	
	@Override
	public String caseInstruction_load(Instruction_load object) {
		return doSwitch(object.getPointer().getType());
	}
	
	private String textOf(EObject object) {
		return NodeModelUtils.getTokenText(NodeModelUtils.getNode(object));
	}
	
	private ResolvedType buildTypeFromSuffix(ResolvedType rettype,
			FunctionTypeOrPointerToFunctionTypeSuffix suffix) {
		List<ResolvedType> paramTypes = new LinkedList<ResolvedType>();
		for (ParameterType t : suffix.getContainedTypes()) {
			paramTypes.add(resolve(t));
		}
		ResolvedType result = new ResolvedFunctionType(rettype, paramTypes);
		return buildPointersTo(result, suffix.getStars());
	}
	
	private ResolvedType buildPointersTo(ResolvedType base, Iterable<Star> stars) {
		ResolvedType result = base;
		for (Star star : stars) {
			String addrSpaceStr = star.getAddressSpace();
			int addrSpace = addrSpaceStr == null ? -1 : Integer.valueOf(addrSpaceStr);
			result = new ResolvedPointerType(result, addrSpace);
		}
		return result;
	}
	
	private ResolvedType getSimpleType(String text) {
		ResolvedType result = simpleTypes.get(text);
		if (result != null) return result;
		if (text.equals("void")) return addSimpleType(new ResolvedVoidType());
		if (floatingTypes.containsKey(text)) {
			return addSimpleType(text, new ResolvedFloatingType(text, floatingTypes.get(text)));
		}
	}

	private ResolvedType addSimpleType(String key, ResolvedType t) {
		simpleTypes.put(key, t);
		return t;
	}
	
}
