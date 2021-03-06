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
 * Copyright (c) 2003, Arjuna Technologies Limited.
 *
 * $Id: BusinessAgreementWithParticipantCompletionServiceTestCase.java,v 1.1.2.2 2004/06/18 15:06:10 nmcl Exp $
 */

package com.arjuna.wst.tests.junit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsba.BusinessActivityConstants;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.messaging.engines.ParticipantCompletionCoordinatorEngine;
import com.arjuna.wst.stub.BusinessAgreementWithParticipantCompletionStub;
import com.arjuna.wst.tests.TestUtil;

public class BusinessAgreementWithParticipantCompletionServiceTestCase
{
    @Before
    public void setUp()
        throws Exception
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String participantCompletionParticipantServiceURI = soapRegistry.getServiceURI(BusinessActivityConstants.SERVICE_PARTICIPANT_COMPLETION_PARTICIPANT) ;

        EndpointReferenceType noExceptionBusinessAgreementWithParticipantCompletionCoordinator = new EndpointReferenceType(new AttributedURIType(participantCompletionParticipantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(noExceptionBusinessAgreementWithParticipantCompletionCoordinator, TestUtil.NOEXCEPTION_PARTICIPANT_IDENTIFIER);

        EndpointReferenceType wrongStateExceptionBusinessAgreementWithParticipantCompletionCoordinator = new EndpointReferenceType(new AttributedURIType(participantCompletionParticipantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(wrongStateExceptionBusinessAgreementWithParticipantCompletionCoordinator, TestUtil.WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER);

        EndpointReferenceType systemExceptionBusinessAgreementWithParticipantCompletionCoordinator = new EndpointReferenceType(new AttributedURIType(participantCompletionParticipantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(systemExceptionBusinessAgreementWithParticipantCompletionCoordinator, TestUtil.SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER);

        EndpointReferenceType nonexistentBusinessAgreementWithParticipantCompletionCoordinator = new EndpointReferenceType(new AttributedURIType(participantCompletionParticipantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(nonexistentBusinessAgreementWithParticipantCompletionCoordinator, TestUtil.NONEXISTENT_PARTICIPANT_IDENTIFIER);

        EndpointReferenceType faultedExceptionBusinessAgreementWithParticipantCompletionCoordinator = new EndpointReferenceType(new AttributedURIType(participantCompletionParticipantServiceURI));
        InstanceIdentifier.setEndpointInstanceIdentifier(faultedExceptionBusinessAgreementWithParticipantCompletionCoordinator, TestUtil.FAULTEDEXCEPTION_PARTICIPANT_IDENTIFIER);

        ParticipantCompletionCoordinatorEngine _noExceptionBusinessAgreementWithParticipantCompletionEngine
                = new ParticipantCompletionCoordinatorEngine("businessAgreementWithParticipantCompletionCoordinator", noExceptionBusinessAgreementWithParticipantCompletionCoordinator);
        ParticipantCompletionCoordinatorEngine _wrongStateExceptionBusinessAgreementWithParticipantCompletionEngine
                = new ParticipantCompletionCoordinatorEngine("businessAgreementWithParticipantCompletionCoordinator", wrongStateExceptionBusinessAgreementWithParticipantCompletionCoordinator);
        ParticipantCompletionCoordinatorEngine _systemExceptionBusinessAgreementWithParticipantCompletionEngine
                = new ParticipantCompletionCoordinatorEngine("businessAgreementWithParticipantCompletionCoordinator", systemExceptionBusinessAgreementWithParticipantCompletionCoordinator);
        ParticipantCompletionCoordinatorEngine _faultedExceptionBusinessAgreementWithParticipantCompletionEngine
                = new ParticipantCompletionCoordinatorEngine("businessAgreementWithParticipantCompletionCoordinator", faultedExceptionBusinessAgreementWithParticipantCompletionCoordinator);

        _noExceptionBusinessAgreementWithParticipantCompletionStub
                = new BusinessAgreementWithParticipantCompletionStub(_noExceptionBusinessAgreementWithParticipantCompletionEngine);
        _wrongStateExceptionBusinessAgreementWithParticipantCompletionStub
                = new BusinessAgreementWithParticipantCompletionStub(_wrongStateExceptionBusinessAgreementWithParticipantCompletionEngine);
        _systemExceptionBusinessAgreementWithParticipantCompletionStub
                = new BusinessAgreementWithParticipantCompletionStub(_systemExceptionBusinessAgreementWithParticipantCompletionEngine);
        _faultedExceptionBusinessAgreementWithParticipantCompletionStub
                = new BusinessAgreementWithParticipantCompletionStub(_faultedExceptionBusinessAgreementWithParticipantCompletionEngine);
    }

    @Test
    public void testCloseWithNoException()
        throws Exception
    {
        _noExceptionBusinessAgreementWithParticipantCompletionStub.close();
    }

    @Test
    public void testCancelWithNoException()
        throws Exception
    {
        _noExceptionBusinessAgreementWithParticipantCompletionStub.cancel();
    }

    @Test
    public void testCompensateWithNoException()
        throws Exception
    {
        _noExceptionBusinessAgreementWithParticipantCompletionStub.compensate();
    }

    @Test
    public void testCloseWithWrongStateException()
        throws Exception
    {
        try
        {
            _wrongStateExceptionBusinessAgreementWithParticipantCompletionStub.close();
            fail("Expected exception \"WrongStateException\"");
        }
        catch (WrongStateException wrongStateException)
        {
        }
    }

    @Test
    public void testCancelWithWrongStateException()
        throws Exception
    {
        try
        {
            _wrongStateExceptionBusinessAgreementWithParticipantCompletionStub.cancel();
            fail("Expected exception \"WrongStateException\"");
        }
        catch (WrongStateException wrongStateException)
        {
        }
    }

    @Test
    public void testCompensateWithWrongStateException()
        throws Exception
    {
        try
        {
            _wrongStateExceptionBusinessAgreementWithParticipantCompletionStub.compensate();
            fail("Expected exception \"WrongStateException\"");
        }
        catch (WrongStateException wrongStateException)
        {
        }
    }

    @Test
    public void testCloseWithSystemException()
        throws Exception
    {
        try
        {
            _systemExceptionBusinessAgreementWithParticipantCompletionStub.close();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    @Test
    public void testCancelWithSystemException()
        throws Exception
    {
        try
        {
            _systemExceptionBusinessAgreementWithParticipantCompletionStub.cancel();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    @Test
    public void testCompensateWithSystemException()
        throws Exception
    {
        try
        {
            _systemExceptionBusinessAgreementWithParticipantCompletionStub.compensate();
            fail("Expected exception \"SystemException\"");
        }
        catch (SystemException systemException)
        {
        }
    }

    @Test
    public void testCompensateWithFaultedException ()
        throws Exception
    {
        try
        {
            _faultedExceptionBusinessAgreementWithParticipantCompletionStub.compensate();
            fail("Expected exception \"FaultedException\"");
        }
        catch (FaultedException faultedException)
        {
        }
    }

    @After
    public void tearDown()
        throws Exception
    {
        _noExceptionBusinessAgreementWithParticipantCompletionStub                    = null;
        _wrongStateExceptionBusinessAgreementWithParticipantCompletionStub            = null;
        _systemExceptionBusinessAgreementWithParticipantCompletionStub                = null;
    }

    private BusinessAgreementWithParticipantCompletionParticipant _noExceptionBusinessAgreementWithParticipantCompletionStub                    = null;
    private BusinessAgreementWithParticipantCompletionParticipant _wrongStateExceptionBusinessAgreementWithParticipantCompletionStub            = null;
    private BusinessAgreementWithParticipantCompletionParticipant _systemExceptionBusinessAgreementWithParticipantCompletionStub                = null;
    private BusinessAgreementWithParticipantCompletionParticipant _faultedExceptionBusinessAgreementWithParticipantCompletionStub               = null;

}
