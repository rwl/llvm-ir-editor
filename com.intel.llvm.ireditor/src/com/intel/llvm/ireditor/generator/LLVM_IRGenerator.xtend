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

package com.intel.llvm.ireditor.generator

import com.intel.llvm.ireditor.lLVM_IR.FunctionDef
import com.intel.llvm.ireditor.lLVM_IR.GlobalVariable
import com.intel.llvm.ireditor.lLVM_IR.Model
import com.intel.llvm.ireditor.lLVM_IR.NonLeftRecursiveType
import com.intel.llvm.ireditor.lLVM_IR.Type
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.generator.IGenerator

class LLVM_IRGenerator implements IGenerator {

	override void doGenerate(Resource resource, IFileSystemAccess fsa) {
        for(m: resource.allContents.toIterable.filter(typeof(Model))) {
            fsa.generateFile("main.ll",
                m.compile);
        }
    }

    def compile(Model m) '''
«««        «IF e.eContainer != null»
«««            package «e.eContainer.fullyQualifiedName»;
«««        «ENDIF»
        ; ModuleID = ''
        «FOR g:m.elements.filter(typeof(GlobalVariable))»
            «g.compile»
        «ENDFOR»
        «FOR g:m.elements.filter(typeof(FunctionDef))»
            «g.compile»
        «ENDFOR»
    '''

    def compile(GlobalVariable g) '''
        @«g.name» = global «g.type.compile»
    '''

    def compile(FunctionDef funcDef) '''
        define
    '''

    def compile(Type t) '''
        «t.baseType.compile»
    '''

    def compile(NonLeftRecursiveType nlrt) '''
        type
    '''
}
