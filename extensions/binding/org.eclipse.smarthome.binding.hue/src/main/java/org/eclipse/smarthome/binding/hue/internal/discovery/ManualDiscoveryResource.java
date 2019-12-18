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
package org.eclipse.smarthome.binding.hue.internal.discovery;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.core.auth.Role;
import org.eclipse.smarthome.io.rest.JSONResponse;
import org.eclipse.smarthome.io.rest.RESTResource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This class acts as a REST resource for manual discovery and is registered with the
 * Jersey servlet.
 *
 * @author MAW - Initial contribution
 */
@Path(ManualDiscoveryResource.PATH_DISCOVERY)
@RolesAllowed({ Role.ADMIN })
@Api(value = ManualDiscoveryResource.PATH_DISCOVERY)
public class ManualDiscoveryResource implements RESTResource {

    /** The URI path to this resource */
    public static final String PATH_DISCOVERY = "discovery";

    private HueBridgeNupnpDiscovery hueBridgeNupnpDiscovery;

    @Context
    private UriInfo uriInfo;
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


    protected void setHueBridgeNupnpDiscovery(HueBridgeNupnpDiscovery hueBridgeNupnpDiscovery) {
        this.hueBridgeNupnpDiscovery = hueBridgeNupnpDiscovery;
    }

    protected void unsetHueBridgeNupnpDiscovery(HueBridgeNupnpDiscovery hueBridgeNupnpDiscovery) {
        this.hueBridgeNupnpDiscovery = null;
    }

}
