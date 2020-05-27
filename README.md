# Course Project
##### Shin Imai, Jon-Michael Hoang, Ronny Recinos (Group member owned)

## Logistics and How to run
This project was created and developed in IntelliJ IDEA 2019.2.1. Tested and run with sbt 1.3.2. 

To run the unit tests, use `sbt clean compile test`.

To run the program, use `sbt clean compile run`. In doing so, a prompt may appear - 
asking to either run this with the [1] driver or [2] launcherClass. Select [1] driver.

logging information is located in ./programlogs/performance_info

## Design

#### Pros:

* Runs on the sbt shell / terminal application, which makes it easy to use
* Supports assignments, blocks/scopes, variable declarations and expressions, which allows simple programs to be created
* Has logging which enables the programmer to efficiently understand and work with the code
* Can keep track of the bindings

#### Cons:

* Runs on the sbt shell / terminal application - pretty boring to use
* Only supports assignments, blocks/scopes, variable declarations and expressions,
  which means that this implementation supports small, simple programs.
* Launcher does not work, does not have a Java Process to run independently and output it to another file.
    
#### Log Messages with Lazy Logging
Log messages are provided to easily trace the application while it is running and to be easily able to debug when problems arise.

#### Performance

The implementation is not large - meaning that there is not a lot of data within memory. However, this is a misconception - 
because the program imports a lot of libraries, it has to link most, if not all of them, to be able to allow us to use 
key functions and features to carry out this implementation. Because of the many libraries
that are used, the memory space that this program takes can be quite large for such a simple program.

As for the runtime analysis, runtime is not a huge problem within this application, but one may notice a small delay when 
compiling and running the program due to the libraries that have to be imported for this implementation.

#### AST Parser and instrumented code
The ASTParser starts in the driver.scala singleton class, where it takes the file specified in application.conf (located in ./src/main/resources/) and converts the java code into an abstract syntax tree using the eclipse JDT API. There is no automated way to specify the java file to parse, however. The user must physically navigate to the ./src/main/resources/SampleJava/ directory and place the desired .java file there. Then he/she must change the sourcefile setting in application.conf to match the desired .java file name. 

Once parsed, the AST will be traversed using ASTVisitor with our overriden concrete visit methods. Listed below are the methods that we have overriden:

* visit(node: Assignment)
* visit(node: Block)
* visit(node: VariableDeclarationFragment)

Assignment node indicates a `left-hand-side OPERATOR right-hand-side` syntax tree. Since we are to keep track of of variables, the concrete visit method for Assignment node is an important concrete visit method that we must override.

Block node indicates the start of a `{ { Statement } }`. This largely correlates to a scope so the concrete visit method for Block is essential to override.

VariableDeclarationFragment nodes indicate a syntax with `Type name` with an optional initializer at the end.

Within the overriden methods, we use ASTRewrite to mark changes that are to be done to the AST.  With ASTRewrite, we insert MethodInvocation nodes to the AST for the TemplateClass instrumentation method calls which will be used for the logging portion of the project.

For example, consider the following example java source file 

     class OtherClass {
         public static void main(String[] args) {
             int x;
             MyClass myObj = new MyClass();
             x = 5;
             while (x > 1) {
                 x = x - 1;
             }
             return;
         }
     }

After running our scala program, the following instrumented statements are added: 

    class OtherClass {
        public static void main(String[] args) {
            TemplateClass.instrum(2, "Block");
    		int x;
    		TemplateClass.instrum(3, "Declaration", "OtherClass.main.x");
            MyClass myObj = new MyClass();
    		TemplateClass.instrum(4, "Declaration", TemplateClass.pair(myObj, "OtherClass.main.myObj"));
            x = 5;
    		TemplateClass.instrum(5, "Assign", TemplateClass.pair(x, "OtherClass.main.x"), 5);
            while (x > 1) {
                TemplateClass.instrum(6, "Block");
    			x = x - 1;
    			TemplateClass.instrum(7, "Assign", TemplateClass.pair(x, "OtherClass.main.x"), x - 1);
            }
            return;
        }
    }
    
As you may see, the production rules we handled were the variable declarations, assignments (simple, infix notation, create new instance), and block declarations.
The line numbers correspond to the line numbers in the file that is created as a back up: it has the same filename as the source file, but with a "old_" prepended to the front of its filename.

#### TemplateClass and logging
For the TemplateClass implementation part we were able to construct a simplistic approach in logging the instrumentation from the AST produced file, in our case would be OtherClass.java

The TemplateClass currently has a static HashMap that will map the unique identifier of each variable. 

Also, it has 3 overloaded instrum methods, each dealing with a different number of parameters and types depending on sent parameters in the modified .java file.

These overloaded instrum methods will log each unique identifier to a variable.
Also, one of the overloaded instrum methods will log the Scope of where each block beings.

For the output of the TemplateClass it would print out a series of log statements to the console which would log each instance of where the TemplateClass.instrum() was called
in the instrumented file (OtherClass.java)

#### Launcher - bootstrap
For the Launcher implementation in the launcherClass.java, we tried to start an instrumented Java program in a separate JVM instance but we were unsuccessful in correctly 
sending the instrumented Java program through the process and having the process write the results to an output file.

What kind of worked was using the ProcessBuilder to create a separate process and have it launch a jar file (parseAST.jar) in a separate process that would run it and search for the java file that would
have a .java file, such as, for our testing purposes would be OtherClass.java to be converted into an AST then modified into having the instrumented methods. But there were some issues with
the Launcher implementation where we were not able to correctly set up the environment and correctly send the instrumented file to the launcher program as an input.



#### 

