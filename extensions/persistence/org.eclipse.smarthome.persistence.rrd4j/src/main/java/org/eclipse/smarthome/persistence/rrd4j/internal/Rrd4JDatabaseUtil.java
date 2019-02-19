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

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.newDirectoryStream;
import static java.nio.file.Files.notExists;
import static org.eclipse.smarthome.config.core.ConfigConstants.getUserDataFolder;
import static org.eclipse.smarthome.persistence.rrd4j.internal.Rrd4JConstants.SERVICE_NAME;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
class Rrd4JDatabaseUtil {
	private static final Path SERVICE_ROOT = Paths.get(getUserDataFolder(), "persistence", SERVICE_NAME);

	static void createRoot() throws IOException {
        if (notExists(SERVICE_ROOT)) {
            createDirectories(SERVICE_ROOT);
        }
	}

	static DirectoryStream<Path> databasePaths() throws IOException {
		return newDirectoryStream(SERVICE_ROOT, "*.rrd");
	}

    static Path databasePath(String itemName) {
        String fileName = itemName + ".rrd";
        return SERVICE_ROOT.resolve(fileName);
    }

    static String itemName(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }
}
