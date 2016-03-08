package org.eclipse.smarthome.core.library;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
    
    static {
        acceptedDataTypes.add(OpenClosedType.class);
        acceptedDataTypes.add(OnOffType.class);
    }
	
    public static GroupFunction create(String groupFunctionString) {
	   	if (groupFunctionString == null) {
    		return GroupItem.createDefaultGroupFunction();
    	}
    	
    	final String[] groupFunctionElements = groupFunctionString.split(SEPARATOR);
    	if (groupFunctionElements.length < 1) {
    		logger.debug("groupFunction '{}' doesn't contain any elements. "
    				+ "Please provide a proper function with all arguments separated by '{}'.", SEPARATOR, groupFunctionElements.toString());
    		return GroupItem.createDefaultGroupFunction();
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
        
        switch (function) {
            case "AND":
                if (args.size() == 2) {
                    groupFunction = new ArithmeticGroupFunction.And(args.get(0), args.get(1));
                    break;
                } else {
                    logger.error("Group function 'AND' requires two arguments. Using Equality instead.");
                }
            case "OR":
                if (args.size() == 2) {
                    groupFunction = new ArithmeticGroupFunction.Or(args.get(0), args.get(1));
                    break;
                } else {
                    logger.error("Group function 'OR' requires two arguments. Using Equality instead.");
                }
            case "NAND":
                if (args.size() == 2) {
                    groupFunction = new ArithmeticGroupFunction.NAnd(args.get(0), args.get(1));
                    break;
                } else {
                    logger.error("Group function 'NOT AND' requires two arguments. Using Equality instead.");
                }
                break;
            case "NOR":
                if (args.size() == 2) {
                    groupFunction = new ArithmeticGroupFunction.NOr(args.get(0), args.get(1));
                    break;
                } else {
                    logger.error("Group function 'NOT OR' requires two arguments. Using Equality instead.");
                }
            case "COUNT":
            	if (args.size() == 1) {
            		groupFunction = new ArithmeticGroupFunction.Count(args.get(0));
            		break;
            	} else {
            		logger.error("Group function 'COUNT' requires one argument. Using Equality instead.");
            	}
            case "AVG":
                groupFunction = new ArithmeticGroupFunction.Avg();
                break;
            case "SUM":
                groupFunction = new ArithmeticGroupFunction.Sum();
                break;
            case "MIN":
                groupFunction = new ArithmeticGroupFunction.Min();
                break;
            case "MAX":
                groupFunction = new ArithmeticGroupFunction.Max();
                break;
            default:
                logger.error("Unknown group function '" + function + "'. Using Equality instead.");
                groupFunction = GroupItem.createDefaultGroupFunction();
        }

        return groupFunction;
    }
	
}
