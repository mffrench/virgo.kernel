/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.management.console;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.script.ScriptException;

import org.eclipse.virgo.management.console.stubs.objects.Dollar;
import org.junit.Test;

import sun.org.mozilla.javascript.internal.Context;
import sun.org.mozilla.javascript.internal.Function;
import sun.org.mozilla.javascript.internal.Scriptable;

/**
 *
 *
 */
public class LoggingJSTests extends AbstractJSTests {
	
	@Test
	public void testPageinit() throws ScriptException, IOException, NoSuchMethodException{
		addCommonObjects();
		readFile("src/main/webapp/js/logging.js");
		
		invokePageInit();
		
		assertTrue(Dollar.getAjaxUrl().contains("LoggerList"));
		
		Dollar.getAjaxSuccess().call(context, scope, scope, new Object[] { getTestLoggers() });
		
		assertTrue("Page ready has not been called", commonUtil.isPageReady());
	}

    private Scriptable getTestLoggers() throws IOException {

        readString("var Data = function() {" + 
            "   this.value = ['logger1','logger2'];" +
            "};");

        Function testData = (Function) scope.get("Data", scope);
        return testData.construct(context, scope, Context.emptyArgs);
    }
}
