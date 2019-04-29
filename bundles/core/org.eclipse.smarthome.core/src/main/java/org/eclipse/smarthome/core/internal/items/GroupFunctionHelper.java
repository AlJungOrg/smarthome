/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.internal.items;

import java.math.BigDecimal;
import java.util.List;

import javax.measure.Quantity;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.GroupFunction;
import org.eclipse.smarthome.core.items.dto.GroupFunctionDTO;
import org.eclipse.smarthome.core.library.types.ArithmeticGroupFunction;
import org.eclipse.smarthome.core.library.types.DateTimeGroupFunction;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityTypeArithmeticGroupFunction;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.LoggerFactory;

/**
 * Creates {@link GroupFunction}s according to the given parameters.
 *
 * @author Henning Treu - initial contribution and API
 * @author Robert Michalak - LATEST and EARLIEST group functions
 *
 */
public class GroupFunctionHelper {

    /**
     * Creates a {@link GroupFunction} according to the given parameters. In case dimension is given the resulting
     * arithmetic group function will take unit conversion into account.
     *
     * @param function the {@link GroupFunctionDTO} describing the group function.
     * @param args a list of {@link State}s as arguments for the resulting group function.
     * @param dimension an optional interface class from {@link Quantity} defining the dimension for unit conversion.
     * @return a {@link GroupFunction} according to the given parameters.
     */
    public GroupFunction createGroupFunction(GroupFunctionDTO function, List<State> args,
            @Nullable Class<? extends Quantity<?>> dimension) {
        if (dimension != null) {
            return createDimensionGroupFunction(function, args, dimension);
        }

        return createDefaultGroupFunction(function, args);
    }

    private GroupFunction createDimensionGroupFunction(GroupFunctionDTO function, List<State> args,
            @NonNull Class<? extends Quantity<?>> dimension) {
        String functionName = function.name;
        switch (functionName.toUpperCase()) {
            case "AVG":
                return new QuantityTypeArithmeticGroupFunction.Avg(dimension);
            case "SUM":
                return new QuantityTypeArithmeticGroupFunction.Sum(dimension);
            case "MIN":
                return new QuantityTypeArithmeticGroupFunction.Min(dimension);
            case "MAX":
                return new QuantityTypeArithmeticGroupFunction.Max(dimension);
            default:
                return createDefaultGroupFunction(function, args);
        }
    }

    private GroupFunction createDefaultGroupFunction(GroupFunctionDTO function, List<State> args) {
        String functionName = function.name;
        switch (functionName.toUpperCase()) {
            case "AND":
                if (args.size() == 2) {
                    return new ArithmeticGroupFunction.And(args.get(0), args.get(1));
                } else {
                    LoggerFactory.getLogger(GroupFunctionHelper.class)
                            .error("Group function 'AND' requires two arguments. Using Equality instead.");
                }
                break;
            case "OR":
                if (args.size() == 2) {
                    return new ArithmeticGroupFunction.Or(args.get(0), args.get(1));
                } else {
                    LoggerFactory.getLogger(GroupFunctionHelper.class)
                            .error("Group function 'OR' requires two arguments. Using Equality instead.");
                }
                break;
            case "XOR":
                if (args.size() == 2) {
                    return new ArithmeticGroupFunction.XOr(args.get(0), args.get(1));
                } else {
                    LoggerFactory.getLogger(GroupFunctionHelper.class)
                            .error("Group function 'XOR' requires two arguments. Using Equality instead.");
                }
                break;
            case "NAND":
                if (args.size() == 2) {
                    return new ArithmeticGroupFunction.NAnd(args.get(0), args.get(1));
                } else {
                    LoggerFactory.getLogger(GroupFunctionHelper.class)
                            .error("Group function 'NOT AND' requires two arguments. Using Equality instead.");
                }
                break;
            case "NOR":
                if (args.size() == 2) {
                    return new ArithmeticGroupFunction.NOr(args.get(0), args.get(1));
                } else {
                    LoggerFactory.getLogger(GroupFunctionHelper.class)
                            .error("Group function 'NOT OR' requires two arguments. Using Equality instead.");
                }
                break;
            case "COUNT":
                if (function.params != null && function.params.length == 1) {
                    State countParam = new StringType(function.params[0]);
                    return new ArithmeticGroupFunction.Count(countParam);
                } else {
                    LoggerFactory.getLogger(GroupFunctionHelper.class)
                            .error("Group function 'COUNT' requires one argument. Using Equality instead.");
                }
                break;
            case "AVG":
                return new ArithmeticGroupFunction.Avg();
            case "SUM":
                return new ArithmeticGroupFunction.Sum();
            case "MIN":
                return new ArithmeticGroupFunction.Min();
            case "MAX":
                return new ArithmeticGroupFunction.Max();
            case "LATEST":
                return new DateTimeGroupFunction.Latest();
            case "EARLIEST":
                return new DateTimeGroupFunction.Earliest();
            case "EQUAL":
            case "EQUALITY":
                return new GroupFunction.Equality();
            case "EVERY_MEMBER_UPDATE":
                return new GroupFunction.EveryMemberUpdate();
            case "THRESHOLD":
                if (function.params != null && (function.params.length == 4 || function.params.length == 5)) {
                	// NOTE: ESH refuses to let us know which state types the group item accepts, or parse them beforehand.
                	// So we hard-code OnOffType and hope it works out. YOLO! 
                    State active = OnOffType.from(function.params[0]);
                    State passive = OnOffType.from(function.params[1]);
                    DecimalType upper = null;
                    DecimalType lower = null;
                    try {
                        upper = new DecimalType(function.params[2]);
                        lower = new DecimalType(function.params[3]);
                    } catch (NullPointerException | NumberFormatException e) {
                        // ignore
                    }
                    DecimalType factor = new DecimalType(BigDecimal.ONE);
                    try {
                        factor = new DecimalType(function.params[4]);
                    } catch (ArrayIndexOutOfBoundsException | NullPointerException | NumberFormatException e) {
                        // ignore
                    }
                    if (active != null && passive != null && upper != null && lower != null) {
                        return new ArithmeticGroupFunction.Threshold(active, passive, upper, lower, factor);
                    }
                }
                LoggerFactory.getLogger(GroupFunctionHelper.class)
                        .error("Group function 'THRESHOLD' requires four arguments. Using Equality instead.");
	            break;
            default:
                LoggerFactory.getLogger(GroupFunctionHelper.class)
                        .error("Unknown group function '{}'. Using Equality instead.", functionName);
        }

        return new GroupFunction.Equality();
    }

}
