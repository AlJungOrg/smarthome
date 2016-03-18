package org.eclipse.smarthome.core.library;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.items.GroupFunction;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.library.types.ArithmeticGroupFunction;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * FIXME: TEE: ab hier beginnt ein zum Teil fieser Hack. Ziel war die GroupFunction ueber REST
 * anlegen zu koennen. Das Problem ist, dass kein BaseItem zur Verfuegung steht und damit auch
 * keine Liste von AcceptedCommandTypes gegen die die uebergebenen Parameter geprueft werden
 * koennten. Aus diesem Grund ist hier eine gewisse Menge Code dupliziert.
 */
public class GroupFunctionFactory {
	
	public static final String SEPARATOR = ";";
	
	private final static Logger logger = LoggerFactory.getLogger(GroupFunctionFactory.class);
	
	private static List<Class<? extends State>> acceptedDataTypes = new ArrayList<Class<? extends State>>();
	
	private static final GroupFunction defaultGroupFunction =  GroupItem.createDefaultGroupFunction();
    
    static {
        acceptedDataTypes.add(OpenClosedType.class);
        acceptedDataTypes.add(OnOffType.class);
    }
    
    public static final Map<String, Class<? extends GroupFunction>> groupFunctionsWithTwoParams = new HashMap<>();
    public static final Map<String, Class<? extends GroupFunction>> groupFunctionsWithOneParam = new HashMap<>(1);
    public static final Map<String, Class<? extends GroupFunction>> groupFunctionsWithoutParam = new HashMap<>();
    
    static {
        groupFunctionsWithTwoParams.put("AND", ArithmeticGroupFunction.And.class);
        groupFunctionsWithTwoParams.put("OR", ArithmeticGroupFunction.Or.class);
        groupFunctionsWithTwoParams.put("XOR", ArithmeticGroupFunction.XOr.class);
        groupFunctionsWithTwoParams.put("NAND", ArithmeticGroupFunction.NAnd.class);
        groupFunctionsWithTwoParams.put("NOR", ArithmeticGroupFunction.NOr.class);
        
        groupFunctionsWithOneParam.put("COUNT", ArithmeticGroupFunction.Count.class);
        
        groupFunctionsWithoutParam.put("AVG", ArithmeticGroupFunction.Avg.class);
        groupFunctionsWithoutParam.put("SUM", ArithmeticGroupFunction.Sum.class);
        groupFunctionsWithoutParam.put("MIN", ArithmeticGroupFunction.Min.class);
        groupFunctionsWithoutParam.put("MAX", ArithmeticGroupFunction.Max.class);
        groupFunctionsWithoutParam.put("EQUALITY", GroupFunction.Equality.class);
    }
	
    public static GroupFunction create(String groupFunctionString) {
	   	if (groupFunctionString == null) {
    		return defaultGroupFunction;
    	}
    	
    	final String[] groupFunctionElements = groupFunctionString.split(SEPARATOR);
    	if (groupFunctionElements.length < 1) {
    		logger.debug("groupFunction '{}' doesn't contain any elements. "
    				+ "Please provide a proper function with all arguments separated by '{}'.", SEPARATOR, groupFunctionElements.toString());
    		return defaultGroupFunction;
    	}
    	
    	final String function = groupFunctionElements[0];
    	
        final List<State> args = new ArrayList<State>();
        
        for (int indx = 1; indx < groupFunctionElements.length; indx++) {
            for (Class<? extends Type> type : acceptedDataTypes) {
	        	try {
	                Method valueOf = type.getMethod("valueOf", String.class);
	                State state = (State) valueOf.invoke(type, groupFunctionElements[indx]);
	                if (state != null) {
	                	args.add(state);
	                    break;
	                }
	            } catch (NoSuchMethodException e) {
	            } catch (IllegalArgumentException e) {
	            } catch (IllegalAccessException e) {
	            } catch (InvocationTargetException e) {
	            }
            }
        }

        GroupFunction groupFunction = null;
        final int argsSize = args.size();
        
        if (argsSize == 2 && groupFunctionsWithTwoParams.containsKey(function)) {
           groupFunction = createGroupFunctionInstance(function, args, groupFunctionsWithTwoParams.get(function)); 
        } else if (argsSize == 1 && groupFunctionsWithOneParam.containsKey(function)) {
            groupFunction = createGroupFunctionInstance(function, args, groupFunctionsWithOneParam.get(function));
        } else if (argsSize == 0 && groupFunctionsWithoutParam.containsKey(function)) {
            groupFunction = createGroupFunctionInstance(function, args, groupFunctionsWithoutParam.get(function));
        } else {
            logger.error("Group function '{}' cannot be created with arguments {}. Using {} instead.", function, args, defaultGroupFunction);
        }

        return groupFunction == null ? defaultGroupFunction : groupFunction;
    }

    private static GroupFunction createGroupFunctionInstance(final String function, final List<State> args, Class<? extends GroupFunction> groupFunctionClass) {
        try {
            Constructor<? extends GroupFunction> constructor = groupFunctionClass.getDeclaredConstructor(State.class, State.class);
            return constructor.newInstance(args.toArray());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException e) {
            logger.error("Could not instantiate GroupFunction for {} and args {}", function, args);
            logger.trace("Instantiation for GroupFunction resulted in ", e);
            return null;
        }
    }
	
}
