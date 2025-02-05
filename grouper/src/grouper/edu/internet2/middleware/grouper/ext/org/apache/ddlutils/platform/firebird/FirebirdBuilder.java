package edu.internet2.middleware.grouper.ext.org.apache.ddlutils.platform.firebird;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.Platform;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.alteration.AddColumnChange;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.alteration.AddPrimaryKeyChange;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.alteration.RemoveColumnChange;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.alteration.TableChange;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Column;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Index;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.platform.SqlBuilder;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.util.Jdbc3Utils;

/**
 * The SQL Builder for the FireBird database.
 * 
 * @version $Revision: 231306 $
 */
public class FirebirdBuilder extends SqlBuilder
{
    /**
     * Creates a new builder instance.
     * 
     * @param platform The plaftform this builder belongs to
     */
    public FirebirdBuilder(Platform platform)
    {
        super(platform);
        addEscapedCharSequence("'", "''");
    }

    /**
     * {@inheritDoc}
     */
    public void createTable(Database database, Table table, Map parameters) throws IOException
    {
        super.createTable(database, table, parameters);

        // creating generator and trigger for auto-increment
        Column[] columns = table.getAutoIncrementColumns();

        for (int idx = 0; idx < columns.length; idx++)
        {
            writeAutoIncrementCreateStmts(database, table, columns[idx]);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dropTable(Table table) throws IOException
    {
        // dropping generators for auto-increment
        Column[] columns = table.getAutoIncrementColumns();

        for (int idx = 0; idx < columns.length; idx++)
        {
            writeAutoIncrementDropStmts(table, columns[idx]);
        }
        super.dropTable(table);
    }

    /**
     * Writes the creation statements to make the given column an auto-increment column.
     * 
     * @param database The database model
     * @param table    The table
     * @param column   The column to make auto-increment
     */
    private void writeAutoIncrementCreateStmts(Database database, Table table, Column column) throws IOException
    {
        print("CREATE GENERATOR ");
        printIdentifier(getGeneratorName(table, column));
        printEndOfStatement();

        print("CREATE TRIGGER ");
        printIdentifier(getConstraintName("trg", table, column.getName(), null));
        print(" FOR ");
        printlnIdentifier(getTableName(table));
        println("ACTIVE BEFORE INSERT POSITION 0 AS");
        print("BEGIN IF (NEW.");
        printIdentifier(getColumnName(column));
        print(" IS NULL) THEN NEW.");
        printIdentifier(getColumnName(column));
        print(" = GEN_ID(");
        printIdentifier(getGeneratorName(table, column));
        print(", 1); END");
        printEndOfStatement();
    }

    /**
     * Writes the statements to drop the auto-increment status for the given column.
     * 
     * @param table  The table
     * @param column The column to remove the auto-increment status for
     */
    private void writeAutoIncrementDropStmts(Table table, Column column) throws IOException
    {
        print("DROP TRIGGER ");
        printIdentifier(getConstraintName("trg", table, column.getName(), null));
        printEndOfStatement();

        print("DROP GENERATOR ");
        printIdentifier(getGeneratorName(table, column));
        printEndOfStatement();
    }

    /**
     * Determines the name of the generator for an auto-increment column.
     * 
     * @param table  The table
     * @param column The auto-increment column
     * @return The generator name
     */
    protected String getGeneratorName(Table table, Column column)
    {
    	return getConstraintName("gen", table, column.getName(), null);
    }

    /**
     * {@inheritDoc}
     */
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        // we're using a generator
    }

    /**
     * {@inheritDoc}
     */
    public String getSelectLastIdentityValues(Table table)
    {
        Column[] columns = table.getAutoIncrementColumns();

        if (columns.length == 0)
        {
            return null;
        }
        else
        {
            StringBuffer result = new StringBuffer();
    
            result.append("SELECT ");
            for (int idx = 0; idx < columns.length; idx++)
            {
                result.append("GEN_ID(");
                result.append(getDelimitedIdentifier(getGeneratorName(table, columns[idx])));
                result.append(", 0)");
            }
            result.append(" FROM RDB$DATABASE");
            return result.toString();
        }
    }

    /**
     * {@inheritDoc}
     */
    protected String getNativeDefaultValue(Column column)
    {
        if ((column.getTypeCode() == Types.BIT) ||
            (Jdbc3Utils.supportsJava14JdbcTypes() && (column.getTypeCode() == Jdbc3Utils.determineBooleanTypeCode())))
        {
            return getDefaultValueHelper().convert(column.getDefaultValue(), column.getTypeCode(), Types.SMALLINT).toString();
        }
        else
        {
            return super.getNativeDefaultValue(column);
        }
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void createExternalForeignKeys(Database database) throws IOException
    {
        for (int idx = 0; idx < database.getTableCount(); idx++)
        {
            createExternalForeignKeys(database, database.getTable(idx));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void writeExternalIndexDropStmt(Table table, Index index) throws IOException
    {
        // Index names in Firebird are unique to a schema and hence Firebird does not
        // use the ON <tablename> clause
        print("DROP INDEX ");
        printIdentifier(getIndexName(index));
        printEndOfStatement();
    }

    /**
     * {@inheritDoc}
     */
    protected void processTableStructureChanges(Database currentModel, Database desiredModel, Table sourceTable, Table targetTable, Map parameters, List changes) throws IOException
    {
    	// TODO: Dropping of primary keys is currently not supported because we cannot
    	//       determine the pk constraint names and drop them in one go
    	//       (We could used a stored procedure if Firebird would allow them to use DDL)
    	//       This will be easier once named primary keys are supported
        boolean pkColumnAdded = false;

        for (Iterator changeIt = changes.iterator(); changeIt.hasNext();)
        {
            TableChange change = (TableChange)changeIt.next();

            if (change instanceof AddColumnChange)
            {
            	AddColumnChange addColumnChange = (AddColumnChange)change;

            	// TODO: we cannot add columns to the primary key this way
            	//       because we would have to drop the pk first and then
            	//       add a new one afterwards which is not supported yet
            	if (addColumnChange.getNewColumn().isPrimaryKey())
                {
                    pkColumnAdded = true;   
                }
                else
            	{
	            	processChange(currentModel, desiredModel, addColumnChange);
	                changeIt.remove();
            	}
            }
            else if (change instanceof RemoveColumnChange)
            {
            	RemoveColumnChange removeColumnChange = (RemoveColumnChange)change;

            	// TODO: we cannot drop primary key columns this way
            	//       because we would have to drop the pk first and then
            	//       add a new one afterwards which is not supported yet
            	if (!removeColumnChange.getColumn().isPrimaryKey())
            	{
	            	processChange(currentModel, desiredModel, removeColumnChange);
	                changeIt.remove();
            	}
            }
        }
        for (Iterator changeIt = changes.iterator(); changeIt.hasNext();)
        {
            TableChange change = (TableChange)changeIt.next();

            // we can only add a primary key if all columns are present in the table
            // i.e. none was added during this alteration
            if ((change instanceof AddPrimaryKeyChange) && !pkColumnAdded)
            {
                processChange(currentModel, desiredModel, (AddPrimaryKeyChange)change);
                changeIt.remove();
            }
        }
    }

    /**
     * Processes the addition of a column to a table.
     * 
     * @param currentModel The current database schema
     * @param desiredModel The desired database schema
     * @param change       The change object
     */
    protected void processChange(Database        currentModel,
                                 Database        desiredModel,
                                 AddColumnChange change) throws IOException
    {
        print("ALTER TABLE ");
        printlnIdentifier(getTableName(change.getChangedTable()));
        printIndent();
        print("ADD ");
        writeColumn(change.getChangedTable(), change.getNewColumn());
        printEndOfStatement();

        Table curTable = currentModel.findTable(change.getChangedTable().getName(), getPlatform().isDelimitedIdentifierModeOn());

        if (!change.isAtEnd())
        {
            Column prevColumn = change.getPreviousColumn();

            if (prevColumn != null)
            {
            	// we need the corresponding column object from the current table
            	prevColumn = curTable.findColumn(prevColumn.getName(), getPlatform().isDelimitedIdentifierModeOn());
            }
            // Even though Firebird can only add columns, we can move them later on
            print("ALTER TABLE ");
            printlnIdentifier(getTableName(change.getChangedTable()));
            printIndent();
            print("ALTER ");
            printIdentifier(getColumnName(change.getNewColumn()));
            print(" POSITION ");
            // column positions start at 1 in Firebird
            print(prevColumn == null ? "1" : String.valueOf(curTable.getColumnIndex(prevColumn) + 2));
            printEndOfStatement();
        }
        if (change.getNewColumn().isAutoIncrement())
        {
            writeAutoIncrementCreateStmts(currentModel, curTable, change.getNewColumn());
        }
        change.apply(currentModel, getPlatform().isDelimitedIdentifierModeOn());
    }

    /**
     * Processes the removal of a column from a table.
     * 
     * @param currentModel The current database schema
     * @param desiredModel The desired database schema
     * @param change       The change object
     */
    protected void processChange(Database           currentModel,
                                 Database           desiredModel,
                                 RemoveColumnChange change) throws IOException
    {
        if (change.getColumn().isAutoIncrement())
        {
            writeAutoIncrementDropStmts(change.getChangedTable(), change.getColumn());
        }
        print("ALTER TABLE ");
        printlnIdentifier(getTableName(change.getChangedTable()));
        printIndent();
        print("DROP ");
        printIdentifier(getColumnName(change.getColumn()));
        printEndOfStatement();
        change.apply(currentModel, getPlatform().isDelimitedIdentifierModeOn());
    }
}
