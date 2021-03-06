/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: BaseTransaction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.arjunacore;

import javax.transaction.NotSupportedException;

import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.logging.jtaLogger;

public class BaseTransaction
{

	public void begin() throws javax.transaction.NotSupportedException,
			javax.transaction.SystemException
	{
		if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("BaseTransaction.begin");
        }

		/*
		 * We can supported subtransactions, so should have the option to let
		 * programmer use them. Strict conformance will always say no.
		 */

		if (!_supportSubtransactions)
		{
		    try
		    {
		        checkTransactionState();
		    }
		    catch (IllegalStateException e1)
		    {
		        NotSupportedException notSupportedException = new NotSupportedException(
		                e1.getMessage());
		        notSupportedException.initCause(e1);
		        throw notSupportedException;
		    }
		    catch (Exception e2)
		    {
		        javax.transaction.SystemException systemException = new javax.transaction.SystemException(
		                e2.toString());
		        systemException.initCause(e2);
		        throw systemException;
		    }
		}

		Integer value = _timeouts.get();
		int v = 0; // if not set then assume 0. What else can we do?

		if (value != null)
		{
			v = value.intValue();
		}
		else
		    v = TxControl.getDefaultTimeout();

		// TODO set default timeout

		TransactionImple.putTransaction(new TransactionImple(v));
	}

	/**
	 * We will never throw a HeuristicRollbackException because if we get a
	 * HeuristicRollback from a resource, and can successfully rollback the
	 * other resources, this is then the same as having simply been forced to
	 * rollback the transaction during phase 1. The OTS interfaces do not allow
	 * a differentiation.
	 */

	public void commit() throws javax.transaction.RollbackException,
			javax.transaction.HeuristicMixedException,
			javax.transaction.HeuristicRollbackException,
			java.lang.SecurityException, java.lang.IllegalStateException,
			javax.transaction.SystemException
	{
		if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("BaseTransaction.commit");
        }

		TransactionImple theTransaction = TransactionImple.getTransaction();

		if (theTransaction == null)
			throw new IllegalStateException(
					"BaseTransaction.commit - "
							+ jtaLogger.i18NLogger.get_transaction_arjunacore_notx());

		theTransaction.commitAndDisassociate();
	}

	public void rollback() throws java.lang.IllegalStateException,
			java.lang.SecurityException, javax.transaction.SystemException
	{
		if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("BaseTransaction.rollback");
        }

		TransactionImple theTransaction = TransactionImple.getTransaction();

		if (theTransaction == null)
			throw new IllegalStateException(
					"BaseTransaction.rollback - "
							+ jtaLogger.i18NLogger.get_transaction_arjunacore_notx());

		theTransaction.rollbackAndDisassociate();
	}

	public void setRollbackOnly() throws java.lang.IllegalStateException,
			javax.transaction.SystemException
	{
		if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("BaseTransaction.setRollbackOnly");
        }

		TransactionImple theTransaction = TransactionImple.getTransaction();

		if (theTransaction == null)
			throw new IllegalStateException(
					jtaLogger.i18NLogger.get_transaction_arjunacore_nosuchtx());

		theTransaction.setRollbackOnly();
	}

	public int getStatus() throws javax.transaction.SystemException
	{
		if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("BaseTransaction.getStatus");
        }

		TransactionImple theTransaction = TransactionImple.getTransaction();

		if (theTransaction == null)
			return javax.transaction.Status.STATUS_NO_TRANSACTION;
		else
			return theTransaction.getStatus();
	}

	public void setTransactionTimeout(int seconds)
			throws javax.transaction.SystemException
	{
		if (seconds >= 0)
		{
		    _timeouts.set(new Integer(seconds));
		}
	}

	public int getTimeout() throws javax.transaction.SystemException
	{
		Integer value = _timeouts.get();

		if (value != null)
		{
			return value.intValue();
		}
		else
			return 0;
	}

	public String toString()
	{
		TransactionImple theTransaction = TransactionImple.getTransaction();

		if (theTransaction == null)
			return "Transaction: unknown";
		else
			return "Transaction: " + theTransaction;
	}

	public TransactionImple createSubordinate () throws javax.transaction.NotSupportedException, javax.transaction.SystemException
	{
		if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("BaseTransaction.createSubordinate");
        }

		try
		{
			checkTransactionState();
		}
		catch (IllegalStateException e1)
		{
            NotSupportedException notSupportedException = new NotSupportedException();
            notSupportedException.initCause(e1);
            throw notSupportedException;
		}
		catch (Exception e2)
		{
            javax.transaction.SystemException systemException = new javax.transaction.SystemException(e2.toString());
            systemException.initCause(e2);
            throw systemException;
		}

		Integer value = _timeouts.get();
		int v = 0; // if not set then assume 0. What else can we do?

		if (value != null)
		{
			v = value.intValue();
		}

		// TODO set default timeout

		return new com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.TransactionImple(v);
	}

	protected BaseTransaction()
	{
	}

	/**
	 * Called when we want to make sure this thread does not already have a
	 * transaction associated with it.
	 */

	final void checkTransactionState() throws IllegalStateException,
			javax.transaction.SystemException
	{
		// ok, no transaction currently associated with thread.

		TransactionImple theTransaction = TransactionImple.getTransaction();

		if (theTransaction == null)
			return;
		else
		{
			if ((theTransaction.getStatus() != javax.transaction.Status.STATUS_NO_TRANSACTION)
					&& !_supportSubtransactions)
			{
				throw new IllegalStateException(
						"BaseTransaction.checkTransactionState - "
								+ jtaLogger.i18NLogger.get_transaction_arjunacore_alreadyassociated());
			}
		}
	}

	private static boolean _supportSubtransactions = false;

	private static ThreadLocal<Integer> _timeouts = new ThreadLocal<Integer>();

	static
	{
            _supportSubtransactions = jtaPropertyManager.getJTAEnvironmentBean().isSupportSubtransactions();
	}

}
