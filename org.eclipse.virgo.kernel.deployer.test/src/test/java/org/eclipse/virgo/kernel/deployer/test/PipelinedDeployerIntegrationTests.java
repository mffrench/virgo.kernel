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

package org.eclipse.virgo.kernel.deployer.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;

import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
import org.eclipse.virgo.kernel.deployer.core.ApplicationDeployer.DeploymentOptions;
import org.eclipse.virgo.util.io.FileCopyUtils;
import org.eclipse.virgo.util.io.FileSystemUtils;
import org.eclipse.virgo.util.io.PathReference;

/**
 * Test the generic deployer using a test deployer. This doesn't check OSGi behaviour but will detect clashes in the
 * file system during deployment.
 * 
 */
public class PipelinedDeployerIntegrationTests extends AbstractDeployerIntegrationTest {

    private ServiceRegistration dummyModuleDeployerServiceRegistration;

    private final PathReference pickup = new PathReference("./target/pickup");

    private final PathReference target = new PathReference("./target");

    private StubInstallArtifactLifecycleListener lifecycleListener;

    private ServiceRegistration lifecycleListenerRegistration;

    private DeploymentIdentity deploymentIdentity;

    @Before
    public void setUp() throws Exception {
        PathReference pr = new PathReference("./target/deployer");
        pr.delete(true);
        pr.createDirectory();

        clearPickup();
        
        this.lifecycleListener = new StubInstallArtifactLifecycleListener();
        this.lifecycleListenerRegistration = this.kernelContext.registerService(InstallArtifactLifecycleListener.class.getName(),
            this.lifecycleListener, null);
    }

    private void clearPickup() {
        for (File file : FileSystemUtils.listFiles(this.pickup.toFile())) {
            file.delete();
        }
    }

    @After
    public void tearDown() throws Exception {
        undeploy();
        clearPickup();
        if (this.dummyModuleDeployerServiceRegistration != null) {
            this.dummyModuleDeployerServiceRegistration.unregister();
        }
        if (this.lifecycleListenerRegistration != null) {
            this.lifecycleListenerRegistration.unregister();
        }
    }

    private void undeploy() throws DeploymentException {
        if (this.deploymentIdentity != null) {
            this.deployer.undeploy(this.deploymentIdentity);
            this.deploymentIdentity = null;
        }
    }

    @Test
    public void testDeployer() throws Exception {
        File file = new File("src/test/resources/dummy.jar");
        this.lifecycleListener.assertLifecycleCounts(0, 0, 0, 0);
        this.deploymentIdentity = this.deployer.deploy(file.toURI());
        this.lifecycleListener.assertLifecycleCounts(1, 1, 0, 0);
        this.deployer.undeploy(this.deploymentIdentity);
        this.deploymentIdentity = null;
        this.lifecycleListener.assertLifecycleCounts(1, 1, 1, 1);
    }

    @Test
    public void testDeployerCleanup() throws Exception {
        File file = new File("src/test/resources/dummy.jar");
        this.lifecycleListener.assertLifecycleCounts(0, 0, 0, 0);
        this.deploymentIdentity = this.deployer.deploy(file.toURI());
        this.lifecycleListener.assertLifecycleCounts(1, 1, 0, 0);
        undeploy();
        this.lifecycleListener.assertLifecycleCounts(1, 1, 1, 1);

        File workDir = new File("target/work/org.eclipse.virgo.kernel/Module/dummy.jar-0");
        assertFalse(workDir.exists());
    }

    @Test
    public void testAwaitRecovery() throws Exception {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = ObjectName.getInstance("org.eclipse.virgo.kernel:category=Control,type=RecoveryMonitor");
        assertTrue(server.isRegistered(objectName));
        assertTrue((Boolean) server.getAttribute(objectName, "RecoveryComplete"));
    }

    @Test
    public void testRepeatDeployment() throws Exception {
        File file = new File("src/test/resources/dummy.jar");
        this.lifecycleListener.assertLifecycleCounts(0, 0, 0, 0);
        this.deploymentIdentity = this.deployer.deploy(file.toURI());
        this.lifecycleListener.assertLifecycleCounts(1, 1, 0, 0);
        undeploy();
        this.lifecycleListener.assertLifecycleCounts(1, 1, 1, 1);
        this.deploymentIdentity = this.deployer.deploy(file.toURI());
        this.lifecycleListener.assertLifecycleCounts(2, 2, 1, 1);
        undeploy();
        this.lifecycleListener.assertLifecycleCounts(2, 2, 2, 2);
    }

    @Test
    public void testDuplicateDeployment() throws Exception {
        File file = new File("src/test/resources/dummy.jar");
        this.lifecycleListener.assertLifecycleCounts(0, 0, 0, 0);
        this.deploymentIdentity = this.deployer.deploy(file.toURI());
        this.lifecycleListener.assertLifecycleCounts(1, 1, 0, 0);
        this.deployer.deploy(file.toURI());
        this.lifecycleListener.assertLifecycleCounts(2, 2, 1, 1);
        undeploy();
        this.lifecycleListener.assertLifecycleCounts(2, 2, 2, 2);
    }

    @Test
    public void testHotDeploy() throws Exception {
        PathReference dummy = new PathReference("src/test/resources/dummy.jar");
        this.lifecycleListener.assertLifecycleCounts(0, 0, 0, 0);
        PathReference deployed = dummy.copy(this.pickup);
        Thread.sleep(10000);
        this.lifecycleListener.assertLifecycleCounts(1, 1, 0, 0);
        deployed.delete();
        Thread.sleep(6000);
        this.lifecycleListener.assertLifecycleCounts(1, 1, 1, 1);
    }

    @Test
    public void testDeployerOwned() throws Exception {
        File dummyCopy = new File(this.target.toFile(), "dummy.jar");
        if (dummyCopy.exists()) {
            dummyCopy.delete();
        }

        PathReference dummy = new PathReference("src/test/resources/dummy.jar");
        PathReference copy = dummy.copy(this.target);
        assertTrue(copy.exists());
        this.lifecycleListener.assertLifecycleCounts(0, 0, 0, 0);
        this.deploymentIdentity = this.deployer.deploy(copy.toURI(), new DeploymentOptions(false, true, true));
        this.lifecycleListener.assertLifecycleCounts(1, 1, 0, 0);
        undeploy();
        this.lifecycleListener.assertLifecycleCounts(1, 1, 1, 1);
        assertFalse(copy.exists());

        copy = dummy.copy(this.target);
        this.deploymentIdentity = this.deployer.deploy(copy.toURI(), new DeploymentOptions(false, true, true));
        copy.delete();
        undeploy();
    }
    
    @Test
    public void testRedeployDeployerOwned() throws Exception {
        File dummyCopy = new File(this.target.toFile(), "dummy.jar");
        if (dummyCopy.exists()) {
            dummyCopy.delete();
        }

        PathReference dummy = new PathReference("src/test/resources/dummy.jar");
        PathReference copy = dummy.copy(this.target);
        assertTrue(copy.exists());
        this.lifecycleListener.assertLifecycleCounts(0, 0, 0, 0);
        this.deploymentIdentity = this.deployer.deploy(copy.toURI(), new DeploymentOptions(false, true, true));
        this.lifecycleListener.assertLifecycleCounts(1, 1, 0, 0);
        
        File dummyModifiedFile = (new PathReference("src/test/resources/dummymodified.jar")).toFile().getAbsoluteFile();
        FileCopyUtils.copy(dummyModifiedFile, copy.toFile().getAbsoluteFile()); //force direct overwrite
        
        // simulate action on MODIFIED     
        this.deploymentIdentity = this.deployer.deploy(copy.toURI(), new DeploymentOptions(false, true, true));
        
        this.lifecycleListener.assertLifecycleCounts(2, 2, 1, 1);
        assertTrue(copy.exists());
        
        undeploy();
        this.lifecycleListener.assertLifecycleCounts(2, 2, 2, 2);
        assertFalse(copy.exists());
    }
}
