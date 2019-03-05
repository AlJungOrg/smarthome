/**
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.persistence.rrd4j.internal;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.eclipse.smarthome.core.auth.Role.ADMIN;
import static org.eclipse.smarthome.core.auth.Role.USER;
import static org.eclipse.smarthome.persistence.rrd4j.internal.Rrd4JDatabaseUtil.databasePath;
import static org.eclipse.smarthome.persistence.rrd4j.internal.Rrd4JDatabaseUtil.databasePaths;
import static org.eclipse.smarthome.persistence.rrd4j.internal.Rrd4JDatabaseUtil.itemName;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@NonNullByDefault
@Path(Rrd4JDatabaseResource.PATH)
@Api(tags = { "db", "rrd4j" })
@Component
public class Rrd4JDatabaseResource implements RESTResource {
    static final String PATH = "db/rrd4j";

    private final Logger logger = LoggerFactory.getLogger(Rrd4JDatabaseCleaner.class);

    @NonNullByDefault({})
    @Context
    UriInfo uriInfo;

    @RolesAllowed({ USER, ADMIN })
    @GET
    @Produces(TEXT_PLAIN)
    @ApiOperation(value="Export all available databases as a base64-encoded zip archive.")
    @ApiResponses(@ApiResponse(code = 200, message = "OK", response = byte[].class))
    public byte[] exportDatabasesZipString() throws IOException {
        logger.debug("Received HTTP GET request at '{}'", uriInfo.getPath());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        OutputStream base64 = Base64.getEncoder().wrap(bytes);
        exportZippedDatabasesTo(base64);
        return bytes.toByteArray();
    }

    @RolesAllowed({ USER, ADMIN })
    @GET
    @Produces(APPLICATION_OCTET_STREAM)
    @ApiOperation("Export all available databases as a zip archive.")
    @ApiResponses(@ApiResponse(code = 200, message = "OK", response = byte[].class))
    public byte[] exportDatabasesZip() throws IOException {
        logger.debug("Received HTTP GET request at '{}'", uriInfo.getPath());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        exportZippedDatabasesTo(bytes);
        return bytes.toByteArray();
    }

    private void exportZippedDatabasesTo(OutputStream stream) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(stream);
                DirectoryStream<java.nio.file.Path> paths = databasePaths()) {
            for (java.nio.file.Path path : paths) {
                ZipEntry entry = new ZipEntry(itemName(path));
                entry.setLastModifiedTime(Files.getLastModifiedTime(path));
                entry.setSize(Files.size(path));
                zip.putNextEntry(entry);
                Files.copy(path, zip);
                zip.closeEntry();
            }
        }
    }

    @RolesAllowed(ADMIN)
    @PUT
    @Consumes(TEXT_PLAIN)
    @ApiOperation("Import all databases from the given base64-encoded zip archive.")
    @ApiResponses({ @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 400, message = "Bad Request") })
    public Response importDatabasesZipString(@ApiParam(value = "base64-encoded zip archive", required = true) byte[] bytes) throws IOException {
        logger.debug("Received HTTP PUT request at '{}'", uriInfo.getPath());
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputStream base64 = Base64.getMimeDecoder().wrap(stream);
        return importZippedDatabases(base64);
    }

    @RolesAllowed(ADMIN)
    @PUT
    @Consumes(APPLICATION_OCTET_STREAM)
    @ApiOperation("Import all databases from the given zip archive.")
    @ApiResponses({ @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 400, message = "Bad Request") })
    public Response importDatabasesZip(@ApiParam(value = "zip archive", required = true) byte[] bytes) throws IOException {
        logger.debug("Received HTTP PUT request at '{}'", uriInfo.getPath());
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        return importZippedDatabases(stream);
    }

    private Response importZippedDatabases(InputStream stream) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(stream)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                java.nio.file.Path path = databasePath(entry.getName());
                Files.copy(zip, path, REPLACE_EXISTING);
            }
            return Response.ok().build();
        } catch (ZipException e) {
            logger.error("received invalid zip file: ", e);
            return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @RolesAllowed(ADMIN)
    @DELETE
    @ApiOperation("Delete all databases.")
    @ApiResponses(@ApiResponse(code = 200, message = "OK"))
    public Response deleteDatabases() throws IOException {
        try (DirectoryStream<java.nio.file.Path> paths = databasePaths()) {
            for (java.nio.file.Path path : paths) {
            	Files.deleteIfExists(path);
            }
        }
        return Response.ok().build();
    }

    @RolesAllowed({ USER, ADMIN })
    @GET
    @Path("/{itemName: [a-zA-Z_0-9]*}")
    @Produces(TEXT_PLAIN)
    @ApiOperation("Export the database for the given item.")
    @ApiResponses(@ApiResponse(code = 200, message = "OK"))
    public byte[] exportDatabase(@PathParam("itemName") @ApiParam(value = "item name", required = true) String itemName) throws IOException {
        logger.debug("Received HTTP GET request at '{}'", uriInfo.getPath());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStream base64 = Base64.getEncoder().wrap(stream);
        java.nio.file.Path path = databasePath(itemName);
        Files.copy(path, base64);
        return stream.toByteArray();
    }

    @RolesAllowed(ADMIN)
    @PUT
    @Path("/{itemName: [a-zA-Z_0-9]*}")
    @Consumes(TEXT_PLAIN)
    @ApiOperation("Import the database for the given item.")
    @ApiResponses({ @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 400, message = "Bad Request") })
    public Response importDatabase(
            @PathParam("itemName") @ApiParam(value = "item name", required = true) String itemName,
            @ApiParam(value = "zip archive", required = true) byte[] bytes) throws IOException {
        logger.debug("Received HTTP PUT request at '{}'", uriInfo.getPath());
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputStream base64 = Base64.getMimeDecoder().wrap(stream);
        java.nio.file.Path path = databasePath(itemName);
        Files.copy(base64, path);
        return Response.ok().build();
    }

    @RolesAllowed(ADMIN)
    @DELETE
    @Path("/{itemName: [a-zA-Z_0-9]*}")
    @ApiOperation("Delete the database for the given item.")
    @ApiResponses(@ApiResponse(code = 200, message = "OK"))
    public Response deleteDatabase(@PathParam("itemName") @ApiParam(value = "item name", required = true) String itemName) throws IOException {
        logger.debug("Received HTTP DELETE request at '{}'", uriInfo.getPath());
        java.nio.file.Path path = databasePath(itemName);
        Files.deleteIfExists(path);
        return Response.ok().build();
    }
}
