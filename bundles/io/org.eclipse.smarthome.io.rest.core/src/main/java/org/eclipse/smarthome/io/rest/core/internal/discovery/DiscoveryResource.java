/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.io.rest.core.internal.discovery;

import java.util.Collection;
import java.util.LinkedHashSet;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.binding.hue.internal.discovery.HueBridgeNupnpDiscovery;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.config.discovery.ScanListener;
import org.eclipse.smarthome.core.auth.Role;
import org.eclipse.smarthome.io.rest.JSONResponse;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This class acts as a REST resource for discovery and is registered with the
 * Jersey servlet.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Kai Kreuzer - refactored for using the OSGi JAX-RS connector
 * @author Yordan Zhelev - Added Swagger annotations
 * @author Ivaylo Ivanov - Added payload to the response of <code>scan</code>
 * @author Franck Dechavanne - Added DTOs to ApiResponses
 */
@Path(DiscoveryResource.PATH_DISCOVERY)
@RolesAllowed({ Role.ADMIN })
@Api(value = DiscoveryResource.PATH_DISCOVERY)
@Component(service = { RESTResource.class, DiscoveryResource.class })
public class DiscoveryResource implements RESTResource {

    /** The URI path to this resource */
    public static final String PATH_DISCOVERY = "discovery";

    private final Logger logger = LoggerFactory.getLogger(DiscoveryResource.class);

    private DiscoveryServiceRegistry discoveryServiceRegistry;

	private HueBridgeNupnpDiscovery hueBridgeNupnpDiscovery;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = discoveryServiceRegistry;
    }

    protected void unsetDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = null;
    }

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	protected void setHueBridgeNupnpDiscovery(HueBridgeNupnpDiscovery hueBridgeNupnpDiscovery) {
		this.hueBridgeNupnpDiscovery = hueBridgeNupnpDiscovery;
	}

	protected void unsetHueBridgeNupnpDiscovery(HueBridgeNupnpDiscovery hueBridgeNupnpDiscovery) {
		this.hueBridgeNupnpDiscovery = null;
	}

    @Context
    private UriInfo uriInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets all bindings that support discovery.", response = String.class, responseContainer = "Set")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = String.class, responseContainer = "Set") })
    public Response getDiscoveryServices() {
        Collection<String> supportedBindings = discoveryServiceRegistry.getSupportedBindings();
        return Response.ok(new LinkedHashSet<>(supportedBindings)).build();
    }

    @POST
    @Path("/bindings/{bindingId}/scan")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Starts asynchronous discovery process for a binding and returns the timeout in seconds of the discovery operation.", response = Integer.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Integer.class) })
    public Response scan(@PathParam("bindingId") @ApiParam(value = "bindingId") final String bindingId) {
        discoveryServiceRegistry.startScan(bindingId, new ScanListener() {
            @Override
            public void onErrorOccurred(Exception exception) {
                logger.error("Error occurred while scanning for binding '{}': {}", bindingId, exception.getMessage(),
                        exception);
            }

            @Override
            public void onFinished() {
                logger.debug("Scan for binding '{}' successfully finished.", bindingId);
            }
        });

        return Response.ok(discoveryServiceRegistry.getMaxScanTimeout(bindingId)).build();
    }

	@PUT
	@Path("/{ip}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Adds a discovery.", response = String.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = String.class) })
	public Response putDiscovery(@PathParam("ip") @ApiParam(value = "ip") final String ip) {
		DiscoveryResult bridge = this.hueBridgeNupnpDiscovery.addDiscovery(ip);
		if (bridge == null) {
			return JSONResponse.createErrorResponse(Status.NOT_FOUND, "Bridge not found");
		}
		return Response.ok(bridge).build();
	}

    @Override
    public boolean isSatisfied() {
        return discoveryServiceRegistry != null && hueBridgeNupnpDiscovery != null;
    }

}
