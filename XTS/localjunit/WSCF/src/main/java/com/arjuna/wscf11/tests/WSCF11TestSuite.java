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
 * (C) 2005-2008,
 * @author JBoss Inc.
 */
/*
 * TestSuite.java
 */

package com.arjuna.wscf11.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
    @Suite.SuiteClasses({
//            com.arjuna.wscf11.tests.model.twophase.StartEnd.class,
            com.arjuna.wscf11.tests.model.twophase.BeginConfirm.class,
            com.arjuna.wscf11.tests.model.twophase.BeginCancel.class,
            com.arjuna.wscf11.tests.model.twophase.Suspend.class,
            com.arjuna.wscf11.tests.model.twophase.SuspendResume.class,
            com.arjuna.wscf11.tests.model.twophase.SuspendConfirm.class,
            com.arjuna.wscf11.tests.model.twophase.CancelOnlyCancel.class,
            com.arjuna.wscf11.tests.model.twophase.CancelOnlyConfirm.class,
            com.arjuna.wscf11.tests.model.twophase.AddParticipant.class,
            com.arjuna.wscf11.tests.model.twophase.SuspendParticipant.class,
            com.arjuna.wscf11.tests.model.twophase.AddSynchronization.class,
            com.arjuna.wscf11.tests.model.twophase.ParticipantSynchronization.class,
            com.arjuna.wscf11.tests.model.sagas.BeginClose.class,
            com.arjuna.wscf11.tests.model.sagas.BeginCancel.class,
            com.arjuna.wscf11.tests.model.sagas.Suspend.class,
            com.arjuna.wscf11.tests.model.sagas.SuspendResume.class,
            com.arjuna.wscf11.tests.model.sagas.SuspendClose.class,
            com.arjuna.wscf11.tests.model.sagas.CancelOnlyCancel.class,
            com.arjuna.wscf11.tests.model.sagas.CancelOnlyClose.class,
            com.arjuna.wscf11.tests.model.sagas.ParticipantExitedClose.class,
            com.arjuna.wscf11.tests.model.sagas.ParticipantCannotCompleteClose.class,
            com.arjuna.wscf11.tests.model.sagas.ParticipantFaultedClose.class,
            com.arjuna.wscf11.tests.model.sagas.AddParticipant.class,
            com.arjuna.wscf11.tests.model.sagas.SuspendResumeMultiParticipant.class
    })
public class WSCF11TestSuite
{
}