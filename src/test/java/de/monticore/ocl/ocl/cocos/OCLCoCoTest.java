package de.monticore.ocl.ocl.cocos;

import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._cocos.ExpressionHasNoSideEffect;
import de.monticore.ocl.ocl._cocos.IterateExpressionVariableUsageIsCorrect;
import de.monticore.ocl.ocl._cocos.OCLCoCoChecker;
import de.monticore.ocl.ocl._parser.OCLParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

public class OCLCoCoTest {

  protected static final String RELATIVE_MODEL_PATH = "src/test/resources";

  /*@ParameterizedTest
  @MethodSource("getValidModels")*/
  public void checkExpressionHasNoSideEffectCoCo(String fileName) throws IOException {
    Optional<ASTOCLCompilationUnit> ast = new OCLParser().parse(Paths.get(RELATIVE_MODEL_PATH + "/example/validGrammarModels/" + fileName).toString());
    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new ExpressionHasNoSideEffect());
    checker.checkAll(ast.get());
  }

  private static String[] getValidModels(){
    File f = new File(RELATIVE_MODEL_PATH + "/example/validGrammarModels");
    String[] filenames = f.list();
    return filenames;
  }
}