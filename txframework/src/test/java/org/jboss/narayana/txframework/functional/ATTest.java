package org.jboss.narayana.txframework.functional;

import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.UserTransactionFactory;
import com.arjuna.wst.TransactionRolledBackException;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.*;
import org.jboss.narayana.txframework.functional.clients.ATClient;
import org.jboss.narayana.txframework.functional.common.SomeApplicationException;
import org.jboss.narayana.txframework.functional.interfaces.AT;
import org.junit.After;
import org.junit.Assert;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import static org.jboss.narayana.txframework.functional.common.ServiceCommand.*;

@RunWith(Arquillian.class)
@Ignore //JBTM-1287
public class ATTest extends BaseFunctionalTestWar
{
    private UserTransaction ut;
    private AT client;

    @Before
    public void setupTest() throws Exception
    {
        ut = UserTransactionFactory.userTransaction();
        client = ATClient.newInstance();
    }

    @After
    public void teardownTest() throws Exception
    {
        assertDataAvailable();
        client.clearLogs();
        rollbackIfActive(ut);
    }

    @Test
    public void testSimple() throws Exception
    {
        ut.begin();
        client.invoke();
        ut.commit();

        assertOrder(PrePrepare.class, Prepare.class, Commit.class, PostCommit.class);
    }

    @Test
    public void testMultiInvoke() throws Exception
    {
        ut.begin();
        client.invoke();
        client.invoke();
        ut.commit();

        assertOrder(PrePrepare.class, Prepare.class, Commit.class, PostCommit.class);
    }

    @Test
    public void testClientDrivenRollback() throws Exception
    {
        ut.begin();
        client.invoke();
        ut.rollback();

        //todo: should rollback be called twice? once for volatile and once for durable
        assertOrder(Rollback.class, Rollback.class);

    }

    @Test(expected = TransactionRolledBackException.class)
    public void testParticipantDrivenRollback() throws Exception
    {
        try
        {
            ut.begin();
            client.invoke(VOTE_ROLLBACK);
            ut.commit();
        }
        catch (TransactionRolledBackException e)
        {
            assertOrder(PrePrepare.class, Prepare.class, Rollback.class);
            throw e;
        }
    }

    @Test
    public void testApplicationException() throws Exception
    {
        try
        {
            ut.begin();
            client.invoke(THROW_APPLICATION_EXCEPTION);
            Assert.fail("Exception should have been thrown by now");
        }
        catch (SomeApplicationException e)
        {
            //Exception expected
        }
        finally
        {
            ut.rollback();
        }
        //todo: should this cause Rollback?
        assertOrder(Rollback.class, Rollback.class);
    }

    private void assertOrder(Class<? extends Annotation>... expectedOrder)
    {
        Assert.assertEquals(Arrays.asList(expectedOrder), client.getEventLog().getEventLog());
    }

    private void assertDataAvailable()
    {
        List<Class<? extends Annotation>> log = client.getEventLog().getDataUnavailableLog();
        if (!log.isEmpty())
        {
            Assert.fail("One or more lifecycle methods could not access the managed data: " + log.toString());
        }
    }
}

