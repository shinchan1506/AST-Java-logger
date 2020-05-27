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

    public static final Logger logger = LoggerFactory.getLogger(TemplateClass.class);

    public static HashMap<String, Object> instrumentedLocation = new HashMap<String, Object>();

    public static <T> String pair (T value, String identifier){

            instrumentedLocation.put(identifier, value);

            return identifier;
    }


    static <T> void instrum(int lineNum, String statementType, String identifier){
        if (instrumentedLocation.get(identifier) != null){

            logger.info("Identifier: " + instrumentedLocation.get(identifier).toString());
        }

    }

    static <T> void instrum(int lineNum, String statementType, String identifier, T expression){
        logger.info("Computed: " + identifier + " " + instrumentedLocation.get(identifier).toString() + " Expression:  " + expression.toString());
    }

    static <T> void instrum(int lineNum, String statementType){
        logger.info("Scope " + statementType + " begins.");
    }

}
