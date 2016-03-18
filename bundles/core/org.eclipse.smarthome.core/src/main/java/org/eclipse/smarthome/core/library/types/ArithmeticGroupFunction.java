/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.library.types;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.eclipse.smarthome.core.items.GroupFunction;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

import static org.eclipse.smarthome.core.library.GroupFunctionFactory.SEPARATOR;

/**
 * This interface is only a container for functions that require the core type library
 * for its calculations.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public interface ArithmeticGroupFunction extends GroupFunction {

    /**
     * This does a logical 'and' operation. Only if all items are of 'activeState' this
     * is returned, otherwise the 'passiveState' is returned.
     * 
     * Through the getStateAs() method, it can be determined, how many
     * items actually are not in the 'activeState'.
     * 
     * @author Kai Kreuzer - Initial contribution and API
     *
     */
    static class And implements GroupFunction {

        protected final State activeState;
        protected final State passiveState;

        public And(State activeValue, State passiveValue) {
            if (activeValue == null || passiveValue == null) {
                throw new IllegalArgumentException("Parameters must not be null!");
            }
            this.activeState = activeValue;
            this.passiveState = passiveValue;
        }

        /**
         * @{inheritDoc
         */
        @Override
        public State calculate(Set<Item> items) {
            if (items != null && items.size() > 0) {
                for (Item item : items) {
                    if (!activeState.equals(item.getState())) {
                        return passiveState;
                    }
                }
                return activeState;
            } else {
                // if we do not have any items, we return the passive state
                return passiveState;
            }
        }

        /**
         * @{inheritDoc
         */
        @Override
        public State getStateAs(Set<Item> items, Class<? extends State> stateClass) {
            State state = calculate(items);
            if (stateClass.isInstance(state)) {
                return state;
            } else {
                if (stateClass == DecimalType.class) {
                    if (items != null) {
                        return new DecimalType(items.size() - count(items, activeState));
                    } else {
                        return DecimalType.ZERO;
                    }
                } else {
                    return null;
                }
            }
        }

        private int count(Set<Item> items, State state) {
            int count = 0;
            if (items != null && state != null) {
                for (Item item : items) {
                    if (state.equals(item.getStateAs(state.getClass()))) {
                        count++;
                    }
                }
            }
            return count;

        }

        @Override
		public String toString() {
			return "AND" + SEPARATOR + activeState + SEPARATOR + passiveState;
		}
    }

    /**
     * This does a logical 'or' operation. If at least one item is of 'activeState' this
     * is returned, otherwise the 'passiveState' is returned.
     * 
     * Through the getStateAs() method, it can be determined, how many
     * items actually are in the 'activeState'.
     * 
     * @author Kai Kreuzer - Initial contribution and API
     *
     */
    static class Or implements GroupFunction {

        protected final State activeState;
        protected final State passiveState;

        public Or(State activeValue, State passiveValue) {
            if (activeValue == null || passiveValue == null) {
                throw new IllegalArgumentException("Parameters must not be null!");
            }
            this.activeState = activeValue;
            this.passiveState = passiveValue;
        }

        /**
         * @{inheritDoc
         */
        @Override
        public State calculate(Set<Item> items) {
            if (items != null) {
                for (Item item : items) {
                    if (activeState.equals(item.getState())) {
                        return activeState;
                    }
                }
            }
            return passiveState;
        }

        /**
         * @{inheritDoc
         */
        @Override
        public State getStateAs(Set<Item> items, Class<? extends State> stateClass) {
            State state = calculate(items);
            if (stateClass.isInstance(state)) {
                return state;
            } else {
                if (stateClass == DecimalType.class) {
                    return new DecimalType(count(items, activeState));
                } else {
                    return null;
                }
            }
        }

        private int count(Set<Item> items, State state) {
            int count = 0;
            if (items != null && state != null) {
                for (Item item : items) {
                    if (state.equals(item.getStateAs(state.getClass()))) {
                        count++;
                    }
                }
            }
            return count;
        }
        
        @Override
		public String toString() {
			return "OR" + SEPARATOR + activeState + SEPARATOR + passiveState;
		}
    }
    
    /**
     * This does a logical 'xor' operation. If just one item is of 'activeState' this
     * is returned, otherwise the 'passiveState' is returned.
     * 
     * Through the getStateAs() method, it can be determined, how many
     * items actually are in the 'activeState'.
     * 
     * @author Sebastian Janzen - Initial contribution and API
     *
     */
    static class XOr implements GroupFunction {

        protected final State activeState;
        protected final State passiveState;

        public XOr(State activeValue, State passiveValue) {
            if (activeValue == null || passiveValue == null) {
                throw new IllegalArgumentException("Parameters must not be null!");
            }
            this.activeState = activeValue;
            this.passiveState = passiveValue;
        }

        /**
         * @{inheritDoc
         */
        @Override
        public State calculate(Set<Item> items) {
            if (items != null) {
                boolean foundOne = false;
                for (Item item : items) {
                    if (activeState.equals(item.getState())) {
                        if (foundOne == true) {
                            return passiveState;
                        } else {
                            foundOne = true;
                        }
                    }
                }
                if (foundOne) { 
                    return activeState;
                }
            }
            return passiveState;
        }

        /**
         * @{inheritDoc
         */
        @Override
        public State getStateAs(Set<Item> items, Class<? extends State> stateClass) {
            final State state = calculate(items);
            if (stateClass.isInstance(state)) {
                return state;
            } else {
                return UnDefType.UNDEF;
            }
        }
        
        @Override
        public String toString() {
            return "XOR" + SEPARATOR + activeState + SEPARATOR + passiveState;
        }
    }


    /**
     * This does a logical 'nand' operation. The state is 'calculated' by
     * the normal 'and' operation and than negated by returning the opposite
     * value. E.g. when the 'and' operation calculates the activeValue the
     * passiveValue will be returned and vice versa.
     * 
     * @author Thomas.Eichstaedt-Engelen
     */
    static class NAnd extends And {

        public NAnd(State activeValue, State passiveValue) {
            super(activeValue, passiveValue);
        }

        @Override
        public State calculate(Set<Item> items) {
            State result = super.calculate(items);
            State notResult = result.equals(activeState) ? passiveState : activeState;
            return notResult;
        }
        
        @Override
		public String toString() {
			return "NAND" + SEPARATOR + activeState + SEPARATOR + passiveState;
		}

    }

    /**
     * This does a logical 'nor' operation. The state is 'calculated' by
     * the normal 'or' operation and than negated by returning the opposite
     * value. E.g. when the 'or' operation calculates the activeValue the
     * passiveValue will be returned and vice versa.
     * 
     * @author Thomas.Eichstaedt-Engelen
     */
    static class NOr extends Or {

        public NOr(State activeValue, State passiveValue) {
            super(activeValue, passiveValue);
        }

        @Override
        public State calculate(Set<Item> items) {
            State result = super.calculate(items);
            State notResult = result.equals(activeState) ? passiveState : activeState;
            return notResult;
        }
        
        @Override
		public String toString() {
			return "NOR" + SEPARATOR + activeState + SEPARATOR + passiveState;
		}

    }

    /**
     * This calculates the numeric average over all item states of decimal type.
     * 
     * @author Kai Kreuzer - Initial contribution and API
     *
     */
    static class Avg implements GroupFunction {

        public Avg() {
        }

        /**
         * @{inheritDoc
         */
        @Override
        public State calculate(Set<Item> items) {
            BigDecimal sum = BigDecimal.ZERO;
            int count = 0;
            if (items != null) {
                for (Item item : items) {
                    DecimalType itemState = (DecimalType) item.getStateAs(DecimalType.class);
                    if (itemState != null) {
                        sum = sum.add(itemState.toBigDecimal());
                        count++;
                    }
                }
            }
            if (count > 0) {
                return new DecimalType(sum.divide(new BigDecimal(count), RoundingMode.HALF_UP));
            } else {
                return UnDefType.UNDEF;
            }
        }

        /**
         * @{inheritDoc
         */
        @Override
        public State getStateAs(Set<Item> items, Class<? extends State> stateClass) {
            State state = calculate(items);
            if (stateClass.isInstance(state)) {
                return state;
            } else {
                return null;
            }
        }
        
        @Override
		public String toString() {
			return "AVG";
		}
        
    }

    /**
     * This calculates the numeric sum over all item states of decimal type.
     * 
     * @author Thomas.Eichstaedt-Engelen
     *
     */
    static class Sum implements GroupFunction {

        public Sum() {
        }

        /**
         * @{inheritDoc
         */
        @Override
        public State calculate(Set<Item> items) {
            BigDecimal sum = BigDecimal.ZERO;
            if (items != null) {
                for (Item item : items) {
                    DecimalType itemState = (DecimalType) item.getStateAs(DecimalType.class);
                    if (itemState != null) {
                        sum = sum.add(itemState.toBigDecimal());
                    }
                }
            }
            return new DecimalType(sum);
        }

        /**
         * @{inheritDoc
         */
        @Override
        public State getStateAs(Set<Item> items, Class<? extends State> stateClass) {
            State state = calculate(items);
            if (stateClass.isInstance(state)) {
                return state;
            } else {
                return null;
            }
        }
        
        @Override
		public String toString() {
			return "SUM";
		}
        
    }

    /**
     * This calculates the minimum value of all item states of decimal type.
     * 
     * @author Kai Kreuzer - Initial contribution and API
     *
     */
    static class Min implements GroupFunction {

        public Min() {
        }

        /**
         * @{inheritDoc
         */
        @Override
        public State calculate(Set<Item> items) {
            if (items != null && items.size() > 0) {
                BigDecimal min = null;
                for (Item item : items) {
                    DecimalType itemState = (DecimalType) item.getStateAs(DecimalType.class);
                    if (itemState != null) {
                        if (min == null || min.compareTo(itemState.toBigDecimal()) > 0) {
                            min = itemState.toBigDecimal();
                        }
                    }
                }
                if (min != null) {
                    return new DecimalType(min);
                }
            }
            return UnDefType.UNDEF;
        }

        /**
         * @{inheritDoc
         */
        @Override
        public State getStateAs(Set<Item> items, Class<? extends State> stateClass) {
            State state = calculate(items);
            if (stateClass.isInstance(state)) {
                return state;
            } else {
                return null;
            }
        }
        
        @Override
		public String toString() {
			return "MIN";
		}
        
    }

    /**
     * This calculates the maximum value of all item states of decimal type.
     * 
     * @author Kai Kreuzer - Initial contribution and API
     *
     */
    static class Max implements GroupFunction {

        public Max() {
        }

        /**
         * @{inheritDoc
         */
        @Override
        public State calculate(Set<Item> items) {
            if (items != null && items.size() > 0) {
                BigDecimal max = null;
                for (Item item : items) {
                    DecimalType itemState = (DecimalType) item.getStateAs(DecimalType.class);
                    if (itemState != null) {
                        if (max == null || max.compareTo(itemState.toBigDecimal()) < 0) {
                            max = itemState.toBigDecimal();
                        }
                    }
                }
                if (max != null) {
                    return new DecimalType(max);
                }
            }
            return UnDefType.UNDEF;
        }

        /**
         * @{inheritDoc
         */
        @Override
        public State getStateAs(Set<Item> items, Class<? extends State> stateClass) {
            State state = calculate(items);
            if (stateClass.isInstance(state)) {
                return state;
            } else {
                return null;
            }
        }
        
        @Override
		public String toString() {
			return "MAX";
		}
    }
    
	/**
	 * This calculates the number of items in the group matching the
	 * regular expression passed in parameter
	 * Group:Number:COUNT(".") will count all items having a string state of one character
	 * Group:Number:COUNT("[5-9]") will count all items having a string state between 5 and 9
	 * ...
	 * 
	 * @author Gaël L'hopital
	 *
	 */
	static class Count implements GroupFunction {
		
		protected final Pattern pattern;
		
		public Count(State regExpr) {
			if(regExpr==null) {
				throw new IllegalArgumentException("Parameter must not be null!");
			}
			this.pattern = Pattern.compile(regExpr.toString());
		}

		/**
		 * @{inheritDoc
		 */
		@Override
		public State calculate(Set<Item> items) {
			int count = 0;
			if(items!=null) {
				for(Item item : items) {
					Matcher matcher = pattern.matcher(item.getState().toString());
					if (matcher.matches()) count++;
				}
			}
			
			return new DecimalType(count);
		}

		/**
		 * @{inheritDoc
		 */
		@Override
		public State getStateAs(Set<Item> items, Class<? extends State> stateClass) {
			State state = calculate(items);
			if(stateClass.isInstance(state)) {
				return state;
			} else {
				return null;
			}
		}
		
		@Override
		public String toString() {
			return "COUNT";
		}
	}


}
