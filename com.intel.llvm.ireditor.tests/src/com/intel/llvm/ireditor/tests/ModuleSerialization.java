package com.intel.llvm.ireditor.tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.generator.InMemoryFileSystemAccess;
import org.eclipse.xtext.resource.SaveOptions;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.EmfFormatter;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Injector;
import com.intel.llvm.ireditor.LLVM_IRStandaloneSetup;
import com.intel.llvm.ireditor.lLVM_IR.BasicBlock;
import com.intel.llvm.ireditor.lLVM_IR.FunctionDef;
import com.intel.llvm.ireditor.lLVM_IR.FunctionHeader;
import com.intel.llvm.ireditor.lLVM_IR.GlobalVariable;
import com.intel.llvm.ireditor.lLVM_IR.Instruction_alloca;
import com.intel.llvm.ireditor.lLVM_IR.Instruction_ret;
import com.intel.llvm.ireditor.lLVM_IR.LLVM_IRFactory;
import com.intel.llvm.ireditor.lLVM_IR.MiddleInstruction;
import com.intel.llvm.ireditor.lLVM_IR.Model;
import com.intel.llvm.ireditor.lLVM_IR.NamedMiddleInstruction;
import com.intel.llvm.ireditor.lLVM_IR.NonLeftRecursiveType;
import com.intel.llvm.ireditor.lLVM_IR.Parameter;
import com.intel.llvm.ireditor.lLVM_IR.ParameterType;
import com.intel.llvm.ireditor.lLVM_IR.Parameters;
import com.intel.llvm.ireditor.lLVM_IR.TerminatorInstruction;
import com.intel.llvm.ireditor.lLVM_IR.Type;
import com.intel.llvm.ireditor.lLVM_IR.impl.LLVM_IRFactoryImpl;

public class ModuleSerialization extends Assert {

    @Test
    public void testSerialization() throws Exception {
        LLVM_IRFactory factory = LLVM_IRFactoryImpl.eINSTANCE;

        Model model = factory.createModel();

        GlobalVariable global = factory.createGlobalVariable();
        global.setName("foo");
        Type type = factory.createType();
        NonLeftRecursiveType nonLeftRecursiveType = factory
                .createNonLeftRecursiveType();
        nonLeftRecursiveType.setType(factory.createOpaqueType());
        type.setBaseType(nonLeftRecursiveType);
        global.setType(type);
        model.getElements().add(global);

        FunctionDef funcDef = factory.createFunctionDef();

        FunctionHeader funcHeader = factory.createFunctionHeader();
        funcHeader.setName("@b1");
        Parameters params = factory.createParameters();
        Parameter param = factory.createParameter();
        param.setName("%argc");
        ParameterType paramType = factory.createParameterType();
        Type type4 = factory.createType();
        NonLeftRecursiveType nonLeftRecursiveType4 = factory
                .createNonLeftRecursiveType();
        nonLeftRecursiveType4.setType(factory.createIntType());
        type4.setBaseType(nonLeftRecursiveType4);
        paramType.setType(type4);
        param.setType(paramType);
        params.getParameters().add(param);
        //	    params.setVararg("...");
        funcHeader.setParameters(params);
        ParameterType pt = factory.createParameterType();
        Type type3 = factory.createType();
        NonLeftRecursiveType nonLeftRecursiveType3 = factory
                .createNonLeftRecursiveType();
        nonLeftRecursiveType3.setType(factory.createVoidType());
        type3.setBaseType(nonLeftRecursiveType3);
        pt.setType(type3);
        funcHeader.setRettype(pt);
        funcDef.setHeader(funcHeader);

        BasicBlock bb = factory.createBasicBlock();
        bb.setName("%entrypoint");
        MiddleInstruction mi = factory.createMiddleInstruction();
        NamedMiddleInstruction nmi = factory.createNamedMiddleInstruction();
        nmi.setName("%a1");
        Instruction_alloca alloca = factory.createInstruction_alloca();
        Type type2 = factory.createType();
        NonLeftRecursiveType nonLeftRecursiveType2 = factory
                .createNonLeftRecursiveType();
        nonLeftRecursiveType2.setType(factory.createIntType());
        type2.setBaseType(nonLeftRecursiveType2);
        alloca.setType(type2);
        alloca.setOpcode("alloca");
        nmi.setInstruction(alloca);
        mi.setInstruction(nmi);
        bb.getInstructions().add(mi);
        TerminatorInstruction ti = factory.createTerminatorInstruction();
        Instruction_ret ret = factory.createInstruction_ret();
        ret.setOpcode("ret");
        ti.setInstruction(ret);
        bb.getInstructions().add(ti);
        funcDef.getBasicBlocks().add(bb);

        model.getElements().add(funcDef);

        //IFileSystemAccess fsa = new JavaIoFileSystemAccess();
        //	    InMemoryFileSystemAccess fsa = new InMemoryFileSystemAccess();

        Injector injector = new LLVM_IRStandaloneSetup()
                .createInjectorAndDoEMFRegistration();
        //	    IGenerator gen = injector.getInstance(IGenerator.class);

        XtextResourceSet resourceSet = injector
                .getInstance(XtextResourceSet.class);
        XtextResource resource = (XtextResource) resourceSet.createResource(URI
                .createURI("platform:/resource/./dummy.ll"));
        resource.getContents().add(model);

        //        gen.doGenerate(resource, fsa);
        //
        //        for (CharSequence cs : fsa.getFiles().values()) {
        //            System.out.println(cs);
        //        }

        //System.out.println(EmfFormatter.listToStr(model.getElements()));

        SaveOptions options = SaveOptions.newBuilder().format().getOptions();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            resource.save(outputStream, options.toOptionsMap());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Serialized result: " + outputStream.toString());
    }

}
