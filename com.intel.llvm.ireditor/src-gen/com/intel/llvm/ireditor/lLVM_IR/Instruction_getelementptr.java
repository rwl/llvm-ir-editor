/**
 */
package com.intel.llvm.ireditor.lLVM_IR;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Instruction getelementptr</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link com.intel.llvm.ireditor.lLVM_IR.Instruction_getelementptr#getPointer <em>Pointer</em>}</li>
 *   <li>{@link com.intel.llvm.ireditor.lLVM_IR.Instruction_getelementptr#getIndices <em>Indices</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.intel.llvm.ireditor.lLVM_IR.LLVM_IRPackage#getInstruction_getelementptr()
 * @model
 * @generated
 */
public interface Instruction_getelementptr extends MemoryInstruction
{
  /**
   * Returns the value of the '<em><b>Pointer</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Pointer</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Pointer</em>' containment reference.
   * @see #setPointer(TypedValue)
   * @see com.intel.llvm.ireditor.lLVM_IR.LLVM_IRPackage#getInstruction_getelementptr_Pointer()
   * @model containment="true"
   * @generated
   */
  TypedValue getPointer();

  /**
   * Sets the value of the '{@link com.intel.llvm.ireditor.lLVM_IR.Instruction_getelementptr#getPointer <em>Pointer</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Pointer</em>' containment reference.
   * @see #getPointer()
   * @generated
   */
  void setPointer(TypedValue value);

  /**
   * Returns the value of the '<em><b>Indices</b></em>' containment reference list.
   * The list contents are of type {@link com.intel.llvm.ireditor.lLVM_IR.TypedValue}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Indices</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Indices</em>' containment reference list.
   * @see com.intel.llvm.ireditor.lLVM_IR.LLVM_IRPackage#getInstruction_getelementptr_Indices()
   * @model containment="true"
   * @generated
   */
  EList<TypedValue> getIndices();

} // Instruction_getelementptr
