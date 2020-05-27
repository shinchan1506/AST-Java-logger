/**
 *
 * Authors: Shin Imai
 *          Ronny Recinos
 *          Jon-Michael Hoang
 *
 * driver.scala: This file is the entry point for the program.
 *               For details on how to actually get the program running,
 *               refer to the README.md file
 *
 */

// import basic java libraries
import java.io.File
import java.util

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

object driver extends App with LazyLogging {

  // load the configuration file application.conf from the resources directory
  val config: Config = ConfigFactory.load()

  // debugger line to print the absolute path
  // println(new File(".").getAbsolutePath());

  // using the configuration file, extract values fromm the file into the vals specified in order to build
  // a directory string to be able to read and write from it
  // also allows production of a backup file in case things wrong
  val filePath: String = config.getString("ASTConfig.resources") + "/" + config.getString("ASTConfig.sourcefile")
  val new_filepath: String = config.getString("ASTConfig.resources") + "/old_" + config.getString("ASTConfig.sourcefile")

  // using the directory string that was built from filePath and new_filepath,
  // pass it into the File constructor to allow the actual process of
  // reading and writing from the given directory string
  val sourcef: File = new File(filePath)
  val newsourcef: File = new File(new_filepath)
  // read file to string in the standard UTF-8 format
  val source: String = readFileToString(sourcef, "UTF-8")

  // if the document that was created was successful
  if (newsourcef.createNewFile() == true) {
    // create a backup file for it
    val old_doc: Document = new Document(source)
    FileUtils.write(newsourcef, old_doc.get, "UTF-8")
  }


  /**
   * JLS 12
   * K_COMPILATION_UNIT = Kind constant used to request that the source be parsed as a compilation unit.
   * setResolveBindings() = Requests that the compiler should provide binding information for the AST nodes it creates.
   */
  val parser: ASTParser = ASTParser.newParser(AST.JLS12)
  parser.setKind(ASTParser.K_COMPILATION_UNIT)
  parser.setResolveBindings(true)

  /**
   * Make sure that the parser uses the same options as the JavaCore
   * not to be confused with Options as discussed in class
   */
  val options: util.Hashtable[String, String] = JavaCore.getOptions()
  parser.setCompilerOptions(options)

  // the name of the compilation unit
  val unitName: String = "test.java"
  // set the name of the parser
  parser.setUnitName(unitName)
  val sources: Array[String] = Array(config.getString("ASTConfig.resources"))
  val classpath: Array[String] = Array(config.getString("ASTConfig.resources"))
  // classpathEntries,  sourcepathEntries, encodings, Include RunningVM Boot class path
  parser.setEnvironment(classpath, sources, Array[String]("UTF-8"), true)

  // creating a new document from the source file specified beforehand
  val document: Document = new Document(source)

  // set the source to parsed by the parser
  // to allow writes and rewrites
  parser.setSource(source.toCharArray)
  val cu:CompilationUnit = parser.createAST(null).asInstanceOf[CompilationUnit]
  // set up the ASTRewrite to be able to rewrite the AST in the future
  val rewriter: ASTRewrite = ASTRewrite.create(cu.getAST)
  cu.recordModifications()

  // debugger line to be able to see if it actually works
  // println(cu.toString)

  // starts the traversal of the AST through the ASTVisitor which implements the
  // visitor design pattern
  cu.accept(new MyASTVisitor(cu, rewriter))

  // rewrite the document and apply changes to it based on the results
  val edits = rewriter.rewriteAST(document, null)
  edits.apply(document)
  FileUtils.write(sourcef, document.get, "UTF-8")

}

