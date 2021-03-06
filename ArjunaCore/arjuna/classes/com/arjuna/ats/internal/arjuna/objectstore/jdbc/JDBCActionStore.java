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
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: JDBCActionStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.objectstore.jdbc;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

/**
 * The transaction log implementation.
*/

public class JDBCActionStore extends JDBCStore
{
    /**
     * The following operation commits a previous write_state operation which
     * was made with the SHADOW StateType argument. This is achieved by
     * renaming the shadow and removing the hidden version.
     */

    public synchronized boolean commit_state (Uid objUid,
                                              String tName) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCActionStore.commit_state(" + objUid + ", " + tName + ")");
        }

        boolean result = false;

        /* Bail out if the object store is not set up */

        if (!storeValid())
            return false;

        if (currentState(objUid, tName) == StateStatus.OS_COMMITTED)
            result = true;
    
        return result;
    }

    public boolean hide_state (Uid u, String tn) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCActionStore.hide_state(" + u + ", " + tn + ")");
        }

        return false;
    }

    public boolean reveal_state (Uid u, String tn) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCActionStore.reveal_state(" + u + ", " + tn + ")");
        }
        
        return false;
    }

    public InputObjectState read_committed (Uid storeUid, String tName) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCActionStore.read_committed(" + storeUid + ", " + tName + ")");
        }
        
        return super.read_committed(storeUid, tName);
    }

    public InputObjectState read_uncommitted (Uid u, String tn) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCActionStore.read_uncommitted(" + u + ", " + tn + ")");
        }
        
        return null;
    }

    public boolean remove_committed (Uid storeUid, String tName) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCActionStore.remove_committed(" + storeUid + ", " + tName + ")");
        }
        
        return super.remove_committed(storeUid, tName);
    }

    public boolean remove_uncommitted (Uid u, String tn) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCActionStore.remove_uncommitted(" + u + ", " + tn + ")");
        }
        
        return false;
    }

    public boolean write_committed (Uid storeUid, String tName, OutputObjectState state) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCActionStore.write_committed(" + storeUid + ", " + tName + ")");
        }
        
        return super.write_committed(storeUid, tName, state);
    }

    public boolean write_uncommitted (Uid u, String tn, OutputObjectState s) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCActionStore.write_uncommitted(" + u + ", " + tn + ", " + s + ")");
        }
        
        return false;
    }

    public JDBCActionStore(JDBCStoreEnvironmentBean jdbcStoreEnvironmentBean) throws ObjectStoreException
    {
        super(jdbcStoreEnvironmentBean);

        _txClassName = jdbcStoreEnvironmentBean.getJdbcTxDbAccessClassName();
    }

    protected String getAccessClassName()
    {
        return _txClassName;
    }

    protected void setAccessClassName(String txClassName)
    {
        _txClassName = txClassName;
    }

    protected String getDefaultTableName()
    {
        return _defaultTxTableName;
    }

    protected JDBCAccess getJDBCAccess()
    {
        return _txJDBCAccess;
    }

    protected void setJDBCAccess(JDBCAccess jdbcAccess)
    {
        _txJDBCAccess = jdbcAccess ;
    }

    protected String getTableName()
    {
        return _txTableName;
    }

    protected void setTableName(String tableName)
    {
        _txTableName = tableName ;
    }

    private JDBCAccess _txJDBCAccess;
    private String _txClassName;
    private String _txTableName;
    private static String _defaultTxTableName = "JBossTSTxTable";

}
