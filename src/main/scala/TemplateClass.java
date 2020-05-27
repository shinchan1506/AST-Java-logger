/**
 *
 * Authors: Shin Imai
 *          Ronny Recinos
 *          Jon-Michael Hoang
 *
 *
 *
 */

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TemplateClass {

    // For logging purposes
    public static final Logger logger = LoggerFactory.getLogger(TemplateClass.class);

    // Static HashMap that maps all unique identifiers to a variable
    public static HashMap<String, Object> instrumentedLocation = new HashMap<String, Object>();

    // This method will add a (key, value) pair to the static HashMap
    public static <T> String pair (T value, String identifier){

            instrumentedLocation.put(identifier, value);

            return identifier;
    }

    // This will log the Identifier provided by the AST from the instrumented file
    static <T> void instrum(int lineNum, String statementType, String identifier){
        if (instrumentedLocation.get(identifier) != null){

            logger.info("Identifier: " + instrumentedLocation.get(identifier).toString());
        }

    }

    // This will log the Identifier provided by the AST from the instrumented file
    static <T> void instrum(int lineNum, String statementType, String identifier, T expression){
        logger.info("Computed: " + identifier + " " + instrumentedLocation.get(identifier).toString() + " Expression:  " + expression.toString());
    }

    // This will log the Scope of each block
    static <T> void instrum(int lineNum, String statementType){
        logger.info("Scope " + statementType + " begins.");
    }

}
