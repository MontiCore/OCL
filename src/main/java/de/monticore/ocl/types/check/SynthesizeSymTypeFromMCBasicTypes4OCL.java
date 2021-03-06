/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.types.check;

import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SynthesizeSymTypeFromMCBasicTypes;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedName;

import java.util.Optional;

public class SynthesizeSymTypeFromMCBasicTypes4OCL extends SynthesizeSymTypeFromMCBasicTypes {

  @Override
  protected Optional<SymTypeExpression> handleIfNotFound(ASTMCQualifiedName qName) {
    if(getScope(qName.getEnclosingScope()).resolveFunction(qName.getQName()).isPresent()){
      return Optional.empty();
    }else{
      return super.handleIfNotFound(qName);
    }
  }
}
