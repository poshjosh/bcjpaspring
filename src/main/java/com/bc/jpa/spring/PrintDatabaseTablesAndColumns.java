package com.bc.jpa.spring;

import com.bc.db.meta.access.MetaDataAccess;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author USER
 */
public class PrintDatabaseTablesAndColumns implements Runnable{

    private static final Logger LOG = LoggerFactory.getLogger(PrintDatabaseTablesAndColumns.class);
    
    private final MetaDataAccess mda;

    public PrintDatabaseTablesAndColumns(MetaDataAccess mda) {
        this.mda = Objects.requireNonNull(mda);
    }

    @Override
    public void run() {
    
        final Map<String, List<String>> catalogToTable = mda.fetchCatalogToTableNameMap();
        
        final Set<String> done = new HashSet<>();
        
        LOG.info("Printing database tables and columns.");
        
        for(String catalog : catalogToTable.keySet()) {
        
            final List<String> tables = catalogToTable.get(catalog);
            
            for(String table : tables) {
                
                if( ! done.contains(table)) {
                
                    done.add(table);
                    
                    final List<String> columns = mda.fetchStringMetaData(catalog, null, table, null, MetaDataAccess.COLUMN_NAME);
                    
                    System.out.println(catalog + '.' + table + " has columns: " + columns);
                }
            }
        }
    }
}
