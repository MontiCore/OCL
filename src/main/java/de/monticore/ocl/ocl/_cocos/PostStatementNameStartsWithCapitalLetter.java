/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.ocl._ast.ASTOCLPostStatement;
import de.se_rwth.commons.logging.Log;

public class PostStatementNameStartsWithCapitalLetter
    implements OCLASTOCLPostStatementCoCo {

  @Override
  public void check(ASTOCLPostStatement astPostStatement) {
    if (astPostStatement.isPresentName() && Character.isLowerCase(astPostStatement.getName().charAt(0))) {
      Log.error(String.format("0xOCL07 post condition name '%s' must start in upper-case.", astPostStatement.getName()),
          astPostStatement.get_SourcePositionStart());
    }
  }
}