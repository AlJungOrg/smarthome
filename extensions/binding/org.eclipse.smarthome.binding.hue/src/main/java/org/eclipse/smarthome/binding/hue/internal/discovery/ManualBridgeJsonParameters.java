package org.eclipse.smarthome.binding.hue.internal.discovery;

import com.google.gson.annotations.SerializedName;

public class ManualBridgeJsonParameters {

	@SerializedName("bridgeid")
	private final String bridgeId;
	private final String mac;
	private final String name;

	public ManualBridgeJsonParameters(String bridgeId, String mac, String name) {
    	this.bridgeId = bridgeId;
    	this.mac = mac;
    	this.name = name;
	}

	public String getBridgeId() {
    	return bridgeId;
	}

	public String getMac() {
    	return mac;
	}

	public String getName() {
		return name;
	}

}
