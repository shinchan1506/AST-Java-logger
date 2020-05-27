// import basic java libraries
// import basic java libraries
import java.io.File
import java.util

import driver.parser
import org.junit.Test

// import advanced libraries that are part of the backbone of this project
// without it, we won't be able to do any of these
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging._
import courseproject.MyASTVisitor
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.{AST, ASTParser, CompilationUnit, MethodInvocation, Statement, StringLiteral}
import org.apache.commons.io.FileUtils._
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite
import org.apache.commons.io.FileUtils
import org.eclipse.jface.text.Document
import org.eclipse.text.edits.TextEdit

class Tests {
  val config: Config = ConfigFactory.load()

  /**
   * Set up parser environment variables
   */
  val parser: ASTParser = ASTParser.newParser(AST.JLS12)
  parser.setKind(ASTParser.K_COMPILATION_UNIT)
  parser.setResolveBindings(true)
  val options: util.Hashtable[String, String] = JavaCore.getOptions()
  parser.setCompilerOptions(options)
  val unitName: String = "test.java"
  parser.setUnitName(unitName)
  parser.setEnvironment(Array(config.getString("ASTConfig.resources_Test")), Array(config.getString("ASTConfig.resources_Test")), Array[String]("UTF-8"), true)

  /**
   * Test if the instrumented code is being inserted for variable declaration
   */
  @Test
  def instrumentationTestVariableDeclaration(): Unit = {
    val javaTest1 : String = config.getString("ASTConfig.javaTest")

    val testDocument : Document = new Document(javaTest1)
    parser.setSource(javaTest1.toCharArray)

    val cu:CompilationUnit = parser.createAST(null).asInstanceOf[CompilationUnit]
    val rewriter: ASTRewrite = ASTRewrite.create(cu.getAST)
    cu.recordModifications()
    cu.accept(new MyASTVisitor(cu, rewriter))

    val edits = rewriter.rewriteAST(testDocument, null)
    edits.apply(testDocument)

    val contents: String = testDocument.get()

    /**
     * Now we want to see if the three instrumented codes were inserted
     */
    assert(
      contents.contains(config.getString("ASTConfig.test1_correct1"))
    )
    assert(
      contents.contains(config.getString("ASTConfig.test1_correct2"))
    )
    assert(
      contents.contains(config.getString("ASTConfig.test1_correct3"))
    )
  }

  /**
   * Test if the instrumentation code for class initialization assignments are inserted
   */
  @Test
  def instrumentationTestClassInitializer() = {
    val javaTest2 : String = config.getString("ASTConfig.javaTesttwo")

    val testDocument : Document = new Document(javaTest2)
    parser.setSource(javaTest2.toCharArray)

    val cu:CompilationUnit = parser.createAST(null).asInstanceOf[CompilationUnit]
    val rewriter: ASTRewrite = ASTRewrite.create(cu.getAST)
    cu.recordModifications()
    cu.accept(new MyASTVisitor(cu, rewriter))

    val edits = rewriter.rewriteAST(testDocument, null)
    edits.apply(testDocument)

    val contents: String = testDocument.get()
    /**
     * Now we want to see if the two instrumented codes were inserted
     */
    assert(
      contents.contains(config.getString("ASTConfig.test2_correct1"))
    )
    assert(
      contents.contains(config.getString("ASTConfig.test2_correct2"))
    )
  }

  /**
   * Test if the instrumentation code for class initialization assignments are inserted
   */
  @Test
  def testBlockInstrumentation() = {
    val javaTest3: String = config.getString("ASTConfig.javaTestThree")
    val testDocument: Document = new Document(javaTest3)
    parser.setSource(javaTest3.toCharArray)
    val cu:CompilationUnit = parser.createAST(null).asInstanceOf[CompilationUnit]
    val rewriter: ASTRewrite = ASTRewrite.create(cu.getAST)
    cu.recordModifications()
    cu.accept(new MyASTVisitor(cu, rewriter))
    val edits = rewriter.rewriteAST(testDocument, null)
    edits.apply(testDocument)
    val contents: String = testDocument.get()
    /**
     * Now we want to see if instrumentation code for block starting was inserted
     */
    assert(
      contents.contains(config.getString("ASTConfig.test3_correct1"))
    )
    assert(
      contents.contains(config.getString("ASTConfig.test3_correct2"))
    )
  }

  /**
   * Test if the instrumentation code for class initialization assignments are inserted
   */
  @Test
  def testAssignment() = {
    val javaTest4: String = config.getString("ASTConfig.javaTestFour")
    val testDocument: Document = new Document(javaTest4)
    parser.setSource(javaTest4.toCharArray)
    val cu:CompilationUnit = parser.createAST(null).asInstanceOf[CompilationUnit]
    val rewriter: ASTRewrite = ASTRewrite.create(cu.getAST)
    cu.recordModifications()
    cu.accept(new MyASTVisitor(cu, rewriter))
    val edits = rewriter.rewriteAST(testDocument, null)
    edits.apply(testDocument)
    val contents: String = testDocument.get()
    /**
     * Now we want to see if instrumentation code for assignments
     * Assignment to a Number Literal 1-3
     * Assignment to a infix notation expression #4
     */
    assert(
      contents.contains(config.getString("ASTConfig.test4_correct1"))
    )
    assert(
      contents.contains(config.getString("ASTConfig.test4_correct2"))
    )
    assert(
      contents.contains(config.getString("ASTConfig.test4_correct3"))
    )
    assert(
      contents.contains(config.getString("ASTConfig.test4_correct4"))
    )
  }

}