/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SuspendParticipant.java,v 1.2 2005/01/15 21:21:03 kconner Exp $
 */

package com.arjuna.wscf.tests.junit.model.twophase;

import com.arjuna.mw.wscf.model.twophase.api.CoordinatorManager;

import com.arjuna.mw.wscf.model.twophase.CoordinatorManagerFactory;

import com.arjuna.mw.wsas.activity.*;

import com.arjuna.wscf.tests.TwoPhaseParticipant;
import com.arjuna.wscf.tests.TwoPhaseSynchronization;
import com.arjuna.wscf.tests.WSCFTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: SuspendParticipant.java,v 1.2 2005/01/15 21:21:03 kconner Exp $
 * @since 1.0.
 */

public class SuspendParticipant
{

    @Test
    public void testSuspendParticipant()
            throws Exception
    {
        System.out.println("Running test : " + this.getClass().getName());

        CoordinatorManager cm = CoordinatorManagerFactory.coordinatorManager();

	try
	{
	    cm.begin("TwoPhaseHLS");

	    cm.enlistParticipant(new TwoPhaseParticipant("p1"));
	    cm.enlistParticipant(new TwoPhaseParticipant("p2"));
	    cm.enlistSynchronization(new TwoPhaseSynchronization());
	    
	    System.out.println("Started: "+cm.identifier()+"\n");

	    ActivityHierarchy hier = cm.suspend();

	    System.out.println("Suspended: "+hier+"\n");

	    if (cm.currentActivity() != null)
	    {
            WSCFTestUtils.cleanup(cm);

            fail("Hierarchy still active.");
	    }
		cm.resume(hier);
		
        System.out.println("Resumed: "+hier+"\n");

		cm.confirm();
	}
	catch (Exception ex)
	{
	    WSCFTestUtils.cleanup(cm);

        throw ex;
    }
    }
}
