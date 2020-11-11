/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.expressions.oclexpressions._ast.ASTLetinExpression;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.se_rwth.commons.logging.Log;

public class ExpressionInOperationConstraintHasToBeLetInExpression
    implements OCLASTOCLOperationConstraintCoCo {

  @Override
  public void check(ASTOCLOperationConstraint astoclOperationConstraint) {
    if (astoclOperationConstraint.isPresentLetDeclaration()) {
      //todo: coco unnecessary with LetDeclaration nonterminal, delete(?)
      /*if (!(astoclOperationConstraint.getLetDeclaration() instanceof ASTLetinExpression)) {
        Log.error(
            String.format("0xOCL0A the expression in an OperationConstraint can only be a LetinExpression, but was %s.", astoclOperationConstraint.getExpression().getClass().getName()));
      }*/
    }
  }
}
