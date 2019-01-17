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
package org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.gap;

import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.BlueGigaCommand;

/**
 * Class to implement the BlueGiga command <b>connectSelective</b>.
 * <p>
 * This command will start the GAP direct connection establishment procedure to a set of
 * dedicated Bluetooth Smart devices. When this command is issued the the module will enter a
 * state where it scans connectable Bluetooth advertisement packets from the remote devices
 * which are registered in the local white list. Upon receiving an advertisement packet from
 * one of the registered devices, the module will send a connection request to this device, and a
 * successful connection will produce a connection status event. The connect selective
 * command can be cancelled with End Procedure command. When in Initiating State there are no
 * scan response events.
 * <p>
 * This class provides methods for processing BlueGiga API commands.
 * <p>
 * Note that this code is autogenerated. Manual changes may be overwritten.
 *
 * @author Chris Jackson - Initial contribution of Java code generator
 */
public class BlueGigaConnectSelectiveCommand extends BlueGigaCommand {
    public static int COMMAND_CLASS = 0x06;
    public static int COMMAND_METHOD = 0x05;

    /**
     * Minimum Connection Interval (in units of 1.25ms). Range: 6 - 3200 The lowest possible
     * Connection Interval is 7.50ms and the largest is 4000ms.
     * <p>
     * BlueGiga API type is <i>uint16</i> - Java type is {@link int}
     */
    private int connIntervalMin;

    /**
     * Maximum Connection Interval (in units of 1.25ms). Range: 6 - 3200 Must be equal or bigger than
     * minimum Connection Interval.
     * <p>
     * BlueGiga API type is <i>uint16</i> - Java type is {@link int}
     */
    private int connIntervalMax;

    /**
     * Supervision Timeout (in units of 10ms). The Supervision Timeout defines how long the
     * devices can be out of range before the connection is closed. Range: 10 - 3200 Minimum time for
     * the Supervision Timeout is 100ms and maximum value is 32000ms. According to the
     * specification, the Supervision Timeout in milliseconds shall be larger than (1 + latency) *
     * conn_interval_max * 2, where conn_interval_max is given in milliseconds.
     * <p>
     * BlueGiga API type is <i>uint16</i> - Java type is {@link int}
     */
    private int timeout;

    /**
     * This parameter configures the slave latency. Slave latency defines how many connection
     * intervals a slave device can skip. Increasing slave latency will decrease the energy
     * consumption of the slave in scenarios where slave does not have data to send at every
     * connection interval. Range: 0 - 500 0 : Slave latency is disabled. Example: Connection
     * interval is 10ms and slave latency is 9: this means that the slave is allowed to communicate
     * every 100ms, but it can communicate every 10ms if needed.
     * <p>
     * BlueGiga API type is <i>uint16</i> - Java type is {@link int}
     */
    private int latency;

    /**
     * Minimum Connection Interval (in units of 1.25ms). Range: 6 - 3200 The lowest possible
     * Connection Interval is 7.50ms and the largest is 4000ms.
     *
     * @param connIntervalMin the connIntervalMin to set as {@link int}
     */
    public void setConnIntervalMin(int connIntervalMin) {
        this.connIntervalMin = connIntervalMin;
    }
    /**
     * Maximum Connection Interval (in units of 1.25ms). Range: 6 - 3200 Must be equal or bigger than
     * minimum Connection Interval.
     *
     * @param connIntervalMax the connIntervalMax to set as {@link int}
     */
    public void setConnIntervalMax(int connIntervalMax) {
        this.connIntervalMax = connIntervalMax;
    }
    /**
     * Supervision Timeout (in units of 10ms). The Supervision Timeout defines how long the
     * devices can be out of range before the connection is closed. Range: 10 - 3200 Minimum time for
     * the Supervision Timeout is 100ms and maximum value is 32000ms. According to the
     * specification, the Supervision Timeout in milliseconds shall be larger than (1 + latency) *
     * conn_interval_max * 2, where conn_interval_max is given in milliseconds.
     *
     * @param timeout the timeout to set as {@link int}
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    /**
     * This parameter configures the slave latency. Slave latency defines how many connection
     * intervals a slave device can skip. Increasing slave latency will decrease the energy
     * consumption of the slave in scenarios where slave does not have data to send at every
     * connection interval. Range: 0 - 500 0 : Slave latency is disabled. Example: Connection
     * interval is 10ms and slave latency is 9: this means that the slave is allowed to communicate
     * every 100ms, but it can communicate every 10ms if needed.
     *
     * @param latency the latency to set as {@link int}
     */
    public void setLatency(int latency) {
        this.latency = latency;
    }

    @Override
    public int[] serialize() {
        // Serialize the header
        serializeHeader(COMMAND_CLASS, COMMAND_METHOD);

        // Serialize the fields
        serializeUInt16(connIntervalMin);
        serializeUInt16(connIntervalMax);
        serializeUInt16(timeout);
        serializeUInt16(latency);

        return getPayload();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("BlueGigaConnectSelectiveCommand [connIntervalMin=");
        builder.append(connIntervalMin);
        builder.append(", connIntervalMax=");
        builder.append(connIntervalMax);
        builder.append(", timeout=");
        builder.append(timeout);
        builder.append(", latency=");
        builder.append(latency);
        builder.append(']');
        return builder.toString();
    }
}
