/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.tests.junit;

import junit.framework.*;
import junit.textui.*;
import org.eclipse.swt.*;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.widgets.*;

/**
 * Automated Test Suite for class org.eclipse.swt.browser.NewWindowListener
 *
 * @see org.eclipse.swt.browser.NewWindowListener
 */
public class Test_org_eclipse_swt_browser_NewWindowListener extends SwtTestCase {

public Test_org_eclipse_swt_browser_NewWindowListener(String name) {
	super(name);
}

public static void main(String[] args) {
	TestRunner.run(suite());
}

protected void setUp() {
}

protected void tearDown() {
}

public void test_newWindowLorg_eclipse_swt_browser_NewWindowEvent() {
	Display display = Display.getCurrent();
	Shell shell = new Shell(display);
	Browser browser = new Browser(shell, SWT.NONE);
	browser.addNewWindowListener(new NewWindowListener() {
		public void newWindow(NewWindowEvent event) {
		}
	});
	shell.close();
}


public static Test suite() {
	TestSuite suite = new TestSuite();
	java.util.Vector methodNames = methodNames();
	java.util.Enumeration e = methodNames.elements();
	while (e.hasMoreElements()) {
		suite.addTest(new Test_org_eclipse_swt_browser_NewWindowListener((String)e.nextElement()));
	}
	return suite;
}

public static java.util.Vector methodNames() {
	java.util.Vector methodNames = new java.util.Vector();
	methodNames.addElement("test_newWindowLorg_eclipse_swt_browser_NewWindowEvent");
	return methodNames;
}

protected void runTest() throws Throwable {
	if (getName().equals("test_newWindowLorg_eclipse_swt_browser_NewWindowEvent")) test_newWindowLorg_eclipse_swt_browser_NewWindowEvent();
}
}
