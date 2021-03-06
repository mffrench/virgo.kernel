/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.deployer.core.event;

import org.osgi.framework.Version;


/**
 * {@link ApplicationDeploying} is an event which is broadcast when an application is about to be deployed.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * This class is immutable and therefore thread safe.
 *
 */
public class ApplicationDeploying extends ApplicationDeploymentEvent {

    /**
     * Construct a {@link ApplicationDeploying} event with the given application symbolic name and version.
     * 
     * @param applicationSymbolicName
     * @param applicationVersion
     */
    public ApplicationDeploying(String applicationSymbolicName, Version applicationVersion) {
        super(applicationSymbolicName, applicationVersion);
    }

}
